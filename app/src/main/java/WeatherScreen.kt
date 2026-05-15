package com.kokila.jalsanchay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.material3.CardDefaults

@Composable
fun WeatherScreen() {

    var city by remember {
        mutableStateOf("Bengaluru")
    }

    var temperature by remember {
        mutableStateOf("--")
    }

    var weatherDesc by remember {
        mutableStateOf("Loading...")
    }

    var humidity by remember {
        mutableStateOf("--")
    }

    var humidityValue by remember {
        mutableStateOf(0f)
    }

    var windSpeed by remember {
        mutableStateOf("--")
    }

    var feelsLike by remember {
        mutableStateOf("--")
    }

    var pressure by remember {
        mutableStateOf("--")
    }

    var visibility by remember {
        mutableStateOf("--")
    }
    var forecastList by remember {
        mutableStateOf(listOf<String>())
    }

    var rainPrediction by remember {
        mutableStateOf("Checking forecast...")
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    fun fetchWeather() {

        isLoading = true

        val retrofit =
            Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .build()

        val api =
            retrofit.create(WeatherApi::class.java)
        api.getForecast(
            city = city,
            apiKey = "8fb13990482baf8d609c49a04394cb8a"
        ).enqueue(object : Callback<ForecastResponse> {

            override fun onResponse(
                call: Call<ForecastResponse>,
                response: Response<ForecastResponse>
            ) {

                if (response.isSuccessful) {

                    val data = response.body()

                    val tempList =
                        mutableListOf<String>()

                    data?.list?.take(5)?.forEach {

                        val temp =
                            "${it.main.temp.toInt()}°C"

                        val desc =
                            it.weather[0].description

                        tempList.add(
                            "$temp • $desc"
                        )
                    }

                    forecastList = tempList
                }
            }

            override fun onFailure(
                call: Call<ForecastResponse>,
                t: Throwable
            ) {

            }
        })

        api.getWeather(
            city = city,
            apiKey = "8fb13990482baf8d609c49a04394cb8a"
        ).enqueue(object : Callback<WeatherResponse> {

            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {

                isLoading = false

                if (response.isSuccessful) {

                    val data = response.body()

                    temperature =
                        "${data?.main?.temp?.toInt()}°C"

                    feelsLike =
                        "${data?.main?.feels_like?.toInt()}°C"

                    pressure =
                        "${data?.main?.pressure} hPa"

                    visibility =
                        "${(data?.visibility ?: 0) / 1000} km"

                    weatherDesc =
                        data?.weather?.get(0)?.description
                            ?: "No Description"
                    WeatherState.weatherCondition =
                        weatherDesc

                    WeatherState.rainChance =
                        weatherDesc.contains(
                            "rain",
                            ignoreCase = true
                        )

                    humidity =
                        "${data?.main?.humidity}%"

                    humidityValue =
                        (data?.main?.humidity ?: 0) / 100f

                    windSpeed =
                        "${data?.wind?.speed} km/h"

                    rainPrediction =
                        if (
                            weatherDesc.contains(
                                "rain",
                                ignoreCase = true
                            )
                        ) {
                            "Heavy rain expected today ☔"
                        } else {
                            "No heavy rainfall expected"
                        }

                } else {

                    temperature = "API Error"

                    weatherDesc =
                        "Invalid City / API Issue"
                }
            }

            override fun onFailure(
                call: Call<WeatherResponse>,
                t: Throwable
            ) {

                isLoading = false

                temperature = "Failed"

                weatherDesc =
                    t.message ?: "Unknown Error"
            }
        })
    }

    LaunchedEffect(Unit) {

        while (true) {

            fetchWeather()

            delay(30000)
        }
    }

    val backgroundColor =
        when {

            weatherDesc.contains(
                "rain",
                true
            ) -> Color(0xFFBBDEFB)

            weatherDesc.contains(
                "cloud",
                true
            ) -> Color(0xFFE0E0E0)

            weatherDesc.contains(
                "clear",
                true
            ) -> Color(0xFFFFF59D)

            else -> Color(0xFFE1F5FE)
        }

    val weatherEmoji =
        when {

            weatherDesc.contains(
                "rain",
                true
            ) -> "🌧"

            weatherDesc.contains(
                "cloud",
                true
            ) -> "☁"

            weatherDesc.contains(
                "clear",
                true
            ) -> "☀"

            else -> "🌤"
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(
                rememberScrollState()
            )
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color.White
                    )
                )
            )
            .padding(16.dp)
    ) {

        Text(
            text = "🌦 Live Weather",
            style =
                MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = city,
            onValueChange = {
                city = it
            },
            label = {
                Text("Enter City")
            },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {

                Icon(
                    imageVector =
                        Icons.Default.Search,
                    contentDescription = null
                )
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                fetchWeather()
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Get Weather")
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {

            CircularProgressIndicator()

            Spacer(modifier = Modifier.height(20.dp))
        }

        // MAIN WEATHER CARD

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
                    elevation =
                    CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                    )
        ) {

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text = weatherEmoji,
                    fontSize = 90.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "📍 $city",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = temperature,
                    style =
                        MaterialTheme.typography.headlineLarge,
                    color = Color.Blue
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = weatherDesc
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Feels Like: $feelsLike"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // FORECAST CARD

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "📅 5-Day Forecast",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                forecastList.forEachIndexed { index, item ->

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Day ${index + 1} → $item"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // HUMIDITY + WIND

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Card(
                modifier = Modifier.weight(1f),
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Color.Blue
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Humidity")

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(humidity)

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { humidityValue },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Air,
                        contentDescription = null,
                        tint = Color.Blue
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Wind")

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(windSpeed)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PRESSURE + VISIBILITY

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Card(
                modifier = Modifier.weight(1f),
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text("Pressure")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(pressure)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Visibility,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Visibility")

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(visibility)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // RAIN PREDICTION

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "☔ Rain Prediction",
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(rainPrediction)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

// SMART WATER ALERT

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "🚨 Smart Water Alert",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                val smartAlert =
                    when {

                        WeatherState.rainChance -> {

                            "🌧 Heavy rainfall expected tomorrow. Prepare storage tanks."
                        }

                        weatherDesc.contains(
                            "rain",
                            true
                        ) ->

                            "☔ Rain expected soon. Prepare storage tanks and harvesting systems."

                        weatherDesc.contains(
                            "cloud",
                            true
                        ) ->

                            "🌥 Cloudy weather detected. Monitor possible rainfall conditions."

                        temperature.replace("°C", "")
                            .toIntOrNull() ?: 0 > 35 ->

                            "🔥 High temperature detected. Conserve stored water carefully."

                        humidity.replace("%", "")
                            .toIntOrNull() ?: 0 > 80 ->

                            "💧 High humidity may indicate upcoming rainfall."

                        else ->

                            "✅ Weather conditions normal for water harvesting."
                    }


                Text(
                    text = smartAlert,
                    color = Color.Blue
                )
            }
        }
    }
}
