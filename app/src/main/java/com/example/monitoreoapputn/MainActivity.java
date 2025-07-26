package com.example.monitoreoapputn;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private LocationManager locationManager;
    private Handler handler;
    private boolean isCollecting = false;
    private DatabaseHelper dbHelper;
    private TextView statusTextView, ipTextView;
    private HTTPServer httpServer;
    public static final String DEVICE_ID = UUID.randomUUID().toString();
    private static final int COLLECTION_INTERVAL = 30000; // 30 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView = findViewById(R.id.statusTextView);
        ipTextView = findViewById(R.id.ipTextView);
        Button toggleButton = findViewById(R.id.toggleButton);

        dbHelper = new DatabaseHelper(this);
        handler = new Handler(Looper.getMainLooper());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Initialize HTTP server
        try {
            httpServer = new HTTPServer(this, dbHelper);
            httpServer.start(fi.iki.elonen.NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            ipTextView.setText("IP: " + getLocalIpAddress() + ":" + HTTPServer.PORT);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to start server", Toast.LENGTH_SHORT).show();
        }

        // Request location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        toggleButton.setOnClickListener(v -> {
            if (isCollecting) {
                stopDataCollection();
                toggleButton.setText("Start Collection");
            } else {
                startDataCollection();
                toggleButton.setText("Stop Collection");
            }
        });
    }

    private void startDataCollection() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            isCollecting = true;
            statusTextView.setText("Status: Collecting data...");
            collectData();
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopDataCollection() {
        isCollecting = false;
        handler.removeCallbacksAndMessages(null);
        statusTextView.setText("Status: Not collecting");
    }

    private void collectData() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, COLLECTION_INTERVAL, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        long timestamp = System.currentTimeMillis();
                        dbHelper.insertLocationData(location.getLatitude(), location.getLongitude(), timestamp, DEVICE_ID);
                        statusTextView.setText(String.format("Status: Collecting data...\nLast: Lat %f, Lon %f", location.getLatitude(), location.getLongitude()));
                    }
                });
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
        }

        // Schedule next collection
        if (isCollecting) {
            handler.postDelayed(this::collectData, COLLECTION_INTERVAL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startDataCollection();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private String getLocalIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress() && addr instanceof java.net.Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (httpServer != null) {
            httpServer.stop();
        }
        stopDataCollection();
        dbHelper.close();
    }
}