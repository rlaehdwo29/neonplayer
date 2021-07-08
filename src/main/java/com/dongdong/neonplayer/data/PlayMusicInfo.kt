package com.dongdong.neonplayer.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PlayMusicInfo(
        val album_uid : Int?,
        val userid : String?,
        val music_title : String?,
        val music_artist : String?,
        val music_uri : String?
)