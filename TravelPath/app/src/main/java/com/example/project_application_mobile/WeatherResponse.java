package com.example.project_application_mobile;

public class WeatherResponse {
    public CurrentWeather current_weather;

    public static class CurrentWeather {
        public double temperature;
        public int weathercode;
        public double windspeed;
    }
}