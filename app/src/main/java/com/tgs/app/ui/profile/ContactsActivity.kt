package com.tgs.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.tgs.app.data.model.Contact
import com.tgs.app.data.repository.ContactRepository
import com.tgs.app.databinding.ActivityContactsBinding

class ContactsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactsBinding
    private lateinit var contactsAdapter: ContactsAdapter
    private val contactsList = mutableListOf<Contact>()
    private val contactRepository = ContactRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contactsAdapter = ContactsAdapter(contactsList)
        binding.contactsRV.apply {
            layoutManager = LinearLayoutManager(this@ContactsActivity)
            adapter = contactsAdapter
        }

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.addBtn.setOnClickListener {
            val intent = Intent(this, AddContactActivity::class.java)
            startActivity(intent)
        }

        loadContacts()
    }

    private fun loadContacts() {
        contactRepository.loadContacts(
            onSuccess = { contacts ->
                contactsList.clear()
                contactsList.addAll(contacts)
                contactsAdapter.notifyDataSetChanged()
            },
            onFailure = { error ->
                Log.e("ContactsActivity", error)
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }
}
