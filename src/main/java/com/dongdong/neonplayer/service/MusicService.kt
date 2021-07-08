package com.dongdong.neonplayer.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.dongdong.neonplayer.R
import com.dongdong.neonplayer.activity.MusicPlayerActivity
import com.dongdong.neonplayer.common.MusicInfoLoadUtil
import com.dongdong.neonplayer.common.Util
import com.dongdong.neonplayer.data.*
import java.lang.ref.WeakReference


class MusicService : Service(), MediaPlayer.OnCompletionListener {

    companion object {
        private val TAG = MusicService::class.java.simpleName

        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PLAY_NEXT = "ACTION_PLAY_NEXT"
        const val ACTION_PLAY_PREVIOUS = "ACTION_PLAY_PREVIOUS"
        const val ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE"
        const val ACTION_PLAYSTATE = "ACTION_PLAYSTATE"
        const val ACTION_PAUSE_UNPLUGGED = "ACTION_PAUSE_UNPLUGGED"
        const val ACTION_PLAY_SELECTED = "ACTION_PLAY_SELECTED"
        const val ACTION_PLAY_FINISH = "ACTION_FINISH"
        const val MUSIC_SERVICE_FILTER = "MUSIC_SERVICE_FILTER"
        const val ACTION_NOTIFICATION = "ACTION_NOTIFICATION"

    }

    var mSession : MediaSessionCompat? = null
    var mMetadata : MediaMetadataCompat? = null
    var playdata : MusicPlayData? = null
    var allPlayList : ArrayList<MyPlayListData>? = null
    var mCurrentMusicInfo : PlayMusicInfo? = null
    private var mAction: String? = null
    private var mCurrentPosition = 0
    var isReady : Boolean? = false
    private var mUnPlugReceiver: UnPlugReceiver? = null
    var isPause : Boolean? = false
    //var mMediaPlayer: MediaPlayer? = null
    var isPlaying : Boolean? = false
    var newMusicCheck : Boolean? = false
    var musicevent : MusicEvent? = null
    var br_playstate : String? = null
    var click_notification_btn : Boolean? = false

    override fun onCreate() {
        super.onCreate()

        //mMediaPlayer = MediaPlayer()
        mUnPlugReceiver = UnPlugReceiver()
        var filter : IntentFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        registerReceiver(mUnPlugReceiver,filter)

        if (allPlayList == null) {
            allPlayList = MusicInfoLoadUtil.myPlayListAll(applicationContext)
            Log.e(TAG,"AllPlayList -> ${allPlayList?.size}")
        }

        ServiceRegisterReceiver()

    }

    override fun onBind(intent: Intent?): IBinder? {
        return ServiceBinder()
    }

    inner class ServiceBinder : Binder() {
        fun getService() : MusicService{ // 서비스 객체를 리턴
            return this@MusicService
        }

    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent != null && intent.action != null) {
            mAction = intent.action
        }
        if (mAction == null){
            stopForeground(true)
            stopService(Intent(applicationContext,MusicService::class.java))
            return START_NOT_STICKY
        }

