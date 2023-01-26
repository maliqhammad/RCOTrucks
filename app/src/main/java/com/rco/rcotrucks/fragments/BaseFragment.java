package com.rco.rcotrucks.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.rco.rcotrucks.R;

public class BaseFragment extends Fragment {

    public void hideSoftKeyboard(Context context, EditText editText) {

        editText.clearFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public void showDialog_Without_Listener(Context context, final String title, final String message, final String buttonText) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                AlertDialog.Builder adbb = new AlertDialog.Builder(context);
                adbb.setIcon(R.drawable.app_truck_icon);
                adbb.setTitle(title);
                if (message != null)
                    adbb.setMessage(message);
                adbb.setPositiveButton(buttonText, null);
                adbb.show();
            }
        });
    }

    public void showDialog_With_Listener(Context context, boolean isTablet, final String title, final String message, final String positiveBtnText, final String negativeBtnText, final DialogInterface.OnClickListener listener) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {

                AlertDialog.Builder adb = new AlertDialog.Builder(context);
                adb.setIcon(R.drawable.app_truck_icon);
                adb.setTitle(title);
                adb.setMessage(message);
                if (positiveBtnText != null && !positiveBtnText.equals("")) {
                    adb.setPositiveButton(positiveBtnText, listener);
                }
                if (negativeBtnText != null && !negativeBtnText.equals("")) {
                    adb.setNegativeButton(negativeBtnText, listener);
                }
                if (isTablet) {
                    adb.show().getWindow().setLayout(1000, WindowManager.LayoutParams.WRAP_CONTENT);
                } else {
                    adb.show().getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                }
            }
        });
    }

}
