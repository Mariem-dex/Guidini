package com.example.tunisiaguide;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

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

        ViewPager2 viewPagerPhotos = findViewById(R.id.viewPagerPhotos);
        ImageSliderAdapter sliderAdapter = new ImageSliderAdapter(this, destination.getImageResIds());
        viewPagerPhotos.setAdapter(sliderAdapter);

        ImageView ivFoodImage = findViewById(R.id.ivFoodImage);
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvRegion = findViewById(R.id.tvDetailRegion);
        TextView tvCategory = findViewById(R.id.tvDetailCategory);
        TextView tvDescription = findViewById(R.id.tvDetailDescription);
        TextView tvVisit = findViewById(R.id.tvDetailVisit);
        TextView tvDo = findViewById(R.id.tvDetailDo);
        TextView tvEat = findViewById(R.id.tvDetailEat);
        Button btnMap = findViewById(R.id.btnMap);

        if (destination.getFoodImageResId() != 0) {
            ivFoodImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(destination.getFoodImageResId())
                    .placeholder(R.drawable.ic_launcher)
                    .into(ivFoodImage);
        } else {
            ivFoodImage.setVisibility(View.GONE);
        }

        tvTitle.setText(destination.getTitle());
        tvRegion.setText(destination.getRegion());
        tvCategory.setText(destination.getCategory());
        tvDescription.setText(destination.getDescription());
        tvVisit.setText(destination.getWhatToVisit());
        tvDo.setText(destination.getWhatToDo());
        tvEat.setText(destination.getWhatToEat());

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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
