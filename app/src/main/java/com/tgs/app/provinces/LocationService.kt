package com.tgs.app.provinces

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

class LocationService {

    private val client = OkHttpClient()
    private val apiKey = "f446c9dcc0msh346372c985bd2bap1582e7jsn0fbfb371f4b1"
    private val apiHost = "ph-locations-api1.p.rapidapi.com"

    private fun fetchData(url: String): JSONArray? {
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("x-rapidapi-key", apiKey)
            .addHeader("x-rapidapi-host", apiHost)
            .build()

        return try {
            val response: Response = client.newCall(request).execute()
            response.body?.string()?.let { JSONArray(it) } ?: JSONArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getProvinces(): List<LocationModels.Province> {
        val url = "https://ph-locations-api1.p.rapidapi.com/get_provinces?limit=100"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("x-rapidapi-key", "f446c9dcc0msh346372c985bd2bap1582e7jsn0fbfb371f4b1")
            .addHeader("x-rapidapi-host", "ph-locations-api1.p.rapidapi.com")
            .build()

        val provinces = mutableListOf<LocationModels.Province>()

        try {
            val response: Response = client.newCall(request).execute()
            val responseData = response.body?.string()

            if (response.isSuccessful && responseData != null) {
                // Parse the JSON response
                val jsonArray = JSONArray(responseData)

                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)

                    // Assuming the keys are 'id' and 'name' in the API response
                    val id = json.getString("id")
                    val name = json.getString("name")

                    provinces.add(LocationModels.Province(id, name))
                }
            } else {
                // Handle API response failure
                println("Error: ${response.message}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return provinces
    }


    fun getCitiesAndMunicipalitiesByProvince(provinceId: String): List<LocationModels.CityMunicipality> {
        val url = "https://ph-locations-api1.p.rapidapi.com/get_cities_municipalities?province_id=$provinceId&limit=100"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("x-rapidapi-key", "f446c9dcc0msh346372c985bd2bap1582e7jsn0fbfb371f4b1")
            .addHeader("x-rapidapi-host", "ph-locations-api1.p.rapidapi.com")
            .build()

        val citiesMunicipalities = mutableListOf<LocationModels.CityMunicipality>()

        try {
            val response: Response = client.newCall(request).execute()
            val responseData = response.body?.string()

            if (response.isSuccessful && responseData != null) {
                // Parse the JSON response
                val jsonArray = JSONArray(responseData)

                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)

                    // Assuming the keys are 'id' and 'name' in the API response
                    val id = json.getString("id")
                    val name = json.getString("name")

                    citiesMunicipalities.add(LocationModels.CityMunicipality(id, name))
                }
            } else {
                // Handle API response failure
                println("Error: ${response.message}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return citiesMunicipalities
    }


    fun getBarangaysByMunicipality(municipalityId: String): List<LocationModels.Barangay> {
        val url = "https://ph-locations-api1.p.rapidapi.com/autocomplete_barangays?municipality_id=$municipalityId&limit=100"
        val data = fetchData(url)
        val barangays = mutableListOf<LocationModels.Barangay>()

        data?.let { jsonArray ->
            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)
                barangays.add(LocationModels.Barangay(json.getString("id"), json.getString("name")))
            }
        }
        return barangays
    }
}
