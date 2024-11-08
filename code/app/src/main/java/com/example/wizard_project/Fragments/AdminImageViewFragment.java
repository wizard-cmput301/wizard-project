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
import androidx.navigation.ui.NavigationUI;

import com.example.wizard_project.Adapters.BrowseImageAdapter;
import com.example.wizard_project.Adapters.BrowseProfileAdapter;
import com.example.wizard_project.Classes.ImageHolder;
import com.example.wizard_project.Classes.User;
import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;
import com.example.wizard_project.databinding.FragmentAdminBinding;
import com.example.wizard_project.databinding.FragmentAdminImageBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdminImageViewFragment extends Fragment {
    private FragmentAdminImageBinding binding;
    private ArrayList<ImageHolder> imageList = new ArrayList<>();
    private BrowseProfileAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Use View Binding to inflate the layout
        binding = FragmentAdminImageBinding.inflate(inflater, container, false);

        // Initialize the ListView with binding
        ListView imageListView = binding.imageListView;

        BrowseImageAdapter imageAdapter = new BrowseImageAdapter(getContext(),imageList);
        // Set the adapter to the ListView
        imageListView.setAdapter(imageAdapter);

        // Load images from Firebase
        loadImages();

        // Set the item click listener for the ListView
        imageListView.setOnItemClickListener((parent, view, position, id) -> {
            ImageHolder image_clicked = imageList.get(position);  // Get the clicked item
            // TODO:create a box that asks if you want to delete.
            deleteImage(image_clicked);
        });

        return binding.getRoot();  // Return the root view of binding
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Access the BottomNavigationView from the Activity
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);

        // Set up the admin-specific menu if we are in the AdminViewFragment
        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.bottom_nav_admin);

        // Access the NavController associated with the Activity's NavHostFragment
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);

        // Connect NavController to BottomNavigationView
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Home Button
            if (item.getItemId() == R.id.nav_profile_browse) {
                navController.navigate(R.id.AdminFragment);
                return true;
            }else  if (item.getItemId() == R.id.nav_home) {
                navController.navigate(R.id.HomeFragment);
                return true;
            }
            return false;
        });

    }

    private void loadImages(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        profileList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User profile = new User(null,null,null,false,false,false,null,null,null,null);
                            profile.setUserData(document);
                            profileList.add(profile);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("AdminProfileViewFragment", "Error getting documents", task.getException());
                    }
                });
    }
    private void deleteImage(ImageHolder image_clicked) {


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // reset the bottom nav bar
    }

}
}
