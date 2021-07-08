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
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import com.dongdong.neonplayer.R
import com.dongdong.neonplayer.common.Contacts
import com.dongdong.neonplayer.common.MusicInfoLoadUtil
import com.dongdong.neonplayer.common.Util
import com.dongdong.neonplayer.data.*
import com.dongdong.neonplayer.room.AppDataBase
import com.dongdong.neonplayer.room.Entity.MyMusicPlayListEntity
import com.dongdong.neonplayer.service.MusicEvent
import com.dongdong.neonplayer.service.MusicService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat

class MusicPlayerActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = MusicPlayerActivity::class.java.simpleName

    var playdata : MusicPlayData? = null
    var playList : ArrayList<MyPlayListData>? = null
    var mMediaPlayer : MediaPlayer? = null
    var tv_title : TextView? = null
    var tv_artist : TextView? = null
    var img_album: ImageView? = null
    var img_playlist : ImageView? = null
    var img_previous : ImageView? = null
    var img_play : ImageView? = null
    var img_pause : ImageView? = null
    var img_next : ImageView? = null
    var seekBar : SeekBar? = null
    var isPlaying : Boolean? = true
    var res : ContentResolver? = null
    var position : Int = 0
    var mcontext : Context? = null
    lateinit var musicURI :Uri
    var audio : AudioManager? = null
    var mtv_total_playtime : TextView? = null
    var mtv_state_playtime :TextView? = null
    var liveTotalPlaytime : MutableLiveData<String> = MutableLiveData()
    var liveStatePlaytime : MutableLiveData<String> = MutableLiveData()
    var mMusicService : MusicService? = null
    var mServiceIntent : Intent? = null
    var mPlayBack : PlayBack? = null
    var musicservice : MusicService? = null
    var isPause : Boolean = false

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
                playdata = intent.getSerializableExtra("playdata") as MusicPlayData
            }
            Log.d(TAG,"받아오는 데이터 = ${playdata!!.play_title}")

        if (musicservice == null) {
            var intent : Intent = Intent(this,MusicService::class.java)
            bindService(intent,conn,0)
        }

        res = contentResolver
        setBindId()
        setOnClickEvent()
        conMusicService()
        mPlayBack = PlayBack()
        getMusicFile(playdata!!)
        setLiveData()
        MusicPlayerRegisterReceiver()
        if (mMediaPlayer?.isPlaying == true) {
            img_play?.visibility = View.GONE
            img_pause?.visibility = View.VISIBLE
        } else {
            img_play?.visibility = View.VISIBLE
            img_pause?.visibility = View.GONE
        }

        mMediaPlayer?.setOnCompletionListener {
            MediaPlayer.OnCompletionListener {
                if (position!!+1 < playList!!.size) {
                    position++
                    getMusicFile(playdata!!)
                }
            }
        }

    }

    override fun onClick(v: View?) {
      when(v?.id) {
          R.id.btn_playlist -> {
              //플레이리스트 띄우기
          }
          R.id.btn_prev -> {
              if(position-1>=0 ){
                  position--
                  var prevIntent = Intent(mcontext,MusicService::class.java)
                      prevIntent.action = MusicService.ACTION_PLAY_PREVIOUS
                      prevIntent.putExtra("playdata",playdata)
                      prevIntent.putExtra("position",getMusicUID())
                      prevIntent.putExtra("playlist",MusicInfoLoadUtil.myPlayListAll(applicationContext))
                      prevIntent.putExtra("isplay",mMediaPlayer?.isPlaying)
                  startService(prevIntent)
                  seekBar?.progress = 0
              }
          }

          R.id.btn_play -> {
              img_pause?.visibility = View.VISIBLE
              img_play?.visibility = View.GONE

              mMediaPlayer?.seekTo(mMediaPlayer!!.currentPosition)

              if (isPause == false) {
                  mMediaPlayer?.pause()
                  mMediaPlayer?.reset()

                  try {
                      Log.d(TAG, "ACTION_PLAY -> ${Util.getParseUri(playdata?.play_title)}")
                      mMediaPlayer?.setDataSource(applicationContext, Util.getParseUri(playdata?.play_title))
                      mMediaPlayer?.prepareAsync()
                      mMediaPlayer?.setOnPreparedListener { mp ->
                          mp?.start()
                          isPlaying = true
                          isPause = false
                          Log.e(TAG, "ACTION_PLAYSTATE Playing State -> ${mp?.isPlaying}")
                          var playIntent = Intent(mcontext, MusicService::class.java)
                          playIntent.putExtra("playdata", playdata)
                          playIntent.putExtra("position", getMusicUID())
                          playIntent.putExtra("playlist", MusicInfoLoadUtil.myPlayListAll(applicationContext))
                          playIntent.putExtra("isplay", mMediaPlayer?.isPlaying)
                          playIntent.putExtra("ispause",isPause)
                          playIntent.action = MusicService.ACTION_PLAYSTATE
                          ProgressUpdate().start()
                          startService(playIntent)
                      }
                  } catch (e: Exception) {
                      e.printStackTrace()
                  }
              }else{
                      mMediaPlayer?.start()
                      isPause = false
                      isPlaying = true
                      var playIntent = Intent(mcontext, MusicService::class.java)
                      playIntent.putExtra("playdata", playdata)
                      playIntent.putExtra("position", getMusicUID())
                      playIntent.putExtra("playlist", MusicInfoLoadUtil.myPlayListAll(applicationContext))
                      playIntent.putExtra("isplay", mMediaPlayer?.isPlaying)
                      playIntent.putExtra("ispause",isPause)
                      playIntent.action = MusicService.ACTION_PLAYSTATE
                      ProgressUpdate().start()
                      startService(playIntent)
              }
          }

          R.id.btn_pause -> {
              img_pause?.visibility = View.GONE
              img_play?.visibility = View.VISIBLE
              mMediaPlayer?.pause()
              isPause = true
              isPlaying = false

              var pauseIntent = Intent(mcontext,MusicService::class.java)
                  pauseIntent.putExtra("playdata",playdata)
                  pauseIntent.putExtra("position",getMusicUID())
                  pauseIntent.putExtra("playlist",MusicInfoLoadUtil.myPlayListAll(applicationContext))
                  pauseIntent.putExtra("isplay",mMediaPlayer?.isPlaying)
                  pauseIntent.putExtra("ispause",isPause)
                  pauseIntent.action = MusicService.ACTION_PLAYSTATE
              startService(pauseIntent)
          }

          R.id.btn_next -> {
              Log.d(TAG,"PlayActivity Action Next -> $position // ${playList?.size} // ${getMusicUID()}")
              if(position+1 < playList!!.size){
                  position++
                  var nextIntent = Intent(mcontext,MusicService::class.java)
                      nextIntent.action = MusicService.ACTION_PLAY_NEXT
                      nextIntent.putExtra("playdata",playdata)
                      nextIntent.putExtra("position",getMusicUID()!!+1)
                      nextIntent.putExtra("playlist",MusicInfoLoadUtil.myPlayListAll(applicationContext))
                      nextIntent.putExtra("isplay",mMediaPlayer?.isPlaying)
                  startService(nextIntent)
                  seekBar?.progress = 0
              }
          }
      }
    }

    fun setBindId(){

        mMediaPlayer = MediaPlayer()
        Log.d(TAG,"setBindId Player State -> ${mMediaPlayer?.isPlaying}")
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
            mMediaPlayer?.reset()
            seekBar?.progress = 0
            tv_title?.text = album_title
            tv_artist?.text = album_artist

            Log.d(TAG, "playMusic -> ${Util.getParseUri(playdata?.play_title)}")
            mMediaPlayer?.setDataSource(applicationContext, Util.getParseUri(playdata?.play_title))
            mMediaPlayer?.prepareAsync()
            mMediaPlayer?.setOnPreparedListener { mp ->
                mp?.start()
                isPlaying = true
                isPause = false
                Log.e(TAG, "playMusic Playing State -> ${mp?.isPlaying}")
                var playIntent = Intent(mcontext, MusicService::class.java)
                playIntent.putExtra("playdata", playdata)
                playIntent.putExtra("position", getMusicUID())
                playIntent.putExtra("playlist", MusicInfoLoadUtil.myPlayListAll(applicationContext))
                playIntent.putExtra("isplay", mp?.isPlaying)
                playIntent.putExtra("ispause",isPause)
                playIntent.action = MusicService.ACTION_PLAYSTATE
                startService(playIntent)

                Log.d(TAG, "playMusic -> ${mMediaPlayer?.isPlaying} // ${mPlayBack?.isPlaying} // ${mPlayBack?.currentTime}")
                ProgressUpdate().start()
                setUI()
            }
        }
        catch (e  : Exception) {
            e.printStackTrace()
        }
    }

    inner class ProgressUpdate : Thread(){
       override fun run() {
           Log.d(TAG,"ProgressUpdate -> ${mMediaPlayer?.isPlaying} // $isPlaying")
            while(isPlaying!!){
                try {
                    sleep(1000)
                    if(mMediaPlayer!=null){
                        seekBar?.progress = mMediaPlayer?.currentPosition!!
                    }
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
            Log.d(TAG,"바인더: ${mMediaPlayer?.isPlaying} // ${mMediaPlayer?.currentPosition}")
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
            if (check_music_data == null)
            db.MyMusicPlayListDao().insertSelectMusic(MyMusicPlayListEntity(Contacts.Admin_UserID,Util.getAlbumInfo(musicDto.play_title)?.get(1)!!,Util.getAlbumInfo(musicDto.play_title)?.get(0)!!,music_uri))
        }

        playList = MusicInfoLoadUtil.myPlayListAll(applicationContext)
        Log.e(TAG,"AllPlayList -> ${playList?.size}")

        playMusic(Util.getAlbumInfo(musicDto.play_title)?.get(1)!!,Util.getAlbumInfo(musicDto.play_title)?.get(0)!!)

    }

     fun setUI() {

            try {
                seekBar?.max = mMediaPlayer?.duration!!

                    if (mMediaPlayer?.isPlaying!!) {
                        img_play?.visibility = View.GONE
                        img_pause?.visibility = View.VISIBLE
                    } else {
                        img_play?.visibility = View.VISIBLE
                        img_pause?.visibility = View.GONE
                }

                seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromuser: Boolean) {

                        //mediaPlayer?.seekTo(progress)
                        //seekBar.progress = progress
                        var timeFormat: SimpleDateFormat = SimpleDateFormat("mm:ss")

                        liveTotalPlaytime?.value = timeFormat.format(mMediaPlayer?.duration)

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
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        mMediaPlayer?.pause()
                    }
                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        mMediaPlayer?.seekTo(seekBar.progress)
                        if(seekBar.progress > 0 && img_play?.visibility == View.GONE){
                            mMediaPlayer?.start()
                            isPlaying = true
                            isPause = false
                            ProgressUpdate().start()
                        }
                    }
                })

            }catch (e : Exception) {
                e.printStackTrace()
        }

    }

    fun setLiveData(){
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

    fun getMediaPlayer() : MediaPlayer? {
        Log.d(TAG,"getMediaPlayer PlayState-> ${mMediaPlayer?.isPlaying}")
        if (mMediaPlayer != null) {
            return mMediaPlayer
        }else{
            return null
        }
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
            Log.d(TAG, "mBroadcastReceiver intent -> ${intent?.getStringExtra("action_state")} // ${intent?.getStringExtra("notification_state")}")
            if (intent?.getStringExtra("action_state").equals(MusicService.ACTION_PLAYSTATE)){
                if (intent?.getStringExtra("notification_state").equals(MusicService.ACTION_PLAY)) {
                    mMediaPlayer?.start()
                    isPlaying = true
                    isPause = false
                    setUI()
                    ProgressUpdate().start()
                    sendService()
                }else if(intent?.getStringExtra("notification_state").equals(MusicService.ACTION_PLAY_PAUSE)){
                    mMediaPlayer?.pause()
                    isPlaying = false
                    isPause = true
                    setUI()
                    sendService()
                }

            } else if(intent?.getStringExtra("action_state").equals(MusicService.ACTION_PLAY_NEXT)){

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
             playIntent.putExtra("isplay", mMediaPlayer?.isPlaying)
             playIntent.putExtra("ispause",isPause)
             playIntent.action = MusicService.ACTION_PLAYSTATE
             startService(playIntent)
    }

}