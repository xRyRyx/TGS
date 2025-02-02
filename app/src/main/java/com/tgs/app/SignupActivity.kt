//package com.tgs.app
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.View
//import android.widget.AdapterView
//import android.widget.ArrayAdapter
//import android.widget.AutoCompleteTextView
//import android.widget.Button
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase
//import com.tgs.app.data.User
//import com.tgs.app.databinding.ActivitySignupBinding
//import com.tgs.app.databinding.AccountCreationBinding
//import com.tgs.app.provinces.Provinces
//
//class SignupActivity : AppCompatActivity() {
//    private lateinit var binding : ActivitySignupBinding
//    private lateinit var binding2 : AccountCreationBinding
//    private lateinit var database : DatabaseReference
//    private lateinit var email : EditText
//    private lateinit var username : EditText
//    private lateinit var password : EditText
//    private lateinit var signupBtn : Button
//    private lateinit var loginBtn : Button
//    private lateinit var houseNumber : EditText
//    private lateinit var streetName : EditText
//    private lateinit var submit : Button
//    private var provinceSelected: String = ""
//    private var municipalitySelected: String = ""
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivitySignupBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        email = binding.email
//        username = binding.username
//        password = binding.password
//        signupBtn = binding.signupBtn
//        loginBtn = binding.loginBtn
//
//        signupBtn.setOnClickListener{
//            val email = binding.email.text.toString()
//            val username = username.text.toString()
//            val password = password.text.toString()
//
//            binding2 = AccountCreationBinding.inflate(layoutInflater)
//            setContentView(binding2.root)
//
//            houseNumber = binding2.houseNumber
//            streetName = binding2.streetName
//            submit = binding2.submitBtn
//
//            val province : AutoCompleteTextView = binding2.provinceAuto
//            val municipality : AutoCompleteTextView = binding2.municipalityAuto
//            val barangay : AutoCompleteTextView = binding2.barangayAuto
//
//            val provinceAdapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, Provinces.provinceItems.keys.toList()
//            )
//            province.setAdapter(provinceAdapter)
//            province.onItemClickListener = AdapterView.OnItemClickListener{
//                adapterView, view, i, l ->
//
//                provinceSelected = adapterView.getItemAtPosition(i).toString()
//                Toast.makeText(this@SignupActivity, "You selected $provinceSelected Province", Toast.LENGTH_SHORT).show()
//
//                val municipalities = Provinces.provinceItems[provinceSelected]?.keys?.toList() ?: emptyList()
//
//                val municipalityAdapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, municipalities)
//                municipality.setAdapter(municipalityAdapter)
//                municipality.visibility = View.VISIBLE
//
//                barangay.setText("")
//                barangay.visibility = View.GONE
//
//                municipality.onItemClickListener = AdapterView.OnItemClickListener{
//                        adapterView, view, i, l ->
//
//                    municipalitySelected = adapterView.getItemAtPosition(i).toString()
//                    Toast.makeText(this@SignupActivity, "You selected $municipalitySelected Municipality", Toast.LENGTH_SHORT).show()
//
//                    val barangays = Provinces.provinceItems[provinceSelected]?.get(municipalitySelected) ?: emptyList()
//                    val barangayAdapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, barangays)
//                    barangay.setAdapter(barangayAdapter)
//                    barangay.visibility = View.VISIBLE
//                }
//            }
//
//            submit.setOnClickListener{
//
//                val houseNumber = binding2.houseNumber.text.toString()
//                val streetName = binding2.streetName.text.toString()
//
//                database = FirebaseDatabase.getInstance().getReference("Users")
//
//                val user = User(email,username,password,houseNumber,streetName, provinceSelected, municipalitySelected)
//                database.child(username).setValue(user).addOnSuccessListener {
//                    binding.email.text.clear()
//                    binding.username.text.clear()
//                    binding.password.text.clear()
//                    binding2.houseNumber.text.clear()
//                    binding2.streetName.text.clear()
//
//                    Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show()
//                }.addOnFailureListener {
//                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//        loginBtn.setOnClickListener{
//            val intent = Intent(this, LoginActivity :: class.java)
//            startActivity(intent)
//        }
//    }
//}