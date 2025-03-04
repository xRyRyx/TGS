package com.tgs.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tgs.app.data.repository.UserRepository
import com.tgs.app.databinding.ActivityAccountBinding
import com.tgs.app.databinding.EditAccountBinding

class AccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountBinding
    private lateinit var editBinding: EditAccountBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var isEditing = false
    private val userRepository = UserRepository()

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

        userRepository.loadAccountData(
            onSuccess = { name, phoneNumber, email ->
                binding.fullName.setText(name)
                binding.phoneNumber.setText(phoneNumber)
                binding.email.setText(email)
            },
            onFailure = { error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )

        editBinding.backBtn.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }

        editBinding.doneBtn.setOnClickListener {
            saveAccountInfo()
        }
    }

    private fun loadAccountInfo() {
        userRepository.loadAccountData(
            onSuccess = { name, phoneNumber, email ->
                binding.fullName.setText(name)
                binding.phoneNumber.setText(phoneNumber)
                binding.email.setText(email)
            },
            onFailure = { error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun saveAccountInfo() {
        val newName = editBinding.fullName.text.toString().trim()
        val newPhone = editBinding.phoneNumber.text.toString().trim()

        if (newName.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        userRepository.saveAccountData(
            newName, newPhone,
            onSuccess = {
                Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
                showViewMode()
            },
            onFailure = { error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }
}