package com.tgs.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tgs.app.data.model.Contact
import com.tgs.app.data.repository.ContactRepository
import com.tgs.app.databinding.ActivityContactsBinding
import com.tgs.app.databinding.EditContactBinding

class ContactsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactsBinding
    private lateinit var editBinding: EditContactBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var contactsAdapter: ContactsAdapter
    private val contactsList = mutableListOf<Contact>()
    private val contactRepository = ContactRepository()
    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showContactsScreen()

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
    }

    private fun showContactsScreen() {
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isEditing = false

        contactsAdapter = ContactsAdapter(contactsList, onEditClick = { contact ->
            showEditScreen(contact)
        }, onDelete = { contact ->
            deleteContact(contact)
        })

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

    private fun showEditScreen(contact: Contact) {
        editBinding = EditContactBinding.inflate(layoutInflater)
        setContentView(editBinding.root)
        isEditing = true

        editBinding.fullName.setText(contact.name)
        editBinding.phoneNumber.setText(contact.phonenumber)
        editBinding.doneBtn.tag = contact.id

        editBinding.backBtn.setOnClickListener {
            showContactsScreen()
        }

        editBinding.doneBtn.setOnClickListener {
            saveContactChanges()
        }
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

    private fun saveContactChanges() {
        val updatedName = editBinding.fullName.text.toString().trim()
        val updatedPhone = editBinding.phoneNumber.text.toString().trim()
        val contactId = editBinding.doneBtn.tag as? String

        if (contactId.isNullOrEmpty()) {
            Toast.makeText(this, "Contact ID is missing!", Toast.LENGTH_SHORT).show()
            return
        }

        if (updatedName.isEmpty() || updatedPhone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        contactRepository.updateContact(
            contactId = contactId,
            newName = updatedName,
            newPhone = updatedPhone,
            onSuccess = {
                Toast.makeText(this, "Contact updated!", Toast.LENGTH_SHORT).show()
                showContactsScreen()
                loadContacts()
            },
            onFailure = { error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun deleteContact(contact: Contact) {
        AlertDialog.Builder(this)
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete ${contact.name}?")
            .setPositiveButton("Yes") { _, _ ->
                contactRepository.deleteContact(contact,
                    onSuccess = {
                        contactsList.remove(contact)
                        contactsAdapter.notifyDataSetChanged()
                        Toast.makeText(this, "${contact.name} deleted", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
