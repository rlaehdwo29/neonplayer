package com.dongdong.neonplayer.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MusicInfo(
        @SerializedName("musictitle")
        val musictitle : String
):Serializable