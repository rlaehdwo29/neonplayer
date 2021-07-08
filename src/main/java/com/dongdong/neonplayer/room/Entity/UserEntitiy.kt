package com.dongdong.neonplayer.room.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserEntitiy (
    var username : String = "",
    var userid : String = "",
    var userpw : String = "",
    var useremail : String = "",
    var userphonetype : String = "",
    var userphonenum : String = "",
    var useraddress : String = ""
){
    @PrimaryKey(autoGenerate = true) var uid : Int = 0
}