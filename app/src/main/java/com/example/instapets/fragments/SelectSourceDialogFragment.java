package com.example.instapets.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.instapets.activities.PostActivity;

/**
 * this class represents the screen that opens after pressing the button of create post
 */

public class SelectSourceDialogFragment extends DialogFragment {

    //This static method is used to create a new instance of the SelectSourceDialogFragment and return it.
    // It's a common pattern for creating dialog fragments with arguments.
    public static SelectSourceDialogFragment newInstance() {
        SelectSourceDialogFragment frag = new SelectSourceDialogFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    //This method is responsible for creating and configuring the dialog that will be displayed.
    // It sets the dialog's title to "Select from" and provides two options using the setItems method:
    //"Camera": Allows the user to select an image from the device's camera.
    //"Gallery": Allows the user to select an image from the device's gallery.
    //When the user selects one of these options, the DialogFragment triggers the appropriate
    // action based on the user's choice. For example, if "Camera" is selected,
    // it calls the selectFromCamera() method from the hosting PostActivity.
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select from")
                .setItems(new String[]{"Camera", "Gallery"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            ((PostActivity) requireActivity()).selectFromCamera();
                            break;
                        case 1:
                            ((PostActivity) requireActivity()).selectFromGallery();
                            break;
                    }
                });
        return builder.create();
    }

    //This method is called when the dialog is canceled (when the user taps outside the dialog or presses
    // the back button). In this case, it calls the startMainActivity() method from the hosting PostActivity.
    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        ((PostActivity) getActivity()).startMainActivity();
    }

}