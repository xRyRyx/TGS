package com.tgs.app.ui.main.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.tgs.app.R
import com.tgs.app.data.repository.LogsRepository
import com.tgs.app.databinding.FragmentHomeBinding
import com.tgs.app.notif.NotificationHelper

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private var logsRepository: LogsRepository? = null
    private var isCelsius = true
    private var currentTempCelsius = 24.2f
    private lateinit var popupLauncher: ActivityResultLauncher<Intent>

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var refreshRunnable: Runnable

    private var lastKnownTemp: Float = -1.0f
    private var lastKnownGas: Boolean = false
    private var lastKnownFlame: Int = -1

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {

        } else {
            // TODO: Inform user that notifications are disabled
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        popupLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            binding.grayOverlay.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        observeSensorData()
        loadHumidity()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logsRepository = LogsRepository(requireContext())

        askNotificationPermission()
        getFCMToken()
        loadTemperatureData()
        updateTemperatureDisplay()

//        logsRepository?.getLatestSensorData(
//            onResult = { temp, gasDetected, flame ->
//                val gasPPM = if (gasDetected) 600 else 0
//
//                NotificationHelper.sendHazardNotification(requireContext(), temp, gasPPM, flame)
//            },
//            onFailure = { error ->
//                Log.e("SensorData", "Failed to fetch sensor data: $error")
//            }
//        )

        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM", "FCM Token: $token")
        }

        if (userId != null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM token failed", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.d("FCM", "FCM Token: $token")

                val userRef = db.collection("users").document(userId)
                userRef.update("accountinfo.fcmtoken", token)
                    .addOnSuccessListener { Log.d("FCM", "Token updated in Firestore!") }
                    .addOnFailureListener { e -> Log.w("FCM", "Error saving token", e) }
            }
        }

        refreshRunnable = object : Runnable {
            override fun run() {
                observeSensorData()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(refreshRunnable)

        binding.temp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val temp = s.toString().toFloatOrNull()
                if (temp != null) {
                    currentTempCelsius = if (isCelsius) temp else fahrenheitToCelsius(temp)
                    binding.tempGauge.setTemperature(currentTempCelsius)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.tempCV.setOnClickListener {
            binding.grayOverlay.visibility = View.VISIBLE

            val intent = Intent(requireContext(), TempPopupActivity::class.java)
            popupLauncher.launch(intent)
        }

        binding.celsius.setOnClickListener {
            if (!isCelsius) {
                isCelsius = true
                binding.tempUnit.text = "°C"
                updateTemperatureDisplay()
            }
        }

        binding.fahrenheit.setOnClickListener {
            if (isCelsius) {
                isCelsius = false
                binding.tempUnit.text = "°F"
                updateTemperatureDisplay()
            }
        }
    }

    private fun loadTemperatureData() {
        logsRepository?.loadTemperatureData(
            onSuccess = { temperature ->
                val temp = temperature.toFloatOrNull() ?: 0.0f
                currentTempCelsius = temp
                updateTemperatureDisplay()
            },
            onFailure = { error ->
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(refreshRunnable)
    }

    private fun updateTemperatureDisplay() {
        val displayedTemp = if (isCelsius) currentTempCelsius else celsiusToFahrenheit(currentTempCelsius)
        binding.temp.text = String.format("%.1f", displayedTemp)

        binding.tempGauge.setTemperature(currentTempCelsius)

        binding.temp.invalidate()
        binding.tempGauge.invalidate()
    }

    private fun updateHumidityDisplay(humidity: String) {
        binding.humidity.text = humidity
        binding.humidity.invalidate()
    }

    private fun celsiusToFahrenheit(celsius: Float): Float {
        return (celsius * 9 / 5) + 32
    }

    private fun fahrenheitToCelsius(fahrenheit: Float): Float {
        return (fahrenheit - 32) * 5 / 9
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // android 13+
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: Show UI explaining why notifications are needed before requesting
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM", "FCM Token: $token")

            // TODO: Send this token to your backend server if needed
        }
    }

    private fun observeSensorData() {
        val database = FirebaseDatabase.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val logsRef = database.getReference("Users").child(uid).child("Logs")

        logsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Firebase", "Snapshot received: ${snapshot.value}")

                // Check for updated temperature and handle dangerous levels
                logsRepository?.loadTemperatureData(
                    onSuccess = { temperature ->
                        val temp = temperature.toFloatOrNull() ?: return@loadTemperatureData
                        // Only update if temp is actually different
                        if (currentTempCelsius != temp) {
                            currentTempCelsius = temp
                            Log.d("Firebase", "Updated temp: $temp")
                            binding.temp.post {
                                updateTemperatureDisplay()
                            }

                            // Check if temp has changed and if it is dangerous
                            if (temp != lastKnownTemp) {
                                checkForDangerousTemperature(temp)
                                lastKnownTemp = temp // Update last known temp
                            }
                        }
                    },
                    onFailure = { Log.e("FirebaseError", "Failed to load temperature") }
                )

                // Check for gas data
                logsRepository?.loadGasData(
                    onSuccess = { isGasDetected ->
                        binding.gasSmokeDetected.post {
                            updateGasDisplay(isGasDetected)
                        }

                        // Check if gas detection has changed and if it is dangerous
                        if (isGasDetected != lastKnownGas) {
                            checkForDangerousGas(isGasDetected)
                            lastKnownGas = isGasDetected // Update last known gas status
                        }
                    },
                    onFailure = { Log.e("FirebaseError", "Failed to load gas data") }
                )

                // Check for flame data
                logsRepository?.loadFlameData(
                    onSuccess = { flameValue ->
                        binding.flameDetected.post {
                            updateFlameDisplay(flameValue)
                        }

                        // Check if flame level has changed and if it is dangerous
                        if (flameValue != lastKnownFlame) {
                            checkForDangerousFlame(flameValue)
                            lastKnownFlame = flameValue // Update last known flame value
                        }
                    },
                    onFailure = { Log.e("FirebaseError", "Failed to load flame data") }
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Database read failed: ${error.message}")
            }
        })
    }

    // Check for dangerous temperature
    private fun checkForDangerousTemperature(temp: Float) {
        if (temp > 50) { // Temperature threshold for danger
            NotificationHelper.sendHazardNotification(requireContext(), temp, lastKnownGasInt(), lastKnownFlame)
        }
    }

    // Check for dangerous gas levels
    private fun checkForDangerousGas(isGasDetected: Boolean) {
        if (isGasDetected) { // Gas detection threshold
            NotificationHelper.sendHazardNotification(requireContext(), currentTempCelsius, 600, lastKnownFlame)
        }
    }

    // Check for dangerous flame levels
    private fun checkForDangerousFlame(flameValue: Int) {
        if (flameValue < 60) { // Flame threshold
            NotificationHelper.sendHazardNotification(requireContext(), currentTempCelsius, lastKnownGasInt(), flameValue)
        }
    }

    // Helper function to convert boolean gas detection to int
    private fun lastKnownGasInt(): Int {
        return if (lastKnownGas) 600 else 0
    }

    private fun loadHumidity() {
        logsRepository?.loadHumidityData(
            onSuccess = { humidity ->
                binding.humidity.text = humidity
            },
            onFailure = { error ->
                binding.humidity.text = error
            }
        )
    }

    private fun updateGasDisplay(isDetected: Boolean) {
        if (isDetected) {
            binding.gasSmokeDetected.text = "Gas Detected"
            binding.gasSmokeDetected.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            binding.gasSmokeDetected.setCompoundDrawablesWithIntrinsicBounds(R.drawable.danger_icon, 0, 0, 0)
        } else {
            binding.gasSmokeDetected.text = "No Gas Detected"
            binding.gasSmokeDetected.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            binding.gasSmokeDetected.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_shield_icon, 0, 0, 0)
        }
    }

    private fun updateFlameDisplay(flameValue: Int) {
        if (flameValue < 60) {
            binding.flameDetected.text = "Flame Detected"
            binding.flameDetected.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            binding.flameDetected.setCompoundDrawablesWithIntrinsicBounds(R.drawable.danger_icon, 0, 0, 0)
        } else {
            binding.flameDetected.text = "No Flame Detected"
            binding.flameDetected.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            binding.flameDetected.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_shield_icon, 0, 0, 0)
        }
    }
}
