package com.example.wizard_project.Fragments;

import android.app.AlertDialog;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * AdminImageViewFragment allows admin users to view and delete images from Firebase Storage.
 */
public class AdminImageViewFragment extends Fragment {
    private FragmentAdminImageBinding binding;
    private ArrayList<ImageHolder> imageList = new ArrayList<>();
    private BrowseImageAdapter imageAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminImageBinding.inflate(inflater, container, false);

        // Initialize the ListView with binding
        ListView imageListView = binding.imageListView;
        imageAdapter = new BrowseImageAdapter(getContext(),imageList);
        imageListView.setAdapter(imageAdapter);

        // Load images from Firebase
        loadImages();

        // Set click listener to handle image deletion
        imageListView.setOnItemClickListener((parent, view, position, id) -> {
            ImageHolder image_clicked = imageList.get(position);  // Get the clicked item
            new AlertDialog.Builder(getContext())
                    .setTitle("Confirmation")
                    .setMessage("Are you sure you want to delete this image?")
                    // Yes button
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Handle the Yes button click here
                        imageList.remove(position);
                        deleteImage(image_clicked);
                        imageAdapter.notifyDataSetChanged();
                        dialog.dismiss(); // Close the dialog
                    })
                    // No button
                    .setNegativeButton("No", (dialog, which) -> {
                        // Handle the No button click here
                        dialog.dismiss(); // Close the dialog without any action
                    })
                    // Display the dialog
                    .show();
        });

        return binding.getRoot();  // Return the root view of binding
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up the admin-specific bottom navigation menu
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.bottom_nav_admin);

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Profile browsing
            if (item.getItemId() == R.id.nav_profile_browse) {
                navController.navigate(R.id.AdminFragment);
                return true;
                // Home page
            } else  if (item.getItemId() == R.id.nav_home) {
                navController.navigate(R.id.HomeFragment);
                return true;
                // Event browsing
            } else  if (item.getItemId() == R.id.nav_events_browse) {
                navController.navigate(R.id.AdminFragmentEventView);
                return true;
            }else if(item.getItemId() == R.id.nav_image_browse){
                navController.navigate(R.id.AdminFragmentImageView);
                return true;
            }
            return false;
        });
    }

    /**
     * Loads all images from the Firebase Storage "images" folder and updates the list.
     */
    private void loadImages() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/");

        // List all items in the "images" folder
        storageRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        // For each item, get the download URL
                        item.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Add image URL and path to the list
                            imageList.add(new ImageHolder(uri.toString(), item.getPath()));

                            // Notify adapter to update RecyclerView after adding all URLs
                            imageAdapter.notifyDataSetChanged();
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load images", Toast.LENGTH_SHORT).show();
                });

    }

    /**
     * Deletes the specified image from Firebase Storage.
     * @param imageToDelete The ImageHolder containing information of the image to delete.
     */
    private void deleteImage(ImageHolder imageToDelete) {
        if(!imageToDelete.getImagePath().equals("")) {
            // Get a reference to the image in Firebase Storage
            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imageToDelete.getImagePath());
            // Delete the image
            imageRef.delete();
            imageToDelete.setImagePath("");
            imageToDelete.setImageUrl("");
        }
    }

    /**
     * Cleans up the View Binding to avoid memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
