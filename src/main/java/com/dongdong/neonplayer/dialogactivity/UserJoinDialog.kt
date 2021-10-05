package com.dongdong.neonplayer.dialogactivity

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintLayout
import com.dongdong.neonplayer.R
import com.dongdong.neonplayer.common.Util
import com.dongdong.neonplayer.firebase.CUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*


class UserJoinDialog(context : Context) {

    val dialog = Dialog(context)
    lateinit var join_medt_name : EditText
    lateinit var join_medt_id : EditText
    lateinit var join_medt_pw : EditText
    lateinit var join_medt_pw_check : EditText
    lateinit var join_medt_email : EditText
    lateinit var join_medt_phonenum : EditText
    lateinit var join_mbtn_join : Button
    lateinit var join_mbtn_close : Button
    lateinit var join_mbtn_id_check : Button
    lateinit var join_mtv_pw_check_state : TextView
    lateinit var join_mcontainer_layout : ConstraintLayout
    lateinit var join_email_spinner : Spinner
    lateinit var join_phone_type_spinner : Spinner
    lateinit var join_address : EditText
    lateinit var join_medt_userjumin_1 : EditText
    lateinit var join_medt_userjumin_2 : EditText
    var id_check_state : Boolean = false
    var pw_check_state : Boolean = false
    var jumin_check_state : Boolean = false
    lateinit var whatcher_text : String
    var mcontext : Context = context
    lateinit var join_layout_pw_check_layout : LinearLayout
    var database : DatabaseReference? = null

    fun start(size : Point){
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.dialog_user_join)
        dialog?.setCancelable(false)

        var resize_x = (size.x * 0.9).toInt()
        var resize_y = (size.y * 0.9).toInt()

        join_mcontainer_layout = dialog.findViewById(R.id.container_user_join)
        join_mcontainer_layout.layoutParams.width = resize_x
        join_mcontainer_layout.layoutParams.height = resize_y

        BindID(dialog)
        database = Firebase.database.reference                  // Firebase Realtime DataBase Setting
        set_email_spinner()                                     // 이메일 Spinner Setting
        set_phone_type_spinner()                                // 통신사 Spinner Setting
        Text_Whatcher()                                         // Text 변경 Event
        Click_Button_Event()                                    // Button Click Event

