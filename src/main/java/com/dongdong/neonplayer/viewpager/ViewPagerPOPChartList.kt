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
import com.dongdong.neonplayer.adapter.MusicNeonChartItemAdapter
import com.dongdong.neonplayer.adapter.MusicPopChartItemAdapter
import com.dongdong.neonplayer.data.MusicInfo
import com.dongdong.neonplayer.data.NeonChartData
import com.dongdong.neonplayer.data.PopChartData
import com.dongdong.neonplayer.retrofit.RetroFitBuilder
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewPagerPOPChartList : Fragment {

    var mContext : Context? = null
    var fragment_num : Int? = 0
    var music_pop_recyclearview : RecyclerView? = null
    var music_pop_list : ArrayList<PopChartData>? = null

    companion object {
        fun newInstance(num : Int) : ViewPagerPOPChartList {
            var fragment : ViewPagerPOPChartList = ViewPagerPOPChartList()
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

        var rootview : ViewGroup = inflater.inflate(R.layout.viewpager_popsonglist,container,false) as ViewGroup
        mContext = rootview.context
        music_pop_recyclearview = rootview.findViewById(R.id.music_poplist)
        val musicpopmanager : RecyclerView.LayoutManager = GridLayoutManager(rootview.context,1,LinearLayoutManager.VERTICAL,false)
        music_pop_recyclearview?.layoutManager = musicpopmanager
        music_pop_list = arrayListOf<PopChartData>()
        getMusicPopitList()
        return rootview
    }

    fun getMusicPopitList() {

        var getCall = RetroFitBuilder.api
        getCall.getPopMusicList().enqueue(object : Callback<List<MusicInfo>> {
            override fun onResponse(call: Call<List<MusicInfo>>, response: Response<List<MusicInfo>>) {
                if (response.isSuccessful){
                    var data = response.body()
                    Log.d("qwer123456","음악 데이터 갯수: ${data?.size}")
                    for (i in 0 until data?.size!!){
                        Log.d("qwer123456","음악 데이터 타이틀: ${data[i].musictitle}")
                        var music_recomment = PopChartData(data[i].musictitle)
                        music_pop_list?.add(music_recomment)
                    }
                    music_pop_recyclearview?.adapter = MusicPopChartItemAdapter(mContext!!,music_pop_list!!)
                }else{
                    Toast.makeText(mContext,"음악 리스트를 가져오는중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<MusicInfo>>, t: Throwable) {
                Toast.makeText(mContext,"음악 리스트를 가져오는중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show()
            }
        })

       /* var storageRef = storage?.getReferenceFromUrl("gs://snsproject-fb39b.appspot.com/")
        var music_pop_ref = storage?.reference?.child("Recommend_Music_Mp3")
        Log.d("ViewPagerPOPChartList","getMusicPopitList 볼까? ${storageRef?.child("Recommend_Music_Mp3")?.listAll()}")

        *//**
         * POP 목록 가져오기
         *//*
        music_pop_ref?.listAll()?.addOnSuccessListener { meta_pop->
            for (items in meta_pop.items){
                Log.d("ViewPagerPOPChartList","POP Item: ${items.name} // ${items.downloadUrl} // ${items.metadata} // }")
                val ref = storageRef?.child("Recommend_Music_Mp3/${items.name}")
                var music_recomment = PopChartData(items.name)
                ref?.metadata?.addOnSuccessListener { metaitem->
                    Log.d("ViewPagerPOPChartList","POP Item 세팅 : ${metaitem.sizeBytes} // ${metaitem.contentType} // ${metaitem.name} }")
                    music_pop_list?.add(music_recomment)
                    music_pop_recyclearview?.adapter = MusicPopChartItemAdapter(mContext!!,music_pop_list!!)
                }?.addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
            }
        }*/
    }

}