package ir.am3n.relog

import android.content.Context
import android.util.Log
import ir.am3n.needtool.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class RL {

    companion object {

        private var context: Context? = null
        private var logger: Logger? = null
        private var client: Client? = null

        internal var url: String = ""
        internal var appKey: String = ""
        internal var appVersionCode: Long? = null
        internal var appVersionName: String? = null
        internal var osVersion: String? = null
        internal var debug: Boolean = true
        internal var logging: Boolean = false

        var firebaseToken: String = ""
            get() {
                return context?.sh("relog")?.str("firebase_token") ?:""
            }
            set(value) {
                field = value
                context?.sh("relog")?.edit()?.putString("firebase_token", value)?.apply()
                client?.hello()
            }

        var identification: String = ""
            get() {
                return context?.sh("relog")?.str("identification") ?:""
            }
            set(value) {
                field = value
                context?.sh("relog")?.edit()?.putString("identification", value)?.apply()
                client?.hello()
            }

        var extraInfo: String = ""
            get() {
                return context?.sh("relog")?.str("extra_info") ?:""
            }
            set(value) {
                field = value
                context?.sh("relog")?.edit()?.putString("extra_info", value)?.apply()
                client?.hello()
            }


        fun init(context: Context?, url: String, appKey: String, logging: Boolean = false) {

            this.context = context
            val device = context?.device()
            val appVersion = device?.get("appVersion") ?:""

            this.url = url
            this.appKey = appKey
            this.appVersionCode = "\\d*".toRegex().find(appVersion)?.value?.toLongOrNull() ?:0
            this.appVersionName = "\\(.*\\)".toRegex().find(appVersion)?.value?.replace(Regex("[()]"), "") ?:""
            this.osVersion = device?.get("osVersion") ?:""
            this.debug = context?.isDebug() ?:true

            this.logging = logging

            logger?.stop()
            client?.stop()
            logger = Logger(context)
            client = Client(context, device) {
                logger?.start(context)
            }

        }

        internal fun canPush(): Boolean {
            return client?.canPush() ?: false
        }

        internal val cid: Long? get() = client?.id

        internal fun setConfig(config: JSONObject?) {
            if (config == null) return
            logger?.setIndivTypeFilter(context, config.optInt("indiv_type_filter"))
            logger?.setTypeFilter(context, config.optInt("type_filter"))
            logger?.setFilterOperator(context, config.optString("filter_operator"))
            logger?.setKeepLogs(context, config.optInt("keep_logs"))
        }

        private val timestamp: String get() {
            return try {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS_", Locale.US).format(Date()) + System.nanoTime().toString().substring(5)
            } catch (t: Throwable) {
                ""
            }
        }

        fun d(tag: String, message: String, relog: Boolean = true) {
            logger?.log(timestamp, Type.DEBUG, tag, message)
            if (relog)
                Log.d(tag, message)
        }

        fun i(tag: String, message: String, relog: Boolean = true) {
            logger?.log(timestamp, Type.INFO, tag, message)
            if (relog)
                Log.d(tag, message)
        }

        fun v(tag: String, message: String, relog: Boolean = true) {
            logger?.log(timestamp, Type.VERBOSE, tag, message)
            if (relog)
                Log.d(tag, message)
        }

        fun w(tag: String, message: String, relog: Boolean = true) {
            logger?.log(timestamp, Type.WARN, tag, message)
            if (relog)
                Log.d(tag, message)
        }

        fun e(tag: String, message: String, relog: Boolean = true) {
            logger?.log(timestamp, Type.ERROR, tag, message)
            if (relog)
                Log.e(tag, message)
        }
        fun e(tag: String, throwable: Throwable?, relog: Boolean = true) {
            e(tag, "", throwable, relog)
        }
        fun e(tag: String, message: String = "", throwable: Throwable?, relog: Boolean = true) {
            logger?.log(timestamp, Type.ERROR, tag, message+"\n"+Log.getStackTraceString(throwable))
            if (relog)
                Log.e(tag, message, throwable)
        }

    }

}