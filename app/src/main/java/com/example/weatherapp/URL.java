package com.example.weatherapp;

public class URL {
    private String baseURL;
    private static final String apiKey = "54a2850ba731a35944a5129657a5505";
    public URL(){

    }
    public void setBaseURL(String lat , String lon){
        baseURL = "http://api.openweathermap.org/data/3.0/onecall?lat= " + lat + "&lon= " + lon +  "&appid=" + apiKey + " &units=metric&lang=vi";
    }
    public String getBaseURL(){
        return baseURL;
    }
}
