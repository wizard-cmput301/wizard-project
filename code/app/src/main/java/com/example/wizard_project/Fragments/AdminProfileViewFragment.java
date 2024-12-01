package com.example.wizard_project.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.wizard_project.Adapters.BrowseProfileAdapter;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentAdminBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * AdminProfileViewFragment provides the UI and functionality for admin users,
 * allowing them to browse and select user profiles for further actions.
 */
public class AdminProfileViewFragment extends Fragment {

    private FragmentAdminBinding binding;
    private final ArrayList<User> profileList = new ArrayList<>();
    private BrowseProfileAdapter adapter;

    /**
     * Inflates the layout for this fragment using View Binding.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminBinding.inflate(inflater, container, false);

        // Initialize the ListView with the profile list adapter
        ListView profileListView = binding.profilelistListview;
        adapter = new BrowseProfileAdapter(getContext(), profileList);
        profileListView.setAdapter(adapter);

        // Load profiles from Firebase
        loadUsers();

        // Set item click listener for profile selection
        binding.profilelistListview.setOnItemClickListener((parent, view, position, id) -> {
            User selectedUser = profileList.get(position);  // Get the clicked item (user)
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.setDeleteUser(selectedUser);

            // Navigate to ProfileFragment with the selected user data
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_AdminProfileFragment_to_ProfileFragment);
        });

        return binding.getRoot();  // Return the root view of binding
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Loads user profiles from Firestore and updates the profile list.
     */
    private void loadUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        profileList.clear(); // Clear existing profiles before loading new ones
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User profile = new User(null, null, false, false, false, null, null, null, null);
                            profile.setUserData(document);
                            profileList.add(profile); // Add each user profile to the list
                        }
                        adapter.notifyDataSetChanged(); // Refresh the ListView with new data
                    } else {
                        Log.e("AdminProfileViewFragment", "Error getting documents", task.getException());
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
