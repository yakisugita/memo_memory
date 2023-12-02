package net.yakijake.memory.ui.theme

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity
data class Memo(
    @PrimaryKey val uuid: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "desc") val desc: String,
    @ColumnInfo(name = "parent") val parent: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
)

@Dao
interface MemoDao {
    @Query("SELECT * FROM memo")
    suspend fun getAll(): List<Memo>

//    @Query("SELECT * FROM memo WHERE uuid IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): List<Memo>

//    @Query("SELECT * FROM memo WHERE first_name LIKE :first AND last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): Memo

//    @Query("SELECT * FROM memo WHERE parent LIKE :dir")
//    fun findByDir(dir: String): Memo

    @Insert
    suspend fun insertAll(vararg users: Memo)

    @Delete
    suspend fun delete(user: Memo)
}

@Database(entities = [Memo::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao
}