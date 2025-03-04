package com.tgs.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.tgs.app.ui.main.MainActivity
import com.tgs.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.loginBtn.setOnClickListener {
            val email = binding.username.text.toString().trim()  // Changed from username to email
            val password = binding.password.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginWithEmail(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signupBTN.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { exception ->
                Log.e("LoginError", "Login failed: ${exception.message}")

                when (exception) {
                    is FirebaseAuthInvalidCredentialsException ->
                        Toast.makeText(this, "Incorrect password", Toast.LENGTH_LONG).show()
                    is FirebaseAuthInvalidUserException ->
                        Toast.makeText(this, "User not found", Toast.LENGTH_LONG).show()
                    else ->
                        Toast.makeText(this, "Login failed. Please check your credentials and try again.", Toast.LENGTH_LONG).show()
                }
            }
    }
}
