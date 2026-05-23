package com.example.trustmebro;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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

public class DatabaseLeakActivity extends AppCompatActivity {

    private static final String TAG = "TrustMeBro";
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private EditText usernameInput;
    private EditText passwordInput;
    private TextView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_database_leak);

        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        resultView = findViewById(R.id.result_view);
        Button loginButton = findViewById(R.id.login_button);
        Button backButton = findViewById(R.id.back_button);

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getWritableDatabase();
        initializeDatabase();

        loginButton.setOnClickListener(v -> attemptLogin());
        backButton.setOnClickListener(v -> finish());

        resultView.setText("Admin Portal\n\nTest: admin / admin123\nOr: john / john456");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeDatabase() {
        try {
            database.execSQL("DROP TABLE IF EXISTS admin_users");
            database.execSQL("CREATE TABLE admin_users (" +
                    "id INTEGER PRIMARY KEY," +
                    "username TEXT UNIQUE," +
                    "password TEXT," +
                    "email TEXT," +
                    "role TEXT," +
                    "last_login TEXT)");

            database.execSQL("INSERT INTO admin_users VALUES (1, 'admin', 'admin123', 'admin@company.com', 'super_admin', '2024-05-20')");
            database.execSQL("INSERT INTO admin_users VALUES (2, 'john', 'john456', 'john@company.com', 'admin', '2024-05-19')");
            database.execSQL("INSERT INTO admin_users VALUES (3, 'sarah', 'sarah789', 'sarah@company.com', 'moderator', '2024-05-18')");
            database.execSQL("INSERT INTO admin_users VALUES (4, 'dev_user', 'dev_pass_2024', 'dev@company.com', 'developer', '2024-05-15')");

            Log.d(TAG, "Database initialized");
        } catch (Exception e) {
            Log.e(TAG, "Database error", e);
        }
    }

    private void attemptLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            resultView.setText("✗ Please enter username and password");
            return;
        }

        // VULNERABLE: String concatenation in SQL query
        String query = "SELECT * FROM admin_users WHERE username='" + username + "' AND password='" + password + "'";

        Log.d(TAG, "Query: " + query);

        try {
            Cursor cursor = database.rawQuery(query, null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                String result = " LOGIN SUCCESSFUL\n\n";
                result += "Welcome, " + cursor.getString(1) + "\n";
                result += "Role: " + cursor.getString(4) + "\n";
                result += "Email: " + cursor.getString(3) + "\n";
                result += "Last Login: " + cursor.getString(5) + "\n\n";
                result += "Access granted to admin panel";

                resultView.setText(result);
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
            } else {
                resultView.setText("Invalid username or password");
            }
            cursor.close();
        } catch (Exception e) {
            resultView.setText("✗ Error: " + e.getMessage());
            Log.e(TAG, "Error", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) database.close();
        if (dbHelper != null) dbHelper.close();
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, "admin_portal.db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {}

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
    }
}