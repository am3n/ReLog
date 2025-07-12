package ir.am3n.relog.data.local.db

import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Keep
@Database(
    entities = [
        Log::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    TypeConverter::class
)
abstract class LogDatabase : RoomDatabase() {

    abstract fun logDao(): LogDao?

}