package ir.am3n.relog.data.remote

import com.google.gson.GsonBuilder
import ir.am3n.relog.RL
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

internal object Relog {

    internal val api: RelogAPIs by lazy {

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
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        return@lazy Retrofit.Builder()
            .baseUrl(RL.url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(RelogAPIs::class.java)

    }

}
