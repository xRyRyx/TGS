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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications
        } else {
            // TODO: Inform user that notifications are disabled
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register before the fragment is started
        popupLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Hide gray overlay when popup closes
            binding.grayOverlay.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        observeSensorData()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logsRepository = LogsRepository(requireContext())

        askNotificationPermission()
        getFCMToken()
        loadTemperatureData()

        observeSensorData()
        updateTemperatureDisplay()

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
                userRef.update("accountinfo.fcmtoken", token)  // 👈 Update inside accountinfo
                    .addOnSuccessListener { Log.d("FCM", "Token updated in Firestore!") }
                    .addOnFailureListener { e -> Log.w("FCM", "Error saving token", e) }
            }
        }

        refreshRunnable = object : Runnable {
            override fun run() {
                observeSensorData() // Fetch new data
                updateTemperatureDisplay() // Refresh UI
                handler.postDelayed(this, 5000) // Repeat every 5 seconds
            }
        }
        handler.post(refreshRunnable) // Start the loop

        // Listener to update gauge when text changes (manual edit simulation)
        binding.temp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val temp = s.toString().toFloatOrNull()
                if (temp != null) {
                    currentTempCelsius = if (isCelsius) temp else fahrenheitToCelsius(temp)
                    binding.tempGauge.setTemperature(currentTempCelsius) // Always in Celsius
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.tempCV.setOnClickListener {
            // Show gray overlay
            binding.grayOverlay.visibility = View.VISIBLE

            // Launch popup activity
            val intent = Intent(requireContext(), TempPopupActivity::class.java)
            popupLauncher.launch(intent)
        }

        // Celsius Button - Switch to Celsius
        binding.celsius.setOnClickListener {
            if (!isCelsius) {
                isCelsius = true
                binding.tempUnit.text = "°C"
                updateTemperatureDisplay()
            }
        }

        // Fahrenheit Button - Convert and switch to Fahrenheit
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

        // Force the UI to redraw
        binding.temp.invalidate()
        binding.tempGauge.invalidate()
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

            // Get the token
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

                logsRepository?.loadTemperatureData(
                    onSuccess = { temperature ->
                        val temp = temperature.toFloatOrNull() ?: return@loadTemperatureData
                        if (currentTempCelsius != temp) { // Update only if there's a change
                            currentTempCelsius = temp
                            Log.d("Firebase", "Updated temp: $temp")

                            binding.temp.post {
                                updateTemperatureDisplay()
                            }
                        }
                    },
                    onFailure = { Log.e("FirebaseError", "Failed to load temperature") }
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Database read failed: ${error.message}")
            }
        })
    }

    private fun sendHazardNotification(title: String, message: String) {
        NotificationHelper.sendHazardNotification(requireContext(), title, message)
    }
}
