package ir.am3n.relog

import android.content.Context
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import ir.am3n.needtool.*
import ir.am3n.relog.data.Config
import ir.am3n.relog.data.remote.HelloRequest
import ir.am3n.relog.data.remote.Remote
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Client(
    private var context: Context?,
    private val device: HashMap<String, String>?
) {

    private var nsr: NetworkStateReceiver? = null
    private var helloing = false
    private var helloSucc: Boolean = false
    private var helloTryedFailed = 0

    init {

        onIO {
            try {
                nsr?.stop()
            } catch (_: Throwable) {
            }
            nsr = NetworkStateReceiver(context, listener = object : NetworkStateReceiver.Listener {
                override fun onChanged(state: NetworkStateReceiver.State, network: Network?) {
                    if (state == NetworkStateReceiver.State.AVAILABLE)
                        hello()
                }

                override fun onChangedOnLowApi(state: NetworkStateReceiver.State) {
                    if (state == NetworkStateReceiver.State.AVAILABLE)
                        hello()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                }
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

        if (RL.debug) Log.d("Relog", "try hello to server")

        val body = HelloRequest(
            RL.clientId,
            RL.firebaseToken,
            RL.identification,
            RL.extraInfo,
            RL.debug,
            device
        )

        Remote.api.hello(RL.appKey, body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                helloing = false
                if (response.isSuccessful) {
                    try {
                        response.body()?.string()?.isJsonObj()?.let { json ->
                            if (json.optString("status") == "ok") {
                                json.optJSONObject("data")?.let { data ->
                                    Config.set(context, data.optJSONObject("config"))
                                    RL.cid = data.optLong("cid")
                                    if (RL.cid > 0) {
                                        if (RL.debug) Log.d("Relog", "hello succeed")
                                        helloSucc = true
                                        helloTryedFailed = 0
                                        onIO(30 * 60_000) { hello() } // every 30 minutes
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
                if (RL.debug) {
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
        return Config.enable
                && Config.clientEnable
                && helloSucc
                && RL.cid > 0
                && RL.clientId.isNotEmpty()
                && nsr?.state == NetworkStateReceiver.State.AVAILABLE
    }

}