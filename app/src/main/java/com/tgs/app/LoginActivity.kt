package com.tgs.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tgs.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    lateinit var username : EditText
    lateinit var password : EditText
    lateinit var loginBtn : Button
    lateinit var signupBtn : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        username = binding.username
        password = binding.password
        loginBtn = binding.loginBtn
        signupBtn = binding.signupBtn

        loginBtn.setOnClickListener{
            val username = username.text.toString()
            val password = password.text.toString()

            Log.i("Test Credentials", "Username : $username and Password : $password")

        }

        signupBtn.setOnClickListener{
            val Intent = Intent(this, SignupActivity ::class.java)
            startActivity(Intent)
        }

    }
}