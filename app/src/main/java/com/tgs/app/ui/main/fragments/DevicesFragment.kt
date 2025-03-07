package com.tgs.app.ui.main.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tgs.app.R
import com.tgs.app.databinding.FragmentDevicesBinding

class DevicesFragment : Fragment() {
    private lateinit var binding: FragmentDevicesBinding
    private lateinit var addDeviceLayout: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDevicesBinding.inflate(inflater, container, false)

        addDeviceLayout = binding.root.findViewById(R.id.addDeviceLayout)
        addDeviceLayout.visibility = View.GONE

        binding.addBtn.setOnClickListener {
            addDeviceLayout.visibility = View.VISIBLE
        }

        return binding.root
    }
}