package com.example.chapter7_weather

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.chapter7_weather.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permission ->
        when {
            permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {

            }
            else -> {

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))

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
            Log.e("lastLocation", it.toString())
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)

        val baseDateTime = BaseDateTime.getBaseDateTime()
        val converter = GeoPointConverter()
        val point = converter.convert(lat = 37.5532, lon = 127.1906)

        service.getVillageForecast(
            serviceKey = "",
            baseDate = baseDateTime.baseDate,
            baseTime = baseDateTime.baseTime,
            nx = point.nx,
            ny = point.ny
        ).enqueue(object : Callback<WeatherEntity> {
            override fun onResponse(call: Call<WeatherEntity>, response: Response<WeatherEntity>) {

                val forecastDateTimeMap = mutableMapOf<String, Forecast>()
                val forecastList = response.body()?.response?.body?.items?.forecastEntities.orEmpty()

                for (forecast in forecastList) {

                    if (forecastDateTimeMap["${forecast.forecastDate}/${forecast.forecastTime}"] == null) {
                        forecastDateTimeMap["${forecast.forecastDate}/${forecast.forecastTime}"] =
                            Forecast(forecastDate = forecast.forecastDate, forecastTime = forecast.forecastTime)
                    }

                    forecastDateTimeMap["${forecast.forecastDate}/${forecast.forecastTime}"]?.apply {
                        when (forecast.category) {
                            Category.POP -> precipitation = forecast.forecastValue.toInt()
                            Category.PTY -> precipitationType = transformRainType(forecast)
                            Category.SKY -> sky = transformSky(forecast)
                            Category.TMP -> temperature = forecast.forecastValue.toDouble()
                            else -> { }
                        }
                    }
                }

                Log.e("Forecast", forecastDateTimeMap.toString())
            }

            override fun onFailure(call: Call<WeatherEntity>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun transformRainType(forecastEntity: ForecastEntity): String {
        return when (forecastEntity.forecastValue.toInt()) {
            0 -> "없음"
            1 -> "비"
            2 -> "비/눈"
            3 -> "눈"
            4 -> "소나기"
            else -> ""
        }
    }

    private fun transformSky(forecastEntity: ForecastEntity): String {
        return when (forecastEntity.forecastValue.toInt()) {
           1 -> "맑음"
            3 -> "구름많음"
            4 -> "흐림"
            else -> ""
        }
    }
}