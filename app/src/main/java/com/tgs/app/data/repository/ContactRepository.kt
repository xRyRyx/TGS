package com.tgs.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tgs.app.data.model.Contact

class ContactRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun saveContact(name: String, phone: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onFailure("User not logged in!")
            return
        }

        val contactId = database.child("users").child(userId)
            .child("emergencycontacts").push().key

        if (contactId == null) {
            onFailure("Failed to generate contact ID!")
            return
        }

        val newContact = mapOf(
            "name" to name,
            "phonenumber" to phone
        )

        database.child("users").child(userId)
            .child("emergencycontacts").child(contactId)
            .setValue(newContact)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Unknown error") }
    }

    fun loadContacts(onSuccess: (List<Contact>) -> Unit, onFailure: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onFailure("User not logged in!")
            return
        }

        val contactsRef = database.child("users").child(userId).child("emergencycontacts")

        contactsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contactsList = mutableListOf<Contact>()
                for (contactSnapshot in snapshot.children) {
                    val contact = contactSnapshot.getValue(Contact::class.java)
                    contact?.let { contactsList.add(it) }
                }
                onSuccess(contactsList)
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure("Failed to load contacts: ${error.message}")
            }
        })
    }
}
