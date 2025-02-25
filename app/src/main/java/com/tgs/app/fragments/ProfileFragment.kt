package com.tgs.app.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tgs.app.R
import com.tgs.app.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        loadUserData()

        return view
    }

    private fun loadUserData() {
        val user = auth.currentUser

        if (user != null) {
            val userRef: DatabaseReference = database.getReference("users").child(user.uid)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val name = snapshot.child("username").value.toString()
                    val email = snapshot.child("email").value.toString()
                    val province = snapshot.child("province").value.toString()
                    val municipality = snapshot.child("municipality").value.toString()
                    val barangay = snapshot.child("barangay").value.toString()

                    binding.name.text = name.ifEmpty { "No Name" }
                    binding.email.text = email.ifEmpty { "No Email" }
                    binding.addressBtn.text = if (province.isNotEmpty() && municipality.isNotEmpty() && barangay.isNotEmpty()) {
                        "$province, $municipality, $barangay"
                    } else {
                        "No Address"
                    }
                } else {
                    Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
