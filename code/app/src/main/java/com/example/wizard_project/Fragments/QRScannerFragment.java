package com.example.wizard_project.Fragments;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

        // Check for camera permission and start scanner
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startQRCodeScanner();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        return view;
    }

    private void startQRCodeScanner() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setPrompt("Scan a QR Code");
        integrator.setOrientationLocked(true);
        integrator.setBeepEnabled(false);
        Intent scanIntent = integrator.createScanIntent();
        scanLauncher.launch(scanIntent); // Start the scanning activity
    }

    // Permission request launcher for camera permission
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startQRCodeScanner();
                } else {
                    Toast.makeText(requireContext(), "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show();
                }
            });

    private void checkEventInFirestore(String qrCodeData) {
        db.collection("events").document(qrCodeData).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        navigateToEventDetails(documentSnapshot);
                    } else {
                        Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to check event", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToEventDetails(DocumentSnapshot documentSnapshot) {
        Bundle bundle = new Bundle();
        bundle.putString("eventId", documentSnapshot.getId());
        bundle.putString("eventName", documentSnapshot.getString("name"));
        bundle.putString("eventDescription", documentSnapshot.getString("description"));

        // Navigate to EventDetailsFragment or EntrantFragment with the event details
        NavHostFragment.findNavController(this).navigate(R.id.action_QRScannerFragment_to_EntrantFragment, bundle);
    }
}
