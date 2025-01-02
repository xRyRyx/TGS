package com.tgs.app

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tgs.app.data.User
import com.tgs.app.databinding.ActivitySignupBinding
class SignupActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignupBinding
    private lateinit var database : DatabaseReference
    lateinit var email : EditText
    lateinit var username : EditText
    lateinit var password : EditText
    lateinit var signupBtn : Button
    lateinit var loginBtn : Button

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

            val User = User(email,username,password)
            database.child(username).setValue(User).addOnSuccessListener {
                binding.email.text.clear()
                binding.username.text.clear()
                binding.password.text.clear()

                Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}