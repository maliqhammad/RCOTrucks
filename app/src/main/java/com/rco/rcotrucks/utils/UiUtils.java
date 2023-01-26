package com.rco.rcotrucks.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatEditText;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.DialogManager;

import java.math.BigDecimal;
import java.util.StringTokenizer;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class UiUtils {

    public static final String TAG = "UiUtils";

    /***
     *
     * @param values
     * @return
     */
    public static boolean isNullOrWhitespacesAll(EditText... values) {
        for (EditText v : values)
            if (!isNullOrWhitespaces(v))
                return false;

        return true;
    }

    /***
     *
     * @param values
     * @return
     */
    public static boolean isNullOrWhitespacesAny(EditText... values) {
        for (EditText v : values)
            if (isNullOrWhitespaces(v))
                return true;

        return false;
    }

    /***
     *
     * @param editText
     * @return
     */
    public static boolean isNullOrWhitespaces(EditText editText) {
        return StringUtils.isNullOrWhitespaces(editText.getText().toString());
    }

    /***
     *
     * @param ctx
     * @param message
     */
    public static void showToast(Context ctx, String message) {
        try {
            Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    public static void showToastLong(Context ctx, String message) {
        try {
            Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    public static void showDialog_Without_Listener(Context ctx, final String title, final String message, final String buttonText) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                android.app.AlertDialog.Builder adbb = new android.app.AlertDialog.Builder(ctx);
                adbb.setIcon(R.drawable.app_truck_icon);
                adbb.setTitle(title);
                if (message != null)
                    adbb.setMessage(message);
                adbb.setPositiveButton(buttonText, null);
                adbb.show();
            }
        });
    }

    public static void showDialog_With_Listener(Context ctx, final String title, final String message, final String positiveBtnText, final String negativeBtnText, final DialogInterface.OnClickListener listener) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {

                android.app.AlertDialog.Builder adb = new android.app.AlertDialog.Builder(ctx);
                adb.setIcon(R.drawable.app_truck_icon);
                adb.setTitle(title);
                adb.setMessage(message);
                if (positiveBtnText != null && !positiveBtnText.equals("")) {
                    adb.setPositiveButton(positiveBtnText, listener);
                }
                if (negativeBtnText != null && !negativeBtnText.equals("")) {
                    adb.setNegativeButton(negativeBtnText, listener);
                }
                adb.show();
            }
        });
    }


    /***
     *
     * @param ctx
     * @param layoutResId
     * @return
     */
    public static AlertDialog showCustomLayoutDialog(Context ctx, int layoutResId, int customDialogStyleId) {
        try {
            LayoutInflater inflater = LayoutInflater.from(ctx);
            View v = inflater.inflate(layoutResId, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(ctx, customDialogStyleId);
            builder.setView(v);

            AlertDialog alert = builder.create();

            if (alert == null || !alert.isShowing()) {
                alert = builder.create();
                alert.show();
            }

            return alert;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }
    }

    public static Dialog showCustomLayoutDialog(Activity a, int layoutDialogResId) {
        final Dialog dialog = new Dialog(a);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layoutDialogResId);
        dialog.show();

        return dialog;
    }

    /***
     *
     * @param ctx
     * @param title
     * @param message
     * @return
     */
    public static AlertDialog showExclamationDialog(Context ctx, String title, String message) {
        return showOkDialog(ctx, title, android.R.drawable.ic_dialog_alert, message, false, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    dialog.cancel();
                } catch (Throwable throwable) {
                    if (throwable != null)
                        throwable.printStackTrace();
                }
            }
        });
    }

    /***
     *
     * @param ctx
     * @param title
     * @param message
     * @param onClickListener
     * @return
     */
    public static AlertDialog showExclamationDialog(Context ctx, String title, String message, DialogInterface.OnClickListener onClickListener) {
        return showOkDialog(ctx, title, android.R.drawable.ic_dialog_alert, message, false, onClickListener);
    }

    /***
     *
     * @param ctx
     * @param title
     * @param iconId
     * @param message
     * @param isCancellable
     * @param onClickListener
     * @return
     */
    public static AlertDialog showOkDialog(Context ctx, String title, int iconId, String message, boolean isCancellable, DialogInterface.OnClickListener onClickListener) {
        try {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ctx);

            alertBuilder.setTitle(title);
            alertBuilder.setIcon(iconId);
            alertBuilder.setMessage(message);
            alertBuilder.setCancelable(isCancellable);

            alertBuilder.setNeutralButton("Ok", onClickListener);


            AlertDialog alert = null;

            if (alert == null || !alert.isShowing()) {
                alert = alertBuilder.create();


                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(alert.getWindow().getAttributes());
                lp.width = 100;
                lp.height = 200;
                lp.x = -170;
                lp.y = 100;
                alert.getWindow().setAttributes(lp);

                alert.show();
//                alert.getWindow().setLayout(50, 20);
            }

            return alert;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }
    }

    public static AlertDialog showOkCancelDialog(Context ctx, String title, int iconId, String message, boolean isCancellable, DialogInterface.OnClickListener onClickListener) {
        try {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ctx);

            alertBuilder.setTitle(title);
            alertBuilder.setIcon(iconId);
            alertBuilder.setMessage(message);
            alertBuilder.setCancelable(isCancellable);
            alertBuilder.setNeutralButton("Ok", onClickListener);
            alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertBuilder.setCancelable(true);
                }
            });

            AlertDialog alert = null;

            if (alert == null || !alert.isShowing()) {
                alert = alertBuilder.create();
                alert.show();
            }

            return alert;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }
    }

    /***
     *
     * @param ctx
     * @param title
     * @param iconId
     * @param neurtralButtonText
     * @param message
     * @param onClickListener
     * @return
     */
    public static AlertDialog showBooleanDialog(Context ctx, String title, int iconId, String neurtralButtonText, String message, DialogInterface.OnClickListener onClickListener) {
        try {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ctx);

            alertBuilder.setTitle(title);
            alertBuilder.setIcon(iconId);
            alertBuilder.setMessage(message);
            alertBuilder.setCancelable(true);
            alertBuilder.setNeutralButton(neurtralButtonText, onClickListener);

            AlertDialog alert = null;

            if (alert == null || !alert.isShowing()) {
                alert = alertBuilder.create();
                alert.show();
            }

            return alert;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }
    }

    public static AlertDialog showBooleanDialog(Context ctx, String title, int iconId, String yesButton, String noButton, String message, DialogInterface.OnClickListener yesClickListener) {
        try {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ctx);

            alertBuilder.setTitle(title);
            alertBuilder.setIcon(iconId);
            alertBuilder.setMessage(message);
            alertBuilder.setCancelable(true);
            alertBuilder.setNeutralButton(yesButton, yesClickListener);
            alertBuilder.setNegativeButton(noButton, null);


            AlertDialog alert = null;

            if (alert == null || !alert.isShowing()) {
                alert = alertBuilder.create();
                alert.show();
            }

            return alert;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }
    }

    public static AlertDialog showBooleanDialog2(Context ctx, String title, int iconId, String yesButton, String noButton, String message, DialogInterface.OnClickListener yesClickListener,
                                                 DialogInterface.OnClickListener noClickListener) {
        try {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ctx);

            alertBuilder.setTitle(title);
            alertBuilder.setIcon(iconId);
            alertBuilder.setMessage(message);
            alertBuilder.setCancelable(true);
            alertBuilder.setNeutralButton(yesButton, yesClickListener);
            alertBuilder.setNegativeButton(noButton, noClickListener);

            AlertDialog alert = null;

            if (alert == null || !alert.isShowing()) {
                alert = alertBuilder.create();
                alert.show();
            }

            return alert;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }
    }

    /***
     *
     * @param a
     * @param resId
     * @param onClickListener
     */
    public static void setOnClickListener(Activity a, int resId, View.OnClickListener onClickListener) {
        try {
            a.findViewById(resId).setOnClickListener(onClickListener);
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    /***
     *
     * @param a
     * @param resId
     * @param imageResourceId
     * @param onClickListener
     */
    public static ImageView setImageViewResource(Activity a, int resId, int imageResourceId, View.OnClickListener onClickListener) {
        try {
            ImageView img = (ImageView) a.findViewById(resId);
            img.setImageResource(imageResourceId);

            if (onClickListener != null)
                img.setOnClickListener(onClickListener);

            return img;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }

        return null;
    }

    /***
     *
     * @param a
     * @param resId
     * @param imageResourceId
     */
    public static void setImageViewResource(Activity a, int resId, int imageResourceId) {
        setImageViewResource(a, resId, imageResourceId, null);
    }

    /***
     *
     * @param a
     * @param resId
     * @param value
     */
    public static void setTextView(Activity a, int resId, String value) {
        try {
            if (a == null)
                return;

            TextView textView = (TextView) a.findViewById(resId);

            if (textView != null)
                textView.setText(value != null ? value : "-");
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    public static void setTextView(View viewBase, int resId, String value) {
        try {
            if (viewBase == null)
                return;

            TextView textView = (TextView) viewBase.findViewById(resId);

            if (textView != null)
                textView.setText(value != null ? value : "-");
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    /***
     *
     * @param v
     * @param resId
     * @param onClickListener
     */
    public static void setTextViewOnClickListener(View v, int resId, View.OnClickListener onClickListener) {
        try {
            if (v == null)
                return;

            TextView textView = (TextView) v.findViewById(resId);

            if (textView != null)
                textView.setOnClickListener(onClickListener);
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    /***
     *
     * @param a
     * @param resIds
     */
    public static void setViewVisibilityGone(Activity a, int... resIds) {
        try {
            if (resIds == null)
                return;

            for (int resId : resIds)
                setViewVisibility(a, resId, View.GONE);
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    /***
     *
     * @param a
     * @param resIds
     */
    public static void setViewVisibilityVisible(Activity a, int... resIds) {
        if (resIds == null)
            return;

        for (int resId : resIds)
            setViewVisibility(a, resId, View.VISIBLE);
    }

    /***
     *
     * @param a
     * @param resIds
     */
    public static void setViewVisibilityInvisible(Activity a, int... resIds) {
        if (resIds == null)
            return;

        for (int resId : resIds)
            setViewVisibility(a, resId, View.INVISIBLE);
    }

    /***
     *
     * @param a
     * @param resId
     * @param visibility
     */
    public static void setViewVisibility(Activity a, int resId, int visibility) {
        try {
            if (a == null || a.findViewById(resId) == null)
                return;

            a.findViewById(resId).setVisibility(visibility);
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    /***
     *
     * @param v
     * @param resId
     * @param visibility
     */
    public static void setViewVisibility(View v, int resId, int visibility) {
        try {
            if (v == null || v.findViewById(resId) == null)
                return;

            v.findViewById(resId).setVisibility(visibility);
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    /***
     *
     * @param v
     * @param resId
     * @param isEnabled
     */
    public static void setViewEnabled(View v, int resId, boolean isEnabled) {
        try {
            if (v == null || v.findViewById(resId) == null) {
                Log.d(TAG, "resId=" + resId + " not found under " + v.getResources().getResourceName(v.getId()));
                return;
            }

            v.findViewById(resId).setEnabled(isEnabled);
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    /***
     *
     * @param ctx
     * @param dpValue
     * @return
     */
    public static int getPixelFromDip(Context ctx, int dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, ctx.getResources().getDisplayMetrics());
    }

    /***
     *
     * @param ctx
     * @param pixelValue
     * @return
     */
    public static int getPixelFromSp(Context ctx, int pixelValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, pixelValue, ctx.getResources().getDisplayMetrics());
    }

    public static final void closeKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            Log.d(TAG, "closeKeyBoard() case view != null, view id:" + getResourceIdNameFromId(activity, view.getId()));

            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } else
            Log.d(TAG, "closeKeyBoard() case view is null");

    }

    public static final void closeKeyboard(View focusedView) {
        View view = focusedView;

        if (view != null) {
            Log.d(TAG, "closeKeyBoard() case view != null, view id:" + view.getId()); // getResourceIdNameFromId(view.getContext(), view.getId()));

            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        } else
            Log.d(TAG, "closeKeyBoard() case view is null");

    }

    public static final void showSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

//    public static void showSoftKeyboard(Activity activity) {
//        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//    }


    public static String getResourceIdNameFromId(Context context, int resourceId) {
        return context.getResources().getResourceName(resourceId);
    }

    //region Screen management

    /***
     *
     * @param context
     * @param defaultValue
     * @return
     */
    public static boolean isLargeScreen(Context context, boolean defaultValue) {
        try {
            //return false;
            return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return defaultValue;
        }
    }

    public static void inflateToFullScreen(Activity a) {
        a.requestWindowFeature(Window.FEATURE_NO_TITLE);
        a.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static void hideSoftKeyboard(Context context, EditText editText) {
        editText.clearFocus();

        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static void hideSoftKeyboard(Context context, AppCompatEditText editText) {
        editText.clearFocus();

        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static void hideSoftKeyboard(Activity activity) {
        activity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    //endregion


    public static boolean isOnline(Context context, String message) {
        try {
            ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

            if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        } catch (NullPointerException nullPointerException) {
            return false;
        }
    }

    public static boolean isOnline(Context context) {
        try {
            ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

            if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
                return false;
            }
            return true;
        } catch (NullPointerException nullPointerException) {
            return false;
        }
    }

    public static boolean checkIsLocationEnabled(Context context) {
        Log.d(TAG, "isLocationEnabled: ");

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false, networkEnabled = false;

        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Throwable throwable) {
            Log.d(TAG, "isLocationEnabled: GPS: Exception: " + throwable.getMessage());
        }

        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Throwable throwable) {
            Log.d(TAG, "isLocationEnabled: Network: Exception: " + throwable.getMessage());
        }

        if (!gpsEnabled || !networkEnabled) {
            return false;
        } else {
            return true;
        }
    }

    public static void showLocationSettingsAlert(Context context) {
        Log.d(TAG, "showLocationSettingsAlert: ");

        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(context);
        alertDialog.setTitle(R.string.location);
        alertDialog.setMessage(R.string.enable_location);
        alertDialog.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(intent);
                } catch (Throwable throwable) {
                    Log.d(TAG, "onClick: throwable: " + throwable.getMessage());
                }
            }
        });
        alertDialog.show();
    }


    public static void isLocationEnabled(Context context) {
        Log.d(TAG, "isLocationEnabled: isLocationEnabled: ");
        if (!canGetLocation(context)) {
            showSettingsAlert(context);
        }
    }

    public static boolean canGetLocation(Context context) {
        Log.d(TAG, "isLocationEnabled: canGetLocation: ");
        boolean result = true;
        LocationManager lm = null;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if (lm == null)
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {

        }
        try {
            network_enabled = lm
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (gps_enabled == false || network_enabled == false) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    public static void showSettingsAlert(Context context) {
        Log.d(TAG, "isLocationEnabled: showSettingsAlert: ");
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(context);
        alertDialog.setTitle("Location!");
        alertDialog.setMessage("GPS is disabled. To enable again go to settings.");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                    }
                });
        alertDialog.show();
    }

    public static int getNetworkSpeed(boolean isUpSpeed, Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
        if (nc == null) {
            return 0;
        }
        int downSpeed = nc.getLinkDownstreamBandwidthKbps();
        int upSpeed = nc.getLinkUpstreamBandwidthKbps();

        if (isUpSpeed) {
            return upSpeed;
        } else {
            return downSpeed;
        }
    }


    //    Oct 11, 2022  -   Trying to convert counting from words to digits
//    Its for google map navigation instructions like convert
//    Seventh street                        => 7th Street
//    One hundred and twenty second avenue  => 122nd Ave
    public static final String[] DIGITS = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};
    public static final String[] TENS = {null, "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
    public static final String[] TEENS = {"ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};
    public static final String[] MAGNITUDES = {"hundred", "thousand", "million", "point"};
    public static final String[] ZERO = {"zero", "oh"};

    public static String replaceNumbers(String input) {
        String result = "";
        String[] decimal = input.split(MAGNITUDES[3]);
        String[] millions = decimal[0].split(MAGNITUDES[2]);

        for (int i = 0; i < millions.length; i++) {
            String[] thousands = millions[i].split(MAGNITUDES[1]);

            for (int j = 0; j < thousands.length; j++) {
                int[] triplet = {0, 0, 0};
                StringTokenizer set = new StringTokenizer(thousands[j]);

                if (set.countTokens() == 1) { //If there is only one token given in triplet
                    String uno = set.nextToken();
                    triplet[0] = 0;
                    for (int k = 0; k < DIGITS.length; k++) {
                        if (uno.equals(DIGITS[k])) {
                            triplet[1] = 0;
                            triplet[2] = k + 1;
                        }
                        if (uno.equals(TENS[k])) {
                            triplet[1] = k + 1;
                            triplet[2] = 0;
                        }
                    }
                } else if (set.countTokens() == 2) {  //If there are two tokens given in triplet
                    String uno = set.nextToken();
                    String dos = set.nextToken();
                    if (dos.equals(MAGNITUDES[0])) {  //If one of the two tokens is "hundred"
                        for (int k = 0; k < DIGITS.length; k++) {
                            if (uno.equals(DIGITS[k])) {
                                triplet[0] = k + 1;
                                triplet[1] = 0;
                                triplet[2] = 0;
                            }
                        }
                    } else {
                        triplet[0] = 0;
                        for (int k = 0; k < DIGITS.length; k++) {
                            if (uno.equals(TENS[k])) {
                                triplet[1] = k + 1;
                            }
                            if (dos.equals(DIGITS[k])) {
                                triplet[2] = k + 1;
                            }
                        }
                    }
                } else if (set.countTokens() == 3) {  //If there are three tokens given in triplet
                    String uno = set.nextToken();
                    String dos = set.nextToken();
                    String tres = set.nextToken();
                    for (int k = 0; k < DIGITS.length; k++) {
                        if (uno.equals(DIGITS[k])) {
                            triplet[0] = k + 1;
                        }
                        if (tres.equals(DIGITS[k])) {
                            triplet[1] = 0;
                            triplet[2] = k + 1;
                        }
                        if (tres.equals(TENS[k])) {
                            triplet[1] = k + 1;
                            triplet[2] = 0;
                        }
                    }
                } else if (set.countTokens() == 4) {  //If there are four tokens given in triplet
                    String uno = set.nextToken();
                    String dos = set.nextToken();
                    String tres = set.nextToken();
                    String cuatro = set.nextToken();
                    for (int k = 0; k < DIGITS.length; k++) {
                        if (uno.equals(DIGITS[k])) {
                            triplet[0] = k + 1;
                        }
                        if (cuatro.equals(DIGITS[k])) {
                            triplet[2] = k + 1;
                        }
                        if (tres.equals(TENS[k])) {
                            triplet[1] = k + 1;
                        }
                    }
                } else {
                    triplet[0] = 0;
                    triplet[1] = 0;
                    triplet[2] = 0;
                }

                result = result + Integer.toString(triplet[0]) + Integer.toString(triplet[1]) + Integer.toString(triplet[2]);
            }
        }

        if (decimal.length > 1) {  //The number is a decimal
            StringTokenizer decimalDigits = new StringTokenizer(decimal[1]);
            result = result + ".";
            System.out.println(decimalDigits.countTokens() + " decimal digits");
            while (decimalDigits.hasMoreTokens()) {
                String w = decimalDigits.nextToken();
                System.out.println(w);

                if (w.equals(ZERO[0]) || w.equals(ZERO[1])) {
                    result = result + "0";
                }
                for (int j = 0; j < DIGITS.length; j++) {
                    if (w.equals(DIGITS[j])) {
                        result = result + Integer.toString(j + 1);
                    }
                }

            }
        }

        return result;
    }

    public static void applyDarkTheme(boolean applyDarkTheme) {
        if (applyDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static String returnStateAbbreviation(String state) {
        if (state.contains("Alabama")) {
            return "AL";
        } else if (state.contains("Alaska")) {
            return "AK";
        } else if (state.contains("American Samoa")) {
            return "AS";
        } else if (state.contains("Arizona")) {
            return "AZ";
        } else if (state.contains("Arkansas")) {
            return "AR";
        } else if (state.contains("California")) {
            return "CA";
        } else if (state.contains("Colorado")) {
            return "CO";
        } else if (state.contains("Connecticut")) {
            return "CT";
        } else if (state.contains("District Of Columbia")) {
            return "DC";
        } else if (state.contains("Florida")) {
            return "FL";
        } else if (state.contains("Federated States Of Micronesia")) {
            return "FM";
        } else if (state.contains("Georgia")) {
            return "GA";
        } else if (state.contains("Guam")) {
            return "GU";
        } else if (state.contains("Hawaii")) {
            return "HI";
        } else if (state.contains("Idaho")) {
            return "ID";
        } else if (state.contains("Illinois")) {
            return "IL";
        } else if (state.contains("Indiana")) {
            return "IN";
        } else if (state.contains("Iowa")) {
            return "IA";
        } else if (state.contains("Kansas")) {
            return "KS";
        } else if (state.contains("Kentucky")) {
            return "KY";
        } else if (state.contains("Louisiana")) {
            return "LA";
        } else if (state.contains("Massachusetts")) {
            return "MA";
        } else if (state.contains("Maine")) {
            return "ME";
        } else if (state.contains("Michigan")) {
            return "MI ";
        } else if (state.contains("Minnesota")) {
            return "MN ";
        } else if (state.contains("Mississippi")) {
            return "MS";
        } else if (state.contains("Missouri")) {
            return "MO";
        } else if (state.contains("Northern Mariana Islands")) {
            return "MP";
        } else if (state.contains("North Carolina")) {
            return "NC";
        } else if (state.contains("North Dakota")) {
            return "ND ";
        } else if (state.contains("Nebraska")) {
            return "NE";
        } else if (state.contains("New Hampshire")) {
            return "NH";
        } else if (state.contains("New Jersey")) {
            return "NJ";
        } else if (state.contains("New Mexico")) {
            return "NM";
        } else if (state.contains("Nevada")) {
            return "NV";
        } else if (state.contains("New York")) {
            return "NY";
        } else if (state.contains("Ohio")) {
            return "OH";
        } else if (state.contains("Oregon")) {
            return "OR";
        } else if (state.contains("Pennsylvania")) {
            return "PA";
        } else if (state.contains("Puerto Rico")) {
            return "PR";
        } else if (state.contains("Palau")) {
            return "PW";
        } else if (state.contains("Rhode Island")) {
            return "RI";
        } else if (state.contains("South Carolina")) {
            return "SC";
        } else if (state.contains("South Dakota")) {
            return "SD";
        } else if (state.contains("Tennessee")) {
            return "TN";
        } else if (state.contains("Texas")) {
            return "TX";
        } else if (state.contains("Utah")) {
            return "UT";
        } else if (state.contains("Virginia")) {
            return "VA";
        } else if (state.contains("Virgin Islands")) {
            return "VI";
        } else if (state.contains("Vermont")) {
            return "VT";
        } else if (state.contains("Washington")) {
            return "WA";
        } else if (state.contains("West Virginia")) {
            return "WV";
        } else if (state.contains("Wyoming")) {
            return "WY";
        }

        return "AL";
    }

    public static String getTwoDecimalValue(String amount) {
        BigDecimal a = new BigDecimal(amount);
        BigDecimal roundOff = a.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        System.out.println(roundOff);
        return "" + roundOff;
    }

    public static String getDateAfterSplittingDateAndTimeBySpace(String dateAndTime) {
        if (dateAndTime.contains(" ")) {
            String[] splitDateAndTimeBySpace = dateAndTime.split(" ");
            if (splitDateAndTimeBySpace.length > 0) {
                return splitDateAndTimeBySpace[0];
            } else {
                return dateAndTime;
            }
        } else {
            return dateAndTime;
        }
    }

    public static String getTimeAfterSplittingDateAndTimeBySpace(String dateAndTime) {
        if (dateAndTime.contains(" ")) {
            String[] splitDateAndTimeBySpace = dateAndTime.split(" ");
            if (splitDateAndTimeBySpace.length > 1) {
                return splitDateAndTimeBySpace[1];
            } else {
                return dateAndTime;
            }
        } else {
            return dateAndTime;
        }
    }

}