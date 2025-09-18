package com.example.caremitra;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("appointment_details")
    Call<List<AppointmentDetails>> getAppointments(
            @Query("status") String status
    );
}
