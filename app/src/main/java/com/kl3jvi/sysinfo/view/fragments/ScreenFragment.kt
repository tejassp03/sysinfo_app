package com.kl3jvi.sysinfo.view.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.example.sysinfo.R
import com.example.sysinfo.databinding.ScreenFragmentBinding
import kotlin.math.sqrt

class ScreenFragment : Fragment(R.layout.screen_fragment) {

    private var _binding: ScreenFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = ScreenFragmentBinding.bind(view)
        
        setupDisplayInfo()
    }
    
    private fun setupDisplayInfo() {
        val windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)
        
        // Resolution
        binding.resolution.text = "${displayMetrics.widthPixels} x ${displayMetrics.heightPixels} Pixels"
        
        // Density
        val densityDpi = displayMetrics.densityDpi
        val densityName = when {
            densityDpi <= DisplayMetrics.DENSITY_LOW -> "LDPI"
            densityDpi <= DisplayMetrics.DENSITY_MEDIUM -> "MDPI"
            densityDpi <= DisplayMetrics.DENSITY_HIGH -> "HDPI"
            densityDpi <= DisplayMetrics.DENSITY_XHIGH -> "XHDPI"
            densityDpi <= DisplayMetrics.DENSITY_XXHIGH -> "XXHDPI"
            densityDpi <= DisplayMetrics.DENSITY_XXXHIGH -> "XXXHDPI"
            else -> "UNKNOWN"
        }
        binding.density.text = "$densityDpi dpi ($densityName)"
        
        // Font Scale
        val configuration = resources.configuration
        binding.fontScale.text = "${configuration.fontScale}"
        
        // Physical Size in inches
        val widthInches = displayMetrics.widthPixels / displayMetrics.xdpi
        val heightInches = displayMetrics.heightPixels / displayMetrics.ydpi
        val screenInches = sqrt((widthInches * widthInches + heightInches * heightInches).toDouble())
        binding.physicalSize.text = String.format("%.2f", screenInches)
        
        // Refresh Rate
        binding.refreshValue.text = "120 Hz"
        
        // HDR Support
        binding.hdrSupport.text = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            val hdrCapabilities = display.hdrCapabilities
            if (hdrCapabilities?.supportedHdrTypes?.isNotEmpty() == true) "Supported" else "Not Supported"
        } else {
            "Not Supported"
        }
        
        // HDR Capabilities
        binding.hdrCapabilities.text = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            val hdrCapabilities = display.hdrCapabilities
            val capabilities = mutableListOf<String>()
            hdrCapabilities?.supportedHdrTypes?.forEach { type ->
                when (type) {
                    Display.HdrCapabilities.HDR_TYPE_HDR10 -> capabilities.add("HDR10")
                    Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS -> capabilities.add("HDR10+")
                    Display.HdrCapabilities.HDR_TYPE_HLG -> capabilities.add("Hybrid Log-Gamma HDR")
                    Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> capabilities.add("Dolby Vision")
                }
            }
            if (capabilities.isEmpty()) "None" else capabilities.joinToString("\n")
        } else {
            "None"
        }
        
        // Brightness Level
        try {
            val brightness = Settings.System.getInt(requireContext().contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            val brightnessPercent = (brightness / 255f * 100).toInt()
            binding.brightnessLevel.text = "$brightnessPercent %"
        } catch (e: Settings.SettingNotFoundException) {
            binding.brightnessLevel.text = "Unknown"
        }
        
        // Brightness Mode
        try {
            val mode = Settings.System.getInt(requireContext().contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
            binding.brightnessMode.text = if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) "Automatic" else "Manual"
        } catch (e: Settings.SettingNotFoundException) {
            binding.brightnessMode.text = "Unknown"
        }
        
        // Screen Timeout
        try {
            val timeout = Settings.System.getInt(requireContext().contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
            val timeoutSeconds = timeout / 1000
            binding.screenTimeout.text = "$timeoutSeconds Seconds"
        } catch (e: Settings.SettingNotFoundException) {
            binding.screenTimeout.text = "Unknown"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
