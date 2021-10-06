package com.dongdong.neonplayer.room.Entity

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "finish_music_data")
class FinishMusicInfoEntity (
    @ColumnInfo(name = "finish_userid")            var finish_userid : String? = "",
    @ColumnInfo(name = "finish_title")             var finish_title : String? = "",
    @ColumnInfo(name = "finish_artist")            var finish_artist : String? = "",
    @ColumnInfo(name = "finish_uri")               var finish_uri : String? = ""
){
    @PrimaryKey(autoGenerate = true)  var finish_uid : Int = 0
}