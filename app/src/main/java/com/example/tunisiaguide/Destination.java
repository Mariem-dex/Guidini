package com.example.tunisiaguide;

import java.io.Serializable;
import java.util.List;

public class Destination implements Serializable {
    private String title;
    private String description;
    private String region;
    private String category;
    private List<Integer> imageResIds;
    private int foodImageResId;
    private double latitude;
    private double longitude;
    private String whatToVisit;
    private String whatToDo;
    private String whatToEat;

    public Destination(String title, String description, String region, String category, List<Integer> imageResIds, int foodImageResId, double latitude, double longitude, String whatToVisit, String whatToDo, String whatToEat) {
        this.title = title;
        this.description = description;
        this.region = region;
        this.category = category;
        this.imageResIds = imageResIds;
        this.foodImageResId = foodImageResId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.whatToVisit = whatToVisit;
        this.whatToDo = whatToDo;
        this.whatToEat = whatToEat;
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

    public List<Integer> getImageResIds() {
        return imageResIds;
    }

    public int getFoodImageResId() {
        return foodImageResId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getWhatToVisit() {
        return whatToVisit;
    }

    public String getWhatToDo() {
        return whatToDo;
    }

    public String getWhatToEat() {
        return whatToEat;
    }
}
