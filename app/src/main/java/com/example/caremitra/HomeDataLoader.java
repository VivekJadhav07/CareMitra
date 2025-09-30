package com.example.caremitra;

import android.content.Context;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeDataLoader {

    private ApiService apiService;

    // Data model for all home screen data
    public static class HomeData {
        String heartRate = "75 bpm";
        String oxygenLevel = "97%";
        String bodyTemp = "98.4°F";
        String sugarLevel = "98 mg/dL";
        // CHANGED: Added more alert messages to the list
        String[] alerts = {
                "High Heart Rate detected at 4:30 PM",
                "Medication due: Paracetamol",
                "Upcoming appointment: Dr. Sharma, Tomorrow 11:30 AM",
                "Low SpO2 reading this morning"
        };


    List<AppointmentDetails> scheduledAppointments; // This will hold our real data
    }

    // Listener interface to send data back to the activity
    public interface DataLoadListener {
        void onDataLoaded(HomeData data);
        void onDataFailed(String error);
    }

    public HomeDataLoader(Context context) {
        // Initialize the ApiService using your existing ApiClient
        // This assumes your ApiClient is set up correctly
        apiService = ApiClient.getClient(context).create(ApiService.class);
    }

    public void loadHomeData(DataLoadListener listener) {
        // Make the network call to get ONLY scheduled appointments
        Call<List<AppointmentDetails>> call = apiService.getAppointments("eq.scheduled");

        call.enqueue(new Callback<List<AppointmentDetails>>() {
            @Override
            public void onResponse(Call<List<AppointmentDetails>> call, Response<List<AppointmentDetails>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Create a HomeData object to hold everything
                    HomeData homeData = new HomeData();
                    // Put the real appointment list into our HomeData object
                    homeData.scheduledAppointments = response.body();

                    // Send the complete data object back to HomeActivity
                    listener.onDataLoaded(homeData);
                } else {
                    listener.onDataFailed("Failed to fetch appointments. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentDetails>> call, Throwable t) {
                listener.onDataFailed("Network Error: " + t.getMessage());
            }
        });
    }
}