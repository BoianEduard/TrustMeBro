package com.example.trustmebro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "TrustMeBro";

    private static final String CORRECT_USERNAME = "admin";
    private static final String CORRECT_PASSWORD_MD5 = "21232f297a57a5a743894a0e4a801fc3";
    
    private EditText usernameInput;
    private EditText passwordInput;
    private TextView statusView;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        statusView = findViewById(R.id.status_view);
        loginButton = findViewById(R.id.login_button);
        Button exitButton = findViewById(R.id.exit_button);

        statusView.setText("Authorized Personnel Only");

        loginButton.setOnClickListener(v -> attemptLogin());
        exitButton.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void attemptLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            statusView.setText("Please enter username and password");
            return;
        }

        // VULNERABLE: Check username in plaintext
        if (!username.equals(CORRECT_USERNAME)) {
            statusView.setText("Invalid credentials");
            return;
        }

        // VULNERABLE: Hash password using weak MD5
        String passwordHash = md5Hash(password);

        // VULNERABLE: Compare MD5 hashes (reversible!)
        if (!passwordHash.equals(CORRECT_PASSWORD_MD5)) {
            statusView.setText("Invalid credentials");
            return;
        }

        // Login successful
        Log.d(TAG, "[LOGIN_SUCCESS] User logged in: " + username);
        statusView.setText("LOGIN SUCCESSFUL");
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

        // Set logged_in flag
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("logged_in", true);
        editor.putString("username", username);
        editor.apply();

        usernameInput.postDelayed(() -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 1000);
    }

    private String md5Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] messageDigest = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "";
        }
    }
}