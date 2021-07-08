package com.dongdong.neonplayer.firebase

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class LoginState(
    var login_state_udid : String?=null,
    var login_state_id : String?=null,
    var login_state_name : String?=null,
    var login_state_jumin : String?=null
) {
}