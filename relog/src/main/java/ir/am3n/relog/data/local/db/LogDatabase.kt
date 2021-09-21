package ir.am3n.relog.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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