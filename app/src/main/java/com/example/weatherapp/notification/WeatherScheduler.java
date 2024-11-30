package com.example.weatherapp.notification;

import android.content.Context;
import android.util.Log;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class WeatherScheduler {

    public static void scheduleWeatherCheck(Context context, double lat, double lon) {
        // Chuẩn bị dữ liệu đầu vào
        Data inputData = new Data.Builder()
                .putDouble("latitude", lat)
                .putDouble("longitude", lon)
                .build();
        // Thiết lập Worker chạy mỗi giờ
        PeriodicWorkRequest weatherRequest = new PeriodicWorkRequest.Builder(
                WeatherCheckWorker.class,
                1, TimeUnit.HOURS // Tần suất chạy: mỗi 1 giờ
        )
                .setInputData(inputData) // Gửi dữ liệu
                .build();

        // Đặt lịch Worker
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "WeatherCheck", // Tên công việc
                ExistingPeriodicWorkPolicy.REPLACE, // Thay thế công việc nếu đã tồn tại
                weatherRequest
        );
    }
}

