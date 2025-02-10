package com.tgs.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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
            val username = binding.username.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                fetchEmailAndLogin(username, password)
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signupBTN.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun fetchEmailAndLogin(username: String, password: String) {
        val database = FirebaseDatabase.getInstance().getReference("Users")

        // Query the "Users" node using the username
        database.child(username).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val email = snapshot.child("email").value.toString()
                if (email.isNotEmpty()) {
                    loginWithEmail(email, password)
                } else {
                    Toast.makeText(this, "Email not found for this username", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Username not found", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch email", Toast.LENGTH_LONG).show()
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Login failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
