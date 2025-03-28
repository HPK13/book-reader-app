package com.example.e_book1;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView appInfoTextView = findViewById(R.id.app_info);
        String appName = "ReadRealm";
        String version = "0.9";
        appInfoTextView.setText("App Name: " + appName + "\nVersion: " + version);

        // Enable the up button (back button)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // Handle the back button click
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
