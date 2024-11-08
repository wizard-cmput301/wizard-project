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

public class AdminImageViewFragment extends Fragment {
    private FragmentAdminImageBinding binding;
    private ArrayList<ImageHolder> imageList = new ArrayList<>();
    private BrowseImageAdapter imageAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Use View Binding to inflate the layout
        binding = FragmentAdminImageBinding.inflate(inflater, container, false);

        // Initialize the ListView with binding
        ListView imageListView = binding.imageListView;

        imageAdapter = new BrowseImageAdapter(getContext(),imageList);
        // Set the adapter to the ListView
        imageListView.setAdapter(imageAdapter);

        // Load images from Firebase
        loadImages();

        // Set the item click listener for the ListView
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
            }else if(item.getItemId() == R.id.nav_image_browse){
                navController.navigate(R.id.AdminFragmentImageView);
                return true;

            }
            return false;
        });

    }

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

    private void deleteImage(ImageHolder image_clicked) {
        if(!image_clicked.getImagePath().equals("")) {
            // Get a reference to the image in Firebase Storage
            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(image_clicked.getImagePath());
            // Delete the image
            imageRef.delete();
            image_clicked.setImagePath("");
            image_clicked.setImageUrl("");

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // reset the bottom nav bar
    }

}

