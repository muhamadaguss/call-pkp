package com.pkp.call_pkp

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import org.linphone.core.*

interface CallApiInterface {
    fun login (domain: String?,
             username: String,
             password: String?,
             transportType: TransportTypeApi?
             ) : LoginResult

    fun outgoingCall(
        domain: String?,
        remote: String?,
    )

    fun hangup()

    fun pauseOrResume()

    suspend fun callStatusa(lifecycleOwner: LifecycleOwner) : LiveData<CallStatus>

    suspend fun registrationStatus(lifecycleOwner: LifecycleOwner) : LiveData<LoginResult>

    fun regisStatus() : Flow<LoginResult>

    fun callStatus() : Flow<CallStatus>
}