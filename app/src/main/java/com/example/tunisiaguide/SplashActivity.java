package com.example.tunisiaguide;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageButton btnThemeToggle = findViewById(R.id.btnThemeToggle);
        boolean isDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        updateThemeIcon(btnThemeToggle, isDarkMode);

        btnThemeToggle.setOnClickListener(v -> {
            boolean dark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
            int newMode = dark ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
            AppCompatDelegate.setDefaultNightMode(newMode);
            saveThemePref(newMode);
            recreate();
        });

        Button btnExplore = findViewById(R.id.btnExplore);
        btnExplore.setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void updateThemeIcon(ImageButton button, boolean isDarkMode) {
        if (isDarkMode) {
            button.setImageResource(R.drawable.ic_light_mode);
            button.setContentDescription("Passer en mode clair");
        } else {
            button.setImageResource(R.drawable.ic_dark_mode);
            button.setContentDescription("Passer en mode sombre");
        }
    }

    private void saveThemePref(int mode) {
        SharedPreferences prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        prefs.edit().putInt("night_mode", mode).apply();
    }
}
