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
        binding.progressBarInternal.progress = dataViewModel.internalStoragePercentage.toFloat()
        binding.externalSize.progress = dataViewModel.externalStoragePercentage.toFloat()
        
        dataViewModel.ramInfo.onEach { result ->
            when (result) {
                is UiResult.Success -> {
                    val usedPercentage = 100 - result.data.percentageAvailable
                    binding.ramProgress.progress = usedPercentage.toFloat()
                }
                is UiResult.Error -> {
                    // Handle error case if needed
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
