package com.example.trustmebro;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WebViewHijackingActivity extends AppCompatActivity {

    private static final String TAG = "TrustMeBro";
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_webview_hijacking);

        webView = findViewById(R.id.webview);
        Button backButton = findViewById(R.id.back_button);

        configureWebView();
        loadBankingPortal();

        backButton.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new android.webkit.WebChromeClient());

        // VULNERABLE: Enable JavaScript
        settings.setJavaScriptEnabled(true);

        // VULNERABLE: Allow file access
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        // VULNERABLE: No mixed content restriction
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebViewClient(new WebViewClient());

        Log.d(TAG, "WebView configured with vulnerabilities");
    }

    private void loadBankingPortal() {
        String html = "<html><head><meta charset='utf-8'><style>" +
                "body{font-family:Arial;background:#f5f5f5;margin:0;padding:10px;}" +
                ".header{background:linear-gradient(135deg,#667eea,#764ba2);color:white;padding:15px;border-radius:8px;margin-bottom:15px;}" +
                ".card{background:white;padding:15px;margin:10px 0;border-radius:8px;box-shadow:0 2px 4px #ccc;}" +
                ".balance{font-size:32px;color:#667eea;font-weight:bold;text-align:center;margin:20px 0;font-family:monospace;}" +
                ".transaction{padding:10px;border-bottom:1px solid #eee;display:flex;justify-content:space-between;}" +
                ".amount{font-weight:bold;color:#667eea;}" +
                ".amount.negative{color:#e74c3c;}" +
                ".btn{width:100%;padding:12px;margin:8px 0;border:none;border-radius:6px;font-size:14px;font-weight:bold;cursor:pointer;background:#667eea;color:white;}" +
                ".btn:active{opacity:0.8;}" +
                "</style></head><body>" +
                "<div class='header'><h1>SecureBank</h1><p>Welcome, Michael Johnson</p><p>Account: ****5678</p></div>" +
                "<div class='card'><h2>Available Balance</h2><div class='balance'>$45,230.50</div></div>" +
                "<div class='card'><h2>Recent Transactions</h2>" +
                "<div class='transaction'><span>Direct Deposit - Acme Corp</span><span class='amount'>+$3,500.00</span></div>" +
                "<div class='transaction'><span>Amazon Purchase</span><span class='amount negative'>-$127.49</span></div>" +
                "<div class='transaction'><span>Monthly Rent</span><span class='amount negative'>-$1,800.00</span></div>" +
                "<div class='transaction'><span>Gas Station</span><span class='amount negative'>-$52.33</span></div>" +
                "<div class='transaction'><span>Restaurant</span><span class='amount negative'>-$68.75</span></div>" +
                "</div>" +
                "<div class='card'><h2>Quick Actions</h2>" +
                "<button class='btn' onclick='transfer()'>Transfer Money</button>" +
                "<button class='btn' onclick='payBill()'>Pay Bills</button>" +
                "<button class='btn' onclick='viewCards()'>View Card Details</button>" +
                "</div>" +
                "<script>" +
                "function transfer(){var amt=prompt('Transfer amount:','100');if(amt)alert('Transferring $'+amt);}" +
                "function payBill(){alert('Bill payment initiated...');}" +
                "function viewCards(){alert('Card ending in 5678\\nExpiry: 12/26\\nCVV: 123');}" +
                "var accountToken='eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjdXN0b21lcl9pZCI6IjEyMzQ1Njc4OTAiLCJhY2NvdW50X251bWJlciI6IjU2NzgiLCJzc24iOiI1NTUtNjYtNzc3NyJ9.Q5S7T8U9V0W1X2Y3Z4A5B6C7D8E9F0';" +
                "var apiKey='sk-live-51Iv1oHGiW8h9K0L1M2N3O4P5Q6R7S8T9U0V1W2X3Y4Z5A6';" +
                "</script>" +
                "</body></html>";

        // Use loadDataWithBaseURL to handle special characters like '#' in CSS
        webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
        }
    }
}