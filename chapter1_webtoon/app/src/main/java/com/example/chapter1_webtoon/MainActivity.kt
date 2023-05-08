package com.example.chapter1_webtoon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.chapter1_webtoon.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val container = binding.fragmentContainer

        binding.button1.setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainer, WebViewFragment())
                commit()
            }
        }

        binding.button2.setOnClickListener {

        }

        setContentView(binding.root)


    }
}