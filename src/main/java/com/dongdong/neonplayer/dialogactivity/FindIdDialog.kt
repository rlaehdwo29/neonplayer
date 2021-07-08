package com.dongdong.neonplayer.dialogactivity

import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.util.Log
import android.view.Window
import android.widget.*
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintLayout
import com.dongdong.neonplayer.R
import com.dongdong.neonplayer.common.Util
import com.dongdong.neonplayer.firebase.User
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class FindIdDialog (context: Context) {

    val dialog = Dialog(context)
    lateinit var medt_name : EditText
    lateinit var mbtn_surch : Button
    lateinit var mbtn_close : Button
    lateinit var medt_findid_jumin_1 : EditText
    lateinit var medt_findid_jumin_2 : EditText
    lateinit var mcontainer_layout : ConstraintLayout
    var database : DatabaseReference? = null

    fun start(size : Point){
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.dialog_find_id)
        dialog?.setCancelable(false)

        var resize_x = (size.x * 0.9).toInt()
        var resize_y = (size.y * 0.8).toInt()

        mcontainer_layout = dialog.findViewById(R.id.container_find_id)
        mcontainer_layout.layoutParams.width = resize_x
        mcontainer_layout.layoutParams.height = resize_y

        medt_name = dialog.findViewById(R.id.edt_name)
        mbtn_surch  = dialog.findViewById(R.id.btn_surch)
        mbtn_close = dialog.findViewById(R.id.btn_close)
        medt_findid_jumin_1 = dialog.findViewById(R.id.edt_findid_userjumin_1)
        medt_findid_jumin_2 = dialog.findViewById(R.id.edt_findid_userjumin_2)

        database = Firebase.database.reference                  // Firebase Realtime DataBase Setting

        mbtn_surch.setOnClickListener {
            if (medt_name.text.length == 0){
                Toast.makeText(dialog.context,"이름을 입력하세요.",Toast.LENGTH_SHORT).show()
            }else if(medt_findid_jumin_1.text.length == 0){
                Toast.makeText(dialog.context,"주민번호 앞자리를 입력해주세요..", Toast.LENGTH_SHORT).show()
                medt_findid_jumin_1.requestFocus()
            }else if(medt_findid_jumin_1.text.length != 6){
                Toast.makeText(dialog.context,"주민번호 앞 6자리를 입력해주세요..", Toast.LENGTH_SHORT).show()
                medt_findid_jumin_1.requestFocus()
                medt_findid_jumin_1.text.clear()
            } else if(medt_findid_jumin_2.text.length == 0){
                Toast.makeText(dialog.context,"주민번호 뒷자리를 입력해주세요..", Toast.LENGTH_SHORT).show()
                medt_findid_jumin_2.requestFocus()
            }else if(medt_findid_jumin_2.text.length != 7){
                Toast.makeText(dialog.context,"주민번호 뒤 7자리를 입력해주세요.", Toast.LENGTH_SHORT).show()
                medt_findid_jumin_2.requestFocus()
                medt_findid_jumin_2.text.clear()
            } else {
                var user_jumin = medt_findid_jumin_1.text.toString() + "-" + medt_findid_jumin_2.text.toString()
                Search_ID(medt_name.text.toString(),user_jumin)
                Log.d("아이디 찾기","찾기 데이터 Result: ${medt_name.text}")
            }
        }

        mbtn_close.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun Search_ID(name : String , jumin : String){

        val query: Query = database!!.child("CUser").child("User_Info").orderByChild("user_jumin").equalTo(jumin)

        query.addListenerForSingleValueEvent (object : ValueEventListener {

            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                Log.d("qwer123465","여긴오나11? ${dataSnapshot.children.count()}")
                if (dataSnapshot.children.count() > 0) {
                    for (childSnapshot in dataSnapshot.children) {
                        var post = childSnapshot.getValue(User::class.java)
                        Log.d("qwer123465", "여긴오나? $post // ${post?.user_name} // $name")
                        if (post?.user_name?.equals(name)!!) {
                            Util.ShowAlertDialog(dialog.context,"알림", "회원님의 ID는 ${post.user_id} 입니다.")
                            //Toast.makeText(dialog.context, "회원님의 ID는 ${post.user_id} 입니다.", Toast.LENGTH_SHORT).show()
                            Log.w("FireBaseData", "getData${post}")
                        } else {
                            Util.ShowAlertDialog(dialog.context,"알림", "존재하는 회원 정보가 없습니다.")
                            //Toast.makeText(dialog.context, "존재하는 회원 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                            Log.w("FireBaseData", "getData 없음.")
                        }
                    }
                }else{
                    Util.ShowAlertDialog(dialog.context,"알림","존재하는 회원 정보가 없습니다.")
                }
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Toast.makeText(dialog.context, "데이터 확인중 오류가 발생하였습니다. \n" + databaseError.message, Toast.LENGTH_SHORT).show()
                Log.w("FireBaseData", "loadPost:onCancelled", databaseError.toException())
            }
        })

    }

}