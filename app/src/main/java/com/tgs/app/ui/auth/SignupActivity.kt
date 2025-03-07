package com.tgs.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.tgs.app.databinding.ActivitySignupBinding
import com.tgs.app.databinding.AccountCreationBinding
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var binding2: AccountCreationBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var submit: Button

    private lateinit var provinceDropdown: AutoCompleteTextView
    private lateinit var cityDropdown: AutoCompleteTextView
    private lateinit var barangayDropdown: AutoCompleteTextView

    private val client = OkHttpClient()
    private val apiKey = "fcb7f3beb3mshf0c1c3aa4a5cfe6p1bb806jsne0accbf9f4cb"

    private var provinceMap = mutableMapOf<String, String>()
    private var cityMap = mutableMapOf<String, String>()

    private var email = ""
    private var password = ""

    private var provinceSelected = ""
    private var municipalitySelected = ""
    private var barangaySelected = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.signupBtn.setOnClickListener {
            email = binding.email.text.toString()
            password = binding.password.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showAccountCreationScreen()
        }

        binding.loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun showAccountCreationScreen() {
        binding2 = AccountCreationBinding.inflate(layoutInflater)
        setContentView(binding2.root)

        submit = binding2.submitBtn
        provinceDropdown = binding2.provinceSpinner
        cityDropdown = binding2.citySpinner
        barangayDropdown = binding2.barangaySpinner

        cityDropdown.isEnabled = false
        barangayDropdown.isEnabled = false

        fetchProvinces()

        binding2.backBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        submit.setOnClickListener {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            saveUserData(user.uid)
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun fetchProvinces() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = makeApiCall("https://ph-locations-api1.p.rapidapi.com/get_provinces")
            val provinces = parseLocations(response)

            withContext(Dispatchers.Main) {
                if (provinces.isNotEmpty()) {
                    provinceMap.clear()
                    provinceMap.putAll(provinces)

                    setupDropdown(provinceDropdown, provinces.values.toList()) { selectedProvince ->
                        provinceSelected = selectedProvince
                        val selectedCode = provinceMap.entries.find { it.value == selectedProvince }?.key ?: ""
                        if (selectedCode.isNotEmpty()) {
                            cityDropdown.isEnabled = true
                            fetchCities(selectedCode)
                        }
                    }
                }
            }
        }
    }

    private fun fetchCities(provinceCode: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = makeApiCall("https://ph-locations-api1.p.rapidapi.com/get_cities_municipalities?province_code=$provinceCode")
            val cities = parseLocations(response)

            withContext(Dispatchers.Main) {
                cityMap.clear()
                if (cities.isNotEmpty()) {
                    cityMap.putAll(cities)
                    setupDropdown(cityDropdown, cities.values.toList()) { selectedCity ->
                        municipalitySelected = selectedCity
                        val selectedCode = cityMap.entries.find { it.value == selectedCity }?.key ?: ""
                        if (selectedCode.isNotEmpty()) {
                            barangayDropdown.isEnabled = true
                            fetchBarangays(selectedCode)
                        }
                    }
                }
            }
        }
    }

    private fun fetchBarangays(cityCode: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = makeApiCall("https://ph-locations-api1.p.rapidapi.com/get_barangays?city_code=$cityCode")
            val barangays = parseLocations(response)

            withContext(Dispatchers.Main) {
                barangayDropdown.setText("")
                if (barangays.isNotEmpty()) {
                    setupDropdown(barangayDropdown, barangays.values.toList()) { selectedBarangay ->
                        barangaySelected = selectedBarangay
                    }
                }
            }
        }
    }

    private fun makeApiCall(url: String): String {
        return try {
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-key", apiKey)
                .addHeader("x-rapidapi-host", "ph-locations-api1.p.rapidapi.com")
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                println("API Response: $body")
                if (!response.isSuccessful) throw Exception("API call failed: ${response.code}")
                body
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun parseLocations(response: String): Map<String, String> {
        if (response.isEmpty()) return emptyMap()

        return try {
            val jsonObject = JSONObject(response)
            val locationsArray = when {
                jsonObject.has("data") -> jsonObject.getJSONArray("data")
                jsonObject.has("locations") -> jsonObject.getJSONArray("locations")
                else -> return emptyMap()
            }

            val locations = mutableMapOf<String, String>()
            for (i in 0 until locationsArray.length()) {
                val location = locationsArray.getJSONObject(i)
                val code = location.optString("code", "")
                val name = location.optString("name", "")
                if (code.isNotEmpty() && name.isNotEmpty()) {
                    locations[code] = name
                }
            }
            locations
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    private fun setupDropdown(dropdown: AutoCompleteTextView, items: List<String>, onItemSelected: ((String) -> Unit)?) {
        if (items.isEmpty()) return

        val sortedItems = items.sorted()
        val adapter = ArrayAdapter(dropdown.context, android.R.layout.simple_dropdown_item_1line, sortedItems)
        dropdown.setAdapter(adapter)

        dropdown.setOnClickListener {
            dropdown.showDropDown()
        }

        dropdown.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position).toString()
            onItemSelected?.invoke(selected)
        }
    }

    private fun saveUserData(uid: String) {
        val houseNumber = binding2.houseNumber.text.toString()
        val streetName = binding2.streetName.text.toString()
        val fullName = binding2.fullName.text.toString()
        val phoneNumber = binding2.phoneNumber.text.toString()
        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""

        if (houseNumber.isBlank() || streetName.isBlank() || fullName.isBlank() || phoneNumber.isBlank() ||
            provinceSelected.isBlank() || municipalitySelected.isBlank() || barangaySelected.isBlank()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance().getReference("users")

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FirebaseDebug", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }

            val fcmtoken = task.result

            val accountInfo = mapOf(
                "email" to email,
                "fullname" to fullName,
                "fcmtoken" to fcmtoken
            )

            val address = mapOf(
                "barangay" to barangaySelected,
                "city" to municipalitySelected,
                "housenumber" to houseNumber,
                "province" to provinceSelected,
                "street" to streetName
            )

            val userInfo = mapOf(
                "address" to address,
                "phonenumber" to phoneNumber
            )

            val user = mapOf(
                "accountinfo" to accountInfo,
                "userinfo" to userInfo
            )

            database.child(uid).setValue(user)
                .addOnSuccessListener {
                    Log.d("FirebaseDebug", "User data successfully saved with FCM token!")
                    Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Log.e("FirebaseDebug", "Failed to save user data: ${exception.message}")
                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
