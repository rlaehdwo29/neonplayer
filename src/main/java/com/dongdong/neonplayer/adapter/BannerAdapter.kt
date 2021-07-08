package com.dongdong.neonplayer.adapter

import android.content.Context
import android.graphics.Point
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dongdong.neonplayer.R
import com.dongdong.neonplayer.data.BannerImageData


class BannerAdapter : RecyclerView.Adapter<BannerAdapter.ViewHolder> {

    var mcontext : Context? = null
    var banner_list : ArrayList<BannerImageData>? = null
    var mpoint : Point? = null
    var mconst_container : ConstraintLayout? = null

    constructor(context: Context, bannerlist: ArrayList<BannerImageData>,point : Point) {
        this.mcontext = context
        this.banner_list = bannerlist
        this.mpoint = point
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view: View = LayoutInflater.from(parent.context).inflate(R.layout.banner_slide_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerAdapter.ViewHolder, position: Int) {
        holder.bindSliderImage(banner_list?.get(position)?.imageuri)
    }

    override fun getItemCount(): Int {
        return banner_list?.size!!
    }

    inner class ViewHolder : RecyclerView.ViewHolder {
        private val BannerImageView: ImageView

        constructor(@NonNull itemView: View) : super(itemView){
            BannerImageView = itemView.findViewById(R.id.imageSlider)
            var drawable_banner : GradientDrawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mcontext!!.getDrawable(R.drawable.item_border_2pt) as GradientDrawable
            } else {
                TODO("VERSION.SDK_INT < LOLLIPOP")
            }
            BannerImageView?.background = drawable_banner
            BannerImageView?.clipToOutline = true

            var resize_x = (mpoint!!.x * 0.8).toInt()
            var resize_y = (mpoint!!.y * 0.2).toInt()
            BannerImageView?.layoutParams?.width = resize_x
            BannerImageView?.layoutParams?.height = resize_y

        }


        fun bindSliderImage(imageURL: Uri?) {
            Glide.with(mcontext!!)
                .load(imageURL)
                .into(BannerImageView)
        }

    }


}