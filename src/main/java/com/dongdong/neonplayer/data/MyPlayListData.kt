package com.dongdong.neonplayer.data

import java.io.Serializable

data class MyPlayListData (
    val album_uid : Int?,
    val userid : String?,
    val music_title : String?,
    val music_artist : String?,
    val music_uri : String?
): Serializable {}