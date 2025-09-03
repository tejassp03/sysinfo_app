package com.kl3jvi.sysinfo.view.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sysinfo.R
import com.example.sysinfo.databinding.StorageFragmentBinding
import com.kl3jvi.sysinfo.utils.UiResult
import com.kl3jvi.sysinfo.viewmodel.DataViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class StorageFragment : Fragment(R.layout.storage_fragment) {

    private var _binding: StorageFragmentBinding? = null
    private val binding get() = _binding!!
    private val dataViewModel: DataViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = StorageFragmentBinding.bind(view)
        setupUIElements()
    }

    private fun setupUIElements() {
        setupStorageInfo()
        setupRamInfo()
    }

    private fun setupStorageInfo() {
        // Internal Storage
        val (internalTotal, internalUsed, internalAvailable) = dataViewModel.getFormattedInternalStorage()
        binding.progressBarInternal.progress = dataViewModel.internalStoragePercentage.toFloat()
        binding.internalUsage.text = "$internalUsed / $internalTotal"
        binding.internalAvailable.text = "Available: $internalAvailable"

        // External Storage  
        val (externalTotal, externalUsed, externalAvailable) = dataViewModel.getFormattedExternalStorage()
        binding.externalSize.progress = dataViewModel.externalStoragePercentage.toFloat()
        binding.externalUsage.text = "$externalUsed / $externalTotal"
        binding.externalAvailable.text = "Available: $externalAvailable"
    }

    private fun setupRamInfo() {
        dataViewModel.ramInfo.onEach { result ->
            when (result) {
                is UiResult.Success -> {
                    val ramData = result.data
                    val usedPercentage = 100 - ramData.percentageAvailable
                    
                    binding.ramProgress.progress = usedPercentage.toFloat()
                    binding.ramUsage.text = "${getUsedFromTotal(ramData.total, usedPercentage)} / ${ramData.total}"
                    binding.ramAvailable.text = "Available: ${ramData.available}"
                }
                is UiResult.Error -> {
                    binding.ramUsage.text = "N/A"
                    binding.ramAvailable.text = "Available: N/A"
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun getUsedFromTotal(totalStr: String, usedPercentage: Int): String {
        return try {
            val total = parseMemoryString(totalStr)
            val used = total * (usedPercentage / 100.0)
            formatMemory(used)
        } catch (e: Exception) {
            "N/A"
        }
    }

    private fun parseMemoryString(memStr: String): Double {
        val cleanStr = memStr.replace(Regex("[^\\d.]"), "")
        val value = cleanStr.toDoubleOrNull() ?: 0.0
        return when {
            memStr.contains("GB", ignoreCase = true) -> value
            memStr.contains("MB", ignoreCase = true) -> value / 1024.0
            memStr.contains("KB", ignoreCase = true) -> value / (1024.0 * 1024.0)
            else -> value / (1024.0 * 1024.0 * 1024.0) // Assume bytes
        }
    }

    private fun formatMemory(sizeInGB: Double): String {
        return String.format("%.1f GB", sizeInGB)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
