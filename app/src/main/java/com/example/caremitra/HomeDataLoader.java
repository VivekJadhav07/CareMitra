package com.example.caremitra;

import android.content.Context;
import android.os.Handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class HomeDataLoader {

    private final Context context;
    private final DataLoadListener listener;
    private final NetworkHelper networkHelper;

    public interface DataLoadListener {
        void onDataLoaded(HomeData data);
        void onDataFailed(String error);
    }

    public HomeDataLoader(Context context, DataLoadListener listener) {
        this.context = context;
        this.listener = listener;
        this.networkHelper = new NetworkHelper(context);
    }

    public void loadHomeData() {
        if (!networkHelper.isNetworkAvailable()) {
            listener.onDataFailed("No internet connection");
            return;
        }

        // Simulate API call with delay
        new Handler().postDelayed(() -> {
            try {
                // Simulate data loading
                HomeData data = generateSampleData();
                listener.onDataLoaded(data);
            } catch (Exception e) {
                listener.onDataFailed("Failed to load data: " + e.getMessage());
            }
        }, 2000); // 2 second delay to show loading
    }

    private HomeData generateSampleData() {
        Random random = new Random();
        String timestamp = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        HomeData data = new HomeData();
        data.welcomeMessage = "Welcome, Bro";
        data.networkStatus = "Connected - Last updated: " + timestamp;
        data.sugarLevel = "Sugar Level: " + (80 + random.nextInt(40)) + " mg/dL";
        data.heartRate = "❤️ Heart Rate: " + (60 + random.nextInt(40)) + " bpm";
        data.oxygenLevel = "🔵 Oxygen Level: " + (95 + random.nextInt(5)) + "%";
        data.bodyTemp = "🌡️ Body Temp: " + (96 + random.nextInt(4)) + "." + random.nextInt(10) + "°F";

        data.alerts = new String[]{
                "• Lab Report uploaded by Dr. Sharma - 2h ago",
                "• Appointment confirmed for tomorrow - 1h ago",
                "• Prescription refill reminder - 3h ago",
                "• Monthly health checkup due - 1 day ago"
        };

        return data;
    }

    public static class HomeData {
        public String welcomeMessage;
        public String networkStatus;
        public String sugarLevel;
        public String heartRate;
        public String oxygenLevel;
        public String bodyTemp;
        public String[] alerts;
    }
}
