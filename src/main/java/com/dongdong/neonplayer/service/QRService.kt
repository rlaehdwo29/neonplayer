package com.dongdong.neonplayer.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import com.dongdong.neonplayer.dialogactivity.QR_Dialog

class QRService : Service() {

    companion object{
        private val TAG = QRService::class.java.simpleName
        const val COUNTDOWNTIMER : Long = 15*1000
    }

    var CDT: CountDownTimer? = null

    override fun onCreate() {
        super.onCreate()
        startTimer()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return ServiceBinder()
    }

    inner class ServiceBinder : Binder() {
        fun getService() : QRService{ // 서비스 객체를 리턴
            return this@QRService
        }

    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    fun startTimer(){
        CDT = object : CountDownTimer(COUNTDOWNTIMER, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                //count_time?.text = (millisUntilFinished / 1000).toString()
                Log.d(TAG,"OnTick -> $millisUntilFinished")
            }

            override fun onFinish() {
                //마지막에 실행할 구문
                Log.d(TAG,"onFinish!!")
                this.cancel()
            }
        }.start()

    }

}