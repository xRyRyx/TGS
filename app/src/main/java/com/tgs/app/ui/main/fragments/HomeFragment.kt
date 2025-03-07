package com.tgs.app.ui.main.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.tgs.app.databinding.FragmentHomeBinding
import com.tgs.app.notif.NotificationHelper

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications
        } else {
            // TODO: Inform user that notifications are disabled
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

        askNotificationPermission()
        getFCMToken()

        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get the token
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

                // Save token inside accountinfo
                val userRef = db.collection("users").document(userId)
                userRef.update("accountinfo.fcmtoken", token)  // 👈 Update inside accountinfo
                    .addOnSuccessListener { Log.d("FCM", "Token updated in Firestore!") }
                    .addOnFailureListener { e -> Log.w("FCM", "Error saving token", e) }
            }
        }

        // Hardcoded example temperature (change to test)
        val initialTemp = 80f
        binding.temp.text = "$initialTemp°"  // Set text in TextView
        binding.tempGauge.setTemperature(initialTemp) // Move indicator

        // Listener to update gauge when text changes (manual edit simulation)
        binding.temp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val temp = s.toString().replace("°", "").toFloatOrNull()
                if (temp != null) {
                    binding.tempGauge.setTemperature(initialTemp) // Move indicator dynamically
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted, do nothing
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
                for (logEntry in snapshot.children) {
                    val temperature = logEntry.child("temperature").getValue(Double::class.java) ?: 0.0
                    val gasStatus = logEntry.child("gasStatus").getValue(String::class.java) ?: "Normal"
                    val flameStatus = logEntry.child("flameStatus").getValue(String::class.java) ?: "No Flame"

                    // Check if hazardous conditions exist
                    if (temperature > 38.0 || gasStatus == "DANGEROUS" || flameStatus.contains("Detected")) {
                        sendHazardNotification("Warning!", "Hazard detected! Tap for details.")
                    }
                }
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
