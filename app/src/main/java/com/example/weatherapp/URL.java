package com.example.weatherapp;

public class URL {
    private String baseURL;
    private static final String apiKey = "54a2850ba731a35944a5129657a55056";
    public URL(){

    }
    public void setBaseURL(double lat , double lon){
        baseURL = "https://api.openweathermap.org/data/3.0/onecall?lat=" + lat + "&lon=" + lon+ "&appid=54a2850ba731a35944a5129657a55056&units=metric&lang=vi";
    }
    public String getBaseURL(){
        return baseURL;
    }

}
