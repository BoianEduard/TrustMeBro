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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class IPCEscalationActivity extends AppCompatActivity {

    private static final String TAG = "TrustMeBro";
    private static final String ACTION_EXPORT = "com.example.trustmebro.EXPORT_DATA";
    private static TextView sLogView; 
    private UnprotectedReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ipc_escalation);

        sLogView = findViewById(R.id.log_view);
        final EditText formatInput = findViewById(R.id.format_input);
        Button exportButton = findViewById(R.id.export_button);
        Button backButton = findViewById(R.id.back_button);

        receiver = new UnprotectedReceiver();
        IntentFilter filter = new IntentFilter(ACTION_EXPORT);
        
        // VULNERABLE: Registering as EXPORTED so ADB can trigger it while Activity is open
        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_EXPORTED);

        exportButton.setOnClickListener(v -> {
            Intent intent = new Intent(ACTION_EXPORT);
            intent.putExtra("format", formatInput.getText().toString());
            intent.putExtra("table", "users_internal");
            intent.putExtra("include_passwords", true);
            intent.putExtra("source", "internal");
            sendBroadcast(intent);
        });

        backButton.setOnClickListener(v -> finish());

        sLogView.setText("Waiting for IPC broadcast...\n\nTarget Action: " + ACTION_EXPORT);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sLogView = null;
        try {
            unregisterReceiver(receiver);
        } catch (Exception ignored) {}
    }

    // Static inner class for manifest registration and dynamic registration
    public static class UnprotectedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String format = intent.getStringExtra("format");
            String table = intent.getStringExtra("table");
            String source = intent.getStringExtra("source");
            boolean includePasswords = intent.getBooleanExtra("include_passwords", false);

            boolean isExternal = !"internal".equals(source);
            String title = isExternal ? "EXTERNAL EXPLOIT SUCCESS" : "INTERNAL BROADCAST RECEIVED";

            StringBuilder sb = new StringBuilder();
            sb.append(title).append("\n\n");
            sb.append("Extracted Format: ").append(format != null ? format : "csv").append("\n");
            sb.append("Extracted Table: ").append(table != null ? table : "users").append("\n");
            sb.append("Passwords Leaked: ").append(includePasswords).append("\n\n");
            
            sb.append("Dumping sensitive data to system logs (VULN_IPC)...\n");
            sb.append("---------------------------------\n");
            sb.append("ID | USERNAME | HASH/TOKEN\n");
            sb.append("1  | admin    | 21232f297a57...\n");
            sb.append("2  | michael  | sk_live_51Iv...\n");
            sb.append("3  | sarah    | ya29.a0AfH6...\n");
            sb.append("---------------------------------");

            String output = sb.toString();
            Log.e("VULN_IPC", output);
            
            if (isExternal) {
                Toast.makeText(context, "IPC EXPLOIT SUCCESS!", Toast.LENGTH_SHORT).show();
            }

            if (sLogView != null) {
                sLogView.post(() -> sLogView.setText(output));
            }
        }
    }
}