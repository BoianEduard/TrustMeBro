package com.example.trustmebro;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Base64;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoKeyActivity extends AppCompatActivity {

    private static final String TAG = "TrustMeBro";

    // VULNERABLE: Hardcoded encryption keys in memory
    // AES keys must be exactly 16, 24, or 32 bytes long
    private static final String MASTER_KEY = "MasterKey2026MobileDevicesABCD26"; // 32 bytes (256-bit)
    private static final String PAYMENT_TOKEN_SECRET = "PaymentTokenSecret123456789AB32"; // 32 bytes (256-bit)
    private static final String OAUTH_CLIENT_SECRET = "OAuthClientSecr"; // 16 bytes (128-bit)

    private EditText creditCardInput;
    private EditText amountInput;
    private TextView encryptedTokenView;
    private TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crypto_key);

        creditCardInput = findViewById(R.id.credit_card_input);
        amountInput = findViewById(R.id.amount_input);
        encryptedTokenView = findViewById(R.id.encrypted_token_view);
        statusView = findViewById(R.id.status_view);
        Button tokenizeButton = findViewById(R.id.tokenize_button);
        Button decryptButton = findViewById(R.id.decrypt_button);
        Button backButton = findViewById(R.id.back_button);

        tokenizeButton.setOnClickListener(v -> tokenizeCard());
        decryptButton.setOnClickListener(v -> decryptToken());
        backButton.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void tokenizeCard() {
        String card = creditCardInput.getText().toString();
        String amount = amountInput.getText().toString();

        if (card.isEmpty() || amount.isEmpty()) {
            Toast.makeText(this, "Enter card and amount", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String payload = card + "|" + amount + "|" + System.currentTimeMillis();
            String encrypted = encryptAES(payload, MASTER_KEY);

            encryptedTokenView.setText(encrypted);
            statusView.setText("Token: tok_" + encrypted.substring(0, 20) + "...\nKeys in plaintext memory");
            Log.d(TAG, "Encrypted: " + encrypted);
        } catch (Exception e) {
            statusView.setText("Error: " + e.getMessage());
        }
    }

    private void decryptToken() {
        String token = encryptedTokenView.getText().toString();
        if (token.isEmpty()) {
            Toast.makeText(this, "No token to decrypt", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String decrypted = decryptAES(token, MASTER_KEY);
            String[] parts = decrypted.split("\\|");

            creditCardInput.setText(parts[0]);
            amountInput.setText(parts[1]);
            statusView.setText("Token decrypted\nCard exposed from memory");
        } catch (Exception e) {
            statusView.setText("Error decrypting");
        }
    }

    private String encryptAES(String data, String key) throws Exception {
        byte[] decodedKey = key.getBytes();
        SecretKeySpec secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        // Using explicit transformation is recommended, but keeping simple for the assignment
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.encodeToString(encrypted, Base64.DEFAULT).replaceAll("\n", "");
    }

    private String decryptAES(String encrypted, String key) throws Exception {
        byte[] decodedKey = key.getBytes();
        SecretKeySpec secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decoded = Base64.decode(encrypted, Base64.DEFAULT);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }

    // VULNERABLE: Public methods expose keys
    public static String getMasterKey() {
        return MASTER_KEY;
    }

    public static String getPaymentTokenSecret() {
        return PAYMENT_TOKEN_SECRET;
    }
}