package com.dongdong.neonplayer.common

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement


class DataBackupUtil {

    companion object {

        private val TAG = DataBackupUtil::class.java.simpleName
        private var sInstance: DataBackupUtil? = null
        private var mContext: Context? = null
        private var mSharedPreferences: SharedPreferences? = null
        private val PREFERENCES_NAME = "LastPlayedSongs"

        var projection = arrayOf<String>(
            //MyPlaylistContract.MyPlaylistEntry._ID,
            MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST,
            MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID
        )

        var selection: String = MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST.toString() + "=?"
        var selectionArgs = arrayOf<String>(MyPlaylistContract.PlaylistNameEntry.PLAYLIST_NAME_LAST_PLAYED)

        @Synchronized
        fun getInstance(context: Context): DataBackupUtil? {
            if (sInstance == null) {
                sInstance = DataBackupUtil()
                mContext = context
                mSharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            }
            return sInstance
        }

    }
    private var mDbHelper: DBHelper? = null

    constructor(){}

    fun saveIsShuffle(isShuffle: Boolean) {
        val editor = mSharedPreferences!!.edit()
        editor.putBoolean("shuffle", isShuffle).commit()
    }

    fun loadIsShuffle(): Boolean {
        return mSharedPreferences!!.getBoolean("shuffle", false)
    }

    fun saveIsRepeat(isRepeat: Boolean) {
        val editor = mSharedPreferences!!.edit()
        editor.putBoolean("repeat", isRepeat).commit()
    }

    fun loadIsRepeat(): Boolean {
        return mSharedPreferences!!.getBoolean("repeat", false)
    }

    /**
     * 현재 플레이 중인 플레이리스트 상에서의 플레이 포지션을 저장한다.
     */
    fun saveCurrentPlayingMusicPosition(position: Int) {
        val editor = mSharedPreferences!!.edit()
        editor.putInt("position", position).commit()
    }

    /**
     * 프리퍼런스에 위치 값이 존재하면 위치 값을, 없다면 -1을 리턴한다.
     *
     * @return
     */
    fun loadCurrentPlayingMusicPosition(): Int {
        val position = mSharedPreferences!!.getInt("position", -1)
        val editor = mSharedPreferences!!.edit()
        editor.remove("position").commit()
        return position
    }

    /**
     * 현재 플레이 중인 플레이리스트를 DB에 저장한다.
     *
     * @param currentPlaylist
     */
    fun saveCurrentPlaylist(currentPlaylist: ArrayList<Long>?) {
        mDbHelper = DBHelper.getInstance(mContext)
        var db: SQLiteDatabase? = null
        val statement: SQLiteStatement
        try {
            if (currentPlaylist != null && currentPlaylist.size > 0) {
                db = mDbHelper?.writableDatabase
                db?.beginTransaction()
                statement = db!!.compileStatement(
                    "INSERT INTO " + MyPlaylistContract.MyPlaylistEntry.TABLE_NAME.toString() + " ( " +
                            MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST.toString() + " , " +
                            MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID.toString() + " , " +
                            MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST_TYPE.toString() + " ) " +
                            "values(?, ?, ?)"
                )
                for (id: Long in currentPlaylist) {
                    var column = 1
                    statement.bindString(column++, MyPlaylistContract.PlaylistNameEntry.PLAYLIST_NAME_LAST_PLAYED)
                    statement.bindLong(column++, id)
                    statement.bindString(column++, MyPlaylistContract.PlaylistNameEntry.PLAYLIST_NAME_LAST_PLAYED)
                    statement.execute()
                }
                statement.close()
                db?.setTransactionSuccessful()
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } finally {
            db?.endTransaction()
        }
    }

    /**
     * DB에 저장된 최종 플레이한 노래들을 불러오고, 그 즉시 기존의 데이터를 삭제한다.
     *
     * @return
     */
    fun loadLastPlayedSongs(): ArrayList<Long>? {
        mDbHelper = DBHelper.getInstance(mContext)
        val db: SQLiteDatabase = mDbHelper!!.writableDatabase
        val cursor: Cursor = db.query(
            true,
            MyPlaylistContract.MyPlaylistEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null,
            null
        )
        val lastPlayedSongs: ArrayList<Long> = ArrayList()
        while (cursor.moveToNext()) {
            lastPlayedSongs.add(cursor.getLong(cursor.getColumnIndexOrThrow(MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID)))
        }
        cursor.close()
        db.delete(MyPlaylistContract.MyPlaylistEntry.TABLE_NAME, selection, selectionArgs)
        return lastPlayedSongs
    }

}