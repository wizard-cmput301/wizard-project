package com.example.wizard_project.Classes;

public class ImageHolder {
    private String imageUrl;
    private String imagePath;

    public ImageHolder(String imageUrl, String imagePath) {
        this.imageUrl = imageUrl;
        this.imagePath = imagePath;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImagePath() {
        return imagePath;
    }
}