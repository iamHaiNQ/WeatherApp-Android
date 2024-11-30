package com.example.weatherapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherapp.R;
import com.example.weatherapp.entities.Daily;
import com.example.weatherapp.adapter.DailyAdapter;

import com.example.weatherapp.UpdateUI;
import com.example.weatherapp.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;

public class FutureActivity extends AppCompatActivity {
    private ArrayList<Daily> items;
    private DailyAdapter dailyAdapter;
    private RecyclerView recyclerViewFuture;

    private TextView textTemperatureToday, textWeatherToday;
    private TextView textFeels, textWind, textHumidity;
    private ImageView imgIcon, imgBack;

    private double latitude, longitude;

    @SuppressLint("DiscouragedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_future);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setMapping();
        recyclerViewFuture.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        items = new ArrayList<>();
        dailyAdapter = new DailyAdapter(items);
        recyclerViewFuture.setAdapter(dailyAdapter);

        imgBack.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        latitude = intent.hasExtra("lat") ? intent.getDoubleExtra("lat", 0) : 0;
        longitude = intent.hasExtra("lon") ? intent.getDoubleExtra("lon", 0) : 0;

        if (latitude == 0 || longitude == 0) {
            Toast.makeText(this, "Invalid location data", Toast.LENGTH_SHORT).show();
            finish(); // Quay lại activity trước đó nếu không có dữ liệu
        }
        textTemperatureToday.setText(intent.getStringExtra("temperature"));
        textWeatherToday.setText(intent.getStringExtra("state"));
        textFeels.setText(intent.getStringExtra("feelsLike"));
        textWind.setText(intent.getStringExtra("windSpeed"));
        textHumidity.setText(intent.getStringExtra("humidity"));
        String iconImg = intent.getStringExtra("imgIconWeather");

        if (iconImg != null) {
            imgIcon.setImageResource(getResources().getIdentifier(String.valueOf(UpdateUI.getIconID(iconImg)), "drawable", getPackageName()));
        }
        get5DaysData(latitude, longitude);
    }

    private void setMapping() {
        textTemperatureToday = findViewById(R.id.textTemperatureToday);
        textWeatherToday = findViewById(R.id.textWeatherToday);
        textFeels = findViewById(R.id.textFeels);
        textWind = findViewById(R.id.textWind);
        textHumidity = findViewById(R.id.textHumidity);
        imgIcon = findViewById(R.id.imgIcon);
        recyclerViewFuture = findViewById(R.id.recyclerViewFuture);
        imgBack = findViewById(R.id.imgback);
    }

    private void get5DaysData(double lat, double lon) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        URL url = new URL();
        url.setBaseURL(lat, lon);
        @SuppressLint("NotifyDataSetChanged")
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url.getBaseURL(),
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("daily");
                        SimpleDateFormat outputFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                        items.clear();

                        int maxDay = 6;
                        int count = 0;

                        for (int i = 1; i < jsonArray.length() && count < maxDay; i++) {
                            JSONObject daily = jsonArray.getJSONObject(i);

                            long dt = daily.getLong("dt") * 1000L;
                            Date date = new Date(dt);
                            String day = outputFormat.format(date);

                            JSONObject tempObj = daily.getJSONObject("temp");
                            double tempMin = tempObj.getDouble("min");  // Truy cập 'min' từ đối tượng temp
                            double tempMax = tempObj.getDouble("max");  // Truy cập 'max' từ đối tượng temp
                            String weatherDescription = daily.getJSONArray("weather").getJSONObject(0).getString("description");
                            String iconDaily = daily.getJSONArray("weather").getJSONObject(0).getString("icon");

                            items.add(new Daily(day, iconDaily, weatherDescription, (int) tempMin, (int) tempMax));
                            count++;
                        }
                        dailyAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        Log.e("get5DayData", "Error parsing JSON", e);
                        Toast.makeText(this, "Error parsing daily data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("get5dayData", "Error fetching data: " + error.getMessage());
                    Toast.makeText(this, "Error fetching day data", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(stringRequest);
    }
}