package com.example.travelshare.travelpath;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherClient {
    private static WeatherApi api;

    public static WeatherApi getApi() {
        if (api == null) {
            api = new Retrofit.Builder()
                    .baseUrl("https://api.open-meteo.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(WeatherApi.class);
        }
        return api;
    }
}