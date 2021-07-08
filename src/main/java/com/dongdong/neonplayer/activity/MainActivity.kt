package com.dongdong.neonplayer.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dongdong.neonplayer.R
import com.dongdong.neonplayer.fragment.*
import com.dongdong.neonplayer.service.MusicService
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    var mContext : Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mContext = this


        nav_view.run {
            setOnNavigationItemSelectedListener{
                when(it.itemId) {
                    R.id.navigation_home -> {
                        changeFragment(FragmentHome())
                    }
                    R.id.navigation_music_list -> {
                        changeFragment(FragmentMusicList())
                    }
                    R.id.navigation_mylist -> {
                        changeFragment( FragmentMyList())
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

    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content_layout, fragment)
            .commit()
    }
    override fun onDestroy() {
        super.onDestroy()
    }

}
