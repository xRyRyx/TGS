package com.tgs.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tgs.app.data.User
import com.tgs.app.databinding.ActivitySignupBinding
import com.tgs.app.databinding.AccountCreationBinding

class SignupActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignupBinding
    private lateinit var binding2 : AccountCreationBinding
    private lateinit var database : DatabaseReference
    private lateinit var loginBtn : Button
    private lateinit var submit : Button
    private var provinceSelected: String = ""
    private var municipalitySelected: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginBtn = binding.loginBtn

        binding.signupBtn.setOnClickListener{
            val email = binding.email.text.toString()
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()

            binding2 = AccountCreationBinding.inflate(layoutInflater)
            setContentView(binding2.root)

            submit = binding2.submitBtn

            submit.setOnClickListener{

                val houseNumber = binding2.houseNumber.text.toString()
                val streetName = binding2.streetName.text.toString()

                database = FirebaseDatabase.getInstance().getReference("Users")

                val user = User(email,username,password,houseNumber,streetName, provinceSelected, municipalitySelected)
                database.child(username).setValue(user).addOnSuccessListener {
                    binding.email.text.clear()
                    binding.username.text.clear()
                    binding.password.text.clear()
                    binding2.houseNumber.text.clear()
                    binding2.streetName.text.clear()

                    Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        loginBtn.setOnClickListener{
            val intent = Intent(this, LoginActivity :: class.java)
            startActivity(intent)
        }
    }
}