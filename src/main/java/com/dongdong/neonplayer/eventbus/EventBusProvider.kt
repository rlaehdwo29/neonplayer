package com.dongdong.neonplayer.eventbus

import android.content.Context
import android.media.MediaPlayer
import com.dongdong.neonplayer.service.MusicService
import com.google.common.eventbus.EventBus
import com.squareup.otto.Bus

class EventBusProvider private constructor(){

    var mediaPlayer : MediaPlayer? = null

    companion object{
        var BUS : Bus = Bus()
        var instance : EventBusProvider? = null
        var mcontext : Context? = null

        fun getinstance(context: Context) : EventBusProvider{
            return instance ?: synchronized(this){
                instance ?: EventBusProvider().also {
                    mcontext = context
                    instance = it
                }
            }
        }
    }

    fun setMediaPlay(mp : MediaPlayer,instance : MusicService) {
        this.mediaPlayer = mp
        this.mediaPlayer?.setOnCompletionListener(instance)
    }

    fun getMediaPlay() : MediaPlayer? {
        return this.mediaPlayer
    }

    fun resetMediaPlay() {
        this.mediaPlayer = null
    }

}