package com.dongdong.neonplayer.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import java.io.Serializable

data class VideoInfo(
    @SerializedName("videopath")
     val videopath : String
):Serializable