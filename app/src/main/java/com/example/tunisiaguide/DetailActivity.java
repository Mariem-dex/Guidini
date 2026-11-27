package com.example.tunisiaguide;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Destination destination = (Destination) getIntent().getSerializableExtra("destination");
        if (destination == null) {
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(destination.getTitle());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ImageButton btnDetailThemeToggle = findViewById(R.id.btnDetailThemeToggle);
        boolean isDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        updateThemeIcon(btnDetailThemeToggle, isDarkMode);
        btnDetailThemeToggle.setOnClickListener(v -> {
            boolean dark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
            int newMode = dark ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
            AppCompatDelegate.setDefaultNightMode(newMode);
            saveThemePref(newMode);
            recreate();
        });

        ViewPager2 viewPagerPhotos = findViewById(R.id.viewPagerPhotos);
        ImageSliderAdapter sliderAdapter = new ImageSliderAdapter(this, destination.getImageResIds());
        viewPagerPhotos.setAdapter(sliderAdapter);

        View cardFoodSection = findViewById(R.id.cardFoodSection);
        ImageView ivFoodImage = findViewById(R.id.ivFoodImage);
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvRegion = findViewById(R.id.tvDetailRegion);
        TextView tvCategory = findViewById(R.id.tvDetailCategory);
        TextView tvDescription = findViewById(R.id.tvDetailDescription);
        TextView tvVisit = findViewById(R.id.tvDetailVisit);
        TextView tvDo = findViewById(R.id.tvDetailDo);
        TextView tvEat = findViewById(R.id.tvDetailEat);
        TextView tvDetailWeather = findViewById(R.id.tvDetailWeather);
        Button btnMap = findViewById(R.id.btnMap);

        if (destination.getFoodImageResId() != 0) {
            cardFoodSection.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(destination.getFoodImageResId())
                    .placeholder(R.drawable.ic_launcher)
                    .into(ivFoodImage);
        } else {
            cardFoodSection.setVisibility(View.GONE);
        }

        tvTitle.setText(destination.getTitle());
        tvRegion.setText(destination.getRegion());
        tvCategory.setText(destination.getCategory());
        tvDescription.setText(destination.getDescription());
        tvVisit.setText(destination.getWhatToVisit());
        tvDo.setText(destination.getWhatToDo());
        tvEat.setText(destination.getWhatToEat());

        // Fetch real-time weather from Open-Meteo REST API via Retrofit
        RetrofitClient.getInstance().getWeatherApi().getWeather(destination.getLatitude(), destination.getLongitude(), true)
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getCurrentWeather() != null) {
                            WeatherResponse.CurrentWeather weather = response.body().getCurrentWeather();
                            String weatherText = "Température: " + weather.getTemperature() + " °C | Vent: " + weather.getWindspeed() + " km/h";
                            tvDetailWeather.setText(weatherText);
                        } else {
                            tvDetailWeather.setText("Météo non disponible actuellement.");
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        tvDetailWeather.setText("Météo : Informations en cours de chargement.");
                    }
                });

        btnMap.setOnClickListener(v -> {
            String uriGeo = "geo:" + destination.getLatitude() + "," + destination.getLongitude() + "?q=" + Uri.encode(destination.getTitle() + ", Tunisia");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriGeo));
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + destination.getLatitude() + "," + destination.getLongitude()));
            try {
                startActivity(intent);
            } catch (Exception e) {
                startActivity(webIntent);
            }
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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
