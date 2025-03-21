package com.tgs.app.ui.main.screens

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.tgs.app.data.repository.UserRepository
import com.tgs.app.ui.profile.AccountActivity
import com.tgs.app.ui.profile.AddressActivity
import com.tgs.app.ui.profile.ContactsActivity
import com.tgs.app.databinding.FragmentProfileBinding
import com.tgs.app.ui.auth.LoginActivity

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val userRepository = UserRepository()

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

        binding.contactsBtn.setOnClickListener {
            val intent = Intent(requireContext(), ContactsActivity::class.java)
            startActivity(intent)
        }

        binding.logoutBtn.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        return binding.root
    }

    private fun loadUserData() {
        userRepository.loadProfileData(
            onSuccess = { name, email, city, province ->
                binding.name.text = name
                binding.email.text = email
                binding.address.text = if (city.isNotEmpty() && province.isNotEmpty()) {
                    "$city, $province"
                } else {
                    "No Address"
                }
            },
            onFailure = { error ->
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        )
    }
}