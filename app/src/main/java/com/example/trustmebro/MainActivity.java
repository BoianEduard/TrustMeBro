package com.example.trustmebro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button backupButton = findViewById(R.id.backup_leak_button);
        backupButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BackupLeakActivity.class)));

        Button cryptoButton = findViewById(R.id.crypto_key_button);
        cryptoButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CryptoKeyActivity.class)));

        Button ipcButton = findViewById(R.id.ipc_escalation_button);
        ipcButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, IPCEscalationActivity.class)));

        Button databaseButton = findViewById(R.id.database_leak_button);
        databaseButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DatabaseLeakActivity.class)));

        Button webviewButton = findViewById(R.id.webview_button);
        webviewButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, WebViewHijackingActivity.class)));

        Button overlayButton = findViewById(R.id.overlay_capture_button);
        overlayButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, OverlayCaptureActivity.class)));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}