package com.dongdong.neonplayer.fragment

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.dongdong.neonplayer.R
import com.dongdong.neonplayer.`interface`.API
import com.dongdong.neonplayer.common.Contacts
import com.dongdong.neonplayer.common.Util
import com.dongdong.neonplayer.data.VideoInfo
import com.dongdong.neonplayer.retrofit.RetroFitBuilder
import com.dongdong.neonplayer.viewpager.MusicList_ViewPagerAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.net.URLConnection
import kotlin.random.Random

class FragmentMusicList : Fragment() {

    var mvideoview : VideoView? = null
    var mContext : Context? = null
    var viewpager : ViewPager2? = null
    var fragmentstateadapter : MusicList_ViewPagerAdapter? = null
    var mtv_pagenum : TextView? = null
    var num_page : Int = 3

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView : View = inflater.inflate(R.layout.fragment_musiclist,container,false)
        setBindId(rootView)
        mContext = rootView.context
        setViewPager()
        return rootView
    }

    /**
     * BackGrdound Mp4 Play
     */
    fun play_background_video() {

        var getCall = RetroFitBuilder.api
        getCall.getVideoFilePath().enqueue(object : Callback<List<VideoInfo>>{
            override fun onResponse(call: Call<List<VideoInfo>>, response: Response<List<VideoInfo>>) {
                if (response.isSuccessful) {
                    Log.d("qwer123456", "비디오: ${response.body() as List<VideoInfo>} \n\n $response ")
                    var data : List<VideoInfo> = response.body()!!
                    Log.d("qwer1234567","비디오 데이터 갯수: ${data.size}")

                    //비디오 랜덤 함
                    var random: Random = Random
                    var random_position = 0

                    for (i in 0 until data.size) {
                        random_position = random.nextInt(data.size)
                    }
                    Log.d("qwer123456", "Video Info:  ${data[random_position].videopath} // ${data.size} // $random_position")

                    //var uri  = "${Contacts.baseurl}${data[random_position].videopath}"
                    var video_uri = Util.getParseUri(data[random_position].videopath)
                    Log.d("qwer123456", "Play Video Uri:$video_uri")

                    mvideoview!!.setVideoURI(video_uri)
                    mvideoview!!.start()

                    mvideoview?.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
                        override fun onPrepared(mediaplayer: MediaPlayer?) {
                            mediaplayer?.isLooping = true
                            mediaplayer?.setVolume(0f, 0f)
                        }
                    })
                }else{
                    //실패처리
                    Toast.makeText(mContext,"영상을 가져오는중 오류가 발생하였습니다.",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<VideoInfo>>, t: Throwable) {
                Log.e("qwer123456","error: ${t.message}")
                t.printStackTrace()
            }
        })

    }

    /**
     * FindId Settings
     */
    fun setBindId(mrootview : View) {
        viewpager = mrootview.findViewById(R.id.vp_musiclist)
        mvideoview = mrootview.findViewById(R.id.video_view)
        mtv_pagenum = mrootview.findViewById(R.id.tv_pagenum)
    }

    /**
     * ViewPager Settings
     */
    fun setViewPager(){
        fragmentstateadapter = MusicList_ViewPagerAdapter(this,num_page)
        viewpager?.adapter = fragmentstateadapter
        viewpager?.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewpager?.currentItem = 0
        viewpager?.offscreenPageLimit = 1

        viewpager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (positionOffsetPixels == 0) {
                    viewpager?.currentItem = position
                }
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("TEST onPageSelected", position.toString())
                var input_text = "${position+1}/$num_page"
                mtv_pagenum?.text = input_text
            }

        })

        val pageMargin: Int = resources.getDimensionPixelOffset(R.dimen.pageMargin)
        val pageOffset: Int = resources.getDimensionPixelOffset(R.dimen.offset);

        viewpager?.setPageTransformer { page, position ->
            var myOffset: Float = position * -(2 * pageOffset + pageMargin);
            if (viewpager?.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                if (ViewCompat.getLayoutDirection(viewpager!!) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                    page.translationX = -myOffset;
                } else {
                    page.translationX = myOffset;
                }
            } else {
                page.translationY = myOffset;
            }
        }
    }

    override fun onResume() {
        super.onResume()
        play_background_video()
    }

}