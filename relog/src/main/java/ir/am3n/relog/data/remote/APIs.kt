package ir.am3n.relog.data.remote

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface APIs {

    @POST("/api/v1/hello")
    fun hello(
        @Header("app-key") appKey: String,
        @Body body: HelloRequest
    ): Call<ResponseBody>

    @POST("/api/v1/push")
    fun push(
        @Header("app-key") appKey: String,
        @Body body: PushRequest
    ): Call<ResponseBody>

}