        /**
         * 데이터 세팅 Action
         */
        Log.d(TAG,"ACTION RESULT -> $mAction || $br_playstate")
        when(mAction) {

            /**
             * Item 선택했을때 데이터 세팅
             */
            ACTION_PLAY_SELECTED -> {
                playdata = intent?.getSerializableExtra("playdata") as MusicPlayData?
                    Log.d(TAG, "[ACTION_PLAY_SELECTED] mCurrentPosition -> $playdata // ${mCurrentPosition} // ${playdata?.play_title}")
                allPlayList = intent?.getSerializableExtra("playlist") as ArrayList<MyPlayListData>?
                    Log.d(TAG, "[ACTION_PLAY_SELECTED] AllPlayListSize -> ${allPlayList?.size}")
                mCurrentPosition = intent?.getIntExtra("position", 0)!!
                 Log.d(TAG,"[ACTION_PLAY_SELECTED] mCurrentMusicInfo -> ${mCurrentMusicInfo?.music_title} || ${mCurrentMusicInfo?.music_artist}")
                mCurrentMusicInfo = MusicInfoLoadUtil.getSelectedMusicInfo(applicationContext, playdata?.play_title)
                isPlaying = intent?.getBooleanExtra("isplay",false)
                Log.d(TAG,"[ACTION_PLAYSTATE] isPlaying -> ${isPlaying}")
                showNotification()
            }

            /**
             * MusicPlayerActicity::class.java에서 Play,Pause 상태 데이터 세팅
             */
            ACTION_PLAYSTATE -> {

                if (intent?.getSerializableExtra("playdata") as MusicPlayData? != null) {
                    var getplaydata = intent?.getSerializableExtra("playdata") as MusicPlayData?
                    Log.d(TAG, "[ACTION_PLAYSTATE] 재생 음악 비교 Frist -> ${playdata?.play_title} || ${getplaydata?.play_title} || $br_playstate")
                    playdata = getplaydata

                }

                    Log.d(TAG, "[ACTION_PLAYSTATE] 기타 LOG -> $br_playstate")

                if (intent?.getIntExtra("position", 0)!! != null) {
                    mCurrentPosition = intent?.getIntExtra("position", 0)!!
                    Log.d(TAG, "[ACTION_PLAYSTATE] mCurrentPosition -> $playdata // ${mCurrentPosition} // ${playdata?.play_title}")
                }
                if (intent?.getSerializableExtra("playlist") as ArrayList<MyPlayListData>? != null) {
                    allPlayList = intent?.getSerializableExtra("playlist") as ArrayList<MyPlayListData>?
                    Log.d(TAG, "[ACTION_PLAYSTATE] AllPlayListSize -> ${allPlayList?.size}")
                }
                if (MusicInfoLoadUtil.getSelectedMusicInfo(applicationContext, playdata?.play_title) != null && playdata?.play_title != null) {
                    mCurrentMusicInfo = MusicInfoLoadUtil.getSelectedMusicInfo(applicationContext, playdata?.play_title)
                    Log.d(TAG,"[ACTION_PLAYSTATE] mCurrentMusicInfo -> ${mCurrentMusicInfo?.music_title} || ${mCurrentMusicInfo?.music_artist}")
                }
                if (intent?.getBooleanExtra("isplay",false) != null) {
                    isPlaying = intent?.getBooleanExtra("isplay", false)
                    Log.d(TAG, "[ACTION_PLAYSTATE] isPlaying -> ${isPlaying}")
                }
                if (intent?.getBooleanExtra("ispause",false) != null) {
                    isPause = intent?.getBooleanExtra("ispause", false)
                    Log.d(TAG, "[ACTION_PLAYSTATE] isPause -> ${isPause}")
                }
                if (intent?.getBooleanExtra("clicknotification",false) != null) {
                    if (intent?.getBooleanExtra("clicknotification",false) == true){
                        if (isPlaying == true){
                            br_playstate = ACTION_PLAY_PAUSE
                        }else{
                            br_playstate = ACTION_PLAY
                        }
                    }
                }
                    click_notification_btn = false
                    showNotification()
            }

            ACTION_NOTIFICATION -> {

            }

            /**
             * 다음 곡으로 이동할때 데이터 세팅
             */
            ACTION_PLAY_NEXT -> {
                mCurrentPosition = intent?.getIntExtra("position", 0)!!
                Log.d(TAG, "[ACTION_PLAY_NEXT] mCurrentPosition -> $playdata // ${mCurrentPosition} // ${playdata?.play_title}")
                allPlayList = intent?.getSerializableExtra("playlist") as ArrayList<MyPlayListData>? ?: null
                Log.d(TAG, "[ACTION_PLAY_NEXT] AllPlayListSize -> ${allPlayList?.size}")
                mCurrentMusicInfo = MusicInfoLoadUtil.getSelectedMusicInfo(applicationContext, playdata?.play_title)
                Log.d(TAG,"[ACTION_PLAY_NEXT] mCurrentMusicInfo -> ${mCurrentMusicInfo?.music_title} || ${mCurrentMusicInfo?.music_artist}")
                isPlaying = intent?.getBooleanExtra("isplay",false)
                Log.d(TAG,"[ACTION_PLAY_NEXT] isPlaying -> ${isPlaying}")
                showNotification()
            }

            /**
             * 이전 곡으로 이동할때 데이터 세팅
             */
            ACTION_PLAY_PREVIOUS -> {
                mCurrentPosition = intent?.getIntExtra("position", 0)!!
                Log.d(TAG, "[ACTION_PLAY_PREVIOUS] mCurrentPosition -> $playdata // ${mCurrentPosition} // ${playdata?.play_title}")
                allPlayList = intent?.getSerializableExtra("playlist") as ArrayList<MyPlayListData>? ?: null
                Log.d(TAG, "[ACTION_PLAY_PREVIOUS] AllPlayListSize -> ${allPlayList?.size}")
                mCurrentMusicInfo = MusicInfoLoadUtil.getSelectedMusicInfo(applicationContext, playdata?.play_title)
                Log.d(TAG,"[ACTION_PLAY_PREVIOUS] mCurrentMusicInfo -> ${mCurrentMusicInfo?.music_title} || ${mCurrentMusicInfo?.music_artist}")
                isPlaying = intent?.getBooleanExtra("isplay",false)
                Log.d(TAG,"[ACTION_PLAY_PREVIOUS] isPlaying -> ${isPlaying}")
                showNotification()
            }
/*
            /**
             * 플레이어 종료시 세팅
             */
            ACTION_PLAY_FINISH -> {

                if (isPlaying == true) {
                    mHandler.removeMessages(0)
                    mMediaPlayer?.stop()
                    mMediaPlayer?.release()
                    mMediaPlayer = null
                } else {
                    mHandler.removeMessages(0)
                    mMediaPlayer?.release()
                    mMediaPlayer = null
                }
                stopForeground(true)
                stopService(Intent(applicationContext, MusicService::class.java))
            }

            *//**
             * 블루투스 장치 제거 시 세팅
             *//*
            ACTION_PAUSE_UNPLUGGED -> {
                if (mMediaPlayer?.isPlaying == true) {
                    mMediaPlayer?.pause()
                    if (mCurrentMusicInfo != null && playdata != null) {
                        sendAllEvent(mMediaPlayer)
                        showNotification()
                    }
                }
            }
        }

