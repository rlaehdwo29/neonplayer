package com.dongdong.neonplayer.common

import android.app.Activity
import android.widget.Toast

class BackPressedForFinish {

    var activity : Activity? = null
    var toast : Toast? = null
    var backKeyPressedTime: Long = 0 // '뒤로' 버튼을 클릭했을 때의 시간
    val TIME_INTERVAL: Long = 2000 // 첫번째 버튼 클릭과 두번째 버튼 클릭 사이의 종료를 위한 시간차를 정의

    constructor(activity: Activity) {
        this.activity = activity
    }

     fun onBackPressed() {

        if (System.currentTimeMillis() > backKeyPressedTime + TIME_INTERVAL) {
            backKeyPressedTime = System.currentTimeMillis()
            showMessage()
        }else{
            toast?.cancel()
            activity?.finish()
        }

    }

    fun showMessage() {
        toast = Toast.makeText(activity, "'뒤로' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT)
        toast?.show()
    }


}