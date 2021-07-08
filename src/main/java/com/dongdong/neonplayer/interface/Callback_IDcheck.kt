package com.dongdong.neonplayer.`interface`

import com.google.android.gms.common.internal.ConnectionErrorMessages

interface Callback_IDcheck {

    fun success(data : String)
    fun fail(errorMessages : String)

}