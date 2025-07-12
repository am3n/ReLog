package ir.am3n.relog.data.remote

import com.google.gson.GsonBuilder
import ir.am3n.needtool.ResettableLazy
import ir.am3n.needtool.ResettableLazyManager
import ir.am3n.relog.RL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

internal object Relog {

    internal val lazyManager = ResettableLazyManager()

    private val resettableLazy: ResettableLazy<RelogAPIs> = ResettableLazy(lazyManager) {

        /*var cipherSuites = ConnectionSpec.MODERN_TLS.cipherSuites()
        if (cipherSuites?.contains(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA) == false) {
            cipherSuites = ArrayList(cipherSuites)
            cipherSuites.add(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA)
            cipherSuites.add(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA)
        }
        val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .cipherSuites(*cipherSuites!!.toTypedArray())
            .build()*/

        val okHttpClient = OkHttpClient.Builder()
            //.connectionSpecs(listOf(spec, ConnectionSpec.CLEARTEXT))
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        return@ResettableLazy Retrofit.Builder()
            .baseUrl(RL.host)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(RelogAPIs::class.java)

    }

    internal val api get() = resettableLazy.lazyHolder.value

}
