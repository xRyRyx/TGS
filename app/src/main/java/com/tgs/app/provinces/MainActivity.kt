package com.tgs.app.provinces

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import com.tgs.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var provinceDropdown: AutoCompleteTextView
    private lateinit var cityDropdown: AutoCompleteTextView
    private lateinit var barangayDropdown: AutoCompleteTextView
    private val client = OkHttpClient()
    private val apiKey = "158871a329msh6a72c2a62279f6ep162d4ejsn26d1f38a192f"

    private var provinceMap = mutableMapOf<String, String>() // provinceCode -> provinceName
    private var cityMap = mutableMapOf<String, String>() // cityCode -> cityName

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        provinceDropdown = findViewById(R.id.provinceSpinner)
        cityDropdown = findViewById(R.id.citySpinner)
        barangayDropdown = findViewById(R.id.barangaySpinner)

        cityDropdown.isEnabled = false
        barangayDropdown.isEnabled = false

        fetchProvinces()
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
                if (cities.isNotEmpty()) {
                    cityMap.clear()
                    cityMap.putAll(cities)

                    setupDropdown(cityDropdown, cities.values.toList()) { selectedCity ->
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
                if (barangays.isNotEmpty()) {
                    setupDropdown(barangayDropdown, barangays.values.toList(), null)
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
                println("API Response: $body")  // 🔍 Debug API response
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
                else -> return emptyMap() // Handle invalid JSON response
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

        dropdown.post {
            dropdown.showDropDown()
        }

        dropdown.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position).toString()
            onItemSelected?.invoke(selected)
        }
    }

}