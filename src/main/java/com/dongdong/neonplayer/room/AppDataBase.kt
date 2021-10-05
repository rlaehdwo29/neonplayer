package com.dongdong.neonplayer.room

import android.content.Context
import androidx.room.*
import com.dongdong.neonplayer.room.Dao.FinishMusicInfoDao
import com.dongdong.neonplayer.room.Dao.MyMusicPlayListDao
import com.dongdong.neonplayer.room.Dao.UserDao
import com.dongdong.neonplayer.room.Entity.FinishMusicInfoEntity
import com.dongdong.neonplayer.room.Entity.MyMusicPlayListEntity
import com.dongdong.neonplayer.room.Entity.UserEntitiy
import com.dongdong.neonplayer.room.Entity.UserTypeConverter

@Database(entities = [UserEntitiy::class, MyMusicPlayListEntity::class, FinishMusicInfoEntity::class], version = 1,exportSchema = false)
@TypeConverters(UserTypeConverter::class)
abstract class AppDataBase : RoomDatabase(){

    abstract fun UserDao() : UserDao
    abstract fun MyMusicPlayListDao() : MyMusicPlayListDao
    abstract fun FinishMusicInfoDao() : FinishMusicInfoDao

    companion object{
        private val DB_NAME = "app-db"
        private var instance : AppDataBase? = null

        fun getInstance(context : Context) : AppDataBase {
            if (instance != null) {
                instance ?: synchronized(this) {
                    instance ?: Room.databaseBuilder(context, AppDataBase::class.java, DB_NAME)
                        .build()
                }
            }
            return instance!!
        }
    }


}