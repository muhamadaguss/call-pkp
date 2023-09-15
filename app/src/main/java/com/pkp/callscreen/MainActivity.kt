package com.pkp.callscreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.pkp.call_pkp.CallApi
import com.pkp.call_pkp.CallApiInterface
import com.pkp.call_pkp.CallStatus
import com.pkp.call_pkp.LoginResult
import com.pkp.call_pkp.TransportTypeApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var callApi: CallApiInterface
    private var myJob: Job? = null
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        callApi = CallApi.getInstance(context = this)
        findViewById<Button>(R.id.hang_up).isEnabled = false
        findViewById<Button>(R.id.pause).isEnabled = false
        findViewById<Button>(R.id.toggle_camera).isEnabled = false
        findViewById<Button>(R.id.toggle_video).isEnabled = false

        findViewById<Button>(R.id.connect).setOnClickListener {
            val username = findViewById<EditText>(R.id.username).text.toString()
            val password = findViewById<EditText>(R.id.password).text.toString()
            val domain = findViewById<EditText>(R.id.domain).text.toString()
            val transportType =
                when (findViewById<RadioGroup>(R.id.transport).checkedRadioButtonId) {
                    R.id.udp -> TransportTypeApi.Udp
                    R.id.tcp -> TransportTypeApi.Tcp
                    else -> TransportTypeApi.Tls
                }
            callApi.login(domain, username, password, transportType)

        }

        lifecycleScope.launch {
            callApi.regisStatus().collect{
                registrationStatus ->
                when(registrationStatus){
                    is LoginResult.Success -> {
                        findViewById<TextView>(R.id.registration_status).text = registrationStatus.message
                        if (packageManager.checkPermission(Manifest.permission.RECORD_AUDIO, packageName) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 0)
                            return@collect
                        }
                        findViewById<Button>(R.id.connect).isEnabled = false
                        findViewById<LinearLayout>(R.id.register_layout).visibility = View.GONE
                        findViewById<RelativeLayout>(R.id.call_layout).visibility = View.VISIBLE
                    }
                    is LoginResult.Error ->{
                        findViewById<TextView>(R.id.registration_status).text = registrationStatus.errorMessage
                        findViewById<Button>(R.id.connect).isEnabled = true
                    }
                }
            }
        }

        lifecycleScope.launch {
            callApi.callStatus().collect {
                    callStatus ->
                when (callStatus) {
                    is CallStatus.Loading -> {
                        findViewById<TextView>(R.id.caller_id).text = "Ringing"
                        findViewById<Chronometer>(R.id.chronometer).visibility = View.VISIBLE
                        findViewById<Chronometer>(R.id.chronometer).visibility = View.GONE
                        findViewById<Chronometer>(R.id.chronometer).stop()
                    }
                    is CallStatus.Success -> {
                        findViewById<Chronometer>(R.id.chronometer).stop()
                        findViewById<TextView>(R.id.caller_id).visibility = View.GONE
                        findViewById<Chronometer>(R.id.chronometer).visibility = View.VISIBLE
                        findViewById<Chronometer>(R.id.chronometer).start()
                        findViewById<Button>(R.id.call).isEnabled = false
                        findViewById<Button>(R.id.pause).isEnabled = true
                        findViewById<Button>(R.id.pause).text = "Pause"
                        findViewById<Button>(R.id.hang_up).isEnabled = true
                        findViewById<EditText>(R.id.remote_address).isEnabled = false
                    }
                    is CallStatus.End -> {
                        callApi.hangup()
                        findViewById<TextView>(R.id.caller_id).visibility = View.GONE
                        findViewById<Chronometer>(R.id.chronometer).visibility = View.GONE
                        findViewById<Chronometer>(R.id.chronometer).stop()
                        findViewById<Button>(R.id.call).isEnabled = true
                        findViewById<Button>(R.id.pause).isEnabled = false
                        findViewById<Button>(R.id.hang_up).isEnabled = false
                        findViewById<EditText>(R.id.remote_address).isEnabled = true
                        findViewById<LinearLayout>(R.id.register_layout).visibility = View.GONE
                        findViewById<RelativeLayout>(R.id.call_layout).visibility = View.VISIBLE
                        findViewById<LinearLayout>(R.id.caller_layout).visibility = View.GONE
                    }
                    is CallStatus.Released ->{
                        callApi.hangup()
                        findViewById<TextView>(R.id.caller_id).visibility = View.GONE
                        findViewById<Chronometer>(R.id.chronometer).visibility = View.GONE
                        findViewById<Chronometer>(R.id.chronometer).stop()
                        findViewById<Button>(R.id.call).isEnabled = true
                        findViewById<Button>(R.id.pause).isEnabled = false
                        findViewById<Button>(R.id.hang_up).isEnabled = false
                        findViewById<EditText>(R.id.remote_address).isEnabled = true
                        findViewById<LinearLayout>(R.id.register_layout).visibility = View.GONE
                        findViewById<RelativeLayout>(R.id.call_layout).visibility = View.VISIBLE
                        findViewById<LinearLayout>(R.id.caller_layout).visibility = View.GONE
                    }

                    else -> {

                    }
                }
            }
        }


        findViewById<Button>(R.id.call).setOnClickListener {
            val domain = findViewById<EditText>(R.id.domain).text.toString()
            val remoteSipUri = findViewById<EditText>(R.id.remote_address).text.toString()
            findViewById<EditText>(R.id.remote_address).isEnabled = false
            findViewById<Button>(R.id.hang_up).isEnabled = true
            findViewById<LinearLayout>(R.id.register_layout).visibility = View.GONE
            findViewById<RelativeLayout>(R.id.call_layout).visibility = View.GONE
            findViewById<LinearLayout>(R.id.caller_layout).visibility = View.VISIBLE
            callApi.outgoingCall(domain,remoteSipUri)
            it.isEnabled = false
        }

        findViewById<Button>(R.id.endCallButton).setOnClickListener {
//            callApi.hangup()
            findViewById<Button>(R.id.call).isEnabled = true
            findViewById<Button>(R.id.pause).isEnabled = false
            findViewById<Button>(R.id.hang_up).isEnabled = false
            findViewById<EditText>(R.id.remote_address).isEnabled = true
            findViewById<LinearLayout>(R.id.register_layout).visibility = View.GONE
            findViewById<RelativeLayout>(R.id.call_layout).visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.caller_layout).visibility = View.GONE
        }
    }
}