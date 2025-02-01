package com.somnium.app;

public class ApiClient {
    private static final String BASE_URL = "https://openrouter.ai/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(String apiKey) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .header("Authorization", "Bearer " + apiKey)
                                .header("HTTP-Referer", "https://your-app.com")
                                .header("X-Title", "Somnium")
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}