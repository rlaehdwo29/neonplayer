package com.dongdong.neonplayer.activity

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dongdong.neonplayer.R
import com.dongdong.neonplayer.room.AppDataBase


class SplashActivity : AppCompatActivity(){

    private val PERMISSIONS_REQUEST_RESULT = 1
    var handler = Handler()
    
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        handler?.postDelayed({
            checkForPhoneStatePermission()
        },1500)

    }

    private fun checkForPhoneStatePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_RESULT)
                    //showPermissionMessage()
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_RESULT)
                }
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_RESULT)
            }
        }
    }

    private fun showPermissionMessage() {
        AlertDialog.Builder(this)
            .setTitle("Read phone state")
            .setMessage("This app requires the permission to read phone state to continue")
            .setPositiveButton("Okay", DialogInterface.OnClickListener { dialog, which ->
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), PERMISSIONS_REQUEST_RESULT)
            }).create().show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            PERMISSIONS_REQUEST_RESULT -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    var intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), PERMISSIONS_REQUEST_RESULT)
                }
            }
        }

    }

    fun DB_Settings(){
        var db = AppDataBase.getInstance(this)                          //DB 생성

    }

}