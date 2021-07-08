package com.dongdong.neonplayer.room.Entity

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playmusic_data")
class MyMusicPlayListEntity (
    @ColumnInfo(name = "userid")            var userid : String? = "",
    @ColumnInfo(name = "music_title")       var music_title : String? = "",
    @ColumnInfo(name = "music_artist")      var music_artist : String? = "",
    @ColumnInfo(name = "music_uri")         var music_uri : String? = ""
){
    @PrimaryKey(autoGenerate = true)  var album_uid : Int = 0
}