package com.example.wizard_project.Classes;


import static android.app.Activity.RESULT_OK;

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
 *  uploadImage - Upload an image to firebase storage
 *  DeleteImage - Deletes image from firebase storage
 *  loadImage - Loads an image from the firebase storage and uploads it into an image view
 */
public class PhotoHandler {
    public static final int PICK_IMAGE_REQUEST = 1; // Request code for image selection


    /**
     * Uploads an image to Firebase.
     * This method uses Glide or a similar image loading library to display the image
     *
     * @param imageUri        the Uri of the image to upload
     * @param successListener the listener triggered on successful upload, returns the download Uri
     * @param failureListener the listener triggered on upload failure, returns an exception
     */
    public void uploadImage(User currentUser, Uri imageUri, OnSuccessListener<Uri> successListener, OnFailureListener failureListener) {
        String path = UUID.randomUUID().toString();
        currentUser.setProfilePath(path);
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(path);
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(successListener))
                .addOnFailureListener(failureListener);
    }
    /**
     * Deletes an image to Firebase.
     * This method uses Glide or a similar image loading library to display the image
     *
     * @param imageName       the String path of the image to delete
     * @param successListener the listener triggered on successful upload, returns the download Uri
     * @param failureListener the listener triggered on upload failure, returns an exception
     */
    public void deleteImage(String imageName, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        // Get a reference to the image in Firebase Storage
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imageName);
        // Delete the image
        imageRef.delete()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    /**
     * Loads an image from Firebase Storage into an ImageView.
     * This method uses Glide or a similar image loading library to display the image
     *
     * @param imageName the path to the image in Firebase Storage
     * @param imageView the ImageView where the image will be displayed
     *
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