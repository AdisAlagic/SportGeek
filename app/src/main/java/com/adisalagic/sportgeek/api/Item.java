
package com.adisalagic.sportgeek.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Item {
    @SerializedName("amount")
    @Expose
    private Integer amount;
    @SerializedName("catalog_id")
    @Expose
    private Integer catalogId;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("image_name")
    @Expose
    private String imageName;
    @SerializedName("image_url")
    @Expose
    private String imageUrl;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("price")
    @Expose
    private double price;

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(Integer catalogId) {
        this.catalogId = catalogId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        this.imageUrl = this.imageUrl.replaceAll("//", "/");
        this.imageUrl = this.imageUrl.replaceAll(":/", "://");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

}
