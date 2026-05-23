package com.example.trustmebro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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

public class IPCEscalationActivity extends AppCompatActivity {

    private static final String TAG = "TrustMeBro";
    private TextView logView;
    private EditText formatInput;
    private UnprotectedReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ipc_escalation);

        logView = findViewById(R.id.log_view);
        formatInput = findViewById(R.id.format_input);
        Button exportButton = findViewById(R.id.export_button);
        Button backButton = findViewById(R.id.back_button);

        receiver = new UnprotectedReceiver(logView);
        IntentFilter filter = new IntentFilter("com.example.trustmebro.EXPORT_USERS");
        registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);

        exportButton.setOnClickListener(v -> triggerExport());
        backButton.setOnClickListener(v -> {
            try {
                unregisterReceiver(receiver);
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            }
            finish();
        });

        logView.setText("Ready to export user database\nNo permission checks on receiver");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void triggerExport() {
        String format = formatInput.getText().toString();
        if (format.isEmpty()) format = "csv";

        Intent intent = new Intent("com.example.trustmebro.EXPORT_USERS");
        intent.putExtra("format", format);
        intent.putExtra("table", "users");
        intent.putExtra("include_passwords", true);

        sendBroadcast(intent);
        Toast.makeText(this, "Export triggered", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            Log.e(TAG, "Error", e);
        }
    }

    public static class UnprotectedReceiver extends BroadcastReceiver {
        private final TextView logView;

        UnprotectedReceiver(TextView logView) {
            this.logView = logView;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            String format = intent.getStringExtra("format");
            String table = intent.getStringExtra("table");
            boolean includePasswords = intent.getBooleanExtra("include_passwords", false);

            String log = " BROADCAST RECEIVED\n\n" +
                            "Format: " + format + "\n" +
                            "Table: " + table + "\n" +
                            "Passwords: " + includePasswords + "\n\n" +
                            "Exporting from /data/data/.../app.db\n\n" +
                            "No signature verification!\n";

            logView.post(() -> logView.setText(log));
        }
    }
}