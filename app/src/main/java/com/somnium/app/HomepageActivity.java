package com.somnium.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class HomepageActivity extends AppCompatActivity {

    private static final String API_URL = "https://api-inference.huggingface.co/models/openai-community/gpt2-large";
    private static final String API_KEY = "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";

    private EditText dreamInput;
    private Button analyzeButton;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        dreamInput = findViewById(R.id.dream_input);
        analyzeButton = findViewById(R.id.analyze_button);
        resultText = findViewById(R.id.result_text);

        analyzeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dreamText = dreamInput.getText().toString().trim();
                if (dreamText.isEmpty()) {
                    Toast.makeText(HomepageActivity.this, "Please enter a dream to analyze!", Toast.LENGTH_SHORT).show();
                } else {
                    analyzeDream(dreamText);
                }
            }
        });
    }

    private void analyzeDream(String dreamText) {
        new Thread(() -> {
            try {
                String payload = "{ \"inputs\": \"Dream interpretation: " + dreamText + "\" }";
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                os.write(payload.getBytes());
                os.flush();
                os.close();

                Scanner scanner = new Scanner(connection.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                String formattedResponse = formatResponse(response.toString());
                runOnUiThread(() -> resultText.setText(formattedResponse));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(HomepageActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    resultText.setText("Failed to analyze dream.");
                });
            }
        }).start();
    }

    private String formatResponse(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            JSONObject firstObject = jsonArray.getJSONObject(0);
            String generatedText = firstObject.getString("generated_text");

            String[] points = generatedText.split("\\. ");
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < Math.min(points.length, 5); i++) { // Limit to 5 points
                formatted.append((i + 1)).append(". ").append(points[i].trim()).append("\n");
            }
            return formatted.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to interpret the response.";
        }
    }
}
