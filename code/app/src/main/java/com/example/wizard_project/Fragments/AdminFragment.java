package com.example.wizard_project.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.wizard_project.Adapters.BrowseProfileAdapter;
import com.example.wizard_project.R;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.databinding.FragmentAdminBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminFragment provides the UI and functionality for admins.
 */
public class AdminFragment extends Fragment {

    private FragmentAdminBinding binding;
    private List<User> profileList = new ArrayList<>();
    private BrowseProfileAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminBinding.inflate(inflater, container, false);
            // Set up RecyclerView
        binding.profileRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BrowseProfileAdapter(profileList);
        binding.profileRecyclerView.setAdapter(adapter);
        loadUsers();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().clear(); // Clear the current menu
        bottomNavigationView.inflateMenu(R.menu.bottom_nav_admin); // Load the admin-specific menu

        // Set the text to display in the fragment
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
                        Log.e("AdminFragment", "Error getting documents", task.getException());
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // reset the bottom nav bar
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().clear(); // Clear the current menu
        bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);
        binding = null;
    }

}