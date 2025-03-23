package com.tgs.app.notif

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.tgs.app.databinding.ActivityQuestionsBinding

class QuestionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionsBinding
    private var currentStep = 1
    private var isAtHome = false
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var numbersRecyclerView: RecyclerView
    private lateinit var numbersAdapter: NumbersAdapter
    private val displayedNumbers = mutableListOf<String>()

    private val emergencyNumbers = mapOf(
        "Metro Manila" to listOf(
            "Bangkal Fire Station: \n\t\t(02) 844-4482",
            "\nComembo Fire Station: \n\t\t(02) 728-7198",
            "\nGuadalupe Fire Station: \n\t\t(02) 882-1843",
            "\nLa Paz Fire Station: \n\t\t(02) 899-2225",
            "\nMakati Central Fire Station: \n\t\t(02) 818-5150", "\t\t(02) 816-2553",
            "\nPio Del Pilar Fire Station: \n\t\t(02) 805-8616", "\t\t(02) 818-4868",
            "\nPlanan Fire Station: \n\t\t(02) 832-2534", "\t\t(02) 551-9401",
            "\nPoblacion Fire Station: \n\t\t(02) 805-5362", "\t\t(02) 245-3269",
            "\nTejeros Fire Station: \n\t\t(02) 794-2679",
            "\nValenzuela Fire Station: \n\t\t(02) 899-9035",
            "\nWest Rembo Fire Station: \n\t\t(02) 882-0531", "\t\t(02) 816-2553"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

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
                } else {
                    binding.questionText.text = "Is there anyone else currently in the house?"
                }
                binding.option1.text = "Yes"
                binding.option2.text = "No"
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
                binding.option1.text = "Yes (Show emergency contacts)"
                binding.option2.text = "No (Show safety tips)"
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
        binding.questionText.text = "Fetching emergency hotlines..."
        binding.answerGroup.visibility = View.GONE
        binding.nextButton.visibility = View.GONE

        fetchUserLocation()
    }

    private fun fetchUserLocation() {
        val userId = auth.currentUser?.uid ?: return

        database.child("users").child(userId).child("userinfo").child("address").child("city").get()
            .addOnSuccessListener { snapshot ->
                val city = snapshot.getValue(String::class.java) ?: "Unknown"
                binding.questionText.text = "Fire Stations in $city"
                updateNumbers(city)
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error fetching location", exception)
                binding.questionText.text = "Failed to fetch location"
            }
    }

    private fun updateNumbers(location: String) {
        displayedNumbers.clear()
        emergencyNumbers[location]?.let { displayedNumbers.addAll(it) }

        // Show RecyclerView for emergency contacts
        numbersRecyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@QuestionsActivity)
            numbersAdapter = NumbersAdapter(displayedNumbers) { phoneNumber ->
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:${phoneNumber.replace(Regex("[^0-9]"), "")}")
                startActivity(intent)
            }
            adapter = numbersAdapter
        }

        binding.root.addView(numbersRecyclerView)
        numbersAdapter.notifyDataSetChanged()
    }

    private fun showSafetyTips() {
        binding.questionText.text = "Here are some safety tips:\n\n1. Stay calm\n2. Call for help\n3. Follow safety protocols"
        binding.answerGroup.visibility = View.GONE
        binding.nextButton.visibility = View.GONE
    }

    // Nested Adapter class for displaying phone numbers
    class NumbersAdapter(private val numbers: List<String>, private val onClick: (String) -> Unit) :
        RecyclerView.Adapter<NumbersAdapter.NumberViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NumberViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return NumberViewHolder(view)
        }

        override fun onBindViewHolder(holder: NumberViewHolder, position: Int) {
            val number = numbers[position]

            // Use regex to identify phone numbers and highlight them
            val regex = Regex("\\(?\\d{2,4}\\)?[-.\\s]?\\d{3,4}[-.\\s]?\\d{3,4}")
            val spannableString = SpannableString(number)

            regex.findAll(number).forEach { match ->
                // Set the phone number to blue
                spannableString.setSpan(
                    ForegroundColorSpan(android.graphics.Color.BLUE),
                    match.range.first,
                    match.range.last + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Add underline to the phone number
                spannableString.setSpan(
                    UnderlineSpan(),
                    match.range.first,
                    match.range.last + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            holder.numberTextView.text = spannableString
            holder.numberTextView.setOnClickListener { onClick(number) }
        }

        override fun getItemCount() = numbers.size

        class NumberViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val numberTextView: android.widget.TextView = view.findViewById(android.R.id.text1)
        }
    }
}