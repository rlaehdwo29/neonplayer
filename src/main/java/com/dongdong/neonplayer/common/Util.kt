package com.dongdong.neonplayer.common

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import com.dongdong.neonplayer.activity.MusicPlayerActivity


class Util {
    companion object {

        private const val REQUEST_PHONE_STATE = 1
        val TAG = MusicPlayerActivity::class.java.simpleName
        fun getDeviceID(): String? {


            val deviceId = "35" + //we make this look like a valid IMEI
                    Build.BOARD.length % 10 +
                    Build.BRAND.length % 10 +
                    Build.CPU_ABI.length % 10 +
                    Build.DEVICE.length % 10 +
                    Build.DISPLAY.length % 10 +
                    Build.HOST.length % 10 +
                    Build.ID.length % 10 +
                    Build.MANUFACTURER.length % 10 +
                    Build.MODEL.length % 10 +
                    Build.PRODUCT.length % 10 +
                    Build.TAGS.length % 10 +
                    Build.TYPE.length % 10 +
                    Build.USER.length % 10

            return deviceId;

        }

        fun ShowAlertDialog(mContext : Context, title : String, message : String){
            var builder = AlertDialog.Builder(mContext)

            builder
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("확인") { dialog, id ->
                    dialog.dismiss()
                }
            var alertDialog = builder.create()
            alertDialog.show()
        }

        /**
         * String 형태로 URI 가공하기
         */
        fun getParseUri(data : String?) : Uri {
            return Uri.parse("${Contacts.baseurl}$data")
        }

        /**
         * 앨범 제목 아티스트 분해
         */
        fun getAlbumInfo(data : String?) : List<String>? {

            Log.d(TAG,"[getAlbumInfo] Data -> $data")

            var path_search : String? = ""

            if (data?.contains("/neon/music/todaynewmusic") == true){
                path_search = data?.replace("/neon/music/todaynewmusic/","")
                Log.d(TAG,"TodayNewMusic Replace -> $path_search")
            }
            if (data?.contains("/neon/music/newmusic") == true) {
                path_search = data?.replace("/neon/music/newmusic/","")
                Log.d(TAG,"NewMusic Replace -> $path_search")
            }
            if (data?.contains("/neon/music/newmusiconeweek") == true) {
                path_search = data?.replace("/neon/music/newmusiconeweek/","")
                Log.d(TAG,"NewMusicOneWeek Replace -:> $path_search")
            }
            if (data?.contains("/neon/music/neonchart") == true) {
                path_search = data?.replace("/neon/music/neonchart/","")
                Log.d(TAG,"NeonChaer Replace -> $path_search")
            }
            if (data?.contains("/neon/music/popmusic") == true) {
                path_search = data?.replace("/neon/music/popmusic/","")
                Log.d(TAG,"Pop Replace -> $path_search")
            }

            var albuminfo = path_search?.replace(".mp3","")
            var album_replace = albuminfo?.split(" - ")
            var album_title = album_replace?.get(1)
            var album_artist = album_replace?.get(0)

            return album_replace
        }


    }
}