package com.somnium.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomepageActivity extends AppCompatActivity {

    private EditText dreamInput;
    private Button analyzeButton;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);  // Ensure this file exists

        // Initializing UI components
        dreamInput = findViewById(R.id.dream_input);
        analyzeButton = findViewById(R.id.analyze_button);
        resultText = findViewById(R.id.result_text);

        // Set click listener for the analyze button
        analyzeButton.setOnClickListener(v -> {
            String dreamText = dreamInput.getText().toString();

            if (dreamText.isEmpty()) {
                Toast.makeText(HomepageActivity.this, "Please enter your dream", Toast.LENGTH_SHORT).show();
            } else {
                // Call the Hugging Face API for dream analysis
                analyzeDream(dreamText);
            }
        });
    }

    // Method to send the dream text to Hugging Face API and get the result
    private void analyzeDream(String dreamText) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api-inference.huggingface.co/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Replace "your-model-name" with the model name you want to use from Hugging Face
        HuggingFaceApi api = retrofit.create(HuggingFaceApi.class);

        // Make the API call
        Call<HuggingFaceResponse> call = api.analyzeDream(dreamText);
        call.enqueue(new Callback<HuggingFaceResponse>() {
            @Override
            public void onResponse(Call<HuggingFaceResponse> call, Response<HuggingFaceResponse> response) {
                if (response.isSuccessful()) {
                    HuggingFaceResponse hugResponse = response.body();
                    if (hugResponse != null) {
                        // Display the result in the TextView
                        resultText.setText(hugResponse.getGeneratedText());
                    }
                } else {
                    Toast.makeText(HomepageActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HuggingFaceResponse> call, Throwable t) {
                Toast.makeText(HomepageActivity.this, "Failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
