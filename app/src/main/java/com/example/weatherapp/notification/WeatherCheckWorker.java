package com.example.weatherapp.notification;

import static com.example.weatherapp.UpdateUI.getIconID;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherapp.R;
import com.example.weatherapp.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherCheckWorker extends Worker {

    public WeatherCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        double lat = getInputData().getDouble("latitude", 0.0);
        double lon = getInputData().getDouble("longitude", 0.0);
        Log.d("WeatherCheckWorker", "Received Latitude: " + lat + ", Longitude: " + lon);

        try {
            // Gọi API và kiểm tra thời tiết
            checkHourlyWeather(lat, lon);
            return Result.success();
        } catch (Exception e) {
            Log.e("WeatherCheckWorker", "Error checking weather", e);
            return Result.retry(); // Thử lại nếu xảy ra lỗi
        }
    }

    private void checkHourlyWeather(double lat, double lon) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        URL url = new URL();
        url.setBaseURL(lat, lon);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url.getBaseURL(),
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("hourly");

                        long currentTime = System.currentTimeMillis();
                        boolean badWeatherAlerted = false;

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject hourly = jsonArray.getJSONObject(i);
                            long dt = hourly.getLong("dt") * 1000L;

                            if (dt > currentTime && dt <= currentTime + 3600 * 1000L) {
                                int idHourly = hourly.getJSONArray("weather").getJSONObject(0).getInt("id");
                                String description = hourly.getJSONArray("weather").getJSONObject(0).getString("description");
                                String icon = hourly.getJSONArray("weather").getJSONObject(0).getString("icon");

                                if (!badWeatherAlerted && isBadWeather(idHourly)) {
                                    badWeatherAlerted = true;
                                    sendBadWeatherNotification(description, icon);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("WeatherCheckWorker", "JSON parsing error", e);
                    }
                },
                error -> Log.e("WeatherCheckWorker", "Error fetching weather data", error)
        );

        requestQueue.add(stringRequest);
    }

    private boolean isBadWeather(int idHourly) {
        // Điều kiện thời tiết xấu
        return (idHourly >= 200 && idHourly <= 233) ||
                (idHourly >= 300 && idHourly <= 330) ||
                (idHourly >= 500 && idHourly <= 540) ||
                (idHourly >= 600 && idHourly <= 630) ||
                (idHourly >= 700 && idHourly <= 790) ;
    }

    @SuppressLint({"MissingPermission", "ObsoleteSdkInt"})
    private void sendBadWeatherNotification(String weatherDescription, String icon) {
        String channelId = "weather_alert_channel";
        String channelName = "Weather Alerts";

        // Tạo kênh thông báo (chỉ cần tạo 1 lần, Android 8.0 trở lên)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Thông báo thời tiết xấu");
            NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        int iconResId = getIconID(icon); // Lấy icon từ mã thời tiết
        Bitmap largeIcon = BitmapFactory.decodeResource(getApplicationContext().getResources(), iconResId);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.cloudy) // Icon nhỏ
                .setLargeIcon(largeIcon) // Icon lớn
                .setContentTitle("Cảnh báo thời tiết xấu")
                .setContentText("Thời tiết xấu: " + weatherDescription)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // Tự động xóa thông báo khi người dùng nhấn vào

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(1, builder.build());
    }
}
