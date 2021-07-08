package com.dongdong.neonplayer.dialogactivity

import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.dongdong.neonplayer.R

class FindUserInfoDialog(context: Context){

    val dialog = Dialog(context)
    lateinit var mbtn_find_id :ImageButton
    lateinit var mbtn_find_pw : ImageButton
    lateinit var mbtn_close : Button
    var mfind_id_dialog : FindIdDialog? = null
    var mfind_pw_dialog : FindPwDialog? = null
    lateinit var mcontainer_layout : ConstraintLayout

    fun start(size : Point){
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.dialog_finduserinfo)
        dialog?.setCancelable(false)

        var resize_x = (size.x * 0.8).toInt()
        var resize_y = (size.y * 0.8).toInt()

        mcontainer_layout = dialog.findViewById(R.id.container_finduserinfo)
        mcontainer_layout.layoutParams.width = resize_x
        mcontainer_layout.layoutParams.height = resize_y

        mbtn_find_id = dialog.findViewById(R.id.btn_find_id)
        mbtn_find_pw = dialog.findViewById(R.id.btn_find_pw)
        mbtn_close  = dialog.findViewById(R.id.btn_close)

        mbtn_find_id.setOnClickListener {
            //Toast.makeText(dialog.context,"아이디 찾기 클릭하셨습니다.",Toast.LENGTH_SHORT).show()
            mfind_id_dialog = FindIdDialog(dialog.context)
            mfind_id_dialog?.start(size)
        }

        mbtn_find_pw.setOnClickListener {
            //Toast.makeText(dialog.context,"비밀번호 찾기를 클릭하셨습니다.",Toast.LENGTH_SHORT).show()
            mfind_pw_dialog = FindPwDialog(dialog.context)
            mfind_pw_dialog?.start(size)
        }

        mbtn_close.setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()
    }




}