package com.tgs.app.notif

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.tgs.app.databinding.ActivityQuestionsBinding

class QuestionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionsBinding
    private var currentStep = 1
    private var isAtHome = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQuestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadQuestion()

        binding.nextButton.setOnClickListener {
            val selectedOption = findViewById<RadioButton>(binding.answerGroup.checkedRadioButtonId)
            if (selectedOption != null) {
                handleNext(selectedOption.text.toString())
            }
        }
    }

    private fun loadQuestion() {
        binding.questionProgress.text = "Question $currentStep"

        when (currentStep) {
            1 -> {
                binding.questionText.text = "Are you currently at home?"
                binding.option1.text = "Yes"
                binding.option2.text = "No"
            }
            2 -> {
                if (isAtHome) {
                    binding.questionText.text = "Do you see or smell anything unusual (smoke, gas, fire)?"
                    binding.option1.text = "Yes"
                    binding.option2.text = "No"
                } else {
                    binding.questionText.text = "Is there anyone else currently in the house?"
                    binding.option1.text = "Yes"
                    binding.option2.text = "No"
                }
            }
            3 -> {
                if (isAtHome) {
                    binding.questionText.text = "Are you feeling unwell (dizziness, breathing issues, irritation)?"
                } else {
                    binding.questionText.text = "Do you want to notify someone at home about this?"
                }
                binding.option1.text = "Yes"
                binding.option2.text = "No"
            }
            4 -> {
                binding.questionText.text = "Are there other people in the house?"
                binding.option1.text = "Yes"
                binding.option2.text = "No"
            }
            5 -> {
                binding.questionText.text = "Do you need assistance?"
                binding.option1.text = "Yes (TODO: show emergency contacts)"
                binding.option2.text = "No (TODO: show safety tips)"
            }
        }
    }

    private fun handleNext(answer: String) {
        when (currentStep) {
            1 -> {
                isAtHome = answer == "Yes"
                currentStep = 2
            }
            2 -> {
                currentStep = if (isAtHome) {
                    if (answer == "Yes") 3 else 4
                } else {
                    if (answer == "Yes") 3 else 4
                }
            }
            3 -> {
                if (isAtHome || answer == "Yes") {
                    currentStep = 5
                } else {
                    showSafetyTips()
                    return
                }
            }
            4 -> currentStep = 5
            5 -> {
                if (answer.contains("emergency")) {
                    showEmergencyHotlines()
                } else {
                    showSafetyTips()
                }
                return
            }
        }
        binding.answerGroup.clearCheck()
        loadQuestion()
    }

    private fun showEmergencyHotlines() {
        binding.questionText.text = "TODO: Show emergency hotlines"
        binding.answerGroup.visibility = View.GONE
        binding.nextButton.visibility = View.GONE
    }

    private fun showSafetyTips() {
        binding.questionText.text = "TODO: Show safety tips"
        binding.answerGroup.visibility = View.GONE
        binding.nextButton.visibility = View.GONE
    }
}