package com.example.instapets.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.example.instapets.R;
import com.example.instapets.databinding.PromptDialogBinding;

/**
 * this class responsible for the screen that appears after we press the create post button
 */

//DialogFragment used to display a dialog with options for user input.
public class PromptDialog extends DialogFragment {
    PromptDialogBinding binding; //connects the UI elements defined in the R.layout.prompt_dialog layout to the object
    private int mRequestCode;
    private DismissListener mDismissListener;

    //An interface that defines callback methods for dialog dismissal events.
    // It provides methods for handling different scenarios when the dialog is dismissed.
    public static interface DismissListener {
        public void onDialogDismiss(int requestCode, int resultCode);
        public void onpPhotoEntry(int requestCode, int resultCode);
        public void onTextEntry(int requestCode, int resultCode);
    }

    //A static factory method for creating an instance of PromptDialog.
    // It allows you to set the request code and the dismiss listener for the dialog.
    public static final PromptDialog getInstance(int requestCode, DismissListener listener) {
        PromptDialog dlg = new PromptDialog();

        dlg.mRequestCode = requestCode;
        dlg.mDismissListener = listener;

        return dlg;
    }

    //A private constructor that does not take any parameters.
    // This is typically used to enforce the use of the factory method for creating instances of PromptDialog.
    private PromptDialog() {
    }

    //This method is called when the dialog is created.
    // It inflates the dialog's layout from R.layout.prompt_dialog,
    // sets various properties for the dialog (e.g., no title, transparent background),
    // and handles click events for different UI elements,
    // starting the appropriate callback methods of the DismissListener when the user interacts with the dialog.
    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.prompt_dialog, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);
        setCancelable(true);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View viewInterface = inflater.inflate(R.layout.prompt_dialog, null);
        binding.cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

                if (mDismissListener != null) {
                    mDismissListener.onDialogDismiss(mRequestCode, Activity.RESULT_OK);
                }
            }
        });
        binding.navPostImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

                if (mDismissListener != null) {
                    mDismissListener.onpPhotoEntry(mRequestCode, Activity.RESULT_OK);
                }
            }
        });
        binding.navPostText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

                if (mDismissListener != null) {
                    mDismissListener.onTextEntry(mRequestCode, Activity.RESULT_OK);
                }
            }
        });

        //This code loads an animation from a resource file and applies it to the root view of the dialog's layout.
        //This animation is used to provide a visual effect when the dialog is displayed.
        //The root view is then returned to be displayed within the dialog.
        //The specific animation effect and its details are defined in the resource file guide_save_succ_dlg_enter.
        final Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.guide_save_succ_dlg_enter);
        binding.getRoot().startAnimation(anim);
        return binding.getRoot();
    }

    //This method is called when the dialog is displayed.
    //It sets the dimensions of the dialog window to match the screen.
    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    //This method is called when the dialog is canceled (when the user presses the cancel button).
    //It calls the appropriate onDialogDismiss method of the DismissListener to handle the dismissal event.
    @Override
    public void onCancel(DialogInterface dialog) {
        if (mDismissListener != null) {
            mDismissListener.onDialogDismiss(mRequestCode, Activity.RESULT_CANCELED);
        }

        super.onCancel(dialog);
    }
}
