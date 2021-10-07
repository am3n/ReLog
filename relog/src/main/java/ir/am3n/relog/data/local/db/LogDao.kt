package ir.am3n.relog.data.local.db

import androidx.room.*
import ir.am3n.needtool.db.BaseDao

@Dao
abstract class LogDao : BaseDao<Log>() {

    @get:Query("SELECT * FROM Log LIMIT 500")
    abstract val chunk: List<Log>?

}