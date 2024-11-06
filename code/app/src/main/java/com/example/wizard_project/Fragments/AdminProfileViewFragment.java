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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.wizard_project.Adapters.BrowseProfileAdapter;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.databinding.FragmentAdminBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * AdminFragment provides the UI and functionality for admins.
 */
public class AdminProfileViewFragment extends Fragment {

    private FragmentAdminBinding binding;
    private ArrayList<User> profileList = new ArrayList<>();
    private BrowseProfileAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Use View Binding to inflate the layout
        binding = FragmentAdminBinding.inflate(inflater, container, false);

        // Initialize the ListView with binding
        ListView profileListView = binding.profilelistListview;

        // Create a new instance of ProfileAdapter with the context and profileList
        adapter = new BrowseProfileAdapter(getContext(), profileList);

        // Set the adapter to the ListView
        profileListView.setAdapter(adapter);

        // Load profiles from Firebase or another data source
        loadUsers();

        // Set the item click listener for the ListView
        binding.profilelistListview.setOnItemClickListener((parent, view, position, id) -> {
            User selectedUser = profileList.get(position);  // Get the clicked item
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.setDeleteUser(selectedUser);
            // Navigate to ProfileFragment
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_AdminProfileFragment_to_ProfileFragment);
        });

        return binding.getRoot();  // Return the root view of binding
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().clear(); // Clear the current menu
        bottomNavigationView.inflateMenu(R.menu.bottom_nav_admin); // Load the admin-specific menu

    }

    private void loadUsers(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        profileList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User profile = new User(null,null,null,false,false,false,null,null,null);
                            profile.setUserData(document);
                            profileList.add(profile);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("AdminProfileViewFragment", "Error getting documents", task.getException());
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // reset the bottom nav bar
    }

}