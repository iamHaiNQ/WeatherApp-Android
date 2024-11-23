package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherapp.adapter.HourlyAdapter;
import com.example.weatherapp.entities.Hourly;
import com.google.android.gms.location.FusedLocationProviderClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ActivityResultLauncher<String[]> locationPermissionRequest;

    private ArrayList<Hourly> items;
    private HourlyAdapter hourlyAdapter;
    private RecyclerView recyclerViewHourly;

    private TextView textNameCity, textNext5Days, textDateTime, textState, textTemperature;
    private TextView textPercentHumidity, textWindSpeed, textFeelsLike;
    private ImageView imgIconWeather, imgSearch;
    private EditText editTextSearch;
    private String nameCity = "";
    private String dateTime, weatherDescription,weatherIcon;
    private String hour, iconHourly;
    private int tempHourly;
    private int  temp, humidity, feelsLike, speed;
    private long pressBackTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

    }

    private void getCurrentWeatherData(String lat, String lon) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        URL url = new URL();
        url.setBaseURL(lat, lon);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url.getBaseURL(),
                response -> {
                    try {
                        items.clear();
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject current = jsonObject.getJSONObject("current");
                        String day = current.getString("dt");
                        long dt = Long.parseLong(day);
                        Date date = new Date(dt * 1000L);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE yyyy-MM-dd | HH:mm a", Locale.ENGLISH);
                        dateTime = simpleDateFormat.format(date);
                        double temp = current.getDouble("temp");
                        double feelsLike = current.getDouble("feels_like");
                        double humidity = current.getInt("humidity");
                        String mainWeather = current.getJSONArray("weather").getJSONObject(0).getString("main");
                        String weatherDescription = current.getJSONArray("weather").getJSONObject(0).getString("description");
                        String weatherIcon = current.getJSONArray("weather").getJSONObject(0).getString("icon");
                        upDateUI();


                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> Log.e("result", "Json parsing error: " + error.getMessage())
                );
        requestQueue.add(stringRequest);
    }
    private void getHourlyData(String lat, String lon) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        URL url = new URL();
        url.setBaseURL(lat, lon);
        @SuppressLint("NotifyDataSetChanged") StringRequest stringRequest = new StringRequest(Request.Method.GET, url.getBaseURL(),
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject((Map) response);
                        JSONArray jsonArray = jsonObject.getJSONArray("hourly");


                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                });
    }



    @SuppressLint({"SetTextI18n", "DiscouragedApi"})
    private void upDateUI() {
        textNameCity.setText(nameCity);
        textDateTime.setText(dateTime);
        textState.setText(weatherDescription);
        textTemperature.setText(temp + "°C");
        textPercentHumidity.setText(humidity + "%");
        textFeelsLike.setText(feelsLike + "°C");
        textWindSpeed.setText(speed + "m/s");
        imgIconWeather.setImageResource(getResources().getIdentifier(String.valueOf(UpdateUI.getIconID(weatherIcon)), "drawable", getPackageName()));
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - pressBackTime < 2000) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressBackTime = System.currentTimeMillis();
    }


}