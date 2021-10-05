package com.dongdong.neonplayer.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import com.dongdong.neonplayer.R
import com.dongdong.neonplayer.common.BackPressedForFinish
import com.dongdong.neonplayer.common.Contacts
import com.dongdong.neonplayer.common.MusicInfoLoadUtil
import com.dongdong.neonplayer.common.Util
import com.dongdong.neonplayer.data.MusicPlayData
import com.dongdong.neonplayer.eventbus.EventBusProvider
import com.dongdong.neonplayer.fragment.*
import com.dongdong.neonplayer.room.AppDataBase
import com.dongdong.neonplayer.room.Entity.FinishMusicInfoEntity
import com.dongdong.neonplayer.service.MusicService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.runBlocking


class MainActivity : AppCompatActivity(),View.OnClickListener{

    companion object {
        var main_playprogressbar: ProgressBar? = null
        var mContext: Context? = null
        private val TAG = MainActivity::class.java.simpleName

        fun returnEventBus(): EventBusProvider {
            return EventBusProvider.getinstance(mContext!!)
        }

    }

    var main_album_title: TextView? = null
    var main_album_singer: TextView? = null
    var main_btn_prev: LinearLayout? = null
    var main_btn_play: LinearLayout? = null
    var main_btn_pause: LinearLayout? = null
    var main_btn_next: LinearLayout? = null
    var playeractivity_field : LinearLayout? = null
    var main_playdata: MusicPlayData? = null
    var liveTitle : MutableLiveData<String> = MutableLiveData()
    var liveSinger : MutableLiveData<String> = MutableLiveData()
    var backPressedForFinish : BackPressedForFinish? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mContext = this
        setLiveData()
        bindID()
        MusicPlayerRegisterReceiver()
        nav_view.run {
            setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.navigation_home -> {
                        changeFragment(FragmentHome())
                    }
                    R.id.navigation_music_list -> {
                        changeFragment(FragmentMusicList())
                    }
                    R.id.navigation_mylist -> {
                        changeFragment(FragmentMyList())
                    }
                    R.id.navigation_search -> {
                        changeFragment(FragmentSearch())
                    }
                    R.id.navigation_settings -> {
                        changeFragment(FragmentSettings())
                    }
                }
                true
            }
            selectedItemId = R.id.navigation_home
        }
    }

    override fun onResume() {
        super.onResume()
        loadPlayData()
    }

    override fun onClick(v: View?) {
        Log.d(TAG, "Main onClickEvent -> ${Util.getParseUri(main_playdata?.play_title)} // ${main_playdata?.play_title}")
       when(v?.id){

           R.id.btn_main_play -> {
               try {
                   if (getFinishMusicState() != null) {
                       main_btn_pause?.visibility = View.VISIBLE
                       main_btn_play?.visibility = View.GONE
                       Log.d(TAG, "ACTION_PLAY -> ${Util.getParseUri(main_playdata?.play_title)}")
                       var playIntent = Intent(mContext, MusicService::class.java)
                       playIntent.putExtra("playdata", main_playdata)
                       playIntent.putExtra("position", getMusicUID())
                       playIntent.putExtra("playlist", MusicInfoLoadUtil.myPlayListAll(applicationContext))
                       playIntent.action = MusicService.ACTION_PLAY
                       startService(playIntent)
                   }else{
                       Toast.makeText(mContext,"현재 재생중인 음악 정보가 없습니다.",Toast.LENGTH_SHORT).show()
                   }
               } catch (e: Exception) {
                   e.printStackTrace()
               }
           }

           R.id.btn_main_pause -> {
               if (getFinishMusicState() != null) {
                   main_btn_pause?.visibility = View.GONE
                   main_btn_play?.visibility = View.VISIBLE

                   var pauseIntent = Intent(mContext, MusicService::class.java)
                   pauseIntent.action = MusicService.ACTION_PAUSE
                   startService(pauseIntent)
               }else{
                   Toast.makeText(mContext,"현재 재생중인 음악 정보가 없습니다.",Toast.LENGTH_SHORT).show()
               }
           }

           R.id.btn_main_next -> {
               if (getFinishMusicState() != null) {
                   var nextIntent = Intent(mContext, MusicService::class.java)
                   nextIntent.action = MusicService.ACTION_PLAY_NEXT
                   nextIntent.putExtra("playdata", main_playdata)
                   startService(nextIntent)
               }else{
                   Toast.makeText(mContext,"현재 재생중인 음악 정보가 없습니다.",Toast.LENGTH_SHORT).show()
               }
           }

           R.id.btn_main_prev -> {
               if (getFinishMusicState() != null) {
                   var prevIntent = Intent(mContext, MusicService::class.java)
                   prevIntent.action = MusicService.ACTION_PLAY_PREVIOUS
                   prevIntent.putExtra("playdata", main_playdata)
                   startService(prevIntent)
               }else{
                   Toast.makeText(mContext,"현재 재생중인 음악 정보가 없습니다.",Toast.LENGTH_SHORT).show()
               }
           }

           R.id.ly_playeractivity_field -> {
               var playeractivityIntent = Intent(mContext,MusicPlayerActivity::class.java)
               playeractivityIntent.putExtra("playdata",main_playdata)
               playeractivityIntent.putExtra("externalstate",true)
               startActivity(playeractivityIntent)
           }

       }
    }

    inner class ProgressUpdate : Thread() {
        override fun run() {
            Log.d(TAG, "Main ProgressUpdate -> ${returnEventBus().getMediaPlay()?.isPlaying}")
            while (returnEventBus().getMediaPlay()?.isPlaying == true) {
                try {
                    sleep(100)
                    if (returnEventBus().getMediaPlay()?.currentPosition != 0){
                            var progress_percent: Int = ((returnEventBus().getMediaPlay()?.currentPosition!!.toDouble() / returnEventBus().getMediaPlay()?.duration!!.toDouble()) * 100.0).toInt()
                            Log.d(TAG, "Main ProgressUpdate Percent -> $progress_percent // ${returnEventBus().getMediaPlay()?.currentPosition!!} // ${returnEventBus().getMediaPlay()?.duration!!} // ${(returnEventBus().getMediaPlay()?.currentPosition!! / returnEventBus().getMediaPlay()?.duration!!)}")
                            main_playprogressbar?.progress = progress_percent
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content_layout, fragment)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
       /* var finishIntent = Intent(this, MusicService::class.java)
        finishIntent.action = MusicService.ACTION_PLAY_FINISH
        startService(finishIntent)*/
    }

    fun bindID() {
        main_playprogressbar = findViewById(R.id.main_playprogressbar)
        main_album_title = findViewById(R.id.main_album_title)
        main_album_singer = findViewById(R.id.main_album_singer)
        main_btn_prev = findViewById(R.id.btn_main_prev)
        main_btn_prev?.setOnClickListener(this)
        main_btn_pause = findViewById(R.id.btn_main_pause)
        main_btn_pause?.setOnClickListener(this)
        main_btn_play = findViewById(R.id.btn_main_play)
        main_btn_play?.setOnClickListener(this)
        main_btn_next = findViewById(R.id.btn_main_next)
        main_btn_next?.setOnClickListener(this)
        playeractivity_field = findViewById(R.id.ly_playeractivity_field)
        playeractivity_field?.setOnClickListener(this)
        backPressedForFinish = BackPressedForFinish(this)
    }

    fun loadPlayData() {

        if (getFinishMusicState() != null) {
            liveTitle.value = getFinishMusicState()?.finish_title
            liveSinger.value = getFinishMusicState()?.finish_artist
        }else{
            liveTitle?.value = "재생중인 음악정보가 없습니다."
            liveSinger?.value = "재생중인 음악정보가 없습니다."
            main_playprogressbar?.progress = 0
        }

    }

    var mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Main Found OnReceiver")
            Log.d(TAG, "Main BroadcastReceiver intent -> ${intent?.getStringExtra("action_state")} // ${intent?.getStringExtra("notification_state")} // ${intent?.getSerializableExtra("playdata")} // ${intent?.getSerializableExtra("playdata")}")
            if (intent?.getStringExtra("action_state").equals(MusicService.ACTION_PLAY)) {
                if (intent?.getSerializableExtra("playdata") != null) {
                    main_playdata = intent?.getSerializableExtra("playdata") as MusicPlayData?
                    if (getFinishMusicState() != null) {
                        liveTitle.value = getFinishMusicState()?.finish_title
                        liveSinger.value = getFinishMusicState()?.finish_artist
                    }else{
                        liveTitle?.value = "재생중인 음악정보가 없습니다."
                        liveSinger?.value = "재생중인 음악정보가 없습니다."
                        main_playprogressbar?.progress = 0
                    }
                }
                main_btn_play?.visibility = View.GONE
                main_btn_pause?.visibility = View.VISIBLE
            } else if (intent?.getStringExtra("action_state").equals(MusicService.ACTION_PAUSE)) {
                main_btn_play?.visibility = View.VISIBLE
                main_btn_pause?.visibility = View.GONE
            } else if (intent?.getStringExtra("action_state").equals(MusicService.ACTION_PLAY_NEXT)) {
            } else if (intent?.getStringExtra("action_state").equals(MusicService.ACTION_PLAY_PREVIOUS)) {
            }
            ProgressUpdate().start()
        }
    }

    fun MusicPlayerRegisterReceiver() {
        var filter: IntentFilter = IntentFilter()
        filter.addAction("com.dongdong.neonplayer.MusicPlayerBroadcastReceiver")
        registerReceiver(mBroadcastReceiver, filter)
    }

    fun getMusicUID() : Int? {
        var getuid = MusicInfoLoadUtil.getMusicUID(applicationContext, main_playdata?.play_title)?.album_uid
        return getuid!! - 1
    }

    override fun onBackPressed() {
        backPressedForFinish?.onBackPressed()
    }

    fun getFinishMusicState() : FinishMusicInfoEntity? {

        var db = Room.databaseBuilder(applicationContext, AppDataBase::class.java,"finish_music_data").build()
        var check_music_data : FinishMusicInfoEntity? = null
        runBlocking {
            check_music_data = db.FinishMusicInfoDao().getSelectFinishPlayData(Contacts.Admin_UserID)
            Log.d(TAG,"뭐야ㅕ뭐여 : ${check_music_data?.finish_uri?.replace(Contacts.baseurl,"")}")
            main_playdata = MusicPlayData(check_music_data?.finish_uri?.replace(Contacts.baseurl,""))
        }
        return check_music_data
    }

    fun setLiveData(){
        liveTitle.observe(this, Observer {
            main_album_title?.text = it
        })
        liveSinger.observe(this, Observer {
            main_album_singer?.text = it
        })
    }

}
