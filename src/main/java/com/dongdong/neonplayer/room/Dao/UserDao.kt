package com.dongdong.neonplayer.room.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.dongdong.neonplayer.room.Entity.UserEntitiy
import io.reactivex.Single

@Dao
interface UserDao {

    @Query("select * from UserEntitiy where userid = :id")
    fun getIDcheck(id : String) : Single<UserEntitiy>

    @Query("select * from UserEntitiy")
    fun getAll() : List<UserEntitiy>

    @Insert
    fun insertAll(user : UserEntitiy)

    @Delete
    fun delete(user : UserEntitiy)
}