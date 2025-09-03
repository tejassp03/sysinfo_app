package com.kl3jvi.sysinfo.view.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sysinfo.R
import com.example.sysinfo.databinding.DeviceFragmentBinding
import com.example.sysinfo.information
import com.kl3jvi.sysinfo.viewmodel.DataViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent

class DeviceFragment : Fragment(R.layout.device_fragment), KoinComponent {

    private val dataViewModel: DataViewModel by viewModel()
    private var _binding: DeviceFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = DeviceFragmentBinding.bind(view)
        setupUIElements()
    }

    private fun setupUIElements() {
        setupDeviceHeader()
        binding.listWithItems.layoutManager = LinearLayoutManager(requireContext())
        binding.listWithItems.withModels {
            dataViewModel.deviceData.forEach {
                information {
                    id(it.title)
                    data(it)
                }
            }
        }
    }

    private fun setupDeviceHeader() {
        val deviceName = dataViewModel.deviceData.firstOrNull { it.title == "Device Name" }?.details ?: "Unknown Device"
        val isOptimusRhino = deviceName.contains("Optimus", ignoreCase = true) && deviceName.contains("Rhino", ignoreCase = true)
        
        binding.deviceNameText.text = deviceName
        
        if (isOptimusRhino) {
            binding.phoneLogoImage.setImageResource(R.drawable.ic_optimus_rhino)
        } else {
            binding.phoneLogoImage.setImageResource(R.drawable.ic_devicee)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
