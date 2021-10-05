package com.dongdong.neonplayer.`interface`

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dongdong.neonplayer.data.BannerInfo
import com.dongdong.neonplayer.data.MusicInfo
import com.dongdong.neonplayer.data.VideoInfo
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface API {
        @GET("neon/api/videofilelist.php")
        fun getVideoFilePath() : Call<List<VideoInfo>>

        @GET("neon/api/todaynewmusiclist.php")
        fun getTodayNewMusicList() : Call<List<MusicInfo>>

        @GET("neon/api/newmusiclist.php")
        fun getNewMusicList() : Call<List<MusicInfo>>

        @GET("neon/api/newmusiconeweeklist.php")
        fun getOneWeekNewMusicList() : Call<List<MusicInfo>>

        @GET("neon/api/neonchartlist.php")
        fun getNeonChartList() : Call<List<MusicInfo>>

        @GET("neon/api/popmusiclist.php")
        fun getPopMusicList() : Call<List<MusicInfo>>

        @GET("neon/api/homebannerfilelist.php")
        fun getHomeBannerFilePath() : Call<List<BannerInfo>>

}