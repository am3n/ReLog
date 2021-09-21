package ir.am3n.relog.data.remote

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import ir.am3n.relog.data.local.db.Log
import java.util.HashMap

@Keep
data class HelloRequest(

    @SerializedName("client_id") @Expose
    var clientId: String?,

    @SerializedName("firebase_token") @Expose
    var firebaseToken: String?,

    @SerializedName("identification") @Expose
    var identification: String?,

    @SerializedName("extra_info") @Expose
    var extraInfo: String?,

    @SerializedName("debug") @Expose
    var debug: Boolean?,

    @SerializedName("app_version") @Expose
    var appVersion: String?,

    @SerializedName("os_version") @Expose
    var osVersion: String?,

    @SerializedName("ps_version") @Expose
    var psVersion: String?,

    @SerializedName("device_imei") @Expose
    var deviceImei: String?,

    @SerializedName("device_model") @Expose
    var deviceModel: String?,

    @SerializedName("cpu") @Expose
    var cpu: String?,

    @SerializedName("device_screen_class") @Expose
    var deviceScreenClass: String?,

    @SerializedName("device_dpi_class") @Expose
    var deviceDpiClass: String?,

    @SerializedName("device_screen_size") @Expose
    var deviceScreenSize: String?,

    @SerializedName("device_screen_dimensions_dpis") @Expose
    var deviceScreenDimensionsDpis: String?,

    @SerializedName("device_screen_dimensions_pixels") @Expose
    var deviceScreenDimensionsPixels: String?

) {

    constructor(clientId: String, firebaseToken: String, identification: String, extraInfo: String, debug: Boolean, device: HashMap<String, String>?) : this(
        clientId,
        firebaseToken,
        identification,
        extraInfo,
        debug,
        device?.get("appVersion"),
        device?.get("osVersion"),
        device?.get("psVersion"),
        device?.get("deviceImei"),
        device?.get("deviceModel"),
        device?.get("cpu"),
        device?.get("deviceScreenClass"),
        device?.get("deviceDpiClass"),
        device?.get("deviceScreenSize"),
        device?.get("deviceScreenDimensionsDpis"),
        device?.get("deviceScreenDimensionsPixels")
    )

}