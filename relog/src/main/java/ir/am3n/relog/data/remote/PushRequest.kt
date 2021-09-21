package ir.am3n.relog.data.remote

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import ir.am3n.relog.data.local.db.Log

@Keep
data class PushRequest(

    @SerializedName("cid") @Expose
    var cid: Long?,

    @SerializedName("logs") @Expose
    var logs: List<Log>?

)