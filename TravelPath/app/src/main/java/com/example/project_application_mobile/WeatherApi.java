package com.example.project_application_mobile;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("v1/forecast?current_weather=true")
    Call<WeatherResponse> getCurrentWeather(@Query("latitude") double lat, @Query("longitude") double lon);
}