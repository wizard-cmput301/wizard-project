package com.example.wizard_project;



import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRScanner extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        // Start the QR code scanner
        new IntentIntegrator(this)
                .setOrientationLocked(false)
                .setPrompt("Scan the QR code for the event")
                .initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null && result.getContents() != null) {
            // Get the scanned QR code content (assuming it contains an event ID)
            String eventId = result.getContents();

            // Start EventDetailActivity with the scanned eventId
            Intent intent = new Intent(this, Event.class);
            intent.putExtra("EVENT_ID", eventId);
            startActivity(intent);
            finish();
        } else {
            // Handle case where no QR code was scanned
            finish();
        }
    }
}
