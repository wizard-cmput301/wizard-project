package com.example.wizard_project;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Event extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventdetails);

        // Retrieve the event ID passed from QRScannerActivity
        String eventId = getIntent().getStringExtra("EVENT_ID");

        // Display the event details (for demo purposes, we just show the event ID)
        TextView eventDetailTextView = findViewById(R.id.eventDetailTextView);
        //eventDetailTextView.setText("Event ID: " + eventId);

        // TODO: Use eventId to load and display actual event details from your database or server
    }
}