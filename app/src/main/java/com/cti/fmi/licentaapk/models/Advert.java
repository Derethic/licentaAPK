package com.cti.fmi.licentaapk.models;

import com.google.gson.annotations.SerializedName;

public class Advert
{
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private float price;

    @SerializedName("currency")
    private String currency;

    @SerializedName("condition")
    private String condition;

    @SerializedName("view_count")
    private int viewCount;

    @SerializedName("user")
    private UserProfile user;

    @SerializedName("mapcategory")
    private MapCategory mapCategory;

    @SerializedName("picture1")
    private String picture1;

    @SerializedName("picture2")
    private String picture2;

    @SerializedName("picture3")
    private String picture3;

    @SerializedName("picture4")
    private String picture4;

    @SerializedName("ad_created_at")
    private String createdAt;

    public Advert(int id, String title, String description, float price, String currency,
                  String condition, int viewCount, UserProfile user, MapCategory mapCategory,
                  String picture1, String picture2, String picture3, String picture4,
                  String createdAt)
    {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.condition = condition;
        this.viewCount = viewCount;
        this.user = user;
        this.mapCategory = mapCategory;
        this.picture1 = picture1;
        this.picture2 = picture2;
        this.picture3 = picture3;
        this.picture4 = picture4;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public UserProfile getUser() {
        return user;
    }

    public void setUser(UserProfile user) {
        this.user = user;
    }

    public MapCategory getMapCategory() {
        return mapCategory;
    }

    public void setMapCategory(MapCategory mapCategory) {
        this.mapCategory = mapCategory;
    }

    public String getPicture1() {
        return picture1;
    }

    public void setPicture1(String picture1) {
        this.picture1 = picture1;
    }

    public String getPicture2() {
        return picture2;
    }

    public void setPicture2(String picture2) {
        this.picture2 = picture2;
    }

    public String getPicture3() {
        return picture3;
    }

    public void setPicture3(String picture3) {
        this.picture3 = picture3;
    }

    public String getPicture4() {
        return picture4;
    }

    public void setPicture4(String picture4) {
        this.picture4 = picture4;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
