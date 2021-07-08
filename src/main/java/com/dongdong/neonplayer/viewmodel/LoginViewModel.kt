package com.dongdong.neonplayer.viewmodel

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.ViewModel
import com.dongdong.neonplayer.callback.LoginCallbacks
import com.dongdong.neonplayer.model.LoginModel

class LoginViewModel : ViewModel {
    private var loginModel: LoginModel? = null
    private var loginCallbacks: LoginCallbacks? = null

    constructor(loginCallbacks: LoginCallbacks?) {
        this.loginCallbacks = loginCallbacks
        loginModel = LoginModel()
    }

    fun IdTextWacher(): TextWatcher? {
        return object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun afterTextChanged(editable: Editable) {
                loginModel?.id = editable.toString()
            }
        }
    }

    fun passTextWacher(): TextWatcher? {
        return object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun afterTextChanged(editable: Editable) {
                loginModel?.password = editable.toString()
            }
        }
    }

    fun onLoginBtnClick(view: View?) {
        if (loginModel?.isValid()!!) {
            loginCallbacks?.onSuccess("Successful")
        } else {
            loginCallbacks?.onFailure("failed")
        }
    }

}