package com.tgs.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
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
            val email = binding.email.text.toString().trim()
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
            .addOnSuccessListener { authResult ->
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                // 🔹 Move to MainActivity IMMEDIATELY
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()

                // 🔹 Update FCM Token in the background (after moving to MainActivity)
                val uid = authResult.user?.uid
                if (uid != null) {
                    updateFCMToken(uid)
                }
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

    private fun updateFCMToken(uid: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FirebaseDebug", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }

            val fcmtoken = task.result
            val database = FirebaseDatabase.getInstance().getReference("users")

            // 🔹 Update only the fcmtoken inside accountinfo
            database.child(uid).child("accountinfo").child("fcmtoken").setValue(fcmtoken)
                .addOnSuccessListener {
                    Log.d("FirebaseDebug", "FCM token updated successfully!")
                }
                .addOnFailureListener { exception ->
                    Log.e("FirebaseDebug", "Failed to update FCM token: ${exception.message}")
                }
        }
    }
}
