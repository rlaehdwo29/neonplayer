package com.dongdong.neonplayer.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.dongdong.neonplayer.R
import com.dongdong.neonplayer.activity.LoginActivity
import com.dongdong.neonplayer.dialogactivity.QR_Dialog
import com.dongdong.neonplayer.adapter.BannerAdapter
import com.dongdong.neonplayer.adapter.NewMusicItemAdapter
import com.dongdong.neonplayer.adapter.TodayNewMusicItemAdapter
import com.dongdong.neonplayer.common.NetWorkState
import com.dongdong.neonplayer.common.Util
import com.dongdong.neonplayer.data.*
import com.dongdong.neonplayer.dialogactivity.FindUserInfoDialog
import com.dongdong.neonplayer.retrofit.RetroFitBuilder
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FragmentHome : Fragment(),View.OnClickListener{

    var btn_login_state : ImageView? = null
    var storage : FirebaseStorage? = null
    var layout_qrcode : LinearLayout? = null
    var today_new_music_list : ArrayList<TodayNewMusicData>? = null
    var new_music_list : ArrayList<NewMusicData>? = null
    var banner_img_list : ArrayList<BannerImageData>? = null
    var networkstate : Int? = 0
    var tv_networkstate : TextView? = null
    var livedata_today_new_music_list : MutableLiveData<ArrayList<TodayNewMusicData>>? = null
    var livedata_new_music_list : MutableLiveData<ArrayList<NewMusicData>>? = null
    var livedata_banner_img_list : MutableLiveData<ArrayList<BannerImageData>>? = null
    var liveData_network_state : MutableLiveData<Int> = MutableLiveData()
    var today_new_music_recycleview : RecyclerView? = null                              // 따끈따끈 신상 음악 Recycleview
    var new_music_recycleview : RecyclerView? = null                                    // 최신 음악 Recycleview
    var banner_img_list_recycleview : RecyclerView? = null                              // 배너 Recycleview
    var mContext : Context? = null
    var tv_user_id : TextView? = null
    var sliderViewPager : ViewPager2? = null
    var layoutIndicator : LinearLayout? = null
    var qrDialog : QR_Dialog? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView : View = inflater.inflate(R.layout.fragment_home,container,false)

        mContext = rootView.context

        layout_qrcode = rootView.findViewById(R.id.btn_qr_code)
        layout_qrcode?.setOnClickListener(this)

        today_new_music_recycleview = rootView.findViewById(R.id.today_new_music_list)
        new_music_recycleview = rootView.findViewById(R.id.new_music_list)
        banner_img_list_recycleview = rootView.findViewById(R.id.banner_img_list)

        val today_new_music_layoutManager: RecyclerView.LayoutManager = GridLayoutManager(rootView.context, 1, LinearLayoutManager.HORIZONTAL, false)
        val new_music_layoutManager: RecyclerView.LayoutManager = GridLayoutManager(rootView.context, 1, LinearLayoutManager.HORIZONTAL, false)
        val banner_layoutManager: RecyclerView.LayoutManager = GridLayoutManager(rootView.context, 1, LinearLayoutManager.HORIZONTAL, false)

        today_new_music_recycleview?.layoutManager = today_new_music_layoutManager
        new_music_recycleview?.layoutManager = new_music_layoutManager
        banner_img_list_recycleview?.layoutManager = banner_layoutManager

        today_new_music_list = arrayListOf<TodayNewMusicData>()
        new_music_list = arrayListOf<NewMusicData>()
        banner_img_list = arrayListOf<BannerImageData>()

        btn_login_state = rootView.findViewById(R.id.btn_login_state)
        tv_networkstate = rootView.findViewById(R.id.tv_networkstate)
        btn_login_state?.setOnClickListener {
            var intent = Intent(context, LoginActivity::class.java)
            activity?.startActivity(intent)
        }

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        liveData_network_state.value = NetWorkState.getNetWorkState(mContext!!)
        getMusicList()
        getBannerList()
        livedata_observer()
    }

    override fun onResume() {
        super.onResume()
    }

    fun getMusicList( ){

        var storageRef = storage?.getReferenceFromUrl("gs://snsproject-fb39b.appspot.com/")
        var music_listRef = storage?.reference?.child("Recommend_Music_Mp3")
        Log.d("qwer123456","볼까? ${storageRef?.child("Recommend_Music_Mp3")?.listAll()}")

        /**
         * 따끈따끈 신상 음악 Adapter 세팅
         */

        var getTodayNewMusicCall = RetroFitBuilder.api
        getTodayNewMusicCall.getTodayNewMusicList().enqueue(object : Callback<List<MusicInfo>> {
            override fun onResponse(call: Call<List<MusicInfo>>, response: Response<List<MusicInfo>>) {
                if (response.isSuccessful){
                    var data = response.body()
                    Log.d("qwer123456","따끈따끈 신상 음악 데이터 갯수: ${data?.size}")
                    for (i in 0 until data?.size!!){
                        Log.d("qwer123456","따끈따끈 신상 음악 데이터 타이: ${data[i].musictitle}")
                        var music_recomment = TodayNewMusicData(data[i].musictitle)
                        today_new_music_list?.add(music_recomment)
                        livedata_today_new_music_list?.postValue(today_new_music_list)
                    }
                    today_new_music_recycleview?.adapter = TodayNewMusicItemAdapter(mContext!!,today_new_music_list!!)
                }else{
                    Toast.makeText(mContext,"따끈따끈 신상 음악 목록을 가져오는중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<MusicInfo>>, t: Throwable) {
                Toast.makeText(mContext,"따끈따끈 신상 음악 목록을 가져오는중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show()
            }
        })

        /**
         *  최신 음악 Adapter 세팅
         */
        var getNewMusicCall = RetroFitBuilder.api
        getNewMusicCall.getNewMusicList().enqueue(object : Callback<List<MusicInfo>> {
            override fun onResponse(call: Call<List<MusicInfo>>, response: Response<List<MusicInfo>>) {
                if (response.isSuccessful){
                    var data = response.body()
                    Log.d("qwer123456","최신 음악 데이터 갯수: ${data?.size}")
                    for (i in 0 until data?.size!!){
                        Log.d("qwer123456","최신 음악  데이터 타이: ${data[i].musictitle}")
                        var music_recomment = NewMusicData(data[i].musictitle)
                        new_music_list?.add(music_recomment)
                        livedata_new_music_list?.postValue(new_music_list)
                    }
                    new_music_recycleview?.adapter = NewMusicItemAdapter(mContext!!,new_music_list!!)
                }else{
                    Toast.makeText(mContext,"최신 음악 리스트를 가져오는중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<MusicInfo>>, t: Throwable) {
                Toast.makeText(mContext,"최신 음악 리스트를 가져오는중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show()
            }
        })

    }

    fun getBannerList(){

        var getBannerCall = RetroFitBuilder.api
        getBannerCall.getHomeBannerFilePath().enqueue(object : Callback<List<BannerInfo>> {
            override fun onResponse(call: Call<List<BannerInfo>>, response: Response<List<BannerInfo>>) {
                if (response.isSuccessful){
                    var data = response.body()
                    Log.d("qwer123456","홈 배너 데이터 갯수: ${data?.size}")
                    for (i in 0 until data?.size!!){
                        Log.d("qwer123456","홈 배너 데이터 타이: ${data[i].homebannerpath}")

                        //var uri  = "${Contacts.baseurl}${data[i].homebannerpath}"
                        var banner_uri = Util.getParseUri(data[i].homebannerpath)

                        Log.d("qwer123456","배너 URI : $banner_uri")

                        var music_recomment = BannerImageData(banner_uri)
                        banner_img_list?.add(music_recomment)
                        livedata_banner_img_list?.postValue(banner_img_list)
                    }
                    banner_img_list_recycleview?.adapter = BannerAdapter(mContext!!,banner_img_list!!,get_device_size())
                }else{
                    Toast.makeText(mContext,"홈 배너를 가져오는중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<BannerInfo>>, t: Throwable) {
                Toast.makeText(mContext,"홈 배너를 가져오는중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show()
            }
        })

       /* var storageRef = storage?.getReferenceFromUrl("gs://snsproject-fb39b.appspot.com/")

        var banner_listRef = storage?.reference?.child("Banner_image")
        banner_listRef?.listAll()?.addOnSuccessListener { bannerdata ->
            for (banneritems in bannerdata.items){
                var ref = storageRef?.child("Banner_image/${banneritems.name}")
                if (ref != null){
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        Log.d("qwer123456","볼까?? ${Glide.with(mContext!!).load(uri)} // ${uri}")
                        var banner_uri = BannerImageData(uri)
                        banner_img_list?.add(banner_uri)
                        banner_img_list_recycleview?.adapter = BannerAdapter(mContext!!,banner_img_list!!,get_device_size())

                    }?.addOnFailureListener { exception ->
                        exception.printStackTrace()
                    }
                }

            }
        }?.addOnFailureListener { exception ->
            exception.printStackTrace()
        }*/

    }


    private fun setupIndicators(count: Int) {
        val indicators = arrayOfNulls<ImageView>(count)
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(16, 8, 16, 8)
        for (i in indicators.indices) {
            indicators[i] = ImageView(mContext!!)
            indicators[i]!!.setImageDrawable(ContextCompat.getDrawable(mContext!!, R.drawable.bg_indicator_inactive))
            indicators[i]!!.layoutParams = params
            layoutIndicator!!.addView(indicators[i])
        }
        setCurrentIndicator(0)
    }

    private fun setCurrentIndicator(position: Int) {
        val childCount = layoutIndicator!!.childCount
        for (i in 0 until childCount) {
            val imageView = layoutIndicator!!.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(mContext!!, R.drawable.bg_indicator_active))
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(mContext!!, R.drawable.bg_indicator_inactive)
                )
            }
        }
    }

    fun get_device_size(): Point {
        var display = activity?.windowManager?.defaultDisplay
        var size = Point()
        display?.getSize(size)

        return size
    }

    fun livedata_observer(){
        livedata_today_new_music_list?.observe(viewLifecycleOwner, Observer {
            today_new_music_list = it
        })
        livedata_new_music_list?.observe(viewLifecycleOwner, Observer {
            new_music_list = it
        })
        livedata_banner_img_list?.observe(viewLifecycleOwner, Observer {
            banner_img_list = it
        })
        liveData_network_state?.observe(viewLifecycleOwner, Observer {
            tv_networkstate?.text = "$it"
        })
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_qr_code -> {
                qrDialog = QR_Dialog(mContext!!)
                qrDialog?.start(get_device_size())
            }
        }
    }

}