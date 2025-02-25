package com.tgs.app.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tgs.app.AccountActivity
import com.tgs.app.AddressActivity
import com.tgs.app.R
import com.tgs.app.SignupActivity
import com.tgs.app.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        loadUserData()

        binding.accountBtn.setOnClickListener {
            val intent = Intent(requireContext(), AccountActivity::class.java)
            startActivity(intent)
        }

        binding.addressBtn.setOnClickListener {
            val intent = Intent(requireContext(), AddressActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun loadUserData() {
        val user = auth.currentUser

        if (user != null) {
            val userRef: DatabaseReference = database.getReference("users").child(user.uid)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val accountInfo = snapshot.child("accountinfo")
                    val userInfo = snapshot.child("userinfo").child("address")

                    val name = accountInfo.child("fullname").value?.toString() ?: ""
                    val email = accountInfo.child("email").value?.toString() ?: ""
                    val city = userInfo.child("city").value?.toString() ?: ""
                    val province = userInfo.child("province").value?.toString() ?: ""

                    binding.name.text = name
                    binding.email.text = email
                    binding.address.text = if (city.isNotEmpty() && province.isNotEmpty()) {
                        "$city, $province"
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
