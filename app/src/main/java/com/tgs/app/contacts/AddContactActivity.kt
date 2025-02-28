package com.tgs.app.contacts

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tgs.app.databinding.ActivityAddContactBinding

class AddContactActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddContactBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.saveBtn.setOnClickListener {
            saveContact()
        }
    }

    private fun saveContact() {
        val name = binding.name.text.toString()
        val phone = binding.phoneNumber.text.toString()

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val contactId = database.child("users").child(userId)
            .child("emergencycontacts").push().key

        if (contactId == null) {
            Toast.makeText(this, "Failed to generate contact ID!", Toast.LENGTH_SHORT).show()
            return
        }

        val newContact = mapOf(
            "name" to name,
            "phonenumber" to phone
        )

        database.child("users").child(userId)
            .child("emergencycontacts").child(contactId)
            .setValue(newContact)
            .addOnSuccessListener {
                Toast.makeText(this, "Contact Added!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show()
            }
    }
}
