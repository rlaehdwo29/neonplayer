package com.dongdong.neonplayer.room.Dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dongdong.neonplayer.data.MusicInfo
import com.dongdong.neonplayer.room.Entity.MyMusicPlayListEntity

@Dao
interface MyMusicPlayListDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSelectMusic(myMusicPlayListEntity: MyMusicPlayListEntity) : Long

    /**
     * 플레이 리스트 음악 찾기
     */
    @Query("SELECT * FROM playmusic_data WHERE userid = :userid AND music_title = :music_title AND music_artist = :music_artist AND music_uri = :music_uri ORDER BY album_uid DESC")
    suspend fun getSelectPlayData(userid: String, music_title : String, music_artist : String, music_uri : String) : MyMusicPlayListEntity

    /**
     * 플레이 리스트 음악 UID 찾기
     */
    @Query("SELECT album_uid FROM playmusic_data WHERE userid = :userid AND music_title = :music_title AND music_artist = :music_artist AND music_uri = :music_uri ORDER BY album_uid DESC")
    suspend fun getPlayDataUID(userid: String, music_title : String, music_artist : String, music_uri : String) : MyMusicPlayListEntity

    /**
     * 플레이 리스트 전부 가져오기
     */
    @Query("select * from playmusic_data WHERE userid = :userid ORDER BY album_uid DESC")
    suspend fun getAllMyPlayListRx(userid : String) : List<MyMusicPlayListEntity>

    @Update
    suspend fun updatePlayList(vararg myMusicPlayListInfo: MyMusicPlayListEntity)

    @Query("DELETE FROM playmusic_data")
    suspend fun deleteAllPlayList()

}