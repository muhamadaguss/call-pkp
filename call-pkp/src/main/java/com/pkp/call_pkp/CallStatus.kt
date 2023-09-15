package com.pkp.call_pkp

sealed class CallStatus{
    data class Success(val message: String) : CallStatus()
    data class Error(val errorMessage: String) : CallStatus()
    data class  Loading(val message: String) : CallStatus()
    data class Paused(val message: String) : CallStatus()
    data class Released(val message: String) : CallStatus()
    data class End(val message: String) : CallStatus()
}
