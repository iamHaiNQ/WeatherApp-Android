package com.example.weatherapp.activites;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import com.example.weatherapp.URL;
import com.example.weatherapp.UpdateUI;
import com.example.weatherapp.adapter.HourlyAdapter;
import com.example.weatherapp.entities.Hourly;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.karumi.dexter.BuildConfig;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private static final String TAG = MainActivity.class.getSimpleName();

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private boolean mRequestingLocationUpdates = false;

    private ArrayList<Hourly> items;
    private HourlyAdapter hourlyAdapter;
    private RecyclerView recyclerViewHourly;

    private TextView textNameCity, textNext5Days, textDateTime, textState, textTemperature;
    private TextView textPercentHumidity, textWindSpeed, textFeelsLike;
    private ImageView imgIconWeather, imgSearch;
    private EditText editTextSearch;
    private String nameCity = "";
    private String dateTime, weatherDescription,weatherIcon, mainWeather;
    private String hour, iconHourly;
    private double tempHourly;
    private double  temp, humidity, feelsLike, speed;
    private long pressBackTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setMapping();
        recyclerViewHourly.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        items = new ArrayList<>();
        hourlyAdapter = new HourlyAdapter(items);
        recyclerViewHourly.setAdapter(hourlyAdapter);

        textNext5Days.setOnClickListener(v -> setIntentExtras());
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();

                double latitude = mCurrentLocation.getLatitude();
                double longitude = mCurrentLocation.getLongitude();
                getCurrentWeatherData(latitude, longitude);
                getHourlyData(latitude, longitude);

                getCityFromLatLon(latitude, longitude);
            }
        };

        mLocationRequest = com.google.android.gms.location.LocationRequest.create()
                .setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
                .setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        mRequestingLocationUpdates = true;
                        startLocationUpdate();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        if(permissionDeniedResponse.isPermanentlyDenied()){
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

//        imgSearch.setOnClickListener(v -> {
//            String city = editTextSearch.getText().toString();
//            if (city.isEmpty()) {
//                Toast.makeText(this, "Please enter city", Toast.LENGTH_SHORT).show();
//            } else {
//                nameCity = city;
//                textNameCity.setText(nameCity);
//                textNameCity.setVisibility(View.VISIBLE);
//                // Chuyển tên thành phố sang lat/lon
//                convertCityToLatLon(city);
//            }
//        });

    }

    private void openSettings(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void startLocationUpdate(){
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
               mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e ).getStatusCode();
                        switch (statusCode){
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade location settings");

                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingItent unable to execute request");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be fixed here, Fix in settings";
                                Log.e(TAG, errorMessage);

                                Toast.makeText(MainActivity.this , errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void stopLocationUpdates(){
        mFusedLocationClient.removeLocationUpdates(mLocationCallback).addOnCompleteListener(this, task -> Log.d(TAG, "Location updates stopped!"));
    }

    private boolean checkPermissions(){
        int permissionState = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mRequestingLocationUpdates && checkPermissions()){
            startLocationUpdate();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mRequestingLocationUpdates){
            stopLocationUpdates();
        }
    }

    private void getCityFromLatLon(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                nameCity = address.getLocality();
                // Nếu không có Locality, dùng một trường khác như AdminArea
                if (nameCity == null || nameCity.isEmpty()) {
                    nameCity = address.getAdminArea(); // Tỉnh, bang
                }
                // Cập nhật UI
                if (nameCity != null && !nameCity.isEmpty()) {
                    textNameCity.setText(nameCity);
                    textNameCity.setVisibility(View.VISIBLE);
                } else {
                    textNameCity.setText("City not found");
                    textNameCity.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(this, "No address found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error fetching address", Toast.LENGTH_SHORT).show();
        }
    }
//    private void convertCityToLatLon(String city) {
//        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//        try {
//            List<Address> addresses = geocoder.getFromLocationName(city, 1); // Lấy 1 kết quả phù hợp
//            if ((addresses != null) && !addresses.isEmpty()) {
//                Address address = addresses.get(0);
//                double lat = address.getLatitude();
//                double lon = address.getLongitude();
//
//                // Gọi API thời tiết với tọa độ
//                getCurrentWeatherData(lat, lon);
//                getHourlyData(lat, lon);
//            } else {
//                Toast.makeText(this, "City not found. Please check the name.", Toast.LENGTH_SHORT).show();
//            }
//        } catch (IOException e) {
//            Log.e("GeocoderError", "Error converting city to lat/lon: " + e.getMessage());
//            Toast.makeText(this, "Unable to convert city to coordinates.", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void setMapping() {
        recyclerViewHourly = findViewById(R.id.recyclerViewHourly);
        textNext5Days = findViewById(R.id.textNext5Days);
        textDateTime = findViewById(R.id.textDateTime);
        editTextSearch = findViewById(R.id.editTextSearch);
        textState = findViewById(R.id.textState);
        textNameCity = findViewById(R.id.textNameCity);
        textTemperature = findViewById(R.id.textTemperature);
        imgIconWeather = findViewById(R.id.imgIconWeather);
        textPercentHumidity = findViewById(R.id.textPercentHumidity);
        textWindSpeed = findViewById(R.id.textWindSpeed);
        textFeelsLike = findViewById(R.id.textFeelsLike);
        imgSearch = findViewById(R.id.imgSearch);
    }

    private void setIntentExtras() {
        String city = editTextSearch.getText().toString();
        Intent intent = new Intent(MainActivity.this, FutureActivity.class);
        intent.putExtra("name", nameCity);
        intent.putExtra("state", textState.getText().toString());
        intent.putExtra("temperature", textTemperature.getText().toString());
        intent.putExtra("feelsLike", textFeelsLike.getText().toString());
        intent.putExtra("windSpeed", textWindSpeed.getText().toString());
        intent.putExtra("humidity", textPercentHumidity.getText().toString());
        intent.putExtra("imgIconWeather", weatherIcon);
        startActivity(intent);
    }

    private void getCurrentWeatherData(double lat, double lon) {
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
                        temp = current.getDouble("temp");
                        feelsLike = current.getDouble("feels_like");
                        humidity = current.getInt("humidity");
                        speed = current.getDouble("speed");
                        mainWeather = current.getJSONArray("weather").getJSONObject(0).getString("main");
                        weatherDescription = current.getJSONArray("weather").getJSONObject(0).getString("description");
                        weatherIcon = current.getJSONArray("weather").getJSONObject(0).getString("icon");
                        upDateUI();


                    } catch (JSONException e) {
                        Toast.makeText(this, "Erorr json", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Log.e("result", "Json parsing error: " + error.getMessage())
                );
        requestQueue.add(stringRequest);
    }


    private void getHourlyData(double lat, double lon) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        URL url = new URL();
        url.setBaseURL(lat, lon);
        @SuppressLint("NotifyDataSetChanged") StringRequest stringRequest = new StringRequest(Request.Method.GET, url.getBaseURL(),
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("hourly");
                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                        String todayDate = dateFormat.format(new Date());
                        for(int i =0 ; i < jsonArray.length(); i++){
                            JSONObject hourly = jsonArray.getJSONObject(i);
                            String dt = hourly.getString("dt");
                            Date date = inputFormat.parse(dt);
                            hour = outputFormat.format(date);
                            String entryDate = dateFormat.format(date);

                            if(entryDate.equals(todayDate)){
                                tempHourly = hourly.getDouble("temp");
//                                String mainWeather = hourly.getJSONArray("weather").getJSONObject(0).getString("main");
                                weatherDescription = hourly.getJSONArray("weather").getJSONObject(0).getString("description");
                                iconHourly = hourly.getJSONArray("weather").getJSONObject(0).getString("icon");

                                items.add(new Hourly(hour, tempHourly, iconHourly));
                            }
                        }
                        hourlyAdapter.notifyDataSetChanged();
                    } catch (JSONException | ParseException e) {
                        Toast.makeText(this, "Erorr json hourly", Toast.LENGTH_SHORT).show();
                    }
                },error -> Log.e("result", "JSON parsing error: " + error.getMessage())
        );
        requestQueue.add(stringRequest);
    }

    @SuppressLint("SetTextI18n")
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