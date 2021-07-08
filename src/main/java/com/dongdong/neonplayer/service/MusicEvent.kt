package com.dongdong.neonplayer.service

import android.media.MediaPlayer
import com.dongdong.neonplayer.data.PlayMusicInfo
import java.io.Serializable

 data class MusicEvent(
    var mMediaPlayer: MediaPlayer? = null,
    var mMusicInfo: PlayMusicInfo? = null
) :Serializable{
 }