package com.dongdong.neonplayer.retrofit

import com.dongdong.neonplayer.`interface`.API
import com.dongdong.neonplayer.common.Contacts
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetroFitBuilder {

    var api : API

    init {
        var gson = GsonBuilder().setLenient().create()

        val retorfit = Retrofit.Builder()
            .baseUrl(Contacts.baseurl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        api = retorfit.create(API::class.java)
    }
    
}