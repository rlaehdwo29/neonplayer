package com.dongdong.neonplayer.viewpager

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dongdong.neonplayer.R
import com.dongdong.neonplayer.adapter.Music24HtisItemAdapter
import com.dongdong.neonplayer.adapter.MusicNeonChartItemAdapter
import com.dongdong.neonplayer.data.Music24HitData
import com.dongdong.neonplayer.data.MusicInfo
import com.dongdong.neonplayer.data.NeonChartData
import com.dongdong.neonplayer.retrofit.RetroFitBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewPagerNeonChartList : Fragment {

    var mContext : Context? = null
    var fragment_num : Int? = 0
    var music_neon_recyclearview : RecyclerView? = null
    var music_neon_list : ArrayList<NeonChartData>? = null

    companion object {
        fun newInstance(num : Int) : ViewPagerNeonChartList {
            var fragment : ViewPagerNeonChartList = ViewPagerNeonChartList()
            var bun = Bundle()
            bun.putInt("pagenum",num)
            fragment.arguments = bun
            return fragment
        }
    }

    constructor(){

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragment_num = arguments?.getInt("pagenum")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var rootview : ViewGroup = inflater.inflate(R.layout.viewpager_neonchartlist,container,false) as ViewGroup
        mContext = rootview.context
        setBingId(rootview)
        getMusicNeonList()
        setMusicNeonChartRecyclearView(rootview)
        return rootview
    }

    fun getMusicNeonList() {

        var getCall = RetroFitBuilder.api
        getCall.getNeonChartList().enqueue(object : Callback<List<MusicInfo>> {
            override fun onResponse(call: Call<List<MusicInfo>>, response: Response<List<MusicInfo>>) {
                if (response.isSuccessful){
                    var data = response.body()
                    Log.d("qwer123456","음악 데이터 갯수: ${data?.size}")
                    for (i in 0 until data?.size!!){
                        Log.d("qwer123456","음악 데이터 타이: ${data[i].musictitle}")
                        var music_recomment = NeonChartData(data[i].musictitle)
                        music_neon_list?.add(music_recomment)
                    }
                    music_neon_recyclearview?.adapter = MusicNeonChartItemAdapter(mContext!!,music_neon_list!!)
                }else{
                    Toast.makeText(mContext,"음악 리스트를 가져오는중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<MusicInfo>>, t: Throwable) {
                Toast.makeText(mContext,"음악 리스트를 가져오는중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show()
            }
        })

    }

    fun setMusicNeonChartRecyclearView(mrootview : View){
        val musicneonchartmanager : RecyclerView.LayoutManager = GridLayoutManager(mrootview.context,1,LinearLayoutManager.VERTICAL,false)
        music_neon_recyclearview?.layoutManager = musicneonchartmanager
        music_neon_list = arrayListOf<NeonChartData>()
    }

    fun setBingId(mrootview : View){
        music_neon_recyclearview = mrootview.findViewById(R.id.music_neonlist)
    }

}