package com.dongdong.neonplayer.room.Dao

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.room.*
import com.dongdong.neonplayer.room.Entity.FinishMusicInfoEntity
import com.dongdong.neonplayer.room.Entity.MyMusicPlayListEntity

@Dao
interface FinishMusicInfoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFinshMusicInfo(finishmusicinfoEntitiy: FinishMusicInfoEntity) : Long

    @Query("SELECT * FROM finish_music_data WHERE finish_userid = :finish_userid ORDER BY finish_uid DESC")
    suspend fun getSelectFinishPlayData(finish_userid: String) : FinishMusicInfoEntity

    @Query("UPDATE finish_music_data SET finish_userid = :finish_userid, finish_title = :finish_title,finish_artist = :finish_artist,finish_uri = :finish_uri WHERE finish_uid")
    suspend fun updateFinishMusicData(finish_userid: String,finish_title : String,finish_artist : String,finish_uri : String)

    @Update(onConflict = OnConflictStrategy.REPLACE)	// 충돌 시 덮어쓰기
    suspend fun update(vararg finishmusicinfoEntitiy: FinishMusicInfoEntity)	// n개의 객체 (배열 형태)

}