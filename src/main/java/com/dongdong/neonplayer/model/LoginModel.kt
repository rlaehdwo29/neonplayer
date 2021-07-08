package com.dongdong.neonplayer.model

import android.text.TextUtils
import androidx.annotation.Nullable

class LoginModel {

    @Nullable
    var id: String? = null
    var password: String? = null

    constructor() {}

    constructor(id: String, password: String) {
        this.id = id
        this.password = password
    }

    fun isValid() : Boolean {
        return !TextUtils.isEmpty(id) && !TextUtils.isEmpty(password) && password?.length!! > 6
    }

}