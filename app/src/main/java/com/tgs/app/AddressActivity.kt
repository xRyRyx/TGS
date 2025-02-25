package com.tgs.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tgs.app.databinding.ActivityAccountBinding
import com.tgs.app.databinding.ActivityAddressBinding

class AddressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddressBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadAddressInfo()

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun loadAddressInfo() {
        val user = auth.currentUser

        if (user != null) {
            val userRef: DatabaseReference = database.getReference("users").child(user.uid)

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
}