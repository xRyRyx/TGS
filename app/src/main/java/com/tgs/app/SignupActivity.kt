package com.tgs.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tgs.app.data.User
import com.tgs.app.databinding.ActivitySignupBinding
class SignupActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignupBinding
    private lateinit var database : DatabaseReference
    private lateinit var email : EditText
    private lateinit var username : EditText
    private lateinit var password : EditText
    private lateinit var signupBtn : Button
    private lateinit var loginBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = binding.email
        username = binding.username
        password = binding.password
        signupBtn = binding.signupBtn
        loginBtn = binding.loginBtn

        signupBtn.setOnClickListener{
            val email = binding.email.text.toString()
            val username = username.text.toString()
            val password = password.text.toString()

            database = FirebaseDatabase.getInstance().getReference("Users")

            val user = User(email,username,password)
            database.child(username).setValue(user).addOnSuccessListener {
                binding.email.text.clear()
                binding.username.text.clear()
                binding.password.text.clear()

                Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }
        }

        loginBtn.setOnClickListener{
            val Intent = Intent(this, LoginActivity ::class.java)
            startActivity(Intent)
        }
    }
}