package com.tgs.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.tgs.app.databinding.ActivityVerifyEmailBinding
import com.tgs.app.databinding.EmailVerifiedBinding

class VerifyEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyEmailBinding
    private lateinit var binding2: EmailVerifiedBinding
    private lateinit var auth: FirebaseAuth
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnResendEmail.setOnClickListener {
            val user = auth.currentUser
            user?.sendEmailVerification()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Verification email sent again!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to resend email.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        checkEmailVerified()
    }

    private fun checkEmailVerified() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                val user = auth.currentUser
                user?.reload()?.addOnCompleteListener {
                    if (user.isEmailVerified) {
                        binding2 = EmailVerifiedBinding.inflate(layoutInflater)
                        setContentView(binding2.root)

                        binding2.btnContinue.setOnClickListener {
                            val intent = Intent(this@VerifyEmailActivity, LoginActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        handler.postDelayed(this, 3000)
                    }
                }
            }
        }, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}