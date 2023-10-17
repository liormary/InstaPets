package com.example.instapets.utilities;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * This class manages the function of the keyboard when the user add comment
 */
public class KeyboardManager {

    //This method is used to open the virtual keyboard.
    // It first checks if there is a currently focused view within the provided Activity.
    // If a view is focused, it obtains the InputMethodManager and uses it to show the soft keyboard.
    public static void openKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    //This method is used to close the virtual keyboard.
    //Similar to openKeyboard, it checks if there is a currently focused view within the provided Activity.
    //If a view is focused, it obtains the InputMethodManager and uses it to hide the soft keyboard.
    public static void closeKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
