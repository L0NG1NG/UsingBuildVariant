package com.longing.linphonecall

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.longing.linphonecall.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.version.text = LinphoneCore(this).getVersionCode()

    }
}