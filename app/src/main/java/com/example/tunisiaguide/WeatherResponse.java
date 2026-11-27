package com.example.tunisiaguide;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {

    @SerializedName("current_weather")
    private CurrentWeather currentWeather;

    public CurrentWeather getCurrentWeather() {
        return currentWeather;
    }

    public static class CurrentWeather {
        @SerializedName("temperature")
        private double temperature;

        @SerializedName("windspeed")
        private double windspeed;

        @SerializedName("weathercode")
        private int weathercode;

        public double getTemperature() {
            return temperature;
        }

        public double getWindspeed() {
            return windspeed;
        }

        public int getWeathercode() {
            return weathercode;
        }
    }
}
