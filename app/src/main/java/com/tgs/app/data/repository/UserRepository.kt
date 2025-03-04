package com.tgs.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun loadProfileData(
        onSuccess: (String, String, String, String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onFailure("User not logged in")
            return
        }

        val userRef = database.child("users").child(user.uid)

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val accountInfo = snapshot.child("accountinfo")
                val userInfo = snapshot.child("userinfo").child("address")

                val name = accountInfo.child("fullname").value?.toString() ?: ""
                val email = accountInfo.child("email").value?.toString() ?: ""
                val city = userInfo.child("city").value?.toString() ?: ""
                val province = userInfo.child("province").value?.toString() ?: ""

                onSuccess(name, email, city, province)
            } else {
                onFailure("User data not found")
            }
        }.addOnFailureListener {
            onFailure("Failed to load data: ${it.message}")
        }
    }

    fun loadAccountData(onSuccess: (String, String, String) -> Unit, onFailure: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val userRef: DatabaseReference = database.child("users").child(user.uid)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val name = snapshot.child("accountinfo/fullname").value?.toString() ?: ""
                    val phoneNumber = snapshot.child("userinfo/phonenumber").value?.toString() ?: ""
                    val email = snapshot.child("accountinfo/email").value?.toString() ?: ""

                    onSuccess(name, phoneNumber, email)
                } else {
                    onFailure("User data not found")
                }
            }.addOnFailureListener {
                onFailure("Failed to load data")
            }
        } else {
            onFailure("User not logged in")
        }
    }

    fun saveAccountData(name: String, phoneNumber: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onFailure("User not logged in!")
            return
        }

        val updates = mapOf(
            "accountinfo/fullname" to name,
            "userinfo/phonenumber" to phoneNumber
        )

        database.child("users").child(user.uid).updateChildren(updates).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onFailure("Update Failed!")
        }
    }

    fun loadAddressData(
        onSuccess: (String, String, String, String, String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user != null) {
            val userRef = database.child("users").child(user.uid).child("userinfo").child("address")

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val houseNumber = snapshot.child("housenumber").value?.toString() ?: ""
                    val street = snapshot.child("street").value?.toString() ?: ""
                    val province = snapshot.child("province").value?.toString() ?: ""
                    val city = snapshot.child("city").value?.toString() ?: ""
                    val barangay = snapshot.child("barangay").value?.toString() ?: ""
                    onSuccess(houseNumber, street, province, city, barangay)
                } else {
                    onFailure("User data not found")
                }
            }.addOnFailureListener {
                onFailure("Failed to load data")
            }
        } else {
            onFailure("User not logged in")
        }
    }

    fun saveAddressData(
        houseNumber: String,
        street: String,
        province: String,
        city: String,
        barangay: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onFailure("User not logged in!")
            return
        }

        val updates = mapOf(
            "userinfo/address/housenumber" to houseNumber,
            "userinfo/address/street" to street,
            "userinfo/address/province" to province,
            "userinfo/address/city" to city,
            "userinfo/address/barangay" to barangay
        )

        database.child("users").child(user.uid).updateChildren(updates).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onFailure("Update Failed!")
        }
    }
}
