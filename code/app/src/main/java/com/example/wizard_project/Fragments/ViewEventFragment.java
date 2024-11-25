package com.example.wizard_project.Fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentViewEventBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Date;

/**
 * ViewEventFragment allows the organizer to view their event, providing multiple functionalities to manage the event.
 */
public class ViewEventFragment extends Fragment {
    private FragmentViewEventBinding binding;
    private Event currentEvent;
    private User currentUser;
    private Button deleteButton;

    /**
     * Creates a new instance of ViewEventFragment with event information passed.
     * @param event The event with the information to be passed in a serialized format.
     * @return A new instance of ViewEventFragment with event information passed.
     */
    public static ViewEventFragment newInstance(Event event) {
        Bundle args = new Bundle();
        args.putSerializable("event", event);

        ViewEventFragment fragment = new ViewEventFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up UI components and retrieve current user and event data
        deleteButton = binding.deleteEventButton;
        MainActivity mainActivity = (MainActivity) requireActivity();
        currentUser = mainActivity.getCurrentUser();
        currentEvent = (Event) getArguments().getSerializable("event");

        // Show delete button for admins and set click listener
        if (currentUser.isAdmin()) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> deleteEvent());
        }

        // Display event details
        NavController navController = NavHostFragment.findNavController(this);
        TextView eventName = binding.eventViewEventName;
        TextView eventPrice = binding.eventViewPrice;
        TextView eventWaitlist = binding.eventViewWaitlist;
        TextView eventDeadline = binding.eventViewDeadline;
        Button editPosterButton = binding.eventViewPosterButton;
        Button viewFacilityButton = binding.viewFacilityButton;
        Button editEventButton = binding.editEventButton;
        Button viewEntrantsButton = binding.viewEntrantsButton;
        Button mapViewButton = binding.mapViewButton;
        Button viewQRButton = binding.viewQrButton;
        Date currentTime = new Date();

        Event event = (Event) getArguments().getSerializable("event");
        assert event != null;
        long deadlineTimeRemaining = event.getEvent_deadline().getTime() - currentTime.getTime();
        int daysRemaining = Math.round((float) deadlineTimeRemaining / (1000 * 60 * 60 * 24));

        // Populate text views with event data
        eventName.setText(String.format("Event Name: %s", event.getEvent_name()));
        eventPrice.setText(String.format("Price: $%d", event.getEvent_price()));
        eventWaitlist.setText(String.format("Availability: %d Spots", event.getEvent_waitlist_limit()));
        eventDeadline.setText(String.format("Deadline: %d Days", daysRemaining));

        // Navigate to edit event on button click
        editEventButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", event);
            navController.navigate(R.id.action_ViewEventFragment_to_EditEventFragment, bundle);
        });

        // TODO: Clear the event's data in memory ?
        viewEntrantsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("event", event);
                navController.navigate(R.id.action_ViewEventFragment_to_EntrantListFragment, bundle);
            }
        });

        viewQRButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", event.getEventId()); // Pass event ID
            navController.navigate(R.id.action_ViewEventFragment_to_ViewQRCodeFragment, bundle);
        });

    }

    /**
     * Deletes the current event from the database and navigates back to the admin event list.
     */
    private void deleteEvent() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("events").document(currentEvent.getEventId());

        // Delete the event's document from Firestore
        docRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("ViewEventFragment", "Event successfully deleted!");
                    Toast.makeText(getContext(), "Event deleted successfully", Toast.LENGTH_SHORT).show();

                    // Navigate back to AdminEventViewFragment to refresh the list
                    NavController navController = Navigation.findNavController(requireView());
                    navController.popBackStack(); // Go back to the previous fragment
                })
                .addOnFailureListener(e -> {
                    Log.w("ViewEventFragment", "Error deleting event", e);
                    Toast.makeText(getContext(), "Failed to delete event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
