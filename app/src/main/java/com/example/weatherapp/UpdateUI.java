package com.example.weatherapp;


public class UpdateUI {
    public static int getIconID(String icon) {
        switch (icon) {
            case "01d":
                return R.drawable.sunny;
            case "01n":
                return R.drawable.clear_night;
            case "02d":
                return R.drawable.cloudy_sunny;
            case "02n":
                return R.drawable.clear_night;
            case "03d":
                return R.drawable.cloudy;
            case "03n":
                return R.drawable.cloudy;
            case "04d":
                return R.drawable.cloudy;
            case "04n":
                return R.drawable.cloudy;
            case "09d":
                return R.drawable.rainy;
            case "09n":
                return R.drawable.rainy;
            case "10d":
                return R.drawable.rainy;
            case "10n":
                return R.drawable.rainy;
            case "11d":
                return R.drawable.storm;
            case "11n":
                return R.drawable.storm;
            case "13d":
                return R.drawable.snowy;
            case "13n":
                return R.drawable.snowy;
            case "50d":
                return R.drawable.windy;
            case "50n":
                return R.drawable.windy;
            default:
                return R.drawable.wind;
        }
    }
}