        *//**
         * 이벤트 Action
         *//*
        when(mAction){
            *//**
             * Item에서 곡을 선택했을 때 설정
             *//*
            ACTION_PLAY_SELECTED -> {
                Log.e(TAG, "MUSIC_SELECTED")
                    isPause = false
                    mMediaPlayer?.pause()
                    mMediaPlayer?.reset()
                    try {
                        Log.d(TAG, "ACTION_PLAY_SELECTED -> ${Util.getParseUri(playdata?.play_title)}")
                        mMediaPlayer?.setDataSource(applicationContext, Util.getParseUri(playdata?.play_title))
                        mMediaPlayer?.prepareAsync()
                        isReady = true
                        mMediaPlayer?.setOnPreparedListener { mp ->
                            mp?.start()
                            sendAllEvent(mp)
                            Log.e(TAG, "ACTION_PLAY_SELECTED -> ${mp?.isPlaying}")
                            showNotification()
                            sendBroadCast()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
            }

            *//**
             * []MusicPlayerActivity::class.java] Play,Pause Action
             *//*
            ACTION_PLAYSTATE -> {
                Log.e(TAG, "ACTION_PLAYSTATE -> ${mMediaPlayer?.isPlaying} // $isPause //$newMusicCheck")

                if (mMediaPlayer?.isPlaying == true) {
                        if (newMusicCheck == false) {
                            mMediaPlayer?.pause()
                            isPause = true
                            showNotification()
                            sendAllEvent(mMediaPlayer)
                            sendBroadCast()
                        } else {
                            mMediaPlayer?.pause()
                            mMediaPlayer?.reset()
                            isPause = false

                            try {
                                Log.d(TAG, "ACTION_PLAYSTATE -> ${Util.getParseUri(playdata?.play_title)}")
                                mMediaPlayer?.setDataSource(applicationContext, Util.getParseUri(playdata?.play_title))
                                mMediaPlayer?.prepareAsync()
                                mMediaPlayer?.setOnPreparedListener { mp ->
                                    mp?.start()
                                    sendAllEvent(mp)
                                    Log.e(TAG, "ACTION_PLAYSTATE Playing State -> ${mp?.isPlaying}")
                                    showNotification()
                                    sendBroadCast()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                }else{
                    if (isPause == true){
                        if (newMusicCheck == false) {
                            mMediaPlayer?.start()
                            isPause = false
                            showNotification()
                            sendAllEvent(mMediaPlayer)
                            sendBroadCast()
                        }else{
                            mMediaPlayer?.pause()
                            mMediaPlayer?.reset()
                            isPause = false

                            try {
                                Log.d(TAG, "ACTION_PLAYSTATE -> ${Util.getParseUri(playdata?.play_title)}")
                                mMediaPlayer?.setDataSource(applicationContext, Util.getParseUri(playdata?.play_title))
                                mMediaPlayer?.prepareAsync()
                                mMediaPlayer?.setOnPreparedListener { mp ->
                                    mp?.start()
                                    sendAllEvent(mp)
                                    Log.e(TAG, "ACTION_PLAYSTATE Playing State -> ${mp?.isPlaying}")
                                    showNotification()
                                    sendBroadCast()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }else {
                            mMediaPlayer?.pause()
                            mMediaPlayer?.reset()
                            isPause = false

                            try {
                                Log.d(TAG, "ACTION_PLAYSTATE -> ${Util.getParseUri(playdata?.play_title)}")
                                mMediaPlayer?.setDataSource(applicationContext, Util.getParseUri(playdata?.play_title))
                                mMediaPlayer?.prepareAsync()
                                mMediaPlayer?.setOnPreparedListener { mp ->
                                    mp?.start()
                                    sendAllEvent(mp)
                                    Log.e(TAG, "ACTION_PLAYSTATE Playing State -> ${mp?.isPlaying}")
                                    showNotification()
                                    sendBroadCast()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                    }
                }
            }

            *//**
             * 다음 곡으로 이동버튼 클릭 시 Action
             *//*
            ACTION_PLAY_NEXT -> {
                Log.e(TAG, "MUSIC_NEXT -> [mCurrentPosition] ${mCurrentPosition}  [list size] ${allPlayList?.size}")
                if (mMediaPlayer?.isPlaying == true) {
                    isPause = true
                    mMediaPlayer?.stop()
                    mMediaPlayer?.reset()
                    if (mCurrentPosition + 1 < allPlayList!!.size) {
                        mCurrentPosition++
                    }
                    try {
                        mMediaPlayer?.setDataSource(applicationContext, Util.getParseUri(allPlayList?.get(mCurrentPosition)?.music_uri))
                        mMediaPlayer?.prepareAsync()
                        isReady = true
                        mMediaPlayer?.setOnPreparedListener { mp ->
                            mp?.start()
                            isPause = false
                            sendAllEvent(mp)
                            showNotification()
                            sendBroadCast()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }else{
                    mMediaPlayer?.reset()
                    isPause = true
                    try {
                        mMediaPlayer?.setDataSource(
                            applicationContext,
                            Util.getParseUri(allPlayList?.get(mCurrentPosition)?.music_uri)
                        )
                        mMediaPlayer?.prepareAsync()
                        isReady = true
                        mMediaPlayer?.setOnPreparedListener { mp ->
                            mp?.start()
                            sendAllEvent(mp)
                            sendBroadCast()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        showNotification()
                    }
                }
            }

            *//**
             * 이전 곡으로 이동버튼 클릭 시 Action
             *//*
            ACTION_PLAY_PREVIOUS -> {
                Log.e(TAG, "MUSIC_PREV")
                if (mMediaPlayer?.isPlaying == true) {
                    mMediaPlayer?.stop()
                    mMediaPlayer?.reset()
                    if (mCurrentPosition - 1 >= 0) {
                        mCurrentPosition--
                    }
                    try {
                        mMediaPlayer?.setDataSource(applicationContext, Util.getParseUri(allPlayList?.get(mCurrentPosition)?.music_uri))
                        mMediaPlayer?.prepareAsync()
                        isReady = true
                        mMediaPlayer?.setOnPreparedListener { mp ->
                            mp?.start()
                            sendAllEvent(mp)
                            sendBroadCast()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        showNotification()
                    }
                }else{
                    mMediaPlayer?.reset()

                    try {
                        mMediaPlayer?.setDataSource(
                            applicationContext,
                            Util.getParseUri(allPlayList?.get(mCurrentPosition)?.music_uri)
                        )
                        mMediaPlayer?.prepareAsync()
                        isReady = true
                        mMediaPlayer?.setOnPreparedListener { mp ->
                            mp?.start()
                            sendAllEvent(mp)
                            sendBroadCast()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        showNotification()
                    }

                }
            }*/
        }
        sendBroadCast(mAction)
        return START_STICKY
    }

    override fun onCompletion(mp: MediaPlayer?) {
        mp?.pause()
        mp?.reset()

        try {
            if (mCurrentMusicInfo != null) {
                setNextMusicInfo(mp)
            }
        }catch (e : Exception){
            e.printStackTrace()
        }finally {
            if (mCurrentMusicInfo != null) {
                showNotification()
                sendMusicEvent(mp)
            }
        }
    }

    fun showNotification(){
        setMetaData()
        showNotificationUpper(mMetadata)
    }

    fun showNotificationUpper(metadata : MediaMetadataCompat?){

        val channelId = "MusicChannel_1"
        val channelName = "MusicNotification"

        var notificationBuilder = NotificationCompat.Builder(this,channelId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var manager:NotificationManager = baseContext.getSystemService(NotificationManager::class.java)

            var serviceChannel : NotificationChannel =  NotificationChannel(channelId,channelName,NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(serviceChannel)
        }

        notificationBuilder.setOngoing(true).setOnlyAlertOnce(true)

        /**
         * Notification 앨범 이미지 세팅
         */
        var bitmap : Bitmap? = null

        notificationBuilder.setShowWhen(false)
        notificationBuilder.setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))
        notificationBuilder.color = Color.parseColor("#2196F3")
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(resources, R.mipmap.testalbum)
        }
        Log.d(TAG,"Notification 정보 : ${mCurrentMusicInfo?.music_title} // ${mCurrentMusicInfo?.music_artist}")
        notificationBuilder.setLargeIcon(bitmap)
        notificationBuilder.setSmallIcon(R.mipmap.icon)
        notificationBuilder.setContentTitle(mCurrentMusicInfo?.music_title)
        notificationBuilder.setContentText(mCurrentMusicInfo?.music_artist)

        /**
         * 플레이 상태체크해서 버튼 변경
         */
        Log.d(TAG,"showNotificationUpper 플레이 상태 확인: ${isPlaying} // ${isPause}")
        var icon : Int
        var state : String
        if (isPlaying == true){
            icon = android.R.drawable.ic_media_pause
            state = "pause"
        }else{
            icon = android.R.drawable.ic_media_play
            state = "play"
        }

        /**
         * 뒤로가기
         */
        var musicPrevIntent : Intent = Intent(applicationContext,MusicService::class.java)
        musicPrevIntent.putExtra("metadata",metadata)
        musicPrevIntent.action = ACTION_PLAY_PREVIOUS
        musicPrevIntent.putExtra("position",getPositionAtPreviousOrNext(ACTION_PLAY_PREVIOUS))

        var musicPrevPendingIntent : PendingIntent = PendingIntent.getService(applicationContext,0,musicPrevIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder.addAction(android.R.drawable.ic_media_previous, "prev", musicPrevPendingIntent)

        /**
         * 재생상태
         */
        var musicPlayStateIntent : Intent = Intent(applicationContext,MusicService::class.java)
        musicPlayStateIntent.putExtra("playdata",playdata)
        musicPlayStateIntent.putExtra("playlist",MusicInfoLoadUtil.myPlayListAll(applicationContext))
        musicPlayStateIntent.putExtra("isplay",isPlaying)
        musicPlayStateIntent.putExtra("ispause",isPause)
        click_notification_btn  =   true
        musicPlayStateIntent.putExtra("clicknotification",click_notification_btn!!)
        musicPlayStateIntent.action = ACTION_PLAYSTATE

        var musicPausePendingIntent : PendingIntent = PendingIntent.getService(applicationContext,1,musicPlayStateIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder.addAction(icon, state, musicPausePendingIntent)

        /**
         * 다음
         */
        var musicNextIntent : Intent = Intent(applicationContext,MusicService::class.java)
        musicNextIntent.putExtra("metadata",metadata)
        musicNextIntent.action = ACTION_PLAY_NEXT
        musicNextIntent.putExtra("position",getPositionAtPreviousOrNext(ACTION_PLAY_NEXT))

        var musicNextPendingIntent : PendingIntent = PendingIntent.getService(applicationContext,2,musicNextIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder.addAction(android.R.drawable.ic_media_next, "next", musicNextPendingIntent)

        /**
         * Notification 클릭
         */
        var activityStartInent : Intent = Intent(applicationContext,MusicPlayerActivity::class.java)
        activityStartInent.putExtra("noti_uid",getCurrentInfo()?.album_uid)
        activityStartInent.putExtra("noti_title",getCurrentInfo()?.music_title)
        activityStartInent.putExtra("noti_artist",getCurrentInfo()?.music_artist)
        activityStartInent.putExtra("noti_uri",getCurrentInfo()?.music_uri)
        activityStartInent.putExtra("playlist",allPlayList)
        activityStartInent.putExtra("playdata",playdata)
        var activityStartPendingIntent : PendingIntent = PendingIntent.getActivity(applicationContext,1,activityStartInent,PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder.setContentIntent(activityStartPendingIntent)

        notificationBuilder.setAutoCancel(false)
        var notification : Notification = notificationBuilder.build()
        startForeground(1,notification)

    }

    fun  getPositionAtPreviousOrNext(flag:String) : Int{
        var position = getCurrentPosition()
        when(flag){
            ACTION_PLAY_NEXT->{
                if (position < getCurrentPosition()){
                    position += 1
                }else{
                    position = 0
                }
            }

            ACTION_PLAY_PREVIOUS->{
                if (position > 0) {
                    position -= 1
                }else{
                    position = getCurrentPlaylistSize()
                }
            }
        }
        return position
    }

    fun getCurrentPosition() : Int{
        return mCurrentPosition
    }

    fun getCurrentPlaylistSize() : Int {
        return if (allPlayList != null) allPlayList?.size!! - 1 else 0
    }

    fun getCurrentInfo() : PlayMusicInfo? {
        Log.d(TAG,"[getCurrentInfo] Current Position -> $mCurrentPosition")
        Log.d(TAG,"getCurrentInfo -> $mCurrentPosition // ${allPlayList?.get(mCurrentPosition)?.music_title}")
         if (allPlayList != null) {
             return MusicInfoLoadUtil.getSelectedMusicInfo(applicationContext, allPlayList?.get(mCurrentPosition)?.music_title,allPlayList?.get(mCurrentPosition)?.music_artist,allPlayList?.get(mCurrentPosition)?.music_uri)
        } else {
             return null
        }
    }

    /*private fun sendEvents() {
        if (mMediaPlayer?.isPlaying == true) {
            Log.d(TAG, "UIRefresher is running")
            var playback = PlayBack()
            playback.isPlaying = mMediaPlayer?.isPlaying
            playback.currentTime = mMediaPlayer?.currentPosition!!
            mHandler.sendEmptyMessageDelayed(0, 1000)
        } else {
            Log.d(TAG, "UIRefresher is stopped")
            var playback = PlayBack()
            playback.isPlaying = mMediaPlayer?.isPlaying
            playback.currentTime = mMediaPlayer?.currentPosition!!
        }
    }*/

    inner class LocalBinder : Binder() {
        fun getService() : MusicService {
            return this@MusicService
        }
    }

    fun sendMusicEvent(mediaPlayer: MediaPlayer?) {
        musicevent = MusicEvent(mediaPlayer,getCurrentInfo())
        Log.d(TAG,"sendMusicEvent PlayState : \n ${mediaPlayer?.isPlaying} \n ${musicevent?.mMediaPlayer?.isPlaying} ")

    }

    @Throws(Exception::class)
    fun setNextMusicInfo(mp : MediaPlayer?){

        Log.d(TAG,"setNextMusicInfo -> $mCurrentPosition // ${getCurrentPosition()}")

        if (mCurrentPosition < getCurrentPosition()) {
            mCurrentPosition++
        }else{
            mCurrentPosition = 0
        }

        mp?.setDataSource(applicationContext,Util.getParseUri(allPlayList!![mCurrentPosition].music_uri))
        mCurrentMusicInfo =  MusicInfoLoadUtil.getSelectedMusicInfo(applicationContext, allPlayList?.get(mCurrentPosition)?.music_title,allPlayList?.get(mCurrentPosition)?.music_artist,allPlayList?.get(mCurrentPosition)?.music_uri)

        if (mCurrentPosition != 0) {
            mp?.prepare()
            mp?.start()
            isReady = true
        } else {
            mp?.stop()
            mp?.reset()
            isReady = false
        }

    }

    fun setMetaData(){
        if (mCurrentMusicInfo != null) {
            var music_uri : Uri = Util.getParseUri(mCurrentMusicInfo?.music_uri)
            //var bitmap = MusicInfoLoadUtil.getAlbumImage(applicationContext,music_uri,4)
            mMetadata = MediaMetadataCompat.Builder()
              //  .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, mCurrentMusicInfo?.music_artist)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mCurrentMusicInfo?.music_title)
                .build()
            if (mSession == null) {
                mSession = MediaSessionCompat(this, "tag", null, null)
                mSession?.setMetadata(mMetadata)
                mSession?.isActive = true
                mSession?.setCallback(object : MediaSessionCompat.Callback() {
                    override fun onPlay() {
                        super.onPlay()
                        Toast.makeText(this@MusicService, "onPlay", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mUnPlugReceiver)
        unregisterReceiver(SERVICE_BR)
    }

    fun getMusicUID() : MusicUIDData? {
        return MusicInfoLoadUtil.getMusicUID(applicationContext, playdata?.play_title)
    }

    var SERVICE_BR : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

        }
    }

    fun ServiceRegisterReceiver(){
        var filter : IntentFilter = IntentFilter()
        filter.addAction("com.dongdong.neonplayer.BroadcastReceiver")
        registerReceiver(SERVICE_BR,filter)
    }

    fun sendBroadCast(message : String?){
        var intent: Intent = Intent()
        intent.action = "com.dongdong.neonplayer.MusicPlayerBroadcastReceiver"
        intent.putExtra("action_state", message)
        intent.putExtra("notification_state", br_playstate)
        Log.d(TAG,"넘겨! $br_playstate")
        sendBroadcast(intent)
        br_playstate = null
    }

}