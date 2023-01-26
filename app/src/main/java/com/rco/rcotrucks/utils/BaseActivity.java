package com.rco.rcotrucks.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.rco.rcotrucks.R;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class BaseActivity extends Activity {

    public static final int REQUEST_CODE_FOR_IMAGE = 100;

    public void extractToFullScreen() {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    public void hideSoftKeyboard(Context context, EditText editText) {

        editText.clearFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public void hideSoftKeyboard(Context context, AppCompatEditText editText) {

        editText.clearFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }


    public void pickImage(Activity activity) {

        Dexter.withActivity(activity)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            showImagePickerOptions(activity);
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog(activity);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    private void showImagePickerOptions(Context context) {

        ImagePickerActivity.showImagePickerOptions(this, new ImagePickerActivity.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                launchCameraIntent(context);
            }

            @Override
            public void onChooseGallerySelected() {
                launchGalleryIntent(context);
            }
        });
    }

    private void launchCameraIntent(Context context) {
        Intent intent = new Intent(context, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_PHOTO_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        startActivityForResult(intent, REQUEST_CODE_FOR_IMAGE);
    }

    private void launchGalleryIntent(Context context) {
        Intent intent = new Intent(context, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, REQUEST_CODE_FOR_IMAGE);
    }


    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private void showSettingsDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.dialog_permission_title));
        builder.setMessage(getString(R.string.dialog_permission_message));
        builder.setPositiveButton(getString(R.string.go_to_settings), (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();

    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    public void loadProfile(Context context, String url, ImageView imageView, boolean isLoadInCircleCrop) {

        if (isLoadInCircleCrop) {
            Glide.with(context)
                    .load(url)
                    .apply(new RequestOptions()
                            .circleCrop())
                    .into(imageView);
        } else {
            Glide.with(context)
                    .load(url)
                    .into(imageView);
        }
        imageView.setColorFilter(ContextCompat.getColor(this, android.R.color.transparent));
    }

    public String getRealPathFromURI(Uri uri) {
        if (uri == null) {
            return null;
        }
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    public void showImageUsingGlide(Activity activity, ImageView imageView, String imageUrl) {
        Glide.with(activity)
                .load(imageUrl)
                .apply(new RequestOptions().circleCrop().placeholder(R.drawable.app_truck_icon))
                .into(imageView);
    }

    public void showImageUsingGlide(Context context, ImageView imageView, String imageUrl) {
        Glide.with(context)
                .load(imageUrl)
                .apply(new RequestOptions().circleCrop().placeholder(R.drawable.app_truck_icon))
                .into(imageView);
    }

    public String getEncoded64ImageStringFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteFormat = stream.toByteArray();
        // get the base 64 string
        String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);

        return imgString;
    }

    public String getFuelCodeFromFuelType(String selfFuelName) {
        if (selfFuelName.equalsIgnoreCase("Diesel")) {
            return "01";
        } else if (selfFuelName.equalsIgnoreCase("Gasoline")) {
            return "02";
        } else if (selfFuelName.equalsIgnoreCase("Ethanol")) {
            return "03";
        } else if (selfFuelName.equalsIgnoreCase("Propane")) {
            return "04";
        } else if (selfFuelName.equalsIgnoreCase("CNG")) {
            return "05";
        } else if (selfFuelName.equalsIgnoreCase("A-55")) {
            return "06";
        } else if (selfFuelName.equalsIgnoreCase("E-85")) {
            return "07";
        } else if (selfFuelName.equalsIgnoreCase("M-85")) {
            return "08";
        } else if (selfFuelName.equalsIgnoreCase("Gasohol")) {
            return "09";
        } else if (selfFuelName.equalsIgnoreCase("LNG")) {
            return "10";
        } else if (selfFuelName.equalsIgnoreCase("Methanol")) {
            return "11";
        } else if (selfFuelName.equalsIgnoreCase("Biodiesel")) {
            return "12";
        } else if (selfFuelName.equalsIgnoreCase("Electricity")) {
            return "13";
        } else if (selfFuelName.equalsIgnoreCase("Hydrogen")) {
            return "14";
        }
        return "";
    }


}
