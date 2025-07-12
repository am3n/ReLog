package ir.am3n.relog

import android.app.Application
import android.util.Log
import ir.am3n.needtool.*
import ir.am3n.relog.data.Config
import ir.am3n.relog.data.remote.Remote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.text.SimpleDateFormat
import java.util.*

class RL {

    companion object {

        private var context: Application? = null
        private var logger: Logger? = null
        private var client: Client? = null

        internal var scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        internal var host: String = ""
        internal var appKey: String = ""
        internal var appVersionCode: Long? = null
        internal var appVersionName: String? = null
        internal var osVersion: String? = null
        internal var debug: Boolean = true

        internal var cid: Long = 0
            get() {
                return context?.sh("relog")?.getLong("cid", 0) ?: 0
            }
            set(value) {
                field = value
                context?.sh("relog")?.edit()?.putLong("cid", value)?.apply()
            }

        var clientId: String = ""
            get() {
                return context?.sh("relog")?.str("client_id") ?:""
            }
            set(value) {
                field = value
                context?.sh("relog")?.edit()?.putString("client_id", value)?.apply()
            }

        var firebaseToken: String = ""
            get() {
                return context?.sh("relog")?.str("firebase_token") ?: ""
            }
            set(value) {
                field = value
                context?.sh("relog")?.edit()?.putString("firebase_token", value)?.apply()
                client?.hello()
            }

        var identification: String = ""
            get() {
                return context?.sh("relog")?.str("identification") ?: ""
            }
            set(value) {
                field = value
                context?.sh("relog")?.edit()?.putString("identification", value)?.apply()
                client?.hello()
            }

        var extraInfo: String = ""
            get() {
                return context?.sh("relog")?.str("extra_info") ?: ""
            }
            set(value) {
                field = value
                context?.sh("relog")?.edit()?.putString("extra_info", value)?.apply()
                client?.hello()
            }

        internal fun canPush(): Boolean {
            return client?.canPush() ?: false
        }

        private val timestamp: String
            get() {
                return try {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS_", Locale.US)
                        .format(Date()) + System.nanoTime().toString().substring(5)
                } catch (t: Throwable) {
                    ""
                }
            }

        fun init(
            application: Application?,
            host: String,
            appKey: String,
            clientId: String? = null
        ) {

            this.context = application
            val device = application?.device()
            val appVersion = device?.get("appVersion") ?: ""

            this.host = host
            this.appKey = appKey
            if (clientId.isNullOrBlank() && this.clientId.isBlank()) {
                this.clientId = UUID.randomUUID().toString()
            } else if (!clientId.isNullOrBlank()) {
                this.clientId = clientId
            }
            this.appVersionCode = "\\d*".toRegex().find(appVersion)?.value?.toLongOrNull() ?: 0
            this.appVersionName = "\\(.*\\)".toRegex().find(appVersion)?.value?.replace(Regex("[()]"), "") ?: ""
            this.osVersion = device?.get("osVersion") ?: ""
            this.debug = application?.isDebug() ?: true

            Config.init(application)

            logger?.stop()
            client?.stop()
            logger = Logger(application)
            client = Client(application, device)

        }

        fun updateHost(value: String) {
            if (this.host != value) {
                this.host = value
                Remote.lazyManager.reset()
            }
        }

        fun updateAppKey(value: String) {
            this.appKey = value
        }

        fun d(tag: String, message: String, androidLog: Boolean = true) {
            logger?.log(timestamp, Type.DEBUG, tag, message)
            if (androidLog)
                Log.d(tag, message)
        }

        fun i(tag: String, message: String, androidLog: Boolean = true) {
            logger?.log(timestamp, Type.INFO, tag, message)
            if (androidLog)
                Log.d(tag, message)
        }

        fun v(tag: String, message: String, androidLog: Boolean = true) {
            logger?.log(timestamp, Type.VERBOSE, tag, message)
            if (androidLog)
                Log.d(tag, message)
        }

        fun w(tag: String, message: String, androidLog: Boolean = true) {
            logger?.log(timestamp, Type.WARN, tag, message)
            if (androidLog)
                Log.d(tag, message)
        }

        fun e(tag: String, message: String, androidLog: Boolean = true) {
            logger?.log(timestamp, Type.ERROR, tag, message)
            if (androidLog)
                Log.e(tag, message)
        }

        fun e(tag: String, throwable: Throwable?, androidLog: Boolean = true) {
            e(tag, "", throwable, androidLog)
        }

        fun e(tag: String, message: String = "", throwable: Throwable?, androidLog: Boolean = true) {
            logger?.log(timestamp, Type.ERROR, tag, message + "\n" + Log.getStackTraceString(throwable))
            if (androidLog)
                Log.e(tag, message, throwable)
        }

    }

}