        dialog.show()
    }

    /**
     * Email Spinner Setting
     */
    fun set_email_spinner() {
        val str: Array<String> = dialog.context.resources.getStringArray(R.array.email)
        val adapter = ArrayAdapter(dialog.context, R.layout.spinner_item, str)
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        join_email_spinner.adapter = adapter

        //spinner 이벤트 리스너
        join_email_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if (join_email_spinner.selectedItemPosition > 0) {
                    //선택된 항목
                }
            }
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    /**
     * 통신사 Spinner
     */

    fun set_phone_type_spinner(){
        val str: Array<String> = dialog.context.resources.getStringArray(R.array.phone_type)
        val adapter = ArrayAdapter(dialog.context, R.layout.spinner_item, str)
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        join_phone_type_spinner.adapter = adapter

        //spinner 이벤트 리스너
        join_phone_type_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if (join_phone_type_spinner.selectedItemPosition > 0) {
                    //선택된 항목
                }
            }
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    fun Text_Whatcher() {

            join_medt_pw.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        whatcher_text = text.toString()

                        if (whatcher_text == join_medt_pw_check.text.toString()) {
                            join_mtv_pw_check_state.text = "비밀번호가 일치합니다."
                            join_mtv_pw_check_state.setTextColor(mcontext.resources.getColor(R.color.orange))
                            pw_check_state = true
                        } else {
                            join_mtv_pw_check_state.text = "비밀번호가 일치하지 않습니다."
                            join_mtv_pw_check_state.setTextColor(mcontext.resources.getColor(R.color.red))
                            pw_check_state = false
                        }

                    }

                    override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {

                        Log.d("qwer123456", "뭐야: ${text.toString()} // ${join_medt_pw_check.text.toString()}")

                        if (text.toString().contains(" ")) {
                            Toast.makeText(dialog.context,"비밀번호에 공백을 넣을 수 없습니다.", Toast.LENGTH_SHORT).show()
                            join_medt_pw.text.clear()
                            return
                        }

                        if (text.toString() == join_medt_pw_check.text.toString()) {
                            join_mtv_pw_check_state.text = "비밀번호가 일치합니다."
                            join_mtv_pw_check_state.setTextColor(mcontext.resources.getColor(R.color.orange))
                            pw_check_state = true
                        } else {
                            join_mtv_pw_check_state.text = "비밀번호가 일치하지 않습니다."
                            join_mtv_pw_check_state.setTextColor(mcontext.resources.getColor(R.color.red))
                            pw_check_state = false
                        }
                    }

                    override fun afterTextChanged(text: Editable?) {
                        Log.d(
                            "qwer123456",
                            "뭐야111: ${text.toString()} // ${join_medt_pw_check.text.toString()}"
                        )

                          if (text.toString().contains(" ")) {
                            Toast.makeText(dialog.context,"비밀번호에 공백을 넣을 수 없습니다.", Toast.LENGTH_SHORT).show()
                            join_medt_pw.text.clear()
                            return
                        }

                        if (text.toString() == join_medt_pw_check.text.toString()) {
                            join_mtv_pw_check_state.text = "비밀번호가 일치합니다."
                            join_mtv_pw_check_state.setTextColor(mcontext.resources.getColor(R.color.orange))
                            pw_check_state = true
                        } else {
                            join_mtv_pw_check_state.text = "비밀번호가 일치하지 않습니다."
                            join_mtv_pw_check_state.setTextColor(mcontext.resources.getColor(R.color.red))
                            pw_check_state = false
                        }
                    }

                })

        /**
         * 비밀번호와 비밀번호 확인이 일치할때
         */
        join_medt_pw_check.setOnFocusChangeListener(View.OnFocusChangeListener { view, gainFocus ->

            if (gainFocus) {                    //포커스가 주어졌을때

                if (join_medt_pw.text.length == 0) {
                    Toast.makeText(dialog.context,"비밀번호를 먼저 입력해주세요.", Toast.LENGTH_SHORT).show()
                    join_medt_pw.requestFocus()
                    return@OnFocusChangeListener
                }

                join_medt_pw_check.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        whatcher_text = text.toString()

                        if (join_medt_pw_check.text.length == 0){
                            join_layout_pw_check_layout.visibility = View.GONE
                        }else{
                            join_layout_pw_check_layout.visibility = View.VISIBLE
                        }

                        if (whatcher_text == join_medt_pw.text.toString()) {
                            join_mtv_pw_check_state.text = "비밀번호가 일치합니다."
                            join_mtv_pw_check_state.setTextColor(mcontext.resources.getColor(R.color.orange))
                            pw_check_state = true
                        } else {
                            join_mtv_pw_check_state.text = "비밀번호가 일치하지 않습니다."
                            join_mtv_pw_check_state.setTextColor(mcontext.resources.getColor(R.color.red))
                            pw_check_state = false
                        }

                    }

                    override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {

                        Log.d("qwer123456", "뭐야: ${text.toString()} // ${join_medt_pw.text.toString()}")

                         if (join_medt_pw_check.text.length == 0){
                            join_layout_pw_check_layout.visibility = View.GONE
                        }else{
                            join_layout_pw_check_layout.visibility = View.VISIBLE
                        }

                          if (text.toString().contains(" ")) {
                            Toast.makeText(dialog.context,"비밀번호에 공백을 넣을 수 없습니다.", Toast.LENGTH_SHORT).show()
                              join_medt_pw_check.text.clear()
                            return
                        }

                        if (text.toString() == join_medt_pw.text.toString()) {
                            join_mtv_pw_check_state.text = "비밀번호가 일치합니다."
                            join_mtv_pw_check_state.setTextColor(mcontext.resources.getColor(R.color.orange))
                            pw_check_state = true
                        } else {
                            join_mtv_pw_check_state.text = "비밀번호가 일치하지 않습니다."
                            join_mtv_pw_check_state.setTextColor(mcontext.resources.getColor(R.color.red))
                            pw_check_state = false
                        }
                    }

                    override fun afterTextChanged(text: Editable?) {
                        Log.d(
                            "qwer123456",
                            "뭐야111: ${text.toString()} // ${join_medt_pw.text.toString()}"
                        )

                         if (join_medt_pw_check.text.length == 0){
                            join_layout_pw_check_layout.visibility = View.GONE
                        }else{
                            join_layout_pw_check_layout.visibility = View.VISIBLE
                        }

                          if (text.toString().contains(" ")) {
                            Toast.makeText(dialog.context,"비밀번호에 공백을 넣을 수 없습니다.", Toast.LENGTH_SHORT).show()
                              join_medt_pw_check.text.clear()
                            return
                        }

                        if (text.toString() == join_medt_pw.text.toString()) {
                            join_mtv_pw_check_state.text = "비밀번호가 일치합니다."
                            join_mtv_pw_check_state.setTextColor(mcontext.resources.getColor(R.color.orange))
                            pw_check_state = true
                        } else {
                            join_mtv_pw_check_state.text = "비밀번호가 일치하지 않습니다."
                            join_mtv_pw_check_state.setTextColor(mcontext.resources.getColor(R.color.red))
                            pw_check_state = false
                        }
                    }

                })

            }else{                              //포커스를 잃었을때
                //키보드 내리기
                var immhide = mcontext.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }

        })
    }

    fun Click_Button_Event(){
        /**
         * 회원 가입 버튼 클릭 시 이벤트
         */
        join_mbtn_join.setOnClickListener {
            if (join_medt_name.text.length == 0){
                Toast.makeText(dialog.context,"이름을 입력하세요.", Toast.LENGTH_SHORT).show()
                join_medt_name.requestFocus()
            }else if(join_medt_userjumin_1.text.length == 0){
                Toast.makeText(dialog.context,"주민번호 앞자리를 입력해주세요..", Toast.LENGTH_SHORT).show()
                join_medt_userjumin_1.requestFocus()
            }else if(join_medt_userjumin_1.text.length != 6){
                Toast.makeText(dialog.context,"주민번호 앞 6자리를 입력해주세요..", Toast.LENGTH_SHORT).show()
                join_medt_userjumin_1.requestFocus()
                join_medt_userjumin_1.text.clear()
            } else if(join_medt_userjumin_2.text.length == 0){
                Toast.makeText(dialog.context,"주민번호 뒷자리를 입력해주세요..", Toast.LENGTH_SHORT).show()
                join_medt_userjumin_2.requestFocus()
            }else if(join_medt_userjumin_2.text.length != 7){
                Toast.makeText(dialog.context,"주민번호 뒤 7자리를 입력해주세요.", Toast.LENGTH_SHORT).show()
                join_medt_userjumin_2.requestFocus()
                join_medt_userjumin_2.text.clear()
            } else if(join_medt_id.text.length == 0){
                Toast.makeText(dialog.context,"ID를 입력하세요.", Toast.LENGTH_SHORT).show()
                join_medt_id.requestFocus()
            } else if(id_check_state  == false) {
                Toast.makeText(dialog.context,"ID 중복체크를 해주세요.", Toast.LENGTH_SHORT).show()
                join_medt_id.requestFocus()
            } else if(join_medt_pw.text.length == 0){
                Toast.makeText(dialog.context,"비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                join_medt_pw.requestFocus()
            }else if(pw_check_state == false){
                Toast.makeText(dialog.context,"비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                join_medt_pw_check.requestFocus()
            } else if(join_medt_email.text.length == 0){
                Toast.makeText(dialog.context,"Email을 입력하세요.", Toast.LENGTH_SHORT).show()
                join_medt_email.requestFocus()
            }else if(join_medt_phonenum.text.length == 0){
                Toast.makeText(dialog.context,"전화번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                join_medt_phonenum.requestFocus()
            }else {
                var user_jumin = join_medt_userjumin_1.text.toString() + "-" + join_medt_userjumin_2.text.toString()
                var user_email = join_medt_email.text.toString() + "@" + join_email_spinner.selectedItem.toString()
                Log.e("qwer123456","주민번호 체크 111: $jumin_check_state")
                if (check_juminnum(user_jumin) == false) {
                    Util.ShowAlertDialog(dialog.context,"알림","이미 가입되어 있는 계정이 있습니다.")
                    return@setOnClickListener
                }else {
                    setDataBase_userinfo(
                        Util.getDeviceID()!!,
                        join_medt_name.text.toString(),
                        user_jumin,
                        age_state(user_jumin),
                        gender_state(user_jumin),
                        join_medt_id.text.toString(),
                        join_medt_pw.text.toString(),
                        user_email,
                        join_phone_type_spinner.selectedItem.toString(),
                        join_medt_phonenum.text.toString(),
                        join_address.text.toString()
                    )
                    Log.e(
                        "Join Result",
                        "회원가입 데이터 Result: ${join_medt_name.text} // ${join_medt_email.text}@${join_email_spinner.selectedItem} // ${join_phone_type_spinner.selectedItem} ${join_medt_phonenum.text}"
                    )
                    Toast.makeText(dialog.context, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        /**
         * 닫기 버튼 클릭 시 이벤트
         */
        join_mbtn_close.setOnClickListener {
            dialog.dismiss()
        }

        /**
         * ID 중복체크 버튼 클릭 이벤트
         */
        join_mbtn_id_check.setOnClickListener {
            check_userid(join_medt_id.text.toString())
        }


    }

    fun BindID(dialog : Dialog){

        join_medt_name = dialog.findViewById(R.id.edt_join_name)
        join_medt_id = dialog.findViewById(R.id.edt_join_id)
        join_medt_pw = dialog.findViewById(R.id.edt_join_password)
        join_medt_pw_check = dialog.findViewById(R.id.edt_join_password_check)
        join_medt_email = dialog.findViewById(R.id.edt_join_email)
        join_medt_phonenum = dialog.findViewById(R.id.edt_join_phonenum)
        join_mbtn_join  = dialog.findViewById(R.id.btn_join)
        join_mbtn_close = dialog.findViewById(R.id.btn_join_close)
        join_mbtn_id_check = dialog.findViewById(R.id.btn_id_check)
        join_mtv_pw_check_state = dialog.findViewById(R.id.tv_pw_check_state)
        join_email_spinner = dialog.findViewById(R.id.join_email_spinner)
        join_phone_type_spinner = dialog.findViewById(R.id.join_phone_type_spinner)
        join_layout_pw_check_layout = dialog.findViewById(R.id.ly_pw_check_state)
        join_address = dialog.findViewById(R.id.edt_detail_user_address)
        join_medt_userjumin_1 = dialog.findViewById(R.id.edt_join_userjumin_1)
        join_medt_userjumin_2 = dialog.findViewById(R.id.edt_join_userjumin_2)
    }

    fun setDataBase_userinfo(
        udid : String,
        name:String,
        jumin: String,
        age : Int,
        gender : String,
        id: String,
        pw:String,
        email:String,
        phonetype:String,
        phonenum:String,
        address:String) {

        val user = CUser(udid,name,jumin,age,gender,id,pw,email,phonetype,phonenum,address)
        database!!.child("CUser").child("User_Info").child(id).setValue(user)

    }

    /**
     * ID 중복체크
     */
    fun check_userid(id:String) {

        val query: Query = database!!.child("CUser").child("User_Info").orderByChild("user_id").equalTo(id)

        query.addListenerForSingleValueEvent (object : ValueEventListener {

            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                if (dataSnapshot.children.count() > 0) {
                    for (childSnapshot in dataSnapshot.children) {
                        var post = childSnapshot.getValue(CUser::class.java)
                        Util.ShowAlertDialog(dialog.context, "알림", "사용할 수 없는 아이디입니다.")
                        //Toast.makeText(dialog.context, "사용할 수 없는 아이디입니다.", Toast.LENGTH_SHORT).show()
                        join_medt_id.text.clear()
                        id_check_state = false
                        Log.w("FireBaseData", "getData${post}")
                    }
                }else{
                    Util.ShowAlertDialog(dialog.context, "알림", "사용 가능한 ID입니다.")
                    id_check_state = true
                }
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Util.ShowAlertDialog(dialog.context,"오류","데이터 확인중 오류가 발생하였습니다.")
                //Toast.makeText(dialog.context, "데이터 확인중 오류가 발생하였습니다. \n" + databaseError.message, Toast.LENGTH_SHORT).show()
                id_check_state = false
                join_medt_id.text.clear()
                Log.w("FireBaseData", "loadPost:onCancelled", databaseError.toException())
            }
        })
     }

    /**
     * 주민번호 중복 체크
     */
    fun check_juminnum(jumin : String) : Boolean{
        var Query = database!!.child("CUser").child("User_Info").orderByChild("user_jumin").equalTo(jumin)

        Query.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(databaseSnapshot: DataSnapshot) {

                Log.d("qwer123456","주민번호 중복 체크123: ${jumin} // ${databaseSnapshot.children.count()}")
                if (databaseSnapshot.children.count() > 0){
                    for (childrenSnapshot in databaseSnapshot.children){
                        var post = childrenSnapshot.getValue(CUser::class.java)
                        Log.d("qwer123456","주민번호 중복 체크: ${post} // ${post?.user_jumin}")
                        jumin_check_state = false
                    }
                }else{
                    jumin_check_state = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        return jumin_check_state
    }


    /**
     * 성인 구분
     */
    fun age_state(jumin : String): Int {
        var age = jumin.substring(0,2).toInt()
        var ch = jumin[7]

        /**
         * 나이 구분
         */
        if (ch == '1' || ch == '2'){
            age = Calendar.getInstance().get(Calendar.YEAR) - (1900 + age) + 1
        }else if(ch == '3' || ch == '4') {
            age = Calendar.getInstance().get(Calendar.YEAR) - (2000 + age) +1
        }
        return age
    }

    /**
     * 성별 구분
     */
    fun gender_state(jumin : String) : String {

        var ch = jumin[7]
        var gender : String

        if (ch == '1' || ch == '3'){
            gender = "남성"
        }else if(ch == '2' || ch == ('4')){
            gender = "여성"
        }else{
            gender = "외국인"
        }
        return gender
    }



}