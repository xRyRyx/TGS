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
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

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

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()

                // update fcmtoken
                val uid = authResult.user?.uid
                if (uid != null) {
                    updateFCMToken(uid)
                    sendUIDToNodeMCU(uid)  // Send the UID and emergency contacts to NodeMCU
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

            database.child(uid).child("accountinfo").child("fcmtoken").setValue(fcmtoken)
                .addOnSuccessListener {
                    Log.d("FirebaseDebug", "FCM token updated successfully!")
                }
                .addOnFailureListener { exception ->
                    Log.e("FirebaseDebug", "Failed to update FCM token: ${exception.message}")
                }
        }
    }

    private fun sendUIDToNodeMCU(uid: String) {
        // Reference to the Firebase Database
        val database = FirebaseDatabase.getInstance().getReference("users")

        // Fetch emergency contacts from Firebase
        database.child(uid).child("emergencycontacts").get().addOnSuccessListener { snapshot ->
            val emergencyContacts = mutableListOf<Map<String, String>>()

            // Iterate over the contacts and collect the name and phone number
            snapshot.children.forEach { contactSnapshot ->
                val name = contactSnapshot.child("name").getValue(String::class.java) ?: ""
                val phoneNumber = contactSnapshot.child("phonenumber").getValue(String::class.java) ?: ""
                if (name.isNotEmpty() && phoneNumber.isNotEmpty()) {
                    emergencyContacts.add(mapOf("name" to name, "phonenumber" to phoneNumber))
                }
            }

            // Prepare the data to send (both UID and emergency contacts)
            val data = mapOf(
                "uid" to uid,
                "emergencyContacts" to emergencyContacts
            )

            // Convert the data to a JSON string
            val jsonData = JSONObject(data).toString()

            // Send the UID and emergency contacts to the NodeMCU
            val url = "http://192.168.100.18/uid"
            val client = OkHttpClient()

            val request = Request.Builder()
                .url(url)
                .post(RequestBody.create("application/json".toMediaTypeOrNull(), jsonData))
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Failed to connect to device!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.d("FirebaseDebug", "UID and emergency contacts sent successfully!")
                }
            })
        }.addOnFailureListener {
            Log.e("FirebaseDebug", "Failed to fetch emergency contacts: ${it.message}")
        }
    }
}