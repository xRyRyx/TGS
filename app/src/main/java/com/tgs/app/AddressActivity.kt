package com.tgs.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tgs.app.databinding.ActivityAddressBinding
import com.tgs.app.databinding.EditAddressBinding

class AddressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddressBinding
    private lateinit var editBinding: EditAddressBinding
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
        binding = ActivityAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isEditing = false

        loadAddressInfo()

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.editBtn.setOnClickListener {
            showEditMode()
        }
    }

    private fun showEditMode() {
        editBinding = EditAddressBinding.inflate(layoutInflater)
        setContentView(editBinding.root)
        isEditing = true

        val user = auth.currentUser
        if (user != null) {
            val userRef: DatabaseReference = database.child("users").child(user.uid)

            userRef.child("userinfo").child("address").child("housenumber").get().addOnSuccessListener { snapshot ->
                editBinding.houseNumber.setText(snapshot.value?.toString() ?: "")
            }

            userRef.child("userinfo").child("address").child("street").get().addOnSuccessListener { snapshot ->
                editBinding.streetName.setText(snapshot.value?.toString() ?: "")
            }

            userRef.child("userinfo").child("address").child("province").get().addOnSuccessListener { snapshot ->
                editBinding.province.setText(snapshot.value?.toString() ?: "")
            }

            userRef.child("userinfo").child("address").child("city").get().addOnSuccessListener { snapshot ->
                editBinding.city.setText(snapshot.value?.toString() ?: "")
            }

            userRef.child("userinfo").child("address").child("barangay").get().addOnSuccessListener { snapshot ->
                editBinding.barangay.setText(snapshot.value?.toString() ?: "")
            }
        }

        editBinding.backBtn.setOnClickListener {
            val intent = Intent(this, AddressActivity::class.java)
            startActivity(intent)
        }

        editBinding.doneBtn.setOnClickListener {
            saveAccountInfo()
        }
    }

    private fun loadAddressInfo() {
        val user = auth.currentUser

        if (user != null) {
            val userRef: DatabaseReference = database.child("users").child(user.uid)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val addressInfo = snapshot.child("userinfo").child("address")

                    val houseNumber = addressInfo.child("housenumber").value?.toString() ?: ""
                    val streetName = addressInfo.child("street").value?.toString() ?: ""
                    val province = addressInfo.child("province").value?.toString() ?: ""
                    val city = addressInfo.child("city").value?.toString() ?: ""
                    val barangay = addressInfo.child("barangay").value?.toString() ?: ""

                    binding.houseNumber.setText(houseNumber)
                    binding.streetName.setText(streetName)
                    binding.province.setText(province)
                    binding.city.setText(city)
                    binding.barangay.setText(barangay)

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

        val newHouseNumber = editBinding.houseNumber.text.toString().trim()
        val newStreetName = editBinding.streetName.text.toString().trim()
        val newProvince = editBinding.province.text.toString().trim()
        val newCity = editBinding.city.text.toString().trim()
        val newBarangay = editBinding.barangay.text.toString().trim()

        if (newHouseNumber.isEmpty() || newStreetName.isEmpty() || newProvince.isEmpty() || newCity.isEmpty() || newBarangay.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf(
            "userinfo/housenumber" to newHouseNumber,
            "userinfo/street" to newStreetName,
            "userinfo/province" to newProvince,
            "userinfo/city" to newCity,
            "userinfo/barangay" to newBarangay
        )

        database.child("users").child(user.uid).updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
            showViewMode() // Go back to view mode after saving
        }.addOnFailureListener {
            Toast.makeText(this, "Update Failed!", Toast.LENGTH_SHORT).show()
        }
    }
}