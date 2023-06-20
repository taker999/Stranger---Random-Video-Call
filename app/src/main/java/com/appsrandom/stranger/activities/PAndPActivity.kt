package com.appsrandom.stranger.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.appsrandom.stranger.databinding.ActivityTandCactivityBinding

class PAndPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTandCactivityBinding
    private val fileName = "privacyPolicy.html"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTandCactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.loadUrl("file:///android_asset/$fileName")
    }
}