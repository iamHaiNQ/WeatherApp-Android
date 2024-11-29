package com.example.weatherapp;

public class URL {
    private String baseURL;
    private static final String apiKey = "54a2850ba731a35944a5129657a55056";
    private String link;
    public URL(){

    }
    public void setBaseURL(double lat , double lon){
        baseURL = "https://api.openweathermap.org/data/3.0/onecall?lat=" + lat + "&lon=" + lon+ "&appid=54a2850ba731a35944a5129657a55056&units=metric";
    }
    public String getBaseURL(){
        return baseURL;
    }
    public void setLink(String city) {
        link = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + apiKey + "&units=metric";
    }

    public String getLink() {
        return link;
    }
}
