package com.dongdong.neonplayer.firebase

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

@IgnoreExtraProperties
data class CUser(
    var user_udid : String?=null,
    var user_name : String?=null,
    var user_jumin : String?=null,
    var user_age : Int = 0,
    var user_sex : String?=null,
    var user_id : String?=null,
    var user_pw : String?=null,
    var user_email : String?=null,
    var user_phonetype : String?=null,
    var user_phonenum : String?=null,
    var user_address : String?=null) {

}