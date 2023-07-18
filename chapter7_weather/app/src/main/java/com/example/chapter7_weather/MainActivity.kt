package com.example.chapter7_weather

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.chapter7_weather.databinding.ActivityMainBinding
import com.example.chapter7_weather.databinding.ItemForecastBinding
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permission ->
        when {
            permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                updateLocation()
            }
            else -> {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))

    }

    private fun updateLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener {
            Log.e("location", it.toString())

            Thread {
                try {
                    val addressList = Geocoder(this, Locale.KOREA).getFromLocation(
                        it.latitude,
                        it.longitude,
                        10
                    )

                    runOnUiThread {
                        binding.locationTextView.text = addressList?.get(0)?.thoroughfare.orEmpty()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()

            WeatherRepository.getVillageForecast(
                longitude = it.longitude,
                latitude = it.latitude,
                successCallback = { list ->

                    val currentForecast = list.first()

                    binding.temperatureTextView.text = getString(R.string.temperature_text, currentForecast.temperature)
                    binding.skyTextView.text = currentForecast.weather
                    binding.precipitationTextView.text = getString(R.string.precipitation_text, currentForecast.precipitation)

                    binding.childForecastLayout.apply {
                        list.forEachIndexed { index, forecast ->
                            if (index == 0) { return@forEachIndexed }

                            val itemView = ItemForecastBinding.inflate(layoutInflater)

                            itemView.timeTextView.text = "${forecast.forecastTime.substring(0, 2).toInt()}시"
                            itemView.weatherTextView.text = forecast.weather
                            itemView.temperatureTextView.text = getString(R.string.temperature_text, forecast.temperature)

                            addView(itemView.root)
                        }
                    }

                    Log.e("Forecast", list.toString())
                },
                failureCallback = {
                    it.printStackTrace()
                }
            )
        }
    }

}