package com.dongdong.neonplayer.common

import android.provider.BaseColumns




class MyPlaylistContract {

    constructor()


    object MyPlaylistEntry : BaseColumns {
        const val TABLE_NAME = "MyPlaylist"
        const val COLUMN_NAME_PLAYLIST = "playlist_name"
        const val COLUMN_NAME_MUSIC_ID = "music_id"
        const val COLUMN_NAME_LAST_PLAYED_TIME = "last_played_time"
        const val COLUMN_NAME_PLAY_COUNT = "play_count"
        const val COLUMN_NAME_PLAYLIST_TYPE = "playlist_type"
    }

    object PlaylistNameEntry {
        // 사용자가 플레이어의 별 아이콘을 눌러 추가한 목록 == 즐겨찾기
        const val PLAYLIST_NAME_FAVORITE = "favorite"

        // 바로 이전에 앱을 종료할 당시의 플레이리스트
        const val PLAYLIST_NAME_LAST_PLAYED = "last_played"

        // 사용자 정의 플레이리스트
        const val PLAYLIST_NAME_USER_DEFINITION = "user_definition"
    }
}