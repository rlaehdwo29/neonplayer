package com.dongdong.neonplayer.activity

import android.content.Context
import android.graphics.Point
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.dongdong.neonplayer.R
import com.dongdong.neonplayer.callback.LoginCallbacks
import com.dongdong.neonplayer.common.Util
import com.dongdong.neonplayer.data.VideoInfo
import com.dongdong.neonplayer.dialogactivity.FindUserInfoDialog
import com.dongdong.neonplayer.dialogactivity.UserJoinDialog
import com.dongdong.neonplayer.firebase.LoginState
import com.dongdong.neonplayer.firebase.CUser
import com.dongdong.neonplayer.retrofit.RetroFitBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random


class LoginActivity : AppCompatActivity(), View.OnClickListener,LoginCallbacks{

    var edt_userid : EditText? = null                               // 아이디 입력창
    var edt_userpw: EditText? = null                                // 비밀번호 입력창
    var layout_area : LinearLayout? = null
    var mbtn_login : Button? = null                                 // 로그인
    var mfind_userinfo : TextView? = null                           // 계정찾기
    var muser_join : TextView? = null                               // 회원가입
    var go_find_userinfo : FindUserInfoDialog? = null
    var go_user_join : UserJoinDialog? = null
    var database : DatabaseReference? = null

