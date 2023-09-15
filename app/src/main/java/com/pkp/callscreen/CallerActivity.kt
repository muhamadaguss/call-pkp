package com.pkp.callscreen

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.lifecycleScope
//import com.pkp.call_pkp.CallApi
//import com.pkp.call_pkp.CallApiInterface
//import com.pkp.call_pkp.CallStatus
import kotlinx.coroutines.launch

class CallerActivity : AppCompatActivity() {

//    private lateinit var callApi: CallApiInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
//        callApi = CallApi.getInstance(this)
        val endCallButton = findViewById<Button>(R.id.endCallButton)
        val chronometer = findViewById<Chronometer>(R.id.chronometer)
        val intent = intent
        val remoteSipUri = intent.getStringExtra("name")
        val domain = intent.getStringExtra("domain")
        findViewById<TextView>(R.id.caller_id).text = "ringing"
//        callApi.outgoingCall(domain, remoteSipUri)
//        endCallButton.setOnClickListener {
//            callApi.hangup()
//            chronometer.stop()
//            val intent = Intent(this, MainActivity::class.java)
//            intent.putExtra("callscreen",true)
//            startActivity(intent)
//
//        }

        findViewById<Chronometer>(R.id.chronometer).visibility = View.GONE

//        lifecycleScope.launch {
//            callApi.callStatus().collect {
//                    callStatus ->
//                when (callStatus) {
//                    is CallStatus.Success -> {
//                        findViewById<Chronometer>(R.id.chronometer).visibility = View.VISIBLE
//                        chronometer.start()
////                        findViewById<Button>(R.id.call).isEnabled = false
////                        findViewById<Button>(R.id.pause).isEnabled = true
////                        findViewById<Button>(R.id.pause).text = "Pause"
//                    }
//                    is CallStatus.End -> {
//                        callApi.hangup()
//                        chronometer.stop()
//                        val intent = Intent(this@CallerActivity, MainActivity::class.java)
//                        intent.putExtra("callscreen",true)
//                        startActivity(intent)
//                    }
//                    is CallStatus.Released ->{
//                        callApi.hangup()
//                        chronometer.stop()
//                        val intent = Intent(this@CallerActivity, MainActivity::class.java)
//                        intent.putExtra("callscreen",true)
//                        startActivity(intent)
//                    }
//
//                    else -> {
//
//                    }
//                }
//            }
//        }
    }
}