package com.example.wizard_project.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.wizard_project.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import android.content.Intent;
import com.example.wizard_project.Classes.Event;

/**
 * QRScannerFragment provides functionality to scan QR codes and check event details in Firestore.
 * If an event is found for the scanned QR code, it navigates to the event details view.
 */
public class QRScannerFragment extends Fragment {

    private FirebaseFirestore db;
    private ActivityResultLauncher<Intent> scanLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qrscanner, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Register activity result launcher for the QR scanner
        scanLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                IntentResult intentResult = IntentIntegrator.parseActivityResult(result.getResultCode(), result.getData());
                if (intentResult != null && intentResult.getContents() != null) {
                    checkEventInFirestore(intentResult.getContents());
                } else {
                    Toast.makeText(requireContext(), "No QR code detected", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Scan canceled", Toast.LENGTH_SHORT).show();
            }
        });

        // Check for camera permission before starting the scanner
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startQRCodeScanner();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        return view;
    }

    /**
     * Starts the QR code scanner using ZXing's IntentIntegrator.
     */
    private void startQRCodeScanner() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setPrompt("Scan a QR Code");
        integrator.setOrientationLocked(true);
        integrator.setBeepEnabled(false);
        Intent scanIntent = integrator.createScanIntent();
        scanLauncher.launch(scanIntent); // Start the scanning activity
    }

    /**
     * Permission request launcher for camera access. Launches the QR scanner if permission is granted.
     */
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startQRCodeScanner();
                } else {
                    Toast.makeText(requireContext(), "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show();
                }
            });

    /**
     * Checks if the QR code data corresponds to an existing event in Firestore.
     *
     * @param qrCodeData The scanned QR code content used to query the Firestore event.
     */
    private void checkEventInFirestore(String qrCodeData) {
        // Check if the QR code content starts with "Event ID: "
        if (qrCodeData.startsWith("Event ID: ")) {
            // Extract the eventId from the QR code content
            String eventId = qrCodeData.substring(10).trim(); // Remove "Event ID: "
            Log.d("QRScannerFragment", "Extracted Event ID: " + eventId);

            // Query Firestore with the extracted eventId
            queryFirestoreForEvent(eventId);
        } else {
            // Handle invalid QR code format
            Toast.makeText(requireContext(), "Invalid QR Code format", Toast.LENGTH_SHORT).show();
        }
    }

    private void queryFirestoreForEvent(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("QRScannerFragment", "Event found: " + documentSnapshot.getId());

                        // Create and populate the Event object
                        Event event = new Event(
                                documentSnapshot.getString("event_name"),
                                documentSnapshot.getString("event_description"),
                                documentSnapshot.getDouble("event_price").intValue(),
                                documentSnapshot.getLong("event_max_entrants").intValue(),
                                documentSnapshot.getDate("registration_open"),
                                documentSnapshot.getDate("registration_close"),
                                documentSnapshot.getString("facilityId"),
                                documentSnapshot.getString("event_location"),
                                documentSnapshot.getBoolean("geolocation_requirement"),
                                documentSnapshot.getString("event_image_path")
                        );


                        event.setEventId(documentSnapshot.getId());

                        // Navigate to ViewEventFragment with the Event object
                        navigateToViewEventFragment(event);
                    } else {
                        Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("QRScannerFragment", "Failed to query Firestore", e);
                    Toast.makeText(requireContext(), "Failed to query event", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Navigates to the event details view, passing the event data as a bundle.
     *
     *
     */

    private void navigateToViewEventFragment(Event event) {
        // Pass the Event object directly to the destination fragment
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event); // Use Serializable to pass the Event object
        NavHostFragment.findNavController(this).navigate(R.id.action_QRScannerFragment_to_ViewEventFragment, bundle);
    }
}
