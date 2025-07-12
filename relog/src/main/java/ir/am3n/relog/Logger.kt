package ir.am3n.relog

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.room.Room
import ir.am3n.needtool.*
import ir.am3n.relog.data.Config
import ir.am3n.relog.data.local.db.Log
import ir.am3n.relog.data.local.db.LogDatabase
import ir.am3n.relog.data.remote.PushRequest
import ir.am3n.relog.data.remote.Remote
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

    private var channel = Channel<Log>()

    init {
        if (application != null) {

            application.unregisterActivityLifecycleCallbacks(this)
            application.registerActivityLifecycleCallbacks(this)

            database = Room.databaseBuilder(application, LogDatabase::class.java, "RelogDatabase").build()

            RL.scope.launch {
                channel.consumeEach { log ->
                    try {

                        database!!.logDao()!!.insert(log)
                        if (RL.debug) android.util.Log.d("Relog", "collect log & insert")

                        if (RL.canPush()) {
                            if (Now - lastPush > 10 * 1000) {
                                push.run()
                            }
                            offUI(push)
                            onUI(push, 500)
                        }

                    } catch (t: Throwable) {
                        if (RL.debug) android.util.Log.e("Relog", "", t)
                    }
                }
            }

        }
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
                        if (RL.debug) android.util.Log.d("Relog", "chunk logs size=${logs.size}")
                        val logsCanPush = logs.filter { canLog(it.type) }
                        val body = PushRequest(RL.cid, logsCanPush)
                        if (RL.debug) android.util.Log.d("Relog", "pushing logs size=${body.logs?.size}")
                        Remote.api.push(RL.appKey, body).enqueue(object : Callback<ResponseBody?> {
                            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                                lastPush = Now
                                if (response.isSuccessful) {
                                    try {
                                        val responseString = response.body()?.string()
                                        val responseJson = responseString?.isJsonObj()
                                        if (responseJson?.optString("status") == "ok") {
                                            onIO {
                                                try {
                                                    database!!.logDao()!!.deleteSync(logs)
                                                    if (RL.debug) android.util.Log.d("Relog", "push succeed")
                                                } catch (t: Throwable) {
                                                    if (RL.debug) android.util.Log.e("Relog", "", t)
                                                } finally {
                                                    pushing = false
                                                }
                                            }
                                            return
                                        }
                                        if (RL.debug) android.util.Log.d("Relog", "push failed with response: $responseString")
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
                                if (RL.debug) android.util.Log.e("Relog", "", t)
                            }
                        })
                    } else {
                        lastPush = Now
                        pushing = false
                        if (RL.debug) android.util.Log.d("Relog", "there is no logs")
                    }
                    return@let
                }
            } catch (t: Throwable) {
                lastPush = Now
                pushing = false
                if (RL.debug) android.util.Log.e("Relog", "", t)
            }
        }
    }

    internal fun log(timestamp: String, type: Type, tag: String, message: String) {
        if (canLog(type)) {
            RL.scope.launch {
                channel.send(
                    Log(
                        timestamp, RL.debug, RL.appVersionCode ?: 0,
                        RL.appVersionName ?: "", RL.osVersion ?: "", type, tag, message
                    )
                )
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
        return Config.enable && Config.clientEnable && (
                (Config.filterOperator == "and" && (checkTypeFilter(type) && checkClientTypeFilter(type)))
                        || (Config.filterOperator == "or" && (checkTypeFilter(type) || checkClientTypeFilter(type)))
                )
    }

    private fun checkTypeFilter(type: Type?): Boolean {
        return type?.code?.and(Config.typeFilter) != type?.code
    }

    private fun checkClientTypeFilter(type: Type?): Boolean {
        return type?.code?.and(Config.clientTypeFilter) != type?.code
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (RL.debug) android.util.Log.d("Relog", "onActivityCreated")
    }

    override fun onActivityStarted(activity: Activity) {
        if (RL.debug) android.util.Log.d("Relog", "onActivityStarted")
    }

    override fun onActivityResumed(activity: Activity) {
        if (RL.debug) android.util.Log.d("Relog", "onActivityResumed")
    }

    override fun onActivityPaused(activity: Activity) {
        if (RL.debug) android.util.Log.d("Relog", "onActivityPaused")
    }

    override fun onActivityStopped(activity: Activity) {
        if (RL.debug) android.util.Log.d("Relog", "onActivityStopped")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        if (RL.debug) android.util.Log.d("Relog", "onActivitySaveInstanceState")
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (RL.debug) android.util.Log.d("Relog", "onActivityDestroyed")
    }

}