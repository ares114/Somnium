package com.somnium.app;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Header;

public interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("chat/completions") // Use relative URL
    Call<ApiResponse> getCompletion(
            @Header("Authorization") String authToken, // Pass API key dynamically
            @Header("HTTP-Referer") String referer,    // Pass application URL dynamically
            @Header("X-Title") String title,          // Pass application name dynamically
            @Body ApiRequest request
    );
}
