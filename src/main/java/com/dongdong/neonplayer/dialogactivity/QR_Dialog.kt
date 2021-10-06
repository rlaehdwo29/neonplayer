package com.dongdong.neonplayer.dialogactivity

import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.os.CountDownTimer
import android.util.Log
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.dongdong.neonplayer.R
import com.dongdong.neonplayer.service.MusicService
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder


class QR_Dialog(mContext : Context) {

    companion object{
        private val TAG = QR_Dialog::class.java.simpleName
    }

    val dialog  = Dialog(mContext)
    var mcontainer_layout  : ConstraintLayout? = null
    var img_qr : ImageView? = null
    var btn_close : ImageButton? = null
    var count_time : TextView? = null

    fun start(size : Point){

        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.qr_activity)
        dialog?.setCancelable(false)

        var resize_x = (size.x * 0.9).toInt()
        var resize_y = (size.y * 0.8).toInt()

        mcontainer_layout = dialog.findViewById(R.id.container_qr)
        mcontainer_layout?.layoutParams?.width = resize_x
        mcontainer_layout?.layoutParams?.height = resize_y

        createQRcode()

        btn_close = dialog.findViewById(R.id.btn_close)
        btn_close?.setOnClickListener { view->
            dialog.dismiss()
        }

        dialog.show()
        count_time = dialog.findViewById(R.id.count_time)
    }

    fun createQRcode() {
        var multiFormatWriter : MultiFormatWriter = MultiFormatWriter()
        try {
            img_qr = dialog.findViewById(R.id.img_qr)
            val bitMatrix = multiFormatWriter.encode("rlaehdwo29", BarcodeFormat.QR_CODE, 800, 800)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            img_qr?.setImageBitmap(bitmap)
        }catch (e : Exception) {
            e.printStackTrace()
        }
    }

}