package com.dongdong.neonplayer.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dongdong.neonplayer.callback.LoginCallbacks
import com.dongdong.neonplayer.viewmodel.LoginViewModel

class LoginViewModelFactory : ViewModelProvider.NewInstanceFactory {

    var loginCallbacks : LoginCallbacks

    constructor(loginCallbacks: LoginCallbacks) {
        this.loginCallbacks = loginCallbacks
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LoginViewModel(loginCallbacks) as T
    }


}