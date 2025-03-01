package com.tgs.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tgs.app.databinding.ActivityAddDeviceBinding
import java.net.HttpURLConnection
import java.net.URL

class AddDeviceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDeviceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sendBtn.setOnClickListener{
            val ssid = binding.wifiName.text.toString()
            val password = binding.password.text.toString()

            sendWiFiCredentialsToArduino(ssid, password)
        }
    }

    private fun sendWiFiCredentialsToArduino(ssid: String, password: String) {
        val url = URL("http://192.168.4.1/send_wifi_credentials?ssid=$ssid&password=$password")

        // Make HTTP request to send credentials to Arduino
        val thread = Thread {
            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()
                connection.inputStream

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread.start()
    }
}