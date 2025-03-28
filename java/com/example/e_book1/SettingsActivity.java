package com.example.e_book1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences sharedPreferences;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the up button (back button)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ImageView profilePicture = findViewById(R.id.profilePicture);
        Button changeAccountButton = findViewById(R.id.changeAccountButton);
        Button saveChangesButton = findViewById(R.id.saveChangesButton);

        changeAccountButton.setOnClickListener(v -> signOut());

        saveChangesButton.setOnClickListener(v -> saveChanges());

        loadProfilePicture();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                updateUI(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Sign-In Failed", Toast.LENGTH_SHORT).show();
                Log.e("SettingsActivity", "Sign-In Failed", e);
            }
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account != null) {
            String userId = account.getId();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

            userRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String role = dataSnapshot.getValue(String.class);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if ("admin".equals(role)) {
                        editor.putBoolean("is_admin", true);
                        Toast.makeText(SettingsActivity.this, "Admin features enabled", Toast.LENGTH_SHORT).show();
                    } else {
                        editor.putBoolean("is_admin", false);
                        Toast.makeText(SettingsActivity.this, "Admin features disabled", Toast.LENGTH_SHORT).show();
                    }
                    editor.apply();

                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("SettingsActivity", "Error fetching user role", databaseError.toException());
                }
            });
        }
    }

    private void saveChanges() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("some_setting", true); // Replace with actual settings
        editor.apply();

        Toast.makeText(SettingsActivity.this, "Changes saved", Toast.LENGTH_SHORT).show();
    }

    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Toast.makeText(SettingsActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void loadProfilePicture() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            String profilePictureUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;
            ImageView profilePicture = findViewById(R.id.profilePicture);

            Glide.with(this)
                    .load(profilePictureUrl)
                    .circleCrop()
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .into(profilePicture);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // Handle the back button click
            onBackPressed();
            return true;
        } else if (id == R.id.action_sign_out) {
            signOut();
            return true;
        }
        else if (id == R.id.action_about_us) {
            Intent intent = new Intent(SettingsActivity.this, AboutUsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
