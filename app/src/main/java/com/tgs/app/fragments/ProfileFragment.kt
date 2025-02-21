package com.tgs.app.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tgs.app.R

class ProfileFragment : Fragment() {

    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var backButton: Button

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        nameTextView = view.findViewById(R.id.name)
        emailTextView = view.findViewById(R.id.email)
        addressTextView = view.findViewById(R.id.province)
        backButton = view.findViewById(R.id.backBtn)

        loadUserData()

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return view
    }

    private fun loadUserData() {
        val user = auth.currentUser

        if (user != null) {
            val userRef: DatabaseReference = database.getReference("Users").child(user.uid)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {

                    val name = snapshot.child("username").value.toString()
                    val email = snapshot.child("email").value.toString()
                    val province = snapshot.child("province").value.toString()


                    Log.d("ProfileFragment", "Name: $name, Email: $email, Province: $province")


                    nameTextView.text = name.ifEmpty { "No Name" }
                    emailTextView.text = email.ifEmpty { "No Email" }
                    addressTextView.text = province.ifEmpty { "No Address" }
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
