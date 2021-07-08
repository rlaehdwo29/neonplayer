package com.dongdong.neonplayer.common

import android.R
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.dongdong.neonplayer.activity.MusicPlayerActivity
import com.dongdong.neonplayer.data.*
import com.dongdong.neonplayer.room.AppDataBase
import com.dongdong.neonplayer.room.Entity.MyMusicPlayListEntity
import com.google.common.eventbus.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class MusicInfoLoadUtil {
    companion object {
        private val TAG = MusicInfoLoadUtil::class.java.simpleName
        /**
         * 나의 플레이 리스트 전부 가져오기
         */
        fun myPlayListAll(context: Context) : ArrayList<MyPlayListData>? {
            var arrayList : ArrayList<MyPlayListData>? = null
            var music_Alldata : List<MyMusicPlayListEntity>
            var muserid : String?
            var muid : Int
            var mtitle : String?
            var martist : String?
            var muri : String?

            runBlocking {
                var db = Room.databaseBuilder(context, AppDataBase::class.java, "playmusic_data").build()
                music_Alldata = db.MyMusicPlayListDao().getAllMyPlayListRx(Contacts.Admin_UserID)
                arrayList = ArrayList()
                for (i in music_Alldata?.indices!!) {
                    Log.d(TAG,"GET Saved My PlayList: $i 번째 음악->[USERID] ${music_Alldata[i].userid} [UID]${music_Alldata[i].album_uid} [TITLE] ${music_Alldata[i].music_title} [ARTIST] ${music_Alldata[i].music_artist} [URI] ${music_Alldata[i].music_uri}")
                    muserid = music_Alldata[i].userid
                    muid = music_Alldata[i].album_uid
                    mtitle = music_Alldata[i].music_title
                    martist = music_Alldata[i].music_artist
                    muri = music_Alldata[i].music_uri
                    if (muserid != null && muid != null && mtitle != null && martist != null && muri != null) {
                        arrayList?.add(MyPlayListData(muid,muserid,mtitle,martist,muri))
                    }
                }
            }
            return arrayList
        }

        /**
         * 음악 UID 가져오기
         */

        fun getMusicUID(context: Context, title: String?) : MusicUIDData? {
            var mMusicUIDInfo : MusicUIDData? = null
            var music_uri = "${Contacts.baseurl}$title"
            var select_music_data : MyMusicPlayListEntity
            var muid : Int?

            var db = Room.databaseBuilder(context, AppDataBase::class.java, "playmusic_data").build()
            Log.d(TAG," [getMusicUID] Set Query Data -> ${Contacts.Admin_UserID}//${ Util.getAlbumInfo(title)?.get(1)!!}//${Util.getAlbumInfo(title)?.get(0)!!}//$music_uri")

            runBlocking {

                select_music_data = db.MyMusicPlayListDao().getPlayDataUID(Contacts.Admin_UserID, Util.getAlbumInfo(title)?.get(1)!!, Util.getAlbumInfo(title)?.get(0)!!, music_uri)
                muid = select_music_data?.album_uid

                Log.d(TAG, "[getMusicUID] Select Room DataBase UID Result -> [UID] $muid")
                if (muid != null) {
                    mMusicUIDInfo = MusicUIDData(muid)
                }
            }

            Log.d(TAG, "[getMusicUID] Selected UID -> [UID] ${mMusicUIDInfo?.album_uid}")
            return mMusicUIDInfo
        }

        /**
         * 선택한 음악 정보 불러오기
         */

        fun getSelectedMusicInfo(context: Context, title: String?) : PlayMusicInfo? {
            var mMusicInfo : PlayMusicInfo? = null
            var music_uri = "${Contacts.baseurl}$title"
            var select_music_data : MyMusicPlayListEntity
            var muserid : String?
            var muid : Int?
            var mtitle : String?
            var martist : String?
            var muri : String?

            Log.d(TAG,"[getSelectedMusicInfo] Get Title -> $title")
                var db = Room.databaseBuilder(context, AppDataBase::class.java, "playmusic_data").build()
                Log.d(TAG,"[getSelectedMusicInfo] Set Query Data-> ${Contacts.Admin_UserID}//${ Util.getAlbumInfo(title)?.get(1)!!}//${Util.getAlbumInfo(title)?.get(0)!!}//$music_uri")

            runBlocking {

                    select_music_data = db.MyMusicPlayListDao().getSelectPlayData(Contacts.Admin_UserID, Util.getAlbumInfo(title)?.get(1)!!, Util.getAlbumInfo(title)?.get(0)!!, music_uri)
                    muserid = select_music_data?.userid
                    muid = select_music_data?.album_uid
                    mtitle = select_music_data?.music_title
                    martist = select_music_data?.music_artist
                    muri = select_music_data?.music_uri

                    Log.d(TAG, "[getSelectedMusicInfo] Select Room DataBase Result -> \n [USERID] $muserid \n [UID] $muid \n [TITLE] $mtitle \n [ARTIST] $martist \n [URI] $muri")
                    if (muserid != null && muid != null && mtitle != null && martist != null && muri != null) {
                        mMusicInfo = PlayMusicInfo(muid, muserid, mtitle, martist, muri)
                    }
            }

            Log.d(TAG, "[getSelectedMusicInfo] Selected Data -> \n [USERID] ${mMusicInfo?.album_uid} \n [UID] ${mMusicInfo?.userid} \n [TITLE] ${mMusicInfo?.music_title} \n [ARTIST] ${mMusicInfo?.music_artist} \n [URI] ${mMusicInfo?.music_uri}")
            return mMusicInfo
         }

        /**
         * 선택한 음악 정보 불러오기
         */

        fun getSelectedMusicInfo(context: Context, title: String?,artist : String?, uri : String?) : PlayMusicInfo? {
            var mMusicInfo : PlayMusicInfo? = null
            var music_uri = "${Contacts.baseurl}$title"
            var select_music_data : MyMusicPlayListEntity
            var muserid : String?
            var muid : Int?
            var mtitle : String?
            var martist : String?
            var muri : String?

            Log.d(TAG,"[getSelectedMusicInfo2] Get Title -> \n [TITLE] $title \n  [ARTIST] $artist \n [URI] $uri")
            var db = Room.databaseBuilder(context, AppDataBase::class.java, "playmusic_data").build()

            runBlocking {

                select_music_data = db.MyMusicPlayListDao().getSelectPlayData(Contacts.Admin_UserID, title!!, artist!!, uri!!)
                muserid = select_music_data?.userid
                muid = select_music_data?.album_uid
                mtitle = select_music_data?.music_title
                martist = select_music_data?.music_artist
                muri = select_music_data?.music_uri

                Log.d(TAG, "[getSelectedMusicInfo2] Select Room DataBase Result -> \n [USERID] $muserid \n [UID] $muid \n [TITLE] $mtitle \n [ARTIST] $martist \n [URI] $muri")
                if (muserid != null && muid != null && mtitle != null && martist != null && muri != null) {
                    mMusicInfo = PlayMusicInfo(muid, muserid, mtitle, martist, muri)
                }
            }

            Log.d(TAG, "[getSelectedMusicInfo2] Selected Data -> \n [USERID] ${mMusicInfo?.album_uid} \n [UID] ${mMusicInfo?.userid} \n [TITLE] ${mMusicInfo?.music_title} \n [ARTIST] ${mMusicInfo?.music_artist} \n [URI] ${mMusicInfo?.music_uri}")
            return mMusicInfo
        }

        /**
         * 이미지 설정
         */
        fun getAlbumImage(context: Context, uri: Uri?, quality: Int): Bitmap? {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val albumArt = retriever.embeddedPicture

            // Bitmap 샘플링
            val options = BitmapFactory.Options()
            options.inSampleSize = quality // 2의 배수
            val bitmap: Bitmap
            bitmap = if (null != albumArt) {
                BitmapFactory.decodeByteArray(albumArt, 0, albumArt.size, options)
            } else {
                BitmapFactory.decodeResource(context.resources, R.mipmap.sym_def_app_icon)
            }

            // id 로부터 bitmap 생성
            return bitmap
        }

    }
}