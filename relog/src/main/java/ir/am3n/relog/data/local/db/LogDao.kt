package ir.am3n.relog.data.local.db

import androidx.room.*
import ir.am3n.needtool.db.BaseDao

@Dao
abstract class LogDao : BaseDao<Log>() {

    @get:Query("SELECT * FROM Log")
    abstract val all: List<Log>?

    @Query("SELECT * FROM Log WHERE id=:id LIMIT 1")
    abstract fun getById(id: Int?): Log?

    @Query("delete from Log")
    abstract fun deleteAll()

}