package ir.am3n.relog

import android.content.Context
import androidx.room.Room
import ir.am3n.needtool.isJsonObj
import ir.am3n.needtool.onIO
import ir.am3n.needtool.sh
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
import java.lang.Thread.sleep
import kotlin.experimental.and

internal class Logger(context: Context?) {

    private var database: LogDatabase? = null
    private var pushing = false
    private var indivTypeFilter: Byte = 0
    private var typeFilter: Byte = 0
    private var filterOperator: String = "and"
    private var keepLogs: Int = 30
    private var pushTimeout: Int = 2

    private var channel = Channel<Log>()

    init {
        indivTypeFilter = context?.sh("relog")?.getInt("config__indiv_type_filter", 0)?.toByte() ?:0.toByte()
        typeFilter = context?.sh("relog")?.getInt("config__type_filter", 0)?.toByte() ?:0.toByte()
        filterOperator = context?.sh("relog")?.getString("config__filter_operator", "and") ?: "and"
        keepLogs = context?.sh("relog")?.getInt("config__keep_logs", 0) ?:30
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


    internal fun start(context: Context?) {
        if (context == null) return

        database = Room.databaseBuilder(context, LogDatabase::class.java, "RelogDatabase").build()

        GlobalScope.launch {
            channel.consumeEach { log ->
                //d("Relog", "Logger > start() > collect")
                database?.logDao()?.insert(log)
                //d("Relog", "Logger > start() > inserted")
            }
        }

        Thread {
            while (true) {
                try {
                    try { sleep(pushTimeout * 1000L) } catch (t: Throwable) {}
                    if (!pushing && RL.canPush()) {
                        pushing = true
                        push()
                    }
                } catch (t: Throwable) {}
            }
        }.start()
    }

    private fun push() {
        Thread {
            try {
                database!!.logDao()!!.all!!.let { logs ->
                    if (logs.isNotEmpty()) {
                        //d("Relog", "Logger > push()")
                        val logsCanPush = logs.filter { canLog(it.type) }
                        val body = PushRequest(RL.cid, logsCanPush)
                        Relog.api.push(RL.appKey, body).enqueue(object : Callback<ResponseBody?> {
                            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                                if (response.isSuccessful) {
                                    try {
                                        response.body()?.string()?.isJsonObj()?.let { json ->
                                            if (json.optString("status") == "ok") {
                                                pushTimeout--
                                                if (pushTimeout < 2) pushTimeout = 2
                                                if (pushTimeout > 4) pushTimeout = 4
                                                //d("Relog", "Logger > push() > ok")
                                                onIO {
                                                    database?.logDao()?.delete(logs)
                                                    pushing = false
                                                }
                                                return
                                            }
                                        }
                                    } catch (t: Throwable) {
                                        pushing = false
                                        onFailure(call, t)
                                        return
                                    }
                                }
                                onFailure(call, Exception(response.errorBody()?.toString()))
                            }
                            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                                pushing = false
                                //d("Relog", "Logger > push() > failed")
                                //t.printStackTrace()
                            }
                        })
                    } else {
                        pushing = false
                        pushTimeout++
                        if (pushTimeout > 15) pushTimeout = 15
                        //d("Relog", "Logger > push() > is empty")
                    }
                    return@let
                }
            } catch (t: Throwable) {
                pushing = false
            }
        }.start()
    }

    internal fun log(timestamp: String, type: Type, tag: String, message: String) {
        if (canLog(type)) {
            GlobalScope.launch {
                channel.send(
                    Log(
                        timestamp,
                        RL.debug,
                        RL.appVersionCode ?:0,
                        RL.appVersionName ?:"",
                        RL.osVersion ?:"",
                        type,
                        tag,
                        message
                    )
                )
                //d("Relog", "Logger > log() > send to flow")
            }
        }
    }

    fun canLog(type: Type?): Boolean {
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

}