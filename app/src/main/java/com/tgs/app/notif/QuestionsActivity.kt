package com.tgs.app.notif

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tgs.app.R

class QuestionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        val hazardTextView = findViewById<TextView>(R.id.textView3)
        hazardTextView.text = "⚠️ Hazard Detected! Check your sensors."

        Toast.makeText(this, "Check your sensors! Hazard detected!", Toast.LENGTH_LONG).show()
    }
}