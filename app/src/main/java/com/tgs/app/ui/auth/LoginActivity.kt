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
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private val client = OkHttpClient()  // Ensure a single OkHttpClient instance

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

                val uid = authResult.user?.uid
                if (uid != null) {
                    updateFCMToken(uid)
                    sendUIDToNodeMCU(uid)  // Send UID and emergency contacts
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
                        Toast.makeText(this, "Login failed. Please check your credentials.", Toast.LENGTH_LONG).show()
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
        val database = FirebaseDatabase.getInstance().getReference("users")

        database.child(uid).child("emergencycontacts").get().addOnSuccessListener { snapshot ->
            val emergencyContactsArray = JSONArray()

            snapshot.children.forEach { contactSnapshot ->
                val name = contactSnapshot.child("name").getValue(String::class.java) ?: ""
                val phoneNumber = contactSnapshot.child("phonenumber").getValue(String::class.java) ?: ""
                if (name.isNotEmpty() && phoneNumber.isNotEmpty()) {
                    val contact = JSONObject().apply {
                        put("name", name)
                        put("phonenumber", phoneNumber)
                    }
                    emergencyContactsArray.put(contact)
                }
            }

            val jsonData = JSONObject().apply {
                put("uid", uid)
                put("emergencyContacts", emergencyContactsArray)
            }.toString()

            Log.d("NodeMCU", "Sending JSON: $jsonData")

            val requestBody = jsonData.toRequestBody("application/json".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("http://192.168.100.18/uid")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("NodeMCU", "Failed to send UID: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    Log.d("NodeMCU", "Response: ${response.code}, Body: $responseBody")
                }
            })
        }.addOnFailureListener {
            Log.e("FirebaseDebug", "Failed to fetch emergency contacts: ${it.message}")
        }
    }
}