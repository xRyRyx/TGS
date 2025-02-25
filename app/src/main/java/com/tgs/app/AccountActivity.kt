package com.tgs.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tgs.app.databinding.ActivityAccountBinding

class AccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadAccountInfo()

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun loadAccountInfo() {
        val user = auth.currentUser

        if (user != null) {
            val userRef: DatabaseReference = database.getReference("users").child(user.uid)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val accountInfo = snapshot.child("accountinfo")
                    val userInfo = snapshot.child("userinfo")

                    val name = accountInfo.child("fullname").value?.toString() ?: ""
                    val phoneNumber = userInfo.child("phonenumber").value?.toString() ?: ""
                    val email = accountInfo.child("email").value?.toString() ?: ""

                    binding.fullName.setText(name)
                    binding.phoneNumber.setText(phoneNumber)
                    binding.email.setText(email)

                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}