package com.dongdong.neonplayer.adapter

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import com.dongdong.neonplayer.R
import com.dongdong.neonplayer.`interface`.OnItemClickListener
import com.dongdong.neonplayer.`interface`.OnItemLongClickListener
import com.dongdong.neonplayer.activity.MusicPlayerActivity
import com.dongdong.neonplayer.common.MusicInfoLoadUtil
import com.dongdong.neonplayer.common.Util
import com.dongdong.neonplayer.data.Music24HitData
import com.dongdong.neonplayer.data.MusicPlayData
import com.dongdong.neonplayer.service.MusicService


class Music24HtisItemAdapter : RecyclerView.Adapter<Music24HtisItemAdapter.ViewHolder> {

    var mcontext : Context? = null
    var itemList : ArrayList<Music24HitData>? = null
    var playData : MusicPlayData? = null
    var mListener  : OnItemClickListener? = null
    var mLongListener   : OnItemLongClickListener? = null
    var newMusicCheck : Boolean? = false         // true -> 새로운 음악 , false -> 새로운 음악 아님

    constructor(){}


    constructor(context : Context,list : ArrayList<Music24HitData>){
        this.mcontext = context
        this.itemList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, position : Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.viewpager_recycleview_listitem, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return itemList?.size!!
    }

    //ViewHolder 클래스
      inner class ViewHolder : RecyclerView.ViewHolder {
        var img_album: ImageView? = null
        var tv_title: TextView? = null
        var tv_artist_name : TextView? = null

        constructor(@Nullable itemView : View):super(itemView){
            img_album = itemView.findViewById(R.id.img_viewpager_album)
            tv_title = itemView.findViewById(R.id.tv_viewpager_title)
            tv_artist_name = itemView.findViewById(R.id.tv_viewpager_artist)
        }

        fun onBind(position: Int) {
            tv_title?.text = Util.getAlbumInfo(itemList?.get(position)?.album_title)?.get(1)            //viewHolder 객체
            tv_artist_name?.text = Util.getAlbumInfo(itemList?.get(position)?.album_title)?.get(0)

            var drawable_album : GradientDrawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mcontext!!.getDrawable(R.drawable.item_border_10pt) as GradientDrawable
            } else {
                TODO("VERSION.SDK_INT < LOLLIPOP")
            }
            img_album?.background = drawable_album
            img_album?.clipToOutline = true

            itemView.setOnClickListener { v ->
                var pos = adapterPosition
                if (playData?.play_title != null) {
                    if (playData?.play_title.equals(itemList?.get(pos)?.album_title) == true) {
                        newMusicCheck = false
                    } else {
                        newMusicCheck = true
                    }
                }else{
                    newMusicCheck = true
                }

                Log.d("qwer123456","클릭 포지션$pos // ${itemList?.get(pos)} // ${playData?.play_title} // $newMusicCheck")

                playData = MusicPlayData(itemList?.get(pos)?.album_title)
                Log.d("qwer123456","앨범 타이틀: ${playData?.play_title}")
                var intent = Intent(mcontext, MusicPlayerActivity::class.java)
                intent.putExtra("playdata",playData)
                intent.putExtra("newmusiccheck",newMusicCheck!!)
                mcontext?.startActivity(intent)

            }

            itemView.setOnLongClickListener { v ->
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    mLongListener!!.onItemLongClick(v, pos)
                }
                true
            }

        }

    }

    fun setOnItemClickListener(listener : OnItemClickListener){
        this.mListener = listener
    }
    fun setOnItemLongClickListener(listener : OnItemLongClickListener) {
        this.mLongListener = listener
    }

    fun getMusicUID() : Int? {
        var getuid = MusicInfoLoadUtil.getMusicUID(mcontext!!, playData?.play_title)?.album_uid
        return getuid!! - 1
    }

}