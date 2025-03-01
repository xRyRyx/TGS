package com.tgs.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tgs.app.databinding.ActivityAccountBinding
import com.tgs.app.databinding.EditAccountBinding

class AccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountBinding
    private lateinit var editBinding: EditAccountBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        showViewMode()

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun showViewMode() {
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isEditing = false

        loadAccountInfo()

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.editBtn.setOnClickListener {
            showEditMode()
        }
    }

    private fun showEditMode() {
        editBinding = EditAccountBinding.inflate(layoutInflater)
        setContentView(editBinding.root)
        isEditing = true

        val user = auth.currentUser
        if (user != null) {
            val userRef: DatabaseReference = database.child("users").child(user.uid)

            userRef.child("accountinfo").child("fullname").get().addOnSuccessListener { snapshot ->
                editBinding.fullName.setText(snapshot.value?.toString() ?: "")
            }

            userRef.child("userinfo").child("phonenumber").get().addOnSuccessListener { snapshot ->
                editBinding.phoneNumber.setText(snapshot.value?.toString() ?: "")
            }

            userRef.child("accountinfo").child("email").get().addOnSuccessListener { snapshot ->
                editBinding.email.setText(snapshot.value?.toString() ?: "")
            }
        }

        editBinding.backBtn.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }

        editBinding.doneBtn.setOnClickListener {
            saveAccountInfo()
        }
    }

    private fun loadAccountInfo() {
        val user = auth.currentUser

        if (user != null) {
            val userRef: DatabaseReference = database.child("users").child(user.uid)

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

    private fun saveAccountInfo() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val newName = editBinding.fullName.text.toString().trim()
        val newPhone = editBinding.phoneNumber.text.toString().trim()

        if (newName.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf(
            "accountinfo/fullname" to newName,
            "userinfo/phonenumber" to newPhone
        )

        database.child("users").child(user.uid).updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
            showViewMode() // Go back to view mode after saving
        }.addOnFailureListener {
            Toast.makeText(this, "Update Failed!", Toast.LENGTH_SHORT).show()
        }
    }
}