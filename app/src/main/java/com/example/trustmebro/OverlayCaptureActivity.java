package com.example.trustmebro;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.KeyEvent;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OverlayCaptureActivity extends AppCompatActivity {

    private static final String TAG = "TrustMeBro";
    private EditText cardNumberInput;
    private EditText cvvInput;
    private EditText expiryInput;
    private TextView orderSummary;
    private TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_overlay_capture);

        cardNumberInput = findViewById(R.id.card_number_input);
        cvvInput = findViewById(R.id.cvv_input);
        expiryInput = findViewById(R.id.expiry_input);
        orderSummary = findViewById(R.id.order_summary);
        statusView = findViewById(R.id.status_view);
        Button payButton = findViewById(R.id.pay_button);
        Button backButton = findViewById(R.id.back_button);

        // VULNERABLE: No FLAG_SECURE - screen can be captured, recorded, overlayed

        orderSummary.setText("Order Summary\n\n" +
                "Item: Premium Subscription\n" +
                "Price: $99.99\n" +
                "Tax: $8.00\n" +
                "Total: $107.99\n\n" +
                "Delivery: Standard (5-7 days)");

        payButton.setOnClickListener(v -> processPayment());
        backButton.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void processPayment() {
        String cardNumber = cardNumberInput.getText().toString();
        String cvv = cvvInput.getText().toString();
        String expiry = expiryInput.getText().toString();

        if (cardNumber.isEmpty() || cvv.isEmpty() || expiry.isEmpty()) {
            statusView.setText("✗ Please fill all fields");
            return;
        }

        // VULNERABLE: Logging sensitive data
        Log.d(TAG, "Processing payment - Card: " + cardNumber + " CVV: " + cvv + " Expiry: " + expiry);

        statusView.setText(" Processing payment...\nPlease wait...");

        // Simulate processing
        cardNumberInput.postDelayed(() -> {
            statusView.setText("✓ PAYMENT SUCCESSFUL\n\n" +
                    "Transaction ID: TXN_2024_" + System.currentTimeMillis() + "\n" +
                    "Card: " + cardNumber.substring(0, 4) + " **** **** " + cardNumber.substring(cardNumber.length() - 4) + "\n" +
                    "Amount: $107.99\n" +
                    "Status: Completed\n\n" +
                    "Order confirmation sent to your email");
            Toast.makeText(this, "Payment successful!", Toast.LENGTH_SHORT).show();
        }, 2000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // VULNERABLE: No protection against key interception
        Log.d(TAG, "Key pressed: " + keyCode);
        return super.onKeyDown(keyCode, event);
    }
}