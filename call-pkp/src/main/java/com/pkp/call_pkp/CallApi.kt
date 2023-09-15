package com.pkp.call_pkp

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.liveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.linphone.core.*

class CallApi(context: Context) : CallApiInterface {

    companion object {

        @Volatile
        private var instance: CallApi? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: CallApi(context).also { instance = it }
            }
    }

    private val _callStatus = MutableLiveData<CallStatus>()

    private val _registrationState = MutableLiveData<LoginResult>()

    val registrationState: Flow<LoginResult> = _registrationState.asFlow()

    val callStatus: Flow<CallStatus> = _callStatus.asFlow()

    private var core: Core

    private val coreListener = object : CoreListenerStub() {
        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState?,
            message: String
        ) {

            if (state == RegistrationState.Failed) {
                _registrationState.postValue(LoginResult.Error(message))

            } else if (state == RegistrationState.Ok) {
                _registrationState.postValue(LoginResult.Success(message))
            }
        }

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {
            // This function will be called each time a call state changes,
            // which includes new incoming/outgoing calls

            when (state) {
                Call.State.OutgoingInit -> {
                    // First state an outgoing call will go through
                    _callStatus.postValue(CallStatus.Loading(message))
                }

                Call.State.OutgoingProgress -> {
                    // Right after outgoing init
                }

                Call.State.OutgoingRinging -> {
                    // This state will be reached upon reception of the 180 RINGING
                }

                Call.State.Connected -> {
                    // When the 200 OK has been received
                }

                Call.State.StreamsRunning -> {
                    // This state indicates the call is active.
                    // You may reach this state multiple times, for example after a pause/resume
                    // or after the ICE negotiation completes
                    // Wait for the call to be connected before allowing a call update
                    _callStatus.postValue(CallStatus.Success(message))
                }

                Call.State.Paused -> {
                    // When you put a call in pause, it will became Paused
                    _callStatus.postValue(CallStatus.Paused(message))
                }

                Call.State.PausedByRemote -> {
                    // When the remote end of the call pauses it, it will be PausedByRemote
                }

                Call.State.Updating -> {
                    // When we request a call update, for example when toggling video
                }

                Call.State.UpdatedByRemote -> {
                    // When the remote requests a call update
                }

                Call.State.Released -> {
                    // Call state will be released shortly after the End state
                    _callStatus.postValue(CallStatus.Released(message))
                }

                Call.State.Error -> {
                    _callStatus.postValue(CallStatus.Error(message))
                    Log.e("ERROR", message)
                }

                Call.State.End -> {
                    _callStatus.postValue(CallStatus.End(message))
                }


                else -> {}
            }
        }
    }



    init {
        val factory = Factory.instance()
        factory.setDebugMode(true, "Hello Linphone")
        core = factory.createCore(null, null, context)
    }

    override fun login(
        domain: String?,
        username: String,
        password: String?,
        transportType: TransportTypeApi?
    ): LoginResult {

        try {
            val authInfo = Factory.instance()
                .createAuthInfo(username, null, password, null, null, domain, null)

            val params = core.createAccountParams()
            val identity = Factory.instance().createAddress("sip:$username@$domain")
            params.identityAddress = identity

            val address = Factory.instance().createAddress("sip:$domain")
            when (transportType) {
                TransportTypeApi.Udp -> address?.transport = TransportType.Udp
                TransportTypeApi.Tcp -> address?.transport = TransportType.Tcp
                TransportTypeApi.Tls -> address?.transport = TransportType.Tls
                else -> {}
            }
            params.serverAddress = address
            params.registerEnabled = true
            val account = core.createAccount(params)

            core.addAuthInfo(authInfo)
            core.addAccount(account)

            // Asks the CaptureTextureView to resize to match the captured video's size ratio
            core.config.setBool("video", "auto_resize_preview_to_keep_ratio", true)

            core.defaultAccount = account
            core.addListener(coreListener)
            core.start()
            return LoginResult.Success("Success")
        } catch (e: Exception) {
            return LoginResult.Error("Error")
        }

    }

    override fun outgoingCall(
        domain: String?,
        remote: String?
    ) {
        val remoteAddress = Factory.instance().createAddress("sip:$remote@$domain")
        remoteAddress
            ?: return // If address parsing fails, we can't continue with outgoing call process

        // We also need a CallParams object
        // Create call params expects a Call object for incoming calls, but for outgoing we must use null safely
        val params = core.createCallParams(null)
        params ?: return // Same for params

        // We can now configure it
        // Here we ask for no encryption but we could ask for ZRTP/SRTP/DTLS
        params.mediaEncryption = MediaEncryption.None
        // If we wanted to start the call with video directly
        //params.enableVideo(true)

        // Finally we start the call
        core.inviteAddressWithParams(remoteAddress, params)
        // Call process can be followed in onCallStateChanged callback from core listener
    }

    override fun hangup() {
        if (core.callsNb == 0) return

        // If the call state isn't paused, we can get it using core.currentCall
        val call = if (core.currentCall != null) core.currentCall else core.calls[0]
        call ?: return

        // Terminating a call is quite simple
        call.terminate()
    }

    override fun pauseOrResume() {
        if (core.callsNb == 0) return
        val call = if (core.currentCall != null) core.currentCall else core.calls[0]
        call ?: return

        if (call.state != Call.State.Paused && call.state != Call.State.Pausing) {
            // If our call isn't paused, let's pause it
            call.pause()
        } else if (call.state != Call.State.Resuming) {
            // Otherwise let's resume it
            call.resume()
        }
    }

    override suspend fun callStatusa(lifecycleOwner: LifecycleOwner): LiveData<CallStatus> =
        liveData {
            _callStatus.observe(
                lifecycleOwner
            ) {
                CoroutineScope(
                    Dispatchers.Unconfined
                ).launch {
                    emit(it)
                }
            }
//        emit(_callStatus.value!!)
        }

    override fun callStatus() = flow<CallStatus> {
        callStatus.collect{
            emit(it)
        }
    }

    override suspend fun registrationStatus(lifecycleOwner: LifecycleOwner): LiveData<LoginResult> =
        liveData (Dispatchers.Main){
            _registrationState.observe(
                lifecycleOwner
            ) {
                CoroutineScope(
                    Dispatchers.Unconfined
                ).launch {
                    emit(it)
                }
            }
//        emit(_callStatus.value!!)
        }

    override fun regisStatus() = flow<LoginResult> {
        registrationState.collect{
            emit(it)
        }
    }

}