package ir.am3n.relog.data.local.db

import androidx.room.TypeConverter
import ir.am3n.relog.Type

class TypeConverter {

    @TypeConverter
    fun toDeviceType(value: String?): Type? {
        return if (value == null) null else Type.valueOf(value)
    }

    @TypeConverter
    fun fromDeviceType(value: Type?): String? {
        return value?.name
    }

}
