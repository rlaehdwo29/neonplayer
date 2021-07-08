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
import com.dongdong.neonplayer.data.Music24HitData
import com.dongdong.neonplayer.data.MusicInfo
import com.dongdong.neonplayer.retrofit.RetroFitBuilder
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewPager24HitsList : Fragment{

    var mContext : Context? = null
    var fragment_num : Int? = 0
    var music_24hit_recyclearview : RecyclerView? = null
    var music_24hit_list : ArrayList<Music24HitData>? = null

    companion object {
        fun newInstance(num : Int) : ViewPager24HitsList {
            var fragment : ViewPager24HitsList = ViewPager24HitsList()
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
        var rootview : ViewGroup = inflater.inflate(R.layout.viewpager_24hitslist,container,false) as ViewGroup
        mContext = rootview.context
        setBingId(rootview)
        getMusic24HitList()
        setMusic24HipRecyclearView(rootview)
        return rootview
    }

    fun getMusic24HitList() {

        var getCall = RetroFitBuilder.api
        getCall.getOneWeekNewMusicList().enqueue(object : Callback<List<MusicInfo>> {
            override fun onResponse(call: Call<List<MusicInfo>>, response: Response<List<MusicInfo>>) {
                if (response.isSuccessful){
                    var data = response.body()
                    Log.d("qwer123456","음악 데이터 갯수: ${data?.size}")
                    for (i in 0 until data?.size!!){
                        Log.d("qwer123456","음악 데이터 타이: ${data[i].musictitle}")
                        var music_recomment = Music24HitData(data[i].musictitle)
                        music_24hit_list?.add(music_recomment)
                    }
                    music_24hit_recyclearview?.adapter = Music24HtisItemAdapter(mContext!!,music_24hit_list!!)
                }else{
                    Toast.makeText(mContext,"음악 리스트를 가져오는중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<MusicInfo>>, t: Throwable) {
                Toast.makeText(mContext,"음악 리스트를 가져오는중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show()
            }
        })

        /*  var storageRef = storage?.getReferenceFromUrl("gs://snsproject-fb39b.appspot.com/")
           var music_24hit_ref = storage?.reference?.child("Recommend_Music_Mp3")
           Log.d("qwer123456","getMusic24itList 볼까? ${storageRef?.child("Recommend_Music_Mp3")?.listAll()}")

           *//**
         * 24시 Hits 목록 가져오기
         *//*
        music_24hit_ref?.listAll()?.addOnSuccessListener { meta_24hits->
            for (items in meta_24hits.items){
                Log.d("qwer123456","24시 Hits Item: ${items.name} // ${items.downloadUrl} // ${items.metadata} // }")
                val ref = storageRef?.child("Recommend_Music_Mp3/${items.name}")
                var music_recomment = Music24HitData(items.name)
                ref?.metadata?.addOnSuccessListener { metaitem->
                    Log.d("qwer123456","오늘의 최신 음악 Item 세팅 : ${metaitem.sizeBytes} // ${metaitem.contentType} // ${metaitem.name} }")
                    music_24hit_list?.add(music_recomment)
                    music_24hit_recyclearview?.adapter = Music24HtisItemAdapter(mContext!!,music_24hit_list!!)
                }?.addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
            }
        }*/
    }

    fun setMusic24HipRecyclearView(mrootview : View){
        val music24hitmanager : RecyclerView.LayoutManager = GridLayoutManager(mrootview.context,1,LinearLayoutManager.VERTICAL,false)
        music_24hit_recyclearview?.layoutManager = music24hitmanager
        music_24hit_list = arrayListOf<Music24HitData>()
    }

    fun setBingId(mrootview : View){
        music_24hit_recyclearview = mrootview.findViewById(R.id.music_24hitlist)
    }



}