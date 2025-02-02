package com.somnium.app;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Header;

public interface ApiService {
    @Headers({
            "Content-Type: application/json",
            "HTTP-Referer: https://your-app.com",
            "X-Title: Somnium"
    })
    @POST("api/v1/chat/completions")
    Call<ApiResponse> getCompletion(@Body ApiRequest request);
}