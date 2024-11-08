package com.example.wizard_project.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.wizard_project.Adapters.BrowseEventAdapter;
import com.example.wizard_project.Classes.Event;
import com.example.wizard_project.databinding.FragmentEventListBinding;

import java.util.ArrayList;

public class EventListFragment extends Fragment {
    private FragmentEventListBinding binding;
    private BrowseEventAdapter adapter;
    private ArrayList<Event> eventList = new ArrayList<Event>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventListBinding.inflate(inflater, container, false);

        ListView eventListView = binding.eventListview;

        adapter = new BrowseEventAdapter(getContext(), eventList);





    }
}
