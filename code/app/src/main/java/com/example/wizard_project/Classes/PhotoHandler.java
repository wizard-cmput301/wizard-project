package com.example.wizard_project.Classes;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;
/**
 *  PhotoHandler is a Utility class that has the method functionality:
 *  getUserImage - to get images from the user
 *  uploadImage - Upload an image to firebase storage
 *  loadImage - loads image from firebase storage and uploads it into an image view
 */
public class PhotoHandler {
    public static final int PICK_IMAGE_REQUEST = 1; // Request code for image selection

    public PhotoHandler() {

    }

    /**
     * Opens the gallery for the user to select an image.
     * @param context creating the intent requires the context
     */
    public void getUserImage(Context context) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, PICK_IMAGE_REQUEST);
        }
    }

    /**
     * Loads an image from Firebase Storage into an ImageView.
     * This method uses Glide or a similar image loading library to display the image
     *
     * @param imageUri        the Uri of the image to upload
     * @param successListener the listener triggered on successful upload, returns the download Uri
     * @param failureListener the listener triggered on upload failure, returns an exception
     */
    public void uploadImage(Uri imageUri, OnSuccessListener<Uri> successListener, OnFailureListener failureListener) {
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(UUID.randomUUID().toString());
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(successListener))
                .addOnFailureListener(failureListener);
    }


    /**
     * Loads an image from Firebase Storage into an ImageView.
     * This method uses Glide or a similar image loading library to display the image
     *
     * @param imageName the path to the image in Firebase Storage
     * @param imageView the ImageView where the image will be displayed
     */
    public void loadImage(String imageName, ImageView imageView, Context context) {

        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imageName);

        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context).load(uri).into(imageView);
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Failed to retrieve image", Toast.LENGTH_SHORT).show();
        });
    }
}