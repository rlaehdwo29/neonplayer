package com.dongdong.neonplayer.common

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper : SQLiteOpenHelper {

companion object{
    const val DATABASE_NAME = "EasyMusic.db"
    const val DATABASE_VERSION = 1

    private val SQL_CREATE_MY_PLAYLIST =
        "CREATE TABLE IF NOT EXISTS " + MyPlaylistContract.MyPlaylistEntry.TABLE_NAME + " (" +
               // MyPlaylistContract.MyPlaylistEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST + " TEXT NOT NULL , " +
                MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID + " INTEGER NOT NULL, " +
                MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST_TYPE + " TEXT NOT NULL " +
                ");"

    private var sSingleton: DBHelper? = null


    @Synchronized
    fun getInstance(context: Context?): DBHelper? {
        if (sSingleton == null) {
            sSingleton = DBHelper(context!!)
        }
        return sSingleton
    }
}

    constructor(context : Context):super(context, DATABASE_NAME,null, DATABASE_VERSION){

    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_MY_PLAYLIST)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }


}