package ir.am3n.relog.data.local.db

import androidx.annotation.Keep
import androidx.room.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import ir.am3n.relog.Type

@Keep
@Entity(tableName = "Log")
open class Log {

    @SerializedName("id") @Expose
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0

    @SerializedName("timestamp") @Expose
    @ColumnInfo(name = "timestamp")
    var timestamp: String? = ""

    @SerializedName("debug") @Expose
    @ColumnInfo(name = "debug")
    var debug: Boolean? = true

    @SerializedName("version_code") @Expose
    @ColumnInfo(name = "version_code")
    var versionCode: Long? = 0

    @SerializedName("version_name") @Expose
    @ColumnInfo(name = "version_name")
    var versionName: String? = ""

    @SerializedName("os_version") @Expose
    @ColumnInfo(name = "os_version")
    var osVersion: String? = ""

    @SerializedName("type") @Expose
    @ColumnInfo(name = "type", defaultValue = "DEBUG")
    @TypeConverters(TypeConverter::class)
    var type: Type? = Type.DEBUG

    @SerializedName("tag") @Expose
    @ColumnInfo(name = "tag")
    var tag: String? = ""

    @SerializedName("message") @Expose
    @ColumnInfo(name = "message")
    var message: String? = ""


    constructor()

    @Ignore
    constructor(timestamp: String, debug: Boolean, versionCode: Long, versionName: String,
                osVersion: String, type: Type, tag: String, message: String) {
        this.timestamp = timestamp
        this.debug = debug
        this.versionCode = versionCode
        this.versionName = versionName
        this.osVersion = osVersion
        this.type = type
        this.tag = tag
        this.message = message
    }


}