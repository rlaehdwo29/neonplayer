package com.dongdong.neonplayer.adapter

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import com.dongdong.neonplayer.R
import com.dongdong.neonplayer.common.Util
import com.dongdong.neonplayer.data.NewMusicData
import java.io.FileNotFoundException
import java.io.IOException


class NewMusicItemAdapter : RecyclerView.Adapter<NewMusicItemAdapter.ViewHolder> {

    var mcontext : Context? = null
    var itemList : ArrayList<NewMusicData>? = null
    lateinit var activity: Activity

    companion object {
        val options : BitmapFactory.Options = BitmapFactory.Options()

        fun getAlbumImage(mcontext : Context , Album_id : String, MAX_IMAGE_SIZE : Int) : Bitmap? {
            var res : ContentResolver = mcontext.contentResolver
            var uri : Uri = Uri.parse("content://media/external/audio/albumart/${Album_id.toInt()}")
            if (uri != null) {
                var fd : ParcelFileDescriptor? = null
                try{
                    fd = res.openFileDescriptor(uri,"r")
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFileDescriptor(fd?.fileDescriptor,null, options)
                    var scale : Int = 0
                    if (options.outHeight > MAX_IMAGE_SIZE || options.outWidth > MAX_IMAGE_SIZE) {
                        scale = Math.pow(2.0, Math.round(Math.log((MAX_IMAGE_SIZE /Math.max(options.outHeight, options.outWidth)).toDouble()) / Math.log(0.5)).toDouble()).toInt()
                    }
                    options.inJustDecodeBounds = false
                    options.inSampleSize = scale

                    var bitmap : Bitmap = BitmapFactory.decodeFileDescriptor(fd?.fileDescriptor,null,options)

                    if (bitmap != null) {
                        if (options.outWidth != MAX_IMAGE_SIZE || options.outHeight != MAX_IMAGE_SIZE) {
                            var tmp : Bitmap = Bitmap.createScaledBitmap(bitmap,MAX_IMAGE_SIZE,MAX_IMAGE_SIZE,true)
                            bitmap.recycle()
                            bitmap = tmp
                        }
                    }
                    return bitmap
                }catch (e:FileNotFoundException) {
                    e.printStackTrace()
                    }finally {
                        try {
                            if (fd != null) {
                                fd.close()
                        }
                    }catch (e : IOException){
                        e.printStackTrace()
                    }
                }
            }
            return null
        }

    }

    constructor(){}


    constructor(context : Context,list : ArrayList<NewMusicData>){
        this.mcontext = context
        this.itemList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, position : Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.item_recommend, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            //var album_image = getAlbumImage(mcontext!!, itemList?.get(position)?.album_id!!, 170)
           // holder.img_album?.setImageBitmap(album_image)

            holder.onBind(position)

            //holder.tv_artist_name?.text = itemList?.get(position)?.album_artist


          /*  holder.img_album?.setOnClickListener(View.OnClickListener { v ->

                var intent = Intent(v.context, ItemActivity::class.java)
            intent.putExtra("number", position)
            intent.putExtra("title", itemList?.get(position)?.album_title)
            v.context.startActivity(intent)
            Toast.makeText(v.context, "클릭 되었습니다.", Toast.LENGTH_SHORT).show()
            })*/
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
            img_album = itemView.findViewById(R.id.music_album)
            tv_title = itemView.findViewById(R.id.music_title)
            tv_artist_name = itemView.findViewById(R.id.music_artist)
        }

        fun onBind(position: Int) {

          /*  var album_replace = itemList?.get(position)?.album_title?.replace("/neon/music/","")?.replace(".mp3","")
            var album_info = album_replace?.split(" - ")
            var album_title = album_info?.get(1)
            var album_artist = album_info?.get(0)*/

            tv_title?.text = Util.getAlbumInfo(itemList?.get(position)?.album_title)?.get(1)                //viewHolder 객체
            tv_artist_name?.text = Util.getAlbumInfo(itemList?.get(position)?.album_title)?.get(0)

            var drawable_album: GradientDrawable =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mcontext!!.getDrawable(R.drawable.item_border_2pt) as GradientDrawable
                } else {
                    TODO("VERSION.SDK_INT < LOLLIPOP")
                }
            img_album?.background = drawable_album
            img_album?.clipToOutline = true
        }
    }

}