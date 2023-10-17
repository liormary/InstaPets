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

public class PromptDialog extends DialogFragment {
    PromptDialogBinding binding;
    private int mRequestCode;
    private DismissListener mDismissListener;

    public static interface DismissListener {
        public void onDialogDismiss(int requestCode, int resultCode);
        public void onpPhotoEntry(int requestCode, int resultCode);
        public void onTextEntry(int requestCode, int resultCode);
    }

    public static final PromptDialog getInstance(int requestCode, DismissListener listener) {
        PromptDialog dlg = new PromptDialog();

        dlg.mRequestCode = requestCode;
        dlg.mDismissListener = listener;

        return dlg;
    }

    private PromptDialog() {
    }

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


        final Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.guide_save_succ_dlg_enter);
        binding.getRoot().startAnimation(anim);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (mDismissListener != null) {
            mDismissListener.onDialogDismiss(mRequestCode, Activity.RESULT_CANCELED);
        }

        super.onCancel(dialog);
    }
}
