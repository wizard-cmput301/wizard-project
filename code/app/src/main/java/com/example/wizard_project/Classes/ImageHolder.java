package com.example.wizard_project.Classes;
/**
 *  ImageHolder is a object class used for the Admin view of Photos
 *  This stores a representation of a single image in the database.
 */
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

    public void  setImageUrl(String imageUrl) {
         this.imageUrl = imageUrl;
    }


    public String getImagePath() {
        return imagePath;
    }

    public void  setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
