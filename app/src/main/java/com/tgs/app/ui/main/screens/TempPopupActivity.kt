package com.tgs.app.ui.main.screens

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tgs.app.R
import com.tgs.app.data.repository.LogsRepository
import com.tgs.app.databinding.ActivityTempPopupBinding
import java.text.SimpleDateFormat
import java.util.*

class TempPopupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTempPopupBinding
    private lateinit var logsRepository: LogsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in_fast, 0)
        binding = ActivityTempPopupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        logsRepository = LogsRepository(this)

        displayTemperatureStats()

        binding.closeBtn.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.fade_in_fast)
    }

    private fun displayTemperatureStats() {
        logsRepository.loadTemperatureData(
            onSuccess = { temperature ->
                logsRepository.saveTemperatureToCsv(temperature)

                val (current, highest, lowest) = logsRepository.getTemperatureStats()

                binding.currentTemp.text = formatTemperature(current)
                binding.highestTemp.text = formatTemperatureWithTime(highest)
                binding.lowestTemp.text = formatTemperatureWithTime(lowest)
            },
            onFailure = {
                binding.currentTemp.text = "No Data"
                binding.highestTemp.text = "No Data"
                binding.lowestTemp.text = "No Data"
            }
        )
    }

    private fun formatTemperature(temp: Double): String {
        return String.format("%.1f°C", temp)
    }

    private fun formatTemperatureWithTime(pair: Pair<Double, String>): String {
        return String.format("%.1f°C at %s", pair.first, formatTime(pair.second))
    }

    private fun formatTime(timestamp: String): String {
        return try {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(timestamp) ?: return timestamp

            val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            outputFormat.format(date)
        } catch (e: Exception) {
            timestamp
        }
    }
}