package com.tgs.app.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tgs.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
}
