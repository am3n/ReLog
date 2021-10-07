package ir.am3n.relog.data.local.db

import androidx.lifecycle.LiveData
import androidx.room.*
import ir.am3n.needtool.db.BaseDao

@Dao
abstract class LogDao : BaseDao<Log>() {

    @get:Query("SELECT * FROM Log LIMIT 100")
    abstract val chunk: List<Log>?

    @Query("SELECT * FROM Log")
    abstract fun all(): LiveData<List<Log>?>

}