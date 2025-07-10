package com.example.smartweather

import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.example.smartweather.databinding.ActivityMainBinding
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fetchWeatherData("Kolkata")
        SearchCity()
    }

    private fun SearchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }


    private fun fetchWeatherData(cityName:String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiInterface::class.java)

        val response = apiService.getWeatherData(
            cityName,
            "d45b9b076c4756a56b9660db27943c5d",
            "metric"
        )

        response.enqueue(object : Callback<SmartWeather> {
            override fun onResponse(
                call: Call<SmartWeather>,
                response: Response<SmartWeather>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val temperature = responseBody.main.temp
                    val humidity = responseBody.main.humidity
                    val windSpeed = responseBody.wind.speed
                    val sunrise = responseBody.sys.sunrise.toLong()
                    val sunset = responseBody.sys.sunset.toLong()
                    val seaLevel = responseBody.main.pressure
                    val condition = responseBody.weather.firstOrNull()?.main?:"unknown"
                    Log.d("Condition", "Condition: $condition")
                    val maxTemp = responseBody.main.temp_max
                    val minTemp = responseBody.main.temp_min
                    val currentTime = System.currentTimeMillis() / 1000
                    val isNight = currentTime < sunrise || currentTime > sunset

                    binding.temperature.text="$temperature"
                    binding.weath.text= "$condition"
                    binding.max.text = "Max Temp: $maxTemp"
                    binding.Min.text = "Min Temp: $minTemp"
                    binding.humidity.text = "$humidity %"
                    binding.windspeed.text = "$windSpeed m/s"
                    binding.sunrise.text = "${time(sunrise)}"
                    binding.sunset.text = "${time(sunset)}"
                    binding.sea.text = "$seaLevel h/pa"
                    binding.city.text = "$cityName"
                    binding.date.text = date()
                    binding.day.text = dayName(System.currentTimeMillis())
                    changeImagesAccordingToWeatherCondition(condition,isNight)
                }
            }

            private fun changeImagesAccordingToWeatherCondition(condition: String, isNight: Boolean) {
                when (condition.lowercase()) {
                    "clear" -> {
                        if (isNight) {
                            binding.root.setBackgroundResource(R.drawable.night)
                        } else {
                            binding.root.setBackgroundResource(R.drawable.day)
                        }
                    }
                    "haze" -> {
                        binding.root.setBackgroundResource(R.drawable.cloud)
                        binding.lottieAnimationView.setAnimation(R.raw.cloudy)
                    }
                    "rain" -> {
                        binding.root.setBackgroundResource(R.drawable.rainy)
                        binding.lottieAnimationView.setAnimation(R.raw.rainyy)
                    }
                    else -> {
                        if (isNight) {
                            binding.root.setBackgroundResource(R.drawable.night)
                        } else {
                            binding.root.setBackgroundResource(R.drawable.morning)
                            binding.lottieAnimationView.setAnimation(R.raw.starting)
                        }
                    }
                }
                binding.lottieAnimationView.playAnimation()
            }

            private fun date(): String {
                val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                return sdf.format((Date()))
            }
            private fun time(timestamp: Long): String {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                return sdf.format((Date(timestamp * 1000)))
            }

            override fun onFailure(call: Call<SmartWeather>, t: Throwable) {
                Log.e("TAG", "onFailure: ${t.message}")
            }
        })

        }
    fun dayName(timestamp: Long): String{
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }
}

