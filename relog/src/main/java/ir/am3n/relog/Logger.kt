package ir.am3n.relog

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.room.Room
import ir.am3n.needtool.*
import ir.am3n.relog.data.local.db.Log
import ir.am3n.relog.data.local.db.LogDatabase
import ir.am3n.relog.data.remote.PushRequest
import ir.am3n.relog.data.remote.Relog
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.experimental.and

internal class Logger(application: Application?) : Application.ActivityLifecycleCallbacks {

    private var database: LogDatabase? = null
    private var pushing = false
    private var lastPush = 0L
    private var indivTypeFilter: Byte = 0
    private var typeFilter: Byte = 0
    private var filterOperator: String = "and"
    private var keepLogs: Int = 30
    private var pushTimeout: Int = 2 // seconds

    private var channel = Channel<Log>()

    init {
        if (application != null) {

            application?.unregisterActivityLifecycleCallbacks(this)
            application?.registerActivityLifecycleCallbacks(this)

            indivTypeFilter = application.sh("relog").getInt("config__indiv_type_filter", 0).toByte()
            typeFilter = application.sh("relog").getInt("config__type_filter", 0).toByte()
            filterOperator = application.sh("relog").getString("config__filter_operator", "and") ?: "and"
            keepLogs = application.sh("relog").getInt("config__keep_logs", 0)

            database = Room.databaseBuilder(application, LogDatabase::class.java, "RelogDatabase").build()

            RL.scope.launch {
                channel.consumeEach { log ->
                    try {

                        database!!.logDao()!!.insert(log)
                        if (RL.logging) android.util.Log.d("Relog", "collect log & insert")

                        if (RL.canPush()) {
                            if (Now - lastPush > 10 * 1000) {
                                push.run()
                            }
                            offUI(push)
                            onUI(push, 500)
                        }

                    } catch (t: Throwable) {
                        if (RL.logging) android.util.Log.e("Relog", "", t)
                    }
                }
            }

        }
    }

    internal fun setIndivTypeFilter(context: Context?, filter: Int) {
        indivTypeFilter = filter.toByte()
        context?.sh("relog")?.edit()?.putInt("config__indiv_type_filter", filter)?.apply()
    }

    internal fun setTypeFilter(context: Context?, filter: Int) {
        typeFilter = filter.toByte()
        context?.sh("relog")?.edit()?.putInt("config__type_filter", filter)?.apply()
    }

    internal fun setFilterOperator(context: Context?, operator: String) {
        filterOperator = operator
        context?.sh("relog")?.edit()?.putString("config__filter_operator", operator)?.apply()
    }

    internal fun setKeepLogs(context: Context?, days: Int) {
        keepLogs = days
        context?.sh("relog")?.edit()?.putInt("config__keep_logs", days)?.apply()
    }

    private val push = java.lang.Runnable {
        if (!pushing) {
            pushing = true
            push()
        }
    }

    private fun push() {
        onIO {
            try {
                database!!.logDao()!!.chunk!!.let { logs ->
                    if (logs.isNotEmpty()) {
                        if (RL.logging) android.util.Log.d("Relog", "chunk logs size=${logs.size}")
                        val logsCanPush = logs.filter { canLog(it.type) }
                        val body = PushRequest(RL.cid, logsCanPush)
                        if (RL.logging) android.util.Log.d("Relog", "pushing logs size=${body.logs?.size}")
                        Relog.api.push(RL.appKey, body).enqueue(object : Callback<ResponseBody?> {
                            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                                lastPush = Now
                                if (response.isSuccessful) {
                                    try {
                                        val r = response.body()?.string()
                                        r?.isJsonObj()?.let { json ->
                                            if (json.optString("status") == "ok") {
                                                pushTimeout--
                                                if (pushTimeout < 2) pushTimeout = 2
                                                if (pushTimeout > 4) pushTimeout = 4
                                                onIO {
                                                    try {
                                                        database!!.logDao()!!.delete(logs)
                                                        pushing = false
                                                        if (RL.logging) android.util.Log.d("Relog", "push succeed")
                                                    } catch (t: Throwable) {
                                                        if (RL.logging) android.util.Log.e("Relog", "", t)
                                                    }
                                                }
                                                return
                                            }
                                        }
                                        if (RL.logging) android.util.Log.d("Relog", "push failed with response: $r")
                                    } catch (t: Throwable) {
                                        pushing = false
                                        onFailure(call, t)
                                        return
                                    }
                                }
                                onFailure(call, Exception(response.errorBody()?.toString()))
                            }
                            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                                lastPush = Now
                                pushing = false
                                if (RL.logging) android.util.Log.e("Relog", "", t)
                            }
                        })
                    } else {
                        lastPush = Now
                        pushing = false
                        pushTimeout++
                        if (pushTimeout > 15) pushTimeout = 15
                        if (RL.logging) android.util.Log.d("Relog", "there is no logs")
                    }
                    return@let
                }
            } catch (t: Throwable) {
                lastPush = Now
                pushing = false
                if (RL.logging) android.util.Log.e("Relog", "", t)
            }
        }
    }

    internal fun log(timestamp: String, type: Type, tag: String, message: String) {
        if (canLog(type)) {
            RL.scope.launch {
                channel.send(Log(timestamp, RL.debug, RL.appVersionCode ?:0,
                    RL.appVersionName ?:"", RL.osVersion ?:"", type, tag, message))
            }
        }
    }

    internal fun stop() {
        try {
            channel.close()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        try {
            database?.close()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun canLog(type: Type?): Boolean {
        return (
            (filterOperator == "and" && (checkTypeFilter(type) && checkIndivTypeFilter(type)))
            ||
            (filterOperator == "or" && (checkTypeFilter(type) || checkIndivTypeFilter(type)))
        )
    }

    private fun checkTypeFilter(type: Type?): Boolean {
        return type?.code?.and(typeFilter) != type?.code
    }

    private fun checkIndivTypeFilter(type: Type?): Boolean {
        return type?.code?.and(indivTypeFilter) != type?.code
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (RL.logging) android.util.Log.d("Relog", "onActivityCreated")
    }

    override fun onActivityStarted(activity: Activity) {
        if (RL.logging) android.util.Log.d("Relog", "onActivityStarted")
    }

    override fun onActivityResumed(activity: Activity) {
        if (RL.logging) android.util.Log.d("Relog", "onActivityResumed")
    }

    override fun onActivityPaused(activity: Activity) {
        if (RL.logging) android.util.Log.d("Relog", "onActivityPaused")
    }

    override fun onActivityStopped(activity: Activity) {
        if (RL.logging) android.util.Log.d("Relog", "onActivityStopped")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        if (RL.logging) android.util.Log.d("Relog", "onActivitySaveInstanceState")
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (RL.logging) android.util.Log.d("Relog", "onActivityDestroyed")
    }

}