package com.pkp.call_pkp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CallApiViewModel(application: Application): AndroidViewModel(application) {
    private val callApi: CallApiInterface = CallApi(application)

    private val _state = MutableSharedFlow<LoginResult>()
    val state: SharedFlow<LoginResult> = _state

    init {
        registrationStatus()
    }


    fun registrationStatus() {
        viewModelScope.launch {
            callApi.regisStatus().collect{
                registrationStatus ->
                when(registrationStatus){
                    is LoginResult.Success -> {
                        _state.emit(LoginResult.Success(registrationStatus.message))
                    }
                    is LoginResult.Error ->{
                        _state.emit(LoginResult.Error(registrationStatus.errorMessage))
                    }
                }
            }
        }
    }
}