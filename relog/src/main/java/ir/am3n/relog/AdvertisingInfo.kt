package ir.am3n.relog

import android.content.Context
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import ir.am3n.needtool.deviceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdvertisingInfo(private val context: Context?) {

    private var aic: AdvertisingIdClient? = null

    init {
        aic = AdvertisingIdClient(context?.applicationContext)
    }

    suspend fun getAdvertisingId(): String? {
        try {
            withContext(Dispatchers.IO) {
                try {
                    aic!!.start()
                    val adIdInfo = aic!!.info
                    aic!!.finish()
                    return@withContext adIdInfo.id
                } catch (t: Throwable) {
                    //t.printStackTrace()
                }
                return@withContext null
            }
        } catch (t: Throwable) {
            //t.printStackTrace()
        }
        return null
    }


    fun getUniqueId(): String = context?.deviceId() ?:""

}