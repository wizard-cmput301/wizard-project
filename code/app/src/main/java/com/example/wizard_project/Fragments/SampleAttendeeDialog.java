package com.example.wizard_project.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.wizard_project.databinding.DialogSampleAttendeesBinding;

public class SampleAttendeeDialog extends DialogFragment {
    private DialogSampleAttendeesBinding binding;

    public static SampleAttendeeDialog newInstance(int attendeeLimit) {
        SampleAttendeeDialog dialog = new SampleAttendeeDialog();
        Bundle args = new Bundle();
        args.putInt("attendeeLimit", attendeeLimit);
        dialog.setArguments(args);
        return dialog;
    }

    public interface SampleAttendeesListener {
        void setDrawAmount(int drawAmount);
    }

    private SampleAttendeesListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(getParentFragment() instanceof SampleAttendeesListener) {
            listener = (SampleAttendeesListener) getParentFragment();
        }
        else {
            throw new RuntimeException(context + " must implement SampleAttendeesListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogSampleAttendeesBinding.inflate(getLayoutInflater());
        NumberPicker numberPicker = binding.attendeeNumberPicker;
        int attendeeLimit = 0;

        if (getArguments() != null) {
            attendeeLimit = getArguments().getInt("attendeeLimit");
        }

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(attendeeLimit);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(binding.getRoot())
                .setTitle("Select the Number of Attendees")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    int drawAmount = numberPicker.getValue();
                    listener.setDrawAmount(drawAmount);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}