    var mvideoview : VideoView? = null
    var storage : FirebaseStorage? = null
    var mContext : Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mContext = this
        bind_item_id()                  // Layout에 각종 ID 바인딩
        setting_bind_id()               // Layout Settings
        play_background_video();
    }


    /**
     * Layout에 각종 ID 바인딩
     */
    fun bind_item_id() {
        edt_userid = findViewById(R.id.login_userid)
        edt_userpw = findViewById(R.id.login_userpw)
        layout_area = findViewById(R.id.ly_area)
        mbtn_login = findViewById(R.id.btn_login)
        mfind_userinfo = findViewById(R.id.find_userinfo)
        muser_join  = findViewById(R.id.user_join)
        mvideoview = findViewById(R.id.video_view)
        click_bind()
    }

    /**
     * Click Binding
     */

    fun click_bind(){
        mbtn_login?.setOnClickListener(this)
        mfind_userinfo?.setOnClickListener(this)
        muser_join?.setOnClickListener(this)
    }

    /**
     * Layout Settings
     */
    fun setting_bind_id(){
        database = Firebase.database.reference
        storage = FirebaseStorage.getInstance()
    }

    /**
     * Click Event
     */
    override fun onClick(v: View?) {

        when(v?.id){

            // 로그인 처리 로직
            R.id.btn_login -> {

                if (edt_userid?.length() == 0){
                    Toast.makeText(this,"ID를 입력해주세요.",Toast.LENGTH_SHORT).show()
                }else if(edt_userpw?.length() == 0) {
                    Toast.makeText(this,"비밀번를 입력해주세요.",Toast.LENGTH_SHORT).show()
                }else {
                    LoginApi(edt_userid?.text.toString(),edt_userpw?.text.toString())
                    Log.e("qwer123456","Login result : ${edt_userid?.text} // ${edt_userpw?.text}")
                }
            }

            // 계정찾기
            R.id.find_userinfo -> {
                //Toast.makeText(this,"계정찾기 기능은 아직 준비중입니다.",Toast.LENGTH_SHORT).show()
                go_find_userinfo = FindUserInfoDialog(this)
                go_find_userinfo?.start(get_device_size())

            }

            // 회원가입
            R.id.user_join -> {
                //Toast.makeText(this,"회원가입 기능은 아직 준비중입니다.",Toast.LENGTH_SHORT).show()
                go_user_join = UserJoinDialog(this)
                go_user_join?.start(get_device_size())

            }

        }

    }

    fun get_device_size(): Point {
        var display = windowManager.defaultDisplay
        var size = Point()
        display.getSize(size)

        return size
    }

    override fun onSuccess(message: String) {
        Toast.makeText(this,"Successful!!!!",Toast.LENGTH_SHORT).show()
    }

    override fun onFailure(message: String) {
        Toast.makeText(this,"Failed!!!!ㅠㅠ",Toast.LENGTH_SHORT).show()
    }

    fun LoginApi(userid : String, userpw : String ) {

        var longin_query = database!!.child("CUser").child("User_Info").orderByChild("user_id").equalTo(userid)

        longin_query.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.w("Login FireBaseData", "getData${dataSnapshot.children.count()}")
                if (dataSnapshot.children.count() > 0 ) {
                    for (childrenSnapshot in dataSnapshot.children) {
                        var post = childrenSnapshot.getValue(CUser::class.java)
                        Log.w("Login FireBaseData", "getData${post}")
                        if (post?.user_pw.equals(userpw)) {

                            val set_login_state = LoginState(post?.user_udid,post?.user_id,post?.user_name,post?.user_jumin)
                            database!!.child("LoginState").child(userid).setValue(set_login_state)
                            finish()
                           /* var main_intent = Intent(this@LoginActivity,MainActivity::class.java)
                                .putExtra("userid",post?.user_id)
                                .putExtra("userpw",post?.user_pw)
                            startActivity(main_intent)*/
                        }else {
                            Util.ShowAlertDialog(this@LoginActivity, "알림", "비밀번호가 일치하지 않습니다.")
                        }
                    }

                }else{
                    Util.ShowAlertDialog(this@LoginActivity, "알림", "일치하는 ID가 없습니다.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun play_background_video(){

        var getCall = RetroFitBuilder.api
        getCall.getVideoFilePath().enqueue(object : Callback<List<VideoInfo>> {
            override fun onResponse(call: Call<List<VideoInfo>>, response: Response<List<VideoInfo>>) {
                if (response.isSuccessful) {
                    Log.d("qwer123456", "비디오: ${response.body() as List<VideoInfo>} \n\n $response ")
                    var data : List<VideoInfo> = response.body()!!
                    Log.d("qwer1234567","비디오 데이터 갯수: ${data.size}")

                    //비디오 랜덤 함
                    var random: Random = Random
                    var random_position = 0

                    for (i in 0 until data.size) {
                        random_position = random.nextInt(data.size)
                    }
                    Log.d("qwer123456", "Video Info:  ${data[random_position].videopath} // ${data.size} // $random_position")

                    //var uri  = "${Contacts.baseurl}${data[random_position].videopath}"
                    var video_uri =  Util.getParseUri(data[random_position].videopath)
                    Log.d("qwer123456", "Play Video Uri:$video_uri")

                    mvideoview!!.setVideoURI(video_uri)
                    mvideoview!!.start()

                    mvideoview?.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
                        override fun onPrepared(mediaplayer: MediaPlayer?) {
                            mediaplayer?.isLooping = true
                            mediaplayer?.setVolume(0f, 0f)
                        }
                    })
                }else{
                    //실패처리
                    Toast.makeText(mContext,"영상을 가져오는중 오류가 발생하였습니다.",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<VideoInfo>>, t: Throwable) {
                Log.e("qwer123456","error: ${t.message}")
                t.printStackTrace()
            }
        })

      /*  var VideostorageRef = storage?.getReferenceFromUrl("gs://snsproject-fb39b.appspot.com/")
        var Video_listRef = storage?.reference?.child("Video")
        Video_listRef?.listAll()?.addOnSuccessListener { videodata ->

            var random : Random = Random
            var random_position = 0

            for (i in 0 until videodata.items.size){
                random_position = random.nextInt(videodata.items.size)
            }
            Log.d("qwer123456","영상 제목111:  ${videodata.items[random_position].name} // ${videodata.items.size} // $random_position")

            var ref = VideostorageRef?.child("Video/${videodata.items[random_position].name}")
            ref?.downloadUrl?.addOnSuccessListener { uri ->
                Log.d("qwer123456","영상 uri111:  ${uri}")
                mvideoview!!.setVideoURI(uri)
                mvideoview!!.start()
            }

        }?.addOnFailureListener { exception ->
            exception.printStackTrace()
        }

        mvideoview?.setOnPreparedListener(object : MediaPlayer.OnPreparedListener{
            override fun onPrepared(mediaplayer: MediaPlayer?) {
                mediaplayer?.isLooping = true
                mediaplayer?.setVolume(0f,0f)
            }
        })*/
    }


}