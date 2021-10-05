package com.dongdong.neonplayer.activity

import android.content.*
import android.database.Cursor
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import com.dongdong.neonplayer.R
import com.dongdong.neonplayer.common.Contacts
import com.dongdong.neonplayer.common.MusicInfoLoadUtil
import com.dongdong.neonplayer.common.Util
import com.dongdong.neonplayer.data.*
import com.dongdong.neonplayer.eventbus.EventBusProvider
import com.dongdong.neonplayer.room.AppDataBase
import com.dongdong.neonplayer.room.Entity.MyMusicPlayListEntity
import com.dongdong.neonplayer.service.MusicEvent
import com.dongdong.neonplayer.service.MusicService
import com.google.common.eventbus.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat

class MusicPlayerActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = MusicPlayerActivity::class.java.simpleName

    var playdata : MusicPlayData? = null
    var playList : ArrayList<MyPlayListData>? = null
    var tv_title : TextView? = null
    var tv_artist : TextView? = null
    var down_activity : LinearLayout? = null
    var img_album: ImageView? = null
    var img_playlist : ImageView? = null
    var img_previous : ImageView? = null
    var img_play : ImageView? = null
    var img_pause : ImageView? = null
    var img_next : ImageView? = null
    var seekBar : SeekBar? = null
    var externalState : Boolean? = null
    var res : ContentResolver? = null
    var position : Int = 0
    var mcontext : Context? = null
    lateinit var musicURI :Uri
    var audio : AudioManager? = null
    var mtv_total_playtime : TextView? = null
    var mtv_state_playtime :TextView? = null
    var liveTitle : MutableLiveData<String> = MutableLiveData()
    var liveArtist : MutableLiveData<String> = MutableLiveData()
    var liveTotalPlaytime : MutableLiveData<String> = MutableLiveData()
    var liveStatePlaytime : MutableLiveData<String> = MutableLiveData()
    var mMusicService : MusicService? = null
    var mServiceIntent : Intent? = null
    var mPlayBack : PlayBack? = null
    var musicservice : MusicService? = null
    var isPause : Boolean = false
    var newMusicCheck : Boolean? = false         // true -> 새로운 음악 , false -> 새로운 음악 아님

    companion object {
        //앨범이 저장되어 있는 경로를 리턴합니다.
      fun getCoverArtPath(albumId : Long, context : Context) : String? {

            var albumCursor : Cursor? = context.contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Albums.ALBUM_ART),
                MediaStore.Audio.Albums._ID + " = ?",
                arrayOf(albumId.toString()),
                null
            )
            var queryResult : Boolean = albumCursor!!.moveToFirst()
            var result : String? = null
            if (queryResult) {
                result = albumCursor.getString(0)
            }
            albumCursor.close()
            return result
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_musicplayer)

        mcontext = this
        var intent = intent
            if (intent != null) {
                if (intent.getSerializableExtra("playdata") != null) {
                    playdata = intent.getSerializableExtra("playdata") as MusicPlayData
                    liveTitle.value = Util.getAlbumInfo(playdata?.play_title)?.get(1)
                    liveArtist.value = Util.getAlbumInfo(playdata?.play_title)?.get(0)
                }
                if (intent.getBooleanExtra("newmusiccheck",false) != null){
                    newMusicCheck = intent.getBooleanExtra("newmusiccheck",false)
                }
                if (intent.getBooleanExtra("externalstate",false) != null) {
                    externalState = intent.getBooleanExtra("externalstate",false)
                }
            }
        Log.d(TAG,"받아오는 데이터 = ${playdata?.play_title}")

        if (musicservice == null) {
            var intent : Intent = Intent(this,MusicService::class.java)
            bindService(intent,conn,0)
        }

        res = contentResolver
        setBindId()
        setOnClickEvent()
        setLiveData()
        conMusicService()
        if (externalState == true){
            setUI()
            playTimeUI()
            ProgressUpdate()?.start()
        }else {
            getMusicFile(playdata!!)
        }
        MusicPlayerRegisterReceiver()
    }

    override fun onClick(v: View?) {
      when(v?.id) {
          R.id.btn_playlist -> {
              //플레이리스트 띄우기
          }

          R.id.down_activity -> {
              finish()
          }

          R.id.btn_play -> {
              img_pause?.visibility = View.VISIBLE
              img_play?.visibility = View.GONE

                  try {
                      Log.d(TAG, "ACTION_PLAY -> ${Util.getParseUri(playdata?.play_title)}")
                          var playIntent = Intent(mcontext, MusicService::class.java)
                          playIntent.putExtra("playdata", playdata)
                          playIntent.putExtra("position", getMusicUID())
                          playIntent.putExtra("playlist", MusicInfoLoadUtil.myPlayListAll(applicationContext))
                          playIntent.action = MusicService.ACTION_PLAY
                          startService(playIntent)
                  } catch (e: Exception) {
                      e.printStackTrace()
                  }
          }

          R.id.btn_pause -> {
              img_pause?.visibility = View.GONE
              img_play?.visibility = View.VISIBLE

              var pauseIntent = Intent(mcontext,MusicService::class.java)
                  pauseIntent.action = MusicService.ACTION_PAUSE
                  startService(pauseIntent)
          }

          R.id.btn_next -> {
              var nextIntent = Intent(mcontext,MusicService::class.java)
              nextIntent.action = MusicService.ACTION_PLAY_NEXT
              nextIntent.putExtra("playdata", playdata)
              startService(nextIntent)
          }

          R.id.btn_prev -> {
              var prevIntent = Intent(mcontext,MusicService::class.java)
              prevIntent.action = MusicService.ACTION_PLAY_PREVIOUS
              prevIntent.putExtra("playdata", playdata)
              startService(prevIntent)
          }

      }
    }

    fun setBindId(){
        down_activity = findViewById(R.id.down_activity)
        down_activity?.setOnClickListener(this)
        tv_title = findViewById(R.id.title)
        tv_title?.isSelected = true
        tv_artist = findViewById(R.id.artist)
        tv_artist?.isSelected = true
        img_album = findViewById(R.id.album)
        img_playlist = findViewById(R.id.btn_playlist)
        img_previous = findViewById(R.id.btn_prev)
        img_play = findViewById(R.id.btn_play)
        img_pause = findViewById(R.id.btn_pause)
        img_next = findViewById(R.id.btn_next)
        seekBar = findViewById(R.id.player_seekbar)
        mtv_total_playtime = findViewById(R.id.tv_total_playtime)
        mtv_state_playtime = findViewById(R.id.tv_state_playtime)
        audio = getSystemService(AUDIO_SERVICE) as AudioManager
    }

    fun setOnClickEvent(){
        img_previous?.setOnClickListener(this)
        img_play?.setOnClickListener(this)
        img_pause?.setOnClickListener(this)
        img_next?.setOnClickListener(this)
    }

    fun playMusic(album_title:String,album_artist : String) {
        try {
            tv_title?.text = album_title
            tv_artist?.text = album_artist

            Log.e(TAG, "Funtion playMusic() -> ${returnEventBus()?.getMediaPlay()?.isPlaying} // $isPause //$newMusicCheck")

            seekBar?.progress = 0

            Log.d(TAG, "playMusic 1 -> ${Util.getParseUri(playdata?.play_title)}")

            Log.e(TAG, "playMusic Playing State 1 -> ${returnEventBus()?.getMediaPlay()?.isPlaying}")
            var playIntent = Intent(mcontext, MusicService::class.java)
            playIntent.putExtra("playdata", playdata)
            playIntent.putExtra("position", getMusicUID())
            playIntent.putExtra("playlist", MusicInfoLoadUtil.myPlayListAll(applicationContext))
            playIntent.action = MusicService.ACTION_PLAY
            startService(playIntent)

            Log.d(TAG, "playMusic 1 -> ${returnEventBus().getMediaPlay()?.isPlaying} // ${mPlayBack?.isPlaying} // ${mPlayBack?.currentTime}")
        }catch (e : Exception) {
            e.printStackTrace()
        }
    }

    inner class ProgressUpdate : Thread(){
       override fun run() {
           Log.d(TAG,"ProgressUpdate -> ${returnEventBus().getMediaPlay()?.isPlaying}")
            while(returnEventBus().getMediaPlay()?.isPlaying == true){
                try {
                    sleep(1000)
                        seekBar?.progress = returnEventBus().getMediaPlay()?.currentPosition!!
                        //Log.d(TAG,"currentPosition -> ${returnEventBus().getMediaPlay()?.currentPosition!!}")
                } catch (e : Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    var conn : ServiceConnection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, Ibinder: IBinder?) {
            var binder : MusicService.ServiceBinder = Ibinder as MusicService.ServiceBinder
            musicservice = binder.getService()
            Log.d(TAG,"바인더: ${returnEventBus().getMediaPlay()?.isPlaying} // ${returnEventBus().getMediaPlay()?.currentPosition}")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun getMusicFile(musicDto : MusicPlayData) {

        Log.d(TAG, "getMusicFile -> ${musicDto.play_title}")

        musicURI = Util.getParseUri(musicDto.play_title)
        var music_uri = "${Contacts.baseurl}${musicDto.play_title}"

        var db = Room.databaseBuilder(applicationContext,AppDataBase::class.java,"playmusic_data").build()

        runBlocking {
            var check_music_data = db.MyMusicPlayListDao().getSelectPlayData(Contacts.Admin_UserID,Util.getAlbumInfo(musicDto.play_title)?.get(1)!!,Util.getAlbumInfo(musicDto.play_title)?.get(0)!!,music_uri)
            if (check_music_data == null) {
                db.MyMusicPlayListDao().insertSelectMusic(MyMusicPlayListEntity(Contacts.Admin_UserID, Util.getAlbumInfo(musicDto.play_title)?.get(1)!!, Util.getAlbumInfo(musicDto.play_title)?.get(0)!!, music_uri))
                Toast.makeText(mcontext, "재생 목록에 추가되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        playList = MusicInfoLoadUtil.myPlayListAll(applicationContext)
        Log.e(TAG,"AllPlayList -> ${playList?.size}")

        playMusic(Util.getAlbumInfo(musicDto.play_title)?.get(1)!!,Util.getAlbumInfo(musicDto.play_title)?.get(0)!!)

    }

     fun setUI() {

            try {

                Log.d(TAG,"SetUI -> ${returnEventBus().getMediaPlay()?.isPlaying}")

                Log.d(TAG,"SetUI SeekBar-> ${returnEventBus().getMediaPlay()?.duration!!}")
                seekBar?.max = returnEventBus().getMediaPlay()?.duration!!

                if (returnEventBus().getMediaPlay()?.isPlaying == false) {
                    this.img_play?.visibility = View.VISIBLE
                    this.img_pause?.visibility = View.GONE
                } else {
                    this.img_play?.visibility = View.GONE
                    this.img_pause?.visibility = View.VISIBLE
                }

                seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                    // onProgressChange - Seekbar 값 변경될때마다 호출
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromuser: Boolean) {

                        var timeFormat: SimpleDateFormat = SimpleDateFormat("mm:ss")

                        liveTotalPlaytime?.value = timeFormat.format(returnEventBus().getMediaPlay()?.duration)

                        var state_min : String = (progress/60000).toString()
                        var state_sec : String = ((progress%60000)/1000).toString()
                        if (state_min.length < 2) {
                            state_min = "0$state_min"
                        }
                        if (state_sec.length < 2){
                            state_sec = "0$state_sec"
                        }
                        liveStatePlaytime?.value = "$state_min:$state_sec"
                    }
                    // onStartTeackingTouch - SeekBar 값 변경위해 첫 눌림에 호출
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        //returnEventBus().getMediaPlay()?.pause()
                    }
                    // onStopTrackingTouch - SeekBar 값 변경 끝나고 드래그 떼면 호출
                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        returnEventBus().getMediaPlay()?.seekTo(seekBar.progress)
                        Log.d(TAG,"onStopTrackingTouch -> ${seekBar.progress}")
                    }
                })

                ProgressUpdate().start()

            }catch (e : Exception) {
                e.printStackTrace()
        }

    }

    fun setLiveData(){
        liveTitle.observe(this, Observer {
            tv_title?.text = it
        })
        liveArtist.observe(this, Observer {
            tv_artist?.text = it
        })
        liveTotalPlaytime.observe(this, Observer {
            mtv_total_playtime?.text = it
        })
        liveStatePlaytime.observe(this, Observer {
            mtv_state_playtime?.text = it
        })
    }

    fun conMusicService(){
        mServiceIntent = Intent(mcontext,MusicService::class.java)
    }

    fun getCurrentInfo(): PlayMusicInfo? {
        if (playdata != null) {
            return MusicInfoLoadUtil.getSelectedMusicInfo(applicationContext, playdata?.play_title)
        } else {
            return null
        }
    }

    fun getMusicUID() : Int? {
        var getuid = MusicInfoLoadUtil.getMusicUID(applicationContext, playdata?.play_title)?.album_uid
        return getuid!! - 1
    }

    var mBroadcastReceiver : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG,"Found OnReceiver")
            Log.d(TAG, "mBroadcastReceiver intent -> ${intent?.getStringExtra("action_state")} // ${intent?.getStringExtra("notification_state")} // ${intent?.getSerializableExtra("playdata")} // ${intent?.getSerializableExtra("playdata")}")
            if (intent?.getStringExtra("action_state").equals(MusicService.ACTION_PLAY)) {
                if (intent?.getSerializableExtra("playdata") != null) {
                    playdata = intent?.getSerializableExtra("playdata") as MusicPlayData?
                    tv_title?.text = MusicInfoLoadUtil.getSelectedMusicInfo(this@MusicPlayerActivity, playdata?.play_title)?.music_title
                    tv_artist?.text = MusicInfoLoadUtil.getSelectedMusicInfo(this@MusicPlayerActivity, playdata?.play_title)?.music_artist
                }
                setUI()
                img_play?.visibility = View.GONE
                img_pause?.visibility = View.VISIBLE
            }else if(intent?.getStringExtra("action_state").equals(MusicService.ACTION_PAUSE)) {
                setUI()
                img_play?.visibility = View.VISIBLE
                img_pause?.visibility = View.GONE
            }else if(intent?.getStringExtra("action_state").equals(MusicService.ACTION_PLAY_NEXT)){

            }else if(intent?.getStringExtra("action_state").equals(MusicService.ACTION_PLAY_PREVIOUS)){

                }
        }
    }

    fun MusicPlayerRegisterReceiver(){
        var filter : IntentFilter = IntentFilter()
        filter.addAction("com.dongdong.neonplayer.MusicPlayerBroadcastReceiver")
        registerReceiver(mBroadcastReceiver,filter)
    }

    fun sendService() {
        var playIntent = Intent(mcontext, MusicService::class.java)
             //playIntent.putExtra("ispause",isPause)
        if (returnEventBus().getMediaPlay()?.isPlaying == true) {
            playIntent.action = MusicService.ACTION_PLAY
        }else{
            playIntent.action = MusicService.ACTION_PAUSE
        }
             startService(playIntent)
    }

    fun returnEventBus() : EventBusProvider{
        return EventBusProvider.getinstance(this)
    }

    fun playTimeUI(){
        var timeFormat: SimpleDateFormat = SimpleDateFormat("mm:ss")

        liveTotalPlaytime?.value = timeFormat.format(returnEventBus().getMediaPlay()?.duration)

        var state_min : String = (returnEventBus().getMediaPlay()?.currentPosition!!/60000).toString()
        var state_sec : String = ((returnEventBus().getMediaPlay()?.currentPosition!!%60000)/1000).toString()
        if (state_min.length < 2) {
            state_min = "0$state_min"
        }
        if (state_sec.length < 2){
            state_sec = "0$state_sec"
        }
        liveStatePlaytime?.value = "$state_min:$state_sec"
        seekBar?.progress = returnEventBus().getMediaPlay()?.currentPosition!!
    }

}