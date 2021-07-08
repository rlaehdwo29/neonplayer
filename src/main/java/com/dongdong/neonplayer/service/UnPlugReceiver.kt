package com.dongdong.neonplayer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class UnPlugReceiver : BroadcastReceiver() {

    private val TAG = UnPlugReceiver::class.java.simpleName
    var mHeadSetState = 0

    override fun onReceive(context: Context?, intent: Intent?) {

        if (Intent.ACTION_HEADSET_PLUG == intent?.action){
                if (intent.hasExtra("state")){
                    mHeadSetState = intent.getIntExtra("state",-1)
                    //헤드셋 연결 종료
                    if (mHeadSetState == 0) {
                        Log.d(TAG,"헤드셋 연결이 분리되었습니다.")
                        var startServiceIntent : Intent = Intent(context,MusicService::class.java)
                        startServiceIntent.action = MusicService.ACTION_PAUSE_UNPLUGGED
                        context?.startService(startServiceIntent)
                    }else if (mHeadSetState == 1){
                        Log.d(TAG,"헤드셋이 연결되었습니다.")
                    }

                }
        }

    }
}