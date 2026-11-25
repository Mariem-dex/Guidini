package com.example.tunisiaguide;

public class Destination {
    private String title;
    private String description;
    private String region;
    private String category;
    private String imageUrl;
    private double latitude;
    private double longitude;

    public Destination(String title, String description, String region, String category, String imageUrl, double latitude, double longitude) {
        this.title = title;
        this.description = description;
        this.region = region;
        this.category = category;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRegion() {
        return region;
    }

    public String getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
