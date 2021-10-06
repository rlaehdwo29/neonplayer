package com.dongdong.neonplayer.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NetWorkState {

    companion object {
        const val TYPE_WIFI = 100
        const val TYPE_MOBILE = 101
        const val TYPE_NO_NETWORK = 102


        fun getNetWorkState(context : Context) : Int {
            var manager : ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            var networkInfo : NetworkInfo = manager.activeNetworkInfo
            if (networkInfo != null) {
                var network_type = networkInfo.type
                if (network_type == ConnectivityManager.TYPE_WIFI) {
                    return TYPE_WIFI
                }else if(network_type == ConnectivityManager.TYPE_MOBILE) {
                    return TYPE_MOBILE
                }
            }
            return TYPE_NO_NETWORK
        }

    }



}