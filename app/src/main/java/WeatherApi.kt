package com.kokila.jalsanchay

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("data/2.5/weather?units=metric")
    fun getWeather(

        @Query("q")
        city: String,

        @Query("appid")
        apiKey: String

    ): Call<WeatherResponse>


    @GET("data/2.5/forecast?units=metric")
    fun getForecast(

        @Query("q")
        city: String,

        @Query("appid")
        apiKey: String

    ): Call<ForecastResponse>
}

