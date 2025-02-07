package com.tgs.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.tgs.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private lateinit var username : EditText
    private lateinit var password : EditText
    private lateinit var loginBtn : Button
    private lateinit var signupBtn : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        username = binding.username
        password = binding.password
        loginBtn = binding.loginBtn
        //signupBtn = binding.signupBtn

        loginBtn.setOnClickListener{
            val username = username.text.toString()
            val password = password.text.toString()

            Log.i("Test Credentials", "Username : $username and Password : $password")

        }

        signupBtn.setOnClickListener{
            val intent = Intent(this, SignupActivity :: class.java)
            startActivity(intent)
        }

    }
}