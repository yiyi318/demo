package com.example.myapplication.utils.service;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import java.util.Date;

public class NetworkMonitorService extends Service {
    private static final String TAG = "NetworkMonitorService";
    private ConnectivityManager.NetworkCallback networkCallback;
    private long disconnectTime = 0;
    private boolean isConnected = true;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");

        // 创建网络请求
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        // 创建网络回调
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                if (!isConnected) {
                    isConnected = true;
                    long duration = (System.currentTimeMillis() - disconnectTime) / 1000;
                    showNotification("网络已恢复", "断网时长: " + duration + "秒");
                    Log.d(TAG, "Network reconnected after " + duration + " seconds");
                }
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                if (isConnected) {
                    isConnected = false;
                    disconnectTime = System.currentTimeMillis();
                    Log.d(TAG, "Network disconnected at " + new Date(disconnectTime));
                }
            }
        };

        // 注册网络回调
        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    private void showNotification(String title, String message) {
        // 在主线程显示Toast
        Toast.makeText(this, title + "\n" + message, Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销网络回调
        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        connectivityManager.unregisterNetworkCallback(networkCallback);
        Log.d(TAG, "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}