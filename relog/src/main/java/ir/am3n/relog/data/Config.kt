package ir.am3n.relog.data

import android.content.Context
import ir.am3n.needtool.sh
import org.json.JSONObject

object Config {

    internal var clientEnable: Boolean = false
    internal var clientTypeFilter: Byte = 0
    internal var enable: Boolean = false
    internal var typeFilter: Byte = 0
    internal var filterOperator: String = "and"
    internal var keepLogs: Int = 30

    internal fun init(context: Context?) {
        context ?: return
        val preferences = context.sh("relog")
        clientEnable = preferences.getBoolean("config__client_enable", false)
        clientTypeFilter = preferences.getInt("config__client_type_filter", 0).toByte()
        enable = preferences.getBoolean("config__enable", false)
        typeFilter = preferences.getInt("config__type_filter", 0).toByte()
        filterOperator = preferences.getString("config__filter_operator", "and") ?: "and"
        keepLogs = preferences.getInt("config__keep_logs", 0)
    }

    internal fun set(context: Context?, config: JSONObject?) {
        context ?: return
        config ?: return
        setClientEnable(context, config.optInt("client_enable") == 1)
        setClientTypeFilter(context, config.optInt("client_type_filter"))
        setEnable(context, config.optInt("enable") == 1)
        setTypeFilter(context, config.optInt("type_filter"))
        setFilterOperator(context, config.optString("filter_operator"))
        setKeepLogs(context, config.optInt("keep_logs"))
    }

    internal fun setClientEnable(context: Context?, value: Boolean) {
        clientEnable = value
        context?.sh("relog")?.edit()?.putBoolean("config__client_enable", value)?.apply()
    }

    internal fun setClientTypeFilter(context: Context?, filter: Int) {
        clientTypeFilter = filter.toByte()
        context?.sh("relog")?.edit()?.putInt("config__client_type_filter", filter)?.apply()
    }

    internal fun setEnable(context: Context?, value: Boolean) {
        enable = value
        context?.sh("relog")?.edit()?.putBoolean("config__enable", value)?.apply()
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

}