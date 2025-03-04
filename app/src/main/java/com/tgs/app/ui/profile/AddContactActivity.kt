package com.tgs.app.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tgs.app.data.repository.ContactRepository
import com.tgs.app.databinding.ActivityAddContactBinding

class AddContactActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddContactBinding
    private val contactRepository = ContactRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }

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

        contactRepository.saveContact(
            name,
            phone,
            onSuccess = {
                Toast.makeText(this, "Contact Added!", Toast.LENGTH_SHORT).show()
                finish()
            },
            onFailure = { error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }
}
