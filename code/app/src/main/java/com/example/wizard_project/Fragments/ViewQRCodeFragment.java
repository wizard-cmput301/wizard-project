package com.example.wizard_project.Fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.wizard_project.Classes.QRCode;
import com.example.wizard_project.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ViewQRCodeFragment extends Fragment {
    private static final String TAG = "ViewQRCodeFragment";

    private String eventId;
    private ImageView qrCodeImageView;
    private Button generateQRCodeButton;
    private Button backButton;

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

        // Initialize UI components
        qrCodeImageView = view.findViewById(R.id.qrCodeImageView);
        generateQRCodeButton = view.findViewById(R.id.generateQRCodeButton);
        backButton = view.findViewById(R.id.backButton);

        // Back button listener
        backButton.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        // Fetch QR code from Firestore
        fetchQRCodeFromFirestore();

        // Generate QR Code button listener
        generateQRCodeButton.setOnClickListener(v -> generateAndSaveQRCode());
    }

    /**
     * Fetches the QR code from Firestore and displays it. If no QR code exists, shows the Generate button.
     */
    private void fetchQRCodeFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("events").document(eventId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String qrCodeData = documentSnapshot.getString("qrCode");
                if (qrCodeData != null) {
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
     */
    private void displayQRCode(String qrCodeData) {
        QRCode qrCodeGenerator = new QRCode();
        Bitmap qrCodeBitmap = qrCodeGenerator.generateQRCode(qrCodeData, 400, 400);
        if (qrCodeBitmap != null) {
            qrCodeImageView.setImageBitmap(qrCodeBitmap);
        } else {
            Toast.makeText(getContext(), "Failed to display QR Code", Toast.LENGTH_SHORT).show();
        }
    }
}
