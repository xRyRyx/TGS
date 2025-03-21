package com.tgs.app.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class LogsRepository (private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun loadTemperatureData(
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onFailure("User not logged in")
            return
        }

        val tempRef = database.child("users").child(user.uid).child("logs").child("Temperature")

        tempRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val temperature = snapshot.value?.toString() ?: "No Data"
                onSuccess(temperature)

                val lastTemp = getLastSavedTemperature()
                if (lastTemp == null || lastTemp.toDoubleOrNull() != temperature.toDoubleOrNull()) {
                    saveTemperatureToCsv(temperature)
                }
            } else {
                onFailure("Temperature data not found")
            }
        }.addOnFailureListener {
            onFailure("Failed to load temperature: ${it.message}")
        }
    }

    fun loadHumidityData(
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onFailure("User not logged in")
            return
        }

        val humidityRef = database.child("users").child(user.uid).child("logs").child("Humidity")

        humidityRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val humidity = snapshot.value?.toString()?.toDoubleOrNull()
                if (humidity != null) {
                    val formattedHumidity = "${humidity.toInt()}%"
                    onSuccess(formattedHumidity)
                } else {
                    onFailure("Invalid humidity data")
                }
            } else {
                onFailure("Humidity data not found")
            }
        }.addOnFailureListener {
            onFailure("Failed to load humidity: ${it.message}")
        }
    }

    fun loadGasData(
        onSuccess: (Boolean) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onFailure("User not logged in")
            return
        }

        val gasRef = database.child("users").child(user.uid).child("logs").child("GasValue")

        gasRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val gasValue = snapshot.value?.toString()?.toIntOrNull()
                if (gasValue != null) {
                    onSuccess(gasValue >= 600)
                } else {
                    onFailure("Invalid gas data")
                }
            } else {
                onFailure("Gas data not found")
            }
        }.addOnFailureListener {
            onFailure("Failed to load gas data: ${it.message}")
        }
    }

    fun loadFlameData(
        onSuccess: (Int) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onFailure("User not logged in")
            return
        }

        val flameRef = database.child("users").child(user.uid).child("logs").child("FlameValue")

        flameRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val flameValue = snapshot.value?.toString()?.toIntOrNull()
                if (flameValue != null) {
                    onSuccess(flameValue)
                } else {
                    onFailure("Invalid flame data")
                }
            } else {
                onFailure("Flame data not found")
            }
        }.addOnFailureListener {
            onFailure("Failed to load flame data: ${it.message}")
        }
    }

    fun getLatestSensorData(
        onResult: (Float, Boolean, Int) -> Unit,
        onFailure: (String) -> Unit
    ) {
        loadTemperatureData(
            onSuccess = { tempStr ->
                val temp = tempStr.toFloatOrNull() ?: -1f
                loadGasData(
                    onSuccess = { gas ->
                        loadFlameData(
                            onSuccess = { flame ->
                                onResult(temp, gas, flame)
                            },
                            onFailure = { flameError ->
                                onFailure("Flame data error: $flameError")
                            }
                        )
                    },
                    onFailure = { gasError ->
                        onFailure("Gas data error: $gasError")
                    }
                )
            },
            onFailure = { tempError ->
                onFailure("Temperature data error: $tempError")
            }
        )
    }

    fun saveTemperatureToCsv(temp: String) {
        try {
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val formattedTemp = String.format("%.1f°C", temp.toDoubleOrNull() ?: 0.0)

            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("Asia/Manila")
            val timestamp = sdf.format(Date())

            val logsFolder = File(context.filesDir, "temperature_logs")
            if (!logsFolder.exists()) logsFolder.mkdirs()

            val csvFile = File(logsFolder, "temperature_$todayDate.csv")

            val lastTemp = getLastTemperature(csvFile)
            if (lastTemp == formattedTemp) {
                Log.d("CSV_LOG", "Skipped saving: Temperature ($formattedTemp) is the same as last recorded.")
                return
            }

            val writer = FileWriter(csvFile, true)
            writer.append("$formattedTemp,$timestamp,$todayDate\n")
            writer.flush()
            writer.close()

            Log.d("CSV_LOG", "CSV saved at: ${csvFile.absolutePath} with data: $formattedTemp,$timestamp,$todayDate")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getLastTemperature(csvFile: File): String? {
        if (!csvFile.exists()) return null

        return csvFile.readLines().lastOrNull()?.split(",")?.firstOrNull()
    }

    fun getLastSavedTemperature(): String? {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val logsFolder = File(context.filesDir, "temperature_logs")
        val csvFile = File(logsFolder, "temperature_$todayDate.csv")
        return getLastTemperature(csvFile) // Reuse existing method
    }

    fun getTemperatureStats(): Triple<Double, Pair<Double, String>, Pair<Double, String>> {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val logsFolder = File(context.filesDir, "temperature_logs")
        val csvFile = File(logsFolder, "temperature_$todayDate.csv")

        if (!csvFile.exists()) return Triple(0.0, Pair(0.0, "No Data"), Pair(0.0, "No Data"))

        var currentTemp = 0.0
        var highestTemp = Double.MIN_VALUE
        var lowestTemp = Double.MAX_VALUE
        var highestTime = ""
        var lowestTime = ""

        csvFile.forEachLine { line ->
            val parts = line.split(",")
            if (parts.size == 3) {
                val temp = parts[0].replace("°C", "").trim().toDoubleOrNull() ?: return@forEachLine
                val timestamp = parts[1]
                val date = parts[2]

                if (date == todayDate) {
                    currentTemp = temp

                    if (temp > highestTemp) {
                        highestTemp = temp
                        highestTime = timestamp
                    }
                    if (temp < lowestTemp) {
                        lowestTemp = temp
                        lowestTime = timestamp
                    }
                }
            }
        }

        return Triple(currentTemp, Pair(highestTemp, highestTime), Pair(lowestTemp, lowestTime))
    }
}