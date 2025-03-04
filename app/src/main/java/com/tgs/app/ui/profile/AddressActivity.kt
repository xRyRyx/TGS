package com.tgs.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tgs.app.data.repository.UserRepository
import com.tgs.app.databinding.ActivityAddressBinding
import com.tgs.app.databinding.EditAddressBinding

class AddressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddressBinding
    private lateinit var editBinding: EditAddressBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val userRepository = UserRepository()
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

        userRepository.loadAddressData(
            onSuccess = { houseNumber, street, province, city, barangay ->
                editBinding.houseNumber.setText(houseNumber)
                editBinding.streetName.setText(street)
                editBinding.province.setText(province)
                editBinding.city.setText(city)
                editBinding.barangay.setText(barangay)
            },
            onFailure = { error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )

        editBinding.backBtn.setOnClickListener {
            val intent = Intent(this, AddressActivity::class.java)
            startActivity(intent)
        }

        editBinding.doneBtn.setOnClickListener {
            saveAddressInfo()
        }
    }

    private fun loadAddressInfo() {
        userRepository.loadAddressData(
            onSuccess = { houseNumber, street, province, city, barangay ->
                binding.houseNumber.setText(houseNumber)
                binding.streetName.setText(street)
                binding.province.setText(province)
                binding.city.setText(city)
                binding.barangay.setText(barangay)
            },
            onFailure = { error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun saveAddressInfo() {
        val newHouseNumber = editBinding.houseNumber.text.toString().trim()
        val newStreetName = editBinding.streetName.text.toString().trim()
        val newProvince = editBinding.province.text.toString().trim()
        val newCity = editBinding.city.text.toString().trim()
        val newBarangay = editBinding.barangay.text.toString().trim()

        if (newHouseNumber.isEmpty() || newStreetName.isEmpty() || newProvince.isEmpty() || newCity.isEmpty() || newBarangay.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        userRepository.saveAddressData(
            newHouseNumber, newStreetName, newProvince, newCity, newBarangay,
            onSuccess = {
                Toast.makeText(this, "Address Updated!", Toast.LENGTH_SHORT).show()
                showViewMode()
            },
            onFailure = { error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }
}