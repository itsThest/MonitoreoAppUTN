package com.example.monitoreoapputn;

import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;
import android.os.storage.StorageManager;
import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class HTTPServer extends NanoHTTPD {
    public static final int PORT = 8888;
    private final Context context;
    private final DatabaseHelper dbHelper;
    private final String deviceId;

    public HTTPServer(Context context, DatabaseHelper dbHelper) throws IOException {
        super(PORT);
        this.context = context;
        this.dbHelper = dbHelper;
        this.deviceId = MainActivity.DEVICE_ID;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        String authHeader = session.getHeaders().get("authorization");
        String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;

        if (token == null || !dbHelper.validateToken(token)) {
            return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Unauthorized");
        }

        if (uri.equals("/api/sensor_data") && session.getMethod().equals(Method.GET)) {
            Map<String, String> params = session.getParms();
            String startTime = params.getOrDefault("start_time", "0");
            String endTime = params.getOrDefault("end_time", String.valueOf(System.currentTimeMillis()));
            String data = dbHelper.getLocationData(Long.parseLong(startTime), Long.parseLong(endTime));
            return newFixedLengthResponse(Response.Status.OK, "application/json", data);
        } else if (uri.equals("/api/device_status") && session.getMethod().equals(Method.GET)) {
            String status = getDeviceStatus();
            return newFixedLengthResponse(Response.Status.OK, "application/json", status);
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not found");
    }

    private String getDeviceStatus() {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        long freeSpace = 0;
        try {
            StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            UUID storageUuid = StorageManager.UUID_DEFAULT;
            freeSpace = storageStatsManager.getFreeBytes(storageUuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String networkStatus = context.getSystemService(Context.CONNECTIVITY_SERVICE) != null ? "Connected" : "Disconnected";

        return String.format("{\"battery_level\":%d,\"network_status\":\"%s\",\"free_space\":%d,\"os_version\":\"%s\",\"model\":\"%s\",\"device_id\":\"%s\"}",
                batteryLevel, networkStatus, freeSpace, Build.VERSION.RELEASE, Build.MODEL, deviceId);
    }
}