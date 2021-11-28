package ir.am3n.relog

import android.content.Context
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import ir.am3n.needtool.*
import ir.am3n.relog.data.remote.HelloRequest
import ir.am3n.relog.data.remote.Relog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Client(
    private var context: Context?,
    private val device: HashMap<String, String>?,
    private var callback: (() -> Unit)?
) {

    var id: Long = 0
        get() {
            return context?.sh("relog")?.getLong("id", 0) ?:0
        }
        set(value) {
            field = value
            context?.sh("relog")?.edit()?.putLong("id", value)?.apply()
        }

    private var clientId: String = ""
        get() {
            return context?.sh("relog")?.str("client_id") ?:""
        }
        set(value) {
            field = value
            context?.sh("relog")?.edit()?.putString("client_id", value)?.apply()
        }

    private var nsr: NetworkStateReceiver? = null
    private var helloing = false
    private var helloSucc: Boolean = false
    private var helloTryedFailed = 0

    init {

        onIO {
            AdvertisingInfo(context).run {
                clientId = getAdvertisingId() ?: ""
                if (clientId.isEmpty()) {
                    clientId = getUniqueId()
                }
                if (RL.logging) Log.d("Relog", "clientId=$clientId")
            }
        }

        onIO {
            try { nsr?.stop() } catch (t: Throwable) {}
            nsr = NetworkStateReceiver(context, listener = object : NetworkStateReceiver.Listener {
                override fun onChanged(state: NetworkStateReceiver.State, network: Network?) {
                    if (state == NetworkStateReceiver.State.AVAILABLE)
                        hello()
                }
                override fun onChangedOnLowApi(state: NetworkStateReceiver.State) {
                    if (state == NetworkStateReceiver.State.AVAILABLE)
                        hello()
                }
                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {}
            })
        }

    }

    internal fun hello() {

        if (helloing) return
        helloing = true

        if (nsr?.state != NetworkStateReceiver.State.AVAILABLE) {
            helloing = false
            return
        }

        offUI(hello)
        onUI(hello, 1500)

    }

    private val hello = Runnable {

        if (RL.logging) Log.d("Relog", "try hello to server")

        val body = HelloRequest(
            clientId,
            RL.firebaseToken,
            RL.identification,
            RL.extraInfo,
            RL.debug,
            device
        )
        Relog.api.hello(RL.appKey, body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                helloing = false
                if (response.isSuccessful) {
                    try {
                        response.body()?.string()?.isJsonObj()?.let { json ->
                            if (json.optString("status") == "ok") {
                                json.optJSONObject("data")?.let { data ->
                                    RL.setConfig(data.optJSONObject("config"))
                                    id = data.optLong("cid")
                                    if (id > 0) {
                                        if (RL.logging) Log.d("Relog", "hello succeed")
                                        helloSucc = true
                                        helloTryedFailed = 0
                                        callback?.invoke()
                                        callback = null
                                        onIO(5 * 60 * 1000) { hello() }
                                        return
                                    }
                                }
                            }
                        }
                    } catch (t: Throwable) {
                        onFailure(call, t)
                        return
                    }
                }
                onFailure(call, Exception(response.errorBody()?.toString()))
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                helloing = false
                helloTryedFailed++
                if (RL.logging) {
                    Log.e("Relog", "hello failed", t)
                }
                onIO((if (helloTryedFailed < 3) 2 else 10) * 1000) {
                    hello()
                }
            }
        })
    }

    internal fun stop() {
        try {
            nsr?.stop()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun canPush(): Boolean {
        return helloSucc && id>0 && clientId.isNotEmpty() && nsr?.state==NetworkStateReceiver.State.AVAILABLE
    }

}