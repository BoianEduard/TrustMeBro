package com.example.trustmebro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BackupLeakActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "api_credentials";
    private EditText apiKeyInput;
    private EditText webhookInput;
    private EditText dbPasswordInput;
    private TextView credentialsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_backup_leak);

        apiKeyInput = findViewById(R.id.api_key_input);
        webhookInput = findViewById(R.id.webhook_input);
        dbPasswordInput = findViewById(R.id.db_password_input);
        credentialsView = findViewById(R.id.credentials_view);
        Button saveButton = findViewById(R.id.save_button);
        Button loadButton = findViewById(R.id.load_button);
        Button backButton = findViewById(R.id.back_button);

        loadStoredCredentials();

        saveButton.setOnClickListener(v -> saveCredentials());
        loadButton.setOnClickListener(v -> loadStoredCredentials());
        backButton.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @SuppressLint("SetTextI18n")
    private void saveCredentials() {
        String apiKey = apiKeyInput.getText().toString();
        String webhook = webhookInput.getText().toString();
        String dbPass = dbPasswordInput.getText().toString();

        if (apiKey.isEmpty() || webhook.isEmpty() || dbPass.isEmpty()) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("stripe_api_key", apiKey);
        editor.putString("webhook_url", webhook);
        editor.putString("postgres_password", dbPass);
        editor.putString("firebase_token", "AIzaSyC8v1_qY6p8H2kL3mN4oP5qR6sT7uV8wX9Y");
        editor.putString("jwt_secret", "your-256-bit-secret-key-for-jwt-tokens-encryption");
        editor.putString("oauth_refresh_token", "ya29.a0AfH6SMBx8Z9Y0Q1W2E3R4T5Y6U7I8O9P0A1S2D3F4G5H6J7K8L9M");
        editor.putString("database_user", "admin_prod");
        editor.putString("redis_password", "redis_prod_2024_secure_pass");
        editor.putString("aws_access_key", "AKIAIOSFODNN7EXAMPLE");
        editor.putString("aws_secret_key", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");

        editor.apply();

        credentialsView.setText("Creds saved");
        Toast.makeText(this, "Credentials saved (plaintext)", Toast.LENGTH_SHORT).show();
    }

    private void loadStoredCredentials() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String apiKey = prefs.getString("stripe_api_key", "sk-live-51234567890abcdefgh");
        String webhook = prefs.getString("webhook_url", "https://api.company.com/webhook");
        String dbPass = prefs.getString("postgres_password", "PgPass@2024Secure!");

        apiKeyInput.setText(apiKey);
        webhookInput.setText(webhook);
        dbPasswordInput.setText(dbPass);

        credentialsView.setText("Loaded from SharedPreferences (plaintext)");
    }
}