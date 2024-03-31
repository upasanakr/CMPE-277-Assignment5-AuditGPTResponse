package com.example.myassignment5;

import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import org.json.JSONArray;

public class MainActivity extends AppCompatActivity {

    private EditText promptInput;
    private Button sendBtn, saveBtn;
    private TextView responseView, saveStatusView;
    private AuditLogManager logManager;
    private Button cancelBtn;

    private static final String API_KEY = "PLACE_YOUR_API_KEY_HERE";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        logManager = new AuditLogManager(this);

        sendBtn.setOnClickListener(v -> new FetchResponseTask().execute(promptInput.getText().toString()));
        saveBtn.setOnClickListener(v -> saveAuditLog(promptInput.getText().toString(), responseView.getText().toString()));
        cancelBtn.setOnClickListener(v -> clearScreen());
    }

    private void initializeViews() {
        promptInput = findViewById(R.id.editTextPrompt);
        sendBtn = findViewById(R.id.buttonSend);
        saveBtn = findViewById(R.id.buttonSave);
        cancelBtn = findViewById(R.id.buttoncancel);
        responseView = findViewById(R.id.textViewResponseContent);
        saveStatusView = findViewById(R.id.textViewSaveStatus);
    }
    private void clearScreen() {
        promptInput.setText("");
        responseView.setText("");
        saveStatusView.setText("");
    }

    private void saveAuditLog(String prompt, String response) {
        if (!prompt.isEmpty() && !response.isEmpty()) {
            logManager.saveEntry(prompt, response);
            saveStatusView.setText("Response saved successfully.");
        } else {
            saveStatusView.setText("Empty prompt or response, nothing saved.");
        }
    }

    private class FetchResponseTask extends AsyncTask<String, Void, String> {
        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(String... prompts) {
            try {
                String payload = generateJsonPayload(prompts[0]);
                RequestBody body = RequestBody.create(payload, JSON);
                Request request = new Request.Builder()
                        .url("https://api.openai.com/v1/chat/completions")
                        .addHeader("Authorization", "Bearer " + "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")
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    return parseResponse(response);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Failed to fetch response: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            responseView.setText(result);
        }

        private String generateJsonPayload(String prompt) {
            // Returns JSON payload string for the request
            return "{\"model\": \"gpt-3.5-turbo\",\"messages\": [{\"role\": \"user\",\"content\": \"" + prompt.replace("\"", "\\\"") + "\"}]}";
        }

        private String parseResponse(Response response) throws Exception {
            // Parses the response body to extract the message content
            if (response.isSuccessful() && response.body() != null) {
                JSONObject jsonResponse = new JSONObject(response.body().string());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject choice = choices.getJSONObject(0);
                    // Assuming the message content is in the "content" field of "message" object
                    JSONObject message = choice.getJSONObject("message");
                    // Extract only the "content" field
                    return message.getString("content");
                }
            }
            return "No response received or error in fetching.";
        }
    }
}
