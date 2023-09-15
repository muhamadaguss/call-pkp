package com.pkp.call_pkp

sealed class LoginResult{
    data class Success(val message: String) : LoginResult()
    data class Error(val errorMessage: String) : LoginResult()
}
