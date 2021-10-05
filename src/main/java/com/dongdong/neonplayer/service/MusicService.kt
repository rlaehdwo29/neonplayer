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
import androidx.room.Room
import com.dongdong.neonplayer.R
import com.dongdong.neonplayer.activity.MainActivity
import com.dongdong.neonplayer.activity.MusicPlayerActivity
import com.dongdong.neonplayer.common.Contacts
import com.dongdong.neonplayer.common.MusicInfoLoadUtil
import com.dongdong.neonplayer.common.Util
import com.dongdong.neonplayer.data.*
import com.dongdong.neonplayer.eventbus.EventBusProvider
import com.dongdong.neonplayer.room.AppDataBase
import com.dongdong.neonplayer.room.Entity.FinishMusicInfoEntity
import com.dongdong.neonplayer.room.Entity.MyMusicPlayListEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*


class MusicService : Service(), MediaPlayer.OnCompletionListener {

    companion object {
        private val TAG = MusicService::class.java.simpleName

        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PLAY_NEXT = "ACTION_PLAY_NEXT"
        const val ACTION_PLAY_PREVIOUS = "ACTION_PLAY_PREVIOUS"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_PLAYSTATE = "ACTION_PLAYSTATE"
        const val ACTION_PAUSE_UNPLUGGED = "ACTION_PAUSE_UNPLUGGED"
        const val ACTION_PLAY_SELECTED = "ACTION_PLAY_SELECTED"
        const val ACTION_PLAY_FINISH = "ACTION_FINISH"
        const val MUSIC_SERVICE_FILTER = "MUSIC_SERVICE_FILTER"
        const val ACTION_NOTIFICATION = "ACTION_NOTIFICATION"
        const val COUNTDOWNTIMER : Long = 60*1000
        const val NOTIFICAIONCHANNELID = 1
        const val CHANNELID = "MusicChannel_1"
        const val CHANNELNAME = "MusicNotification"
        const val TIMERUPDATE = 100
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
    var musicevent : MusicEvent? = null
    var br_playstate : String? = null
    var click_notification_btn : Boolean? = false
    var newMusicCheck : Boolean? = false         // true -> 새로운 음악 , false -> 새로운 음악 아님
    var manager : NotificationManager? = null
    var musicPlayerActivity : MusicPlayerActivity? = null
    var mainActivity : MainActivity? = null
    var CDT: CountDownTimer? = null

    override fun onCreate() {
        super.onCreate()

        //mMediaPlayer = MediaPlayer()
        musicPlayerActivity = MusicPlayerActivity()
        mainActivity = MainActivity()
        returnEventBus().setMediaPlay(MediaPlayer(),this)
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

            ACTION_PLAY -> {
                if (intent?.getSerializableExtra("playdata") as MusicPlayData? != null) {
                    var getplaydata = intent?.getSerializableExtra("playdata") as MusicPlayData?
                    Log.d(TAG, "[ACTION_PLAY] 재생 음악 비교 Frist -> ${playdata?.play_title} || ${getplaydata?.play_title} || $br_playstate")

                    if (playdata?.play_title.equals(getplaydata?.play_title) == true){
                        newMusicCheck = false
                    }else{
                        newMusicCheck = true
                        playdata = getplaydata
                    }
                }

                Log.d(TAG, "[ACTION_PLAY] 기타 LOG -> $br_playstate")

                if (intent?.getIntExtra("position", 0)!! != null) {
                    mCurrentPosition = intent?.getIntExtra("position", 0)!!
                    Log.d(TAG, "[ACTION_PLAY] mCurrentPosition -> $playdata // ${mCurrentPosition} // ${playdata?.play_title}")
                }
                if (intent?.getSerializableExtra("playlist") as ArrayList<MyPlayListData>? != null) {
                    allPlayList = intent?.getSerializableExtra("playlist") as ArrayList<MyPlayListData>?
                    Log.d(TAG, "[ACTION_PLAY] AllPlayListSize -> ${allPlayList?.size}")
                }
                if (MusicInfoLoadUtil.getSelectedMusicInfo(applicationContext, playdata?.play_title) != null && playdata?.play_title != null) {
                    mCurrentMusicInfo = MusicInfoLoadUtil.getSelectedMusicInfo(applicationContext, playdata?.play_title)
                    Log.d(TAG,"[ACTION_PLAY] mCurrentMusicInfo -> ${mCurrentMusicInfo?.music_title} || ${mCurrentMusicInfo?.music_artist}")
                }
                if (intent?.getBooleanExtra("isplay",false) != null) {
                    Log.d(TAG, "[ACTION_PLAY] isPlaying -> ${returnEventBus().getMediaPlay()?.isPlaying}")
                }
                if (intent?.getBooleanExtra("clicknotification",false) != null) {
                    if (intent?.getBooleanExtra("clicknotification",false) == true){
                        br_playstate = ACTION_PLAY
                    }
                }
            }

            ACTION_PAUSE -> {
                Log.d(TAG, "[ACTION_PAUSE] isPause -> ")
                if (intent?.getBooleanExtra("clicknotification",false) != null) {
                    if (intent?.getBooleanExtra("clicknotification",false) == true){
                        br_playstate = ACTION_PAUSE
                    }
                }
            }

            /**
             * 다음 곡으로 이동할때 데이터 세팅
             */
            ACTION_PLAY_NEXT -> {
                if (intent?.getSerializableExtra("playdata") as MusicPlayData? != null) {
                    var getplaydata = intent?.getSerializableExtra("playdata") as MusicPlayData?
                    Log.d(TAG, "[ACTION_PLAY_NEXT] 재생 음악 비교 Frist -> ${playdata?.play_title} || ${getplaydata?.play_title} || $br_playstate")
                    playdata = getplaydata
                }
            }

            /**
             * 이전 곡으로 이동할때 데이터 세팅
             */
            ACTION_PLAY_PREVIOUS -> {
                if (intent?.getSerializableExtra("playdata") as MusicPlayData? != null) {
                    var getplaydata = intent?.getSerializableExtra("playdata") as MusicPlayData?
                    Log.d(TAG, "[ACTION_PLAY_PREVIOUS] 재생 음악 비교 Frist -> ${playdata?.play_title} || ${getplaydata?.play_title} || $br_playstate")
                        playdata = getplaydata
                }
            }

            /**
             * 플레이어 종료시 세팅
             */
            ACTION_PLAY_FINISH -> {

                if (returnEventBus().getMediaPlay()?.isPlaying == true) {
                    returnEventBus().getMediaPlay()?.apply {
                        stop()
                        release()
                    }
                    returnEventBus().resetMediaPlay()
                } else {
                    returnEventBus().getMediaPlay()?.apply { release() }
                    returnEventBus().resetMediaPlay()
                }
                stopForeground(true)
                stopService(Intent(applicationContext, MusicService::class.java))
            }

            /**
             * 블루투스 장치 제거 시 세팅
             **/
            ACTION_PAUSE_UNPLUGGED -> {
                if (returnEventBus().getMediaPlay()?.isPlaying == true) {
                    returnEventBus().getMediaPlay()?.apply { pause() }
                    startTimer()
                    if (mCurrentMusicInfo != null && playdata != null) {
                        showNotification()
                    }
                }
            }
        }

        /**
         * 이벤트 Action
         **/
        when(mAction){

            /**
             * 재생 ACTION
             **/
            ACTION_PLAY -> {
                Log.e(TAG, "ACTION_PLAY -> ${returnEventBus().getMediaPlay()?.isPlaying} //$newMusicCheck")

                returnEventBus().getMediaPlay()?.seekTo(returnEventBus().getMediaPlay()!!.currentPosition)

                if (returnEventBus().getMediaPlay()?.isPlaying == true) {
                    if (newMusicCheck == false) {
                        returnEventBus().getMediaPlay()?.apply {
                            pause()
                        }
                        startTimer()
                        showNotification()
                        sendBroadCast(mAction)
                    } else {
                            returnEventBus().getMediaPlay()?.apply { reset() }

                            try {
                                Log.d(TAG, "ACTION_PLAY -> ${Util.getParseUri(playdata?.play_title)}")
                                returnEventBus()?.getMediaPlay()?.apply {
                                    setDataSource(applicationContext, Util.getParseUri(playdata?.play_title))
                                    prepareAsync()
                                    setOnPreparedListener{ mp ->
                                        mp?.start()
                                        setFinishMusicInfo()
                                        Log.e(TAG, "ACTION_PLAY Playing State -> ${mp?.isPlaying}")
                                        click_notification_btn = false
                                        sendBroadCast(mAction)
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                }else{
                        if (newMusicCheck == false) {
                            returnEventBus().getMediaPlay()?.apply {
                                start()
                            }
                            setFinishMusicInfo()
                            sendBroadCast(mAction)
                            //isPause = false
                        }else{

                            try {
                                Log.d(TAG, "ACTION_PLAY -> ${Util.getParseUri(playdata?.play_title)}")
                                returnEventBus().getMediaPlay()?.apply {
                                    setDataSource(applicationContext, Util.getParseUri(playdata?.play_title))
                                    prepareAsync()
                                    setOnPreparedListener{  mp ->
                                        mp?.start()
                                        setFinishMusicInfo()
                                        click_notification_btn = false
                                        sendBroadCast(mAction)
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                }
            }

            /**
             * 일시정지 ACTION
             */
            ACTION_PAUSE -> {
                returnEventBus().getMediaPlay()?.apply { pause() }
                //isPause = true
                startTimer()
                showNotification()
                sendBroadCast(mAction)
            }

            /**
             * 다음 곡으로 이동버튼 클릭 시 Action
             **/
            ACTION_PLAY_NEXT -> {
                Log.e(TAG, "MUSIC_NEXT -> [mCurrentPosition] ${mCurrentPosition}  [list size] ${allPlayList?.size}")
                setAtPreviousOrNextMusicInfo(returnEventBus().getMediaPlay(), ACTION_PLAY_NEXT)
                showNotification()
                sendBroadCast(mAction)
            }

            /**
             * 이전 곡으로 이동버튼 클릭 시 Action
             **/
            ACTION_PLAY_PREVIOUS -> {
                Log.e(TAG, "MUSIC_PREV")
                setAtPreviousOrNextMusicInfo(returnEventBus().getMediaPlay(), ACTION_PLAY_PREVIOUS)
                showNotification()
                sendBroadCast(mAction)
            }
        }

        return START_STICKY
    }

    override fun onCompletion(mp: MediaPlayer?) {
        Log.d(TAG,"onCompletion! -> ${mCurrentMusicInfo?.music_title}")
        try {
            //setAtNextMusicInfo(mp)
        }catch (e : Exception){
            e.printStackTrace()
        }finally {
            showNotification()
        }
    }

    fun showNotification(){

        var notificationBuilder = NotificationCompat.Builder(this, CHANNELID)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager = baseContext.getSystemService(NotificationManager::class.java)

            var serviceChannel : NotificationChannel =  NotificationChannel(CHANNELID,CHANNELNAME,NotificationManager.IMPORTANCE_LOW)
            manager?.createNotificationChannel(serviceChannel)
        }

        /**
         * Notification 앨범 이미지 세팅
         */
        var bitmap : Bitmap? = null

        notificationBuilder.setShowWhen(false)
        notificationBuilder.setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))
        notificationBuilder.color = Color.parseColor("#000000")
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(resources, R.mipmap.testalbum)
        }
        Log.d(TAG,"Notification 정보 : ${mCurrentMusicInfo?.music_title} // ${mCurrentMusicInfo?.music_artist}")
        notificationBuilder.setOngoing(true).setOnlyAlertOnce(true)
        notificationBuilder.setLargeIcon(bitmap)
        notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        notificationBuilder.setSmallIcon(R.mipmap.icon)
        notificationBuilder.setContentTitle(mCurrentMusicInfo?.music_title)
        notificationBuilder.setContentText(mCurrentMusicInfo?.music_artist)

        /**
         * 뒤로가기
         */
        var musicPrevIntent : Intent = Intent(applicationContext,MusicService::class.java)
        musicPrevIntent.action = ACTION_PLAY_PREVIOUS

        var musicPrevPendingIntent : PendingIntent = PendingIntent.getService(applicationContext,0,musicPrevIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder.addAction(android.R.drawable.ic_media_previous, "prev", musicPrevPendingIntent)

        /**
         * 재생상태
         */
        var musicPlayStateIntent : Intent = Intent(applicationContext,MusicService::class.java)
        musicPlayStateIntent.putExtra("playdata",playdata)
        musicPlayStateIntent.putExtra("playlist",MusicInfoLoadUtil.myPlayListAll(applicationContext))
        musicPlayStateIntent.putExtra("isplay",returnEventBus().getMediaPlay()?.isPlaying)
        //musicPlayStateIntent.putExtra("ispause",isPause)
        click_notification_btn = true
        musicPlayStateIntent.putExtra("clicknotification",click_notification_btn!!)
        //musicPlayStateIntent.action = ACTION_PLAYSTATE

        /**
         * 플레이 상태체크해서 버튼 변경
         */
        Log.d(TAG,"showNotificationUpper 플레이 상태 확인: ${returnEventBus().getMediaPlay()?.isPlaying}")
        var icon : Int
        var state : String
        if (returnEventBus().getMediaPlay()?.isPlaying == true){
            icon = android.R.drawable.ic_media_pause
            state = "pause"
            musicPlayStateIntent.action = ACTION_PAUSE
        }else{
            icon = android.R.drawable.ic_media_play
            state = "play"
            musicPlayStateIntent.action = ACTION_PLAY
        }

        var musicPausePendingIntent : PendingIntent = PendingIntent.getService(applicationContext,1,musicPlayStateIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder.addAction(icon, state, musicPausePendingIntent)

        /**
         * 다음
         */
        var musicNextIntent : Intent = Intent(applicationContext,MusicService::class.java)
        musicNextIntent.action = ACTION_PLAY_NEXT

        var musicNextPendingIntent : PendingIntent = PendingIntent.getService(applicationContext,2,musicNextIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder.addAction(android.R.drawable.ic_media_next, "next", musicNextPendingIntent)

        /**
         * Notification 클릭
         */
        var activityStartInent : Intent = Intent(applicationContext,MusicPlayerActivity::class.java)
        activityStartInent.putExtra("playlist",allPlayList)
        activityStartInent.putExtra("playdata",playdata)
        activityStartInent.putExtra("externalstate",true)
        var activityStartPendingIntent : PendingIntent = PendingIntent.getActivity(applicationContext,1,activityStartInent,PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder.setContentIntent(activityStartPendingIntent)

        notificationBuilder.setAutoCancel(false)
        var notification : Notification = notificationBuilder.build()
        startForeground(NOTIFICAIONCHANNELID,notification)

    }

    fun  getPositionAtPreviousOrNext(flag:String) : Int?{
        var positionUid : Int? = null

        Log.d(TAG,"Flag * -> $flag")
        when(flag){
            ACTION_PLAY_NEXT -> {
                Log.d(TAG,"Flag * next playdata.play_title -> ${playdata?.play_title}")
                Log.d(TAG,"Flag * nextUid -> ${MusicInfoLoadUtil.getMusicUID(applicationContext,playdata?.play_title)?.album_uid}")
                positionUid = MusicInfoLoadUtil.getMusicUID(applicationContext,playdata?.play_title)?.album_uid!!
                if (positionUid > 1) {
                    positionUid--
                }
                Log.d(TAG,"Flag * nextUid2 -> ${positionUid}")
            }

            ACTION_PLAY_PREVIOUS -> {
                Log.d(TAG,"Flag * prev playdata.play_title -> ${playdata?.play_title}")
                Log.d(TAG,"Flag * prevUid -> ${MusicInfoLoadUtil.getMusicUID(applicationContext,playdata?.play_title)?.album_uid}")
                positionUid = MusicInfoLoadUtil.getMusicUID(applicationContext,playdata?.play_title)?.album_uid!!
                if (positionUid < allPlayList?.size!!) {
                    positionUid++
                }
                Log.d(TAG,"Flag * prevUid2 -> ${positionUid}")
            }
        }
        Log.d(TAG,"resetUid1 -> ${positionUid} // ${allPlayList?.size}")
        if (positionUid!! == allPlayList?.size!!){
            positionUid = 1
            return positionUid
        } else if(positionUid == 1) {
            positionUid = allPlayList?.size
            return positionUid
        }
        Log.d(TAG,"resetUid2 -> ${positionUid}")
        return positionUid
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

    @Throws(Exception::class)
    fun setAtPreviousOrNextMusicInfo(mp : MediaPlayer?,mFlag : String?){

        Log.d(TAG,"allPLayList Size -> ${allPlayList?.size}")

        var xUid = getPositionAtPreviousOrNext(mFlag!!)
        Log.d(TAG,"xUid -> ${xUid}")

        for(i in 0 until allPlayList?.size!!) {

            Log.d(TAG,"setNextMusicInfo allPlayList -> $i // ${xUid} // ${allPlayList?.get(i)?.album_uid} // ${allPlayList?.get(i)?.music_title} // ${allPlayList?.get(i)?.music_uri} // ${Util.getNextParseUri(allPlayList!![i].music_uri)}")

            if (allPlayList?.get(i)?.album_uid?.equals(xUid) == true) {
                Log.d(TAG,"setAlbumUid -> ${MusicInfoLoadUtil.getMusicUID(applicationContext,playdata?.play_title)?.album_uid} // ${allPlayList!![i].music_uri} ")
                mp?.reset()
                mp?.setDataSource(applicationContext,Util.getNextParseUri(allPlayList!![i].music_uri))
                playdata = MusicPlayData(allPlayList?.get(i)?.music_uri?.replace(Contacts.baseurl,""))
                mCurrentMusicInfo =  MusicInfoLoadUtil.getSelectedMusicInfo(applicationContext, allPlayList?.get(i)?.music_title,allPlayList?.get(i)?.music_artist,allPlayList?.get(i)?.music_uri)
                newMusicCheck = true
            }
        }
        mp?.prepareAsync()
        mp?.setOnPreparedListener { mp ->
            mp?.start()
            setFinishMusicInfo()
            sendBroadCast(ACTION_PLAY)
        }
    }

    /**
     * 한곡의 음악 재생이 완료 되었을 때 메소드 실행.
     *
     * 이전,다음 강의 눌렀을때 리스트의 끝까지 가면 onComplete 메소드가 실행되는데
     * 이 메소드는 Next만 구현되어있고 Prev는 구현되어 있지 않음. 구현 예정
     */
    @Throws(Exception::class)
    fun setAtNextMusicInfo(mp : MediaPlayer?){

        Log.d(TAG,"setAtNextMusicInfo allPLayList Size -> ${allPlayList?.size}")
        Log.d(TAG,"setAtNextMusicInfo playdata title -> ${playdata?.play_title}")
        var xUid = MusicInfoLoadUtil.getMusicUID(applicationContext,playdata?.play_title)?.album_uid!!
        Log.d(TAG,"setAtNextMusicInfo xUid1 -> ${xUid}")
        if (xUid > 1) {
            xUid--
            Log.d(TAG,"setAtNextMusicInfo xUid1_1 -> ${xUid}")
        }else if (xUid == 1) {
            xUid == allPlayList?.size
            Log.d(TAG,"setAtNextMusicInfo xUid1_2 -> ${xUid}")
        }

        Log.d(TAG,"setAtNextMusicInfo xUid2 -> ${xUid}")

        for(i in 0 until allPlayList?.size!!) {

            Log.d(TAG,"setAtNextMusicInfo allPlayList -> $i // ${xUid} // ${allPlayList?.get(i)?.album_uid} // ${allPlayList?.get(i)?.music_title} // ${allPlayList?.get(i)?.music_uri} // ${Util.getNextParseUri(allPlayList!![i].music_uri)}")

            if (allPlayList?.get(i)?.album_uid?.equals(xUid) == true) {
                Log.d(TAG,"setAtNextMusicInfo setAlbumUid -> ${MusicInfoLoadUtil.getMusicUID(applicationContext,playdata?.play_title)?.album_uid} // ${allPlayList!![i].music_uri} ")
                mp?.reset()
                mp?.setDataSource(applicationContext,Util.getNextParseUri(allPlayList!![i].music_uri))
                playdata = MusicPlayData(allPlayList?.get(i)?.music_uri?.replace(Contacts.baseurl,""))
                mCurrentMusicInfo =  MusicInfoLoadUtil.getSelectedMusicInfo(applicationContext, allPlayList?.get(i)?.music_title,allPlayList?.get(i)?.music_artist,allPlayList?.get(i)?.music_uri)
                newMusicCheck = true
            }
        }
        mp?.prepareAsync()
        mp?.setOnPreparedListener { mp ->
            mp?.start()
            setFinishMusicInfo()
            sendBroadCast(ACTION_PLAY)
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
        intent.putExtra("playdata",playdata)
        Log.d(TAG,"넘겨! $br_playstate")
        sendBroadcast(intent)
        br_playstate = null
    }

    fun main_sendBroadCast(message : String?){
        var intent: Intent = Intent()
        intent.action = "com.dongdong.neonplayer.MusicPlayerBroadcastReceiver"
        intent.putExtra("action_state", message)
        intent.putExtra("playdata",playdata)
        Log.d(TAG,"넘겨! $br_playstate")
        sendBroadcast(intent)
        br_playstate = null
    }

    fun startTimer(){
        Log.d(TAG,"Funtion finishnorification2222 : ${returnEventBus().getMediaPlay()?.isPlaying}")

         CDT = object : CountDownTimer(COUNTDOWNTIMER, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (returnEventBus().getMediaPlay()?.isPlaying == true){
                    this.cancel()
                }
                Log.d(TAG,"OnTick -> $millisUntilFinished")
            }

            override fun onFinish() {
                //마지막에 실행할 구문
                Log.d(TAG,"onFinish!!")
                manager?.cancel(NOTIFICAIONCHANNELID)
                stopForeground(true)
                this.cancel()
            }
        }.start()

    }

    fun returnEventBus() : EventBusProvider {
        return EventBusProvider.getinstance(this)
    }

    fun setFinishMusicInfo() {
        var db = Room.databaseBuilder(applicationContext, AppDataBase::class.java,"finish_music_data").build()

        Log.d(TAG,"데이터 저장 및 업데이트 -> ${mCurrentMusicInfo?.music_title} // ${mCurrentMusicInfo?.music_artist} // ${mCurrentMusicInfo?.music_uri}")

        GlobalScope.launch(Dispatchers.IO) {
            try {
                var check_music_data =
                    db.FinishMusicInfoDao().getSelectFinishPlayData(Contacts.Admin_UserID)
                if (check_music_data == null) {
                    Log.d(TAG, "하아...1")
                    db.FinishMusicInfoDao().insertFinshMusicInfo(FinishMusicInfoEntity(Contacts.Admin_UserID, mCurrentMusicInfo?.music_title, mCurrentMusicInfo?.music_artist, mCurrentMusicInfo?.music_uri))
                } else {
                    Log.d(TAG, "하아...2")
                    db.FinishMusicInfoDao().updateFinishMusicData(Contacts.Admin_UserID, mCurrentMusicInfo?.music_title!!, mCurrentMusicInfo?.music_artist!!, mCurrentMusicInfo?.music_uri!!)
                }
                Log.d(TAG, "데이터 저장 및 업데이트 -> ${check_music_data} ${check_music_data?.finish_userid} // ${check_music_data?.finish_title} // ${check_music_data?.finish_artist} // ${check_music_data?.finish_uri}")
            }catch (e : Exception){
                e.printStackTrace()
            }
        }
        showNotification()
        musicPlayerActivity?.setUI()
        mainActivity?.liveTitle?.value = mCurrentMusicInfo?.music_title
        mainActivity?.liveSinger?.value = mCurrentMusicInfo?.music_artist
        mainActivity?.ProgressUpdate()?.start()
    }

    fun foundDB(){
        var db = Room.databaseBuilder(applicationContext, AppDataBase::class.java,"finish_music_data").build()
        GlobalScope.launch {
            var check_music_data = db.FinishMusicInfoDao().getSelectFinishPlayData(Contacts.Admin_UserID)
            Log.d(TAG,"데이터 저장 및 업데이트123 -> ${check_music_data} ${check_music_data?.finish_title} // ${check_music_data?.finish_title} // ${check_music_data?.finish_artist} // ${check_music_data?.finish_uri}")
        }
    }

}