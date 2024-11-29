package com.example.wizard_project.Fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.wizard_project.Classes.QRCode;
import com.example.wizard_project.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * ViewQRCodeFragment displays a QR code for a specific event, allowing users to generate. view, and download it.
 */
public class ViewQRCodeFragment extends Fragment {
    private static final String TAG = "ViewQRCodeFragment";

    private String eventId;
    private ImageView qrCodeImageView;
    private Button generateQRCodeButton;
    private Button backButton;
    private Button downloadQRCodeButton;
    private Bitmap qrCodeBitmap;
    private FirebaseFirestore db;
    private DocumentReference eventDocRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_qr_code, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve event ID from arguments
        eventId = getArguments() != null ? getArguments().getString("eventId") : null;
        if (eventId == null) {
            Toast.makeText(getContext(), "Event ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize Firebase Firestore and document reference
        db = FirebaseFirestore.getInstance();
        eventDocRef = db.collection("events").document(eventId);

        // Initialize UI components
        qrCodeImageView = view.findViewById(R.id.qrCodeImageView);
        generateQRCodeButton = view.findViewById(R.id.generateQRCodeButton);
        backButton = view.findViewById(R.id.backButton);
        downloadQRCodeButton = view.findViewById(R.id.downloadQRCodeButton);

        // Set up button listeners
        backButton.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        generateQRCodeButton.setOnClickListener(v -> generateAndSaveQRCode());
        downloadQRCodeButton.setOnClickListener(v -> downloadQRCodeImage());

        // Fetch and display the QR code
        fetchQRCodeFromFirestore();
    }

    /**
     * Fetches the QR code from Firestore and displays it.
     * If no QR code exists, shows the Generate button.
     */
    private void fetchQRCodeFromFirestore() {
        eventDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String qrCodeData = documentSnapshot.getString("qrCode");
                if (qrCodeData != null && !qrCodeData.isEmpty()) {
                    // QR Code exists, display it
                    displayQRCode(qrCodeData);
                    generateQRCodeButton.setVisibility(View.GONE); // Hide the Generate button
                } else {
                    // No QR code exists, show the Generate button
                    generateQRCodeButton.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(getContext(), "Event not found!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching QR Code", e);
            Toast.makeText(getContext(), "Failed to fetch QR Code", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Generates a QR code, saves it to Firestore, and displays it.
     */
    private void generateAndSaveQRCode() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("events").document(eventId);

        // QR code data (customize as needed)
        String qrCodeData = "Event ID: " + eventId;

        // Use QRCode helper class to generate a QR code bitmap
        QRCode qrCodeGenerator = new QRCode();
        Bitmap qrCodeBitmap = qrCodeGenerator.generateQRCode(qrCodeData, 400, 400);
        if (qrCodeBitmap == null) {
            Toast.makeText(getContext(), "Failed to generate QR Code", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save QR code data to Firestore
        docRef.update("qrCode", qrCodeData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "QR Code successfully saved!");
                    Toast.makeText(getContext(), "QR Code generated successfully", Toast.LENGTH_SHORT).show();
                    displayQRCode(qrCodeData); // Display the QR code
                    generateQRCodeButton.setVisibility(View.GONE); // Hide the Generate button
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving QR Code", e);
                    Toast.makeText(getContext(), "Failed to save QR Code", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Displays the QR code in the ImageView.
     *
     * @param qrCodeData The data to generate the QR code from.
     */
    private void displayQRCode(String qrCodeData) {
        QRCode qrCodeGenerator = new QRCode();
        qrCodeBitmap = qrCodeGenerator.generateQRCode(qrCodeData, 400, 400);
        if (qrCodeBitmap != null) {
            qrCodeImageView.setImageBitmap(qrCodeBitmap);
        } else {
            Toast.makeText(getContext(), "Failed to display QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Downloads the QR code as an image to the user's device.
     */
    private void downloadQRCodeImage() {
        if (qrCodeBitmap == null) {
            Toast.makeText(getContext(), "No QR Code to download!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "QR Codes");
            if (!directory.exists() && !directory.mkdirs()) {
                Toast.makeText(getContext(), "Failed to create directory for QR Codes", Toast.LENGTH_SHORT).show();
                return;
            }

            File file = new File(directory, "QR_Code_" + eventId + ".png");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                Toast.makeText(getContext(), "QR Code downloaded to: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error downloading QR Code image", e);
            Toast.makeText(getContext(), "Failed to download QR Code", Toast.LENGTH_SHORT).show();
        }
    }
}
