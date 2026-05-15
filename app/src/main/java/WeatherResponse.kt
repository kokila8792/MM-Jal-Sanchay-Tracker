package com.kokila.jalsanchay

data class WeatherResponse(

    val main: MainWeather,

    val weather: List<Weather>,

    val wind: Wind,

    val visibility: Int
)

data class MainWeather(

    val temp: Double,

    val feels_like: Double,

    val pressure: Int,

    val humidity: Int
)

data class Weather(

    val description: String
)

data class Wind(

    val speed: Double
)

