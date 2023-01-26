package com.rco.rcotrucks.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.rco.rcotrucks.BuildConfig;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.adapters.ServersListAdapter;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.ServerUrl;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.businesslogic.rms.User;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.SessionManagement;
import com.rco.rcotrucks.utils.UiUtils;
import com.rco.rcotrucks.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Permission;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

//public class LoginActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int SPEECH_REQUEST_CODE = 100;
    private int EXTERNAL_STORAGE_PERMISSION_CODE = 23;

    SessionManagement sessionManagement;
    private BusinessRules rules = BusinessRules.instance();
    private ServersListAdapter adapter;
    private boolean isProcessing = false;

    ImageView backIcon, clearUsernameIcon, appIcon;
    ImageButton ivVoiceInputMic;
    TextView versionNumber, tvVoiceInput;
    Button loginButton;
    EditText username, password;
    AppCompatCheckBox rememberPassword;
    ListView serversList;
    ConstraintLayout authenticatingPanelLayout;
    boolean mIsNight = false, allFilesAccessPermissionGranted = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
//        try {
//            Jan 03, 2022  -   Added handleDayOrNightView because its destroying our activity and recreating it again
//            so Login Activity is created twice
        handleDayOrNightView();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_update);

        setIds();
        initializeBusinessRules();
        initialize();
        setListeners();

        //rules.unsetAuthenticatedUser();
        loadServersList();


//            requestReadWritePermissions();
//        } catch (Throwable throwable) {
//            Log.d(TAG, "requestPermissions: onCreate: throwable: " + throwable.getMessage());
//        }

//        Jan 14, 2022  -
//        In android 30+(including android 13) we need permission all files access permission to phone internal documents
//        which we can use to access zipped file from phones memory and then unzipped that folder to access offline tiles(images.png)
        requestPermissionToAccessAllFiles();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
//        For now, we are not showing the server list to choose
//        Initially it is shown when testing/debugging the app and make it invisible when in release build
//        if (rules.isProductionMode()) {
//            serversList.setVisibility(View.INVISIBLE);
//        }

        rules.setSelectedServerUrl("fox");
//        rules.setSelectedServerUrl("lion");

//        Jan 06, 2022  -   Here we will only check that do we have all files access permission and if yes only then we will go and perform copy paste of
//        filtered_tiles.zip from our project assets resource into app installed phone documents folder -
//        And in case we don't have the permission we already have requestPermissionToAccessAllFiles to request all files access permission
        if (checkPermissionToAccessAllFiles()) {
//            Jan 06, 2022  -   We should check if we already have this filtered_tiles.zip file in our desired destination address or not
//            obviously if no only then paste it into phone's memory else ignore
//            String unzipAtLocationPath = "/storage/emulated/0/Documents/RCOTruck/tiles.zip";
//            File unZippedFileDirectory = new File(unzipAtLocationPath);
//
//            boolean isZipTileExists = unZippedFileDirectory.exists();
//            Log.d(TAG, "onResume: isZipTileExists: "+isZipTileExists);
//            if (!isZipTileExists) {
////                copyAsset("filtered_tiles.zip");
//                copyZoomLevel("eleven.zip");
//            }

            String unzipElevenAtLocationPath = "/storage/emulated/0/Documents/RCOTruck/tiles.zip";
//            String unzipElevenAtLocationPath = "/storage/emulated/0/Documents/RCOTruck/eleven.zip";
//            String unzipElevenAtLocationPath = "/storage/emulated/0/Documents/RCOTruck/eleven.rar";
//            String unzipElevenAtLocationPath = "/storage/emulated/0/Documents/RCOTruck/ten.zip";
            File unZippedFileDirectoryEleven = new File(unzipElevenAtLocationPath);

            boolean isZipTileElevenExists = unZippedFileDirectoryEleven.exists();
            Log.d(TAG, "onResume: isZipTileExists: "+isZipTileElevenExists);
            if (!isZipTileElevenExists) {
                copyAsset("tiles.zip");
//                copyAsset("filtered_tiles.zip");
//                copyZoomLevel("eleven.zip");
//                copyZoomLevel("eleven.rar");
//                copyZoomLevel("ten.zip");
            }


        }
    }

    boolean checkPermissionToAccessAllFiles() {
        Log.d(TAG, "checkPermissionToAccessAllFiles: ");
        if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                Log.d(TAG, "checkPermissionToAccessAllFiles: return false");
                return false;
            }
        }
        Log.d(TAG, "checkPermissionToAccessAllFiles: return true");
        return true;
    }

    void requestPermissionToAccessAllFiles() {
        Log.d(TAG, "requestPermissionToAccessAllFiles: ");
        if (Build.VERSION.SDK_INT >= 30) {
            allFilesAccessPermissionGranted = Environment.isExternalStorageManager();
            Log.d(TAG, "requestPermissionToAccessAllFiles: allFilesAccessPermissionGranted: " + allFilesAccessPermissionGranted);
            if (!allFilesAccessPermissionGranted) {
                Intent getPermissions = new Intent();
                getPermissions.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getPermissions);
            }
        }
    }


    void setIds() {
        Log.d(TAG, "setIds: ");

        tvVoiceInput = findViewById(R.id.tv_voice_input);
        ivVoiceInputMic = findViewById(R.id.iv_mic_voice_input);

        backIcon = findViewById(R.id.back);
        versionNumber = findViewById(R.id.version_number);
        loginButton = findViewById(R.id.connect);
        username = findViewById(R.id.username);
        clearUsernameIcon = findViewById(R.id.clearUsernameIcon);
        password = findViewById(R.id.password);
        rememberPassword = findViewById(R.id.remember_password);
        serversList = findViewById(R.id.servers_list);
        authenticatingPanelLayout = findViewById(R.id.authenticating_panel_layout);
        appIcon = findViewById(R.id.appIcon);
    }

    void initializeBusinessRules() {

        rules.instatiateDatabase(getApplicationContext());
        rules.setDeviceId(LoginActivity.this);
    }

    void initialize() {
        Log.d(TAG, "initialize: ");

        ivVoiceInputMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
                invokeBluetoothPermissions();
            }
        });

        sessionManagement = new SessionManagement(LoginActivity.this);
        versionNumber.setText(getString(R.string.app_version2, BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME));
        rememberPassword.setChecked(rules.rememberUserPassword());

        if (rules.existsLastLoggedInUsername()) {
            username.setText(rules.getLastLoggedInUsername());
            clearUsernameIcon.setVisibility(View.VISIBLE);

            if (rules.rememberUserPassword())
                password.setText(rules.getLastLoggedInUserPassword());
        } else {
            clearUsernameIcon.setVisibility(View.GONE);
        }

        username.requestFocus();
    }

    void setListeners() {
        Log.d(TAG, "setListeners: ");


        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: loginButton: isProcessing: " + isProcessing);
                if (isProcessing)
                    return;

//                Log.d(TAG, "onClick: isEmpty: " + (UiUtils.isNullOrWhitespacesAny(username, password)));
                if (UiUtils.isNullOrWhitespacesAny(username, password)) {
                    UiUtils.showToast(LoginActivity.this, "Please provide a username / password and try again.");
                    return;
                }

                invokeBluetoothPermissions();

//                LoginUserTask loginUserTask = new LoginUserTask();
//                loginUserTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, username.getText().toString(), password.getText().toString());
            }
        });

        username.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    password.requestFocus();
                    return true;
                }
                return false;
            }
        });

        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: input: " + username.getText().toString());
                if (username.getText().length() > 0) {

                    if (clearUsernameIcon.getVisibility() == View.GONE) {

                        clearUsernameIcon.setVisibility(View.VISIBLE);
                    }
                } else {

                    if (clearUsernameIcon.getVisibility() == View.VISIBLE) {

                        clearUsernameIcon.setVisibility(View.GONE);
                    }
                }
            }
        });


        password.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    loginButton.callOnClick();

                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                    return true;
                return false;
            }
        });


        rememberPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rules.switchRememberUserPassword();
            }
        });

        clearUsernameIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (username.getText().length() > 0) {
                    username.setText("");
                }
            }
        });


        appIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                requestReadWritePermissions();
                Log.d(TAG, "onClick: requestReadWritePermissions: appIcon: ");
//                ActivityCompat.requestPermissions(LoginActivity.this,
//                        new String[]{
//                                Manifest.permission.READ_EXTERNAL_STORAGE,
//                                Manifest.permission.READ_MEDIA_AUDIO,
//                                Manifest.permission.READ_MEDIA_IMAGES,
//                                Manifest.permission.READ_MEDIA_VIDEO},
//                        EXTERNAL_STORAGE_PERMISSION_CODE);

            }
        });
    }


    public static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1234;

    //    @RequiresApi(api = 31)
    public void invokeBluetoothPermissions() {
//        if (Build.VERSION.SDK_INT >= 31) {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE
                },
                BLUETOOTH_PERMISSION_REQUEST_CODE);
//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
//                    BLUETOOTH_PERMISSION_REQUEST_CODE);
//        }
//        rules.setLastCtx(this);
    }

    private void loadServersList() {
//        Log.d(TAG, "loadServersList: ");
        ArrayList<ServerUrl> serverUrls = rules.getServerUrls();

        adapter = new ServersListAdapter(this, serverUrls);
        if (serversList != null) {
            serversList.setAdapter(adapter);
            serversList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    rules.setSelectedServerUrl(position);
                    loadServersList();
                }
            });
        }
    }

//    @Override
//    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//        rules.setSelectedServerUrl(i);
//        loadServersList();
//    }

    @Override
    public void onBackPressed() {
        Utils.exitApplication(this);
    }

    public class LoginUserTask extends AsyncTask<String, Integer, Integer> {
        private static final int INVALID_CREDENTIALS = 1, UNABLE_TO_COMM = 2, UNABLE_TO_AUTHENTICATE = 3,
                UNABLE_TO_GET_USER_DATA = 4;
        private static final int AUTHENTICATING = 7;
        private static final int AUTHENTICATION_DONE = 5;

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "LoginUserTask: onPreExecute: ");
            //rules.unsetAuthenticatedUser();
            setProcessingState(true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            Log.d(TAG, "LoginUserTask: doInBackground: ");
            String username = null;
            String password = null;

            try {
                publishProgress(AUTHENTICATING);

                username = params[0];
                password = params[1];

                Log.d(TAG, "LoginUserTask: doInBackground: username: " + username + " password: " + password);
                User user = rules.authenticate(username, password);

//                Log.d(TAG, "LoginUserTask: doInBackground: user: " + user);
                if (user == null)
                    throw new Exception("Null user");

                List<String> truckAndTrailerInfoList = rules.getDriverTruckAndTrailerInfo(user.getRmsUserId());
                Log.d(TAG, "doInBackground: truckAndTrailerInfoList: size: " + truckAndTrailerInfoList.size());
                if (truckAndTrailerInfoList.size() > 0) {
                    user.setTruckNumber(truckAndTrailerInfoList.get(0));
                }
                if (truckAndTrailerInfoList.size() > 1) {
                    user.setTrailerNumber(truckAndTrailerInfoList.get(1));
                }
                if (truckAndTrailerInfoList.size() > 2) {
                    user.setTrailerNumber2(truckAndTrailerInfoList.get(2));
                }

                if (!sessionManagement.isLoggedIn()) {
                    createLoginSession(user);
                } else {
                    sessionManagement.logoutUser();
                    createLoginSession(user);
                }

//                Log.d(TAG, "LoginUserTask: doInBackground: usertype: " + user.getItemType());
                rules.setAuthenticatedUser(user, username, password.trim());
//                Sep 02, 2022  -
//                rules.storeUser(user, "offlineuser");
                rules.storeUserCredentials(user.getRmsUserId(), username, password);


                if (!rules.existsUserRights(user.getRmsUserId())) {
                    ArrayList<String> userRights = rules.syncUserRights(user.getRmsUserId());
                    rules.deleteUserRights(user.getRmsUserId());
                    rules.setUserRights(user.getRmsUserId(), userRights);
                } else
                    rules.loadUserRights(user.getRmsUserId());

                return BusinessRules.OK;
            } catch (Throwable throwable) {
                Log.d(TAG, "doInBackground: throwable: " + throwable.getMessage());
//                Log.d(TAG, "LoginUserTask: doInBackground: throwable: ");
                try {
                    if (rules.existsUserCredentials(username, password)) {
                        User user = rules.authenticateOffline(username, password);
                        Log.d(TAG, "LoginUserTask: doInBackground: user: " + user);
                        Log.d(TAG, "LoginUserTask: doInBackground: user: getItemType: " + user.getItemType());
                        Log.d(TAG, "LoginUserTask: doInBackground: user: reportingPeriod: " + user.getReportingPeriod());

                        rules.setAuthenticatedUser(user, username, password);

                        ArrayList<String> userRights = rules.getUserRights(user.getRmsUserId());
                        Log.d(TAG, "LoginUserTask: doInBackground: userRights: " + userRights);
                        rules.setUserRights(user.getRmsUserId(), userRights);
//                        Dec 29, 2022  -   Offline is taking too much time to login so added this return unable_to_sync
//                        it seems working
                        return BusinessRules.UNABLE_TO_SYNC;
                    }
                } catch (Throwable t2) {
                    Log.d(TAG, "doInBackground: t2: " + t2.getMessage());
                }

                throwable.printStackTrace();

                return BusinessRules.UNABLE_TO_SYNC;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
//            Log.d(TAG, "LoginUserTask: onProgressUpdate: ");
            if (values[0] == AUTHENTICATING)
                setProcessingMessage("Authenticating...");
            else if (values[0] == AUTHENTICATION_DONE)
                setProcessingMessage("Synchronizing...");
        }

        @Override
        protected void onPostExecute(Integer result) {
            try {
                switch (result) {

                    case BusinessRules.UNABLE_TO_SYNC:
                        Log.d(TAG, "onPostExecute: UNABLE_TO_SYNC: ");
                        if (!UiUtils.isOnline(LoginActivity.this,
                                getResources().getString(R.string.no_internet_connection) + " Logging in offline mode.")) {
                        } else if (rules.isAuthenticatedUser()) {
                            UiUtils.showToast(LoginActivity.this, "Unable to sync. Logging in offline mode.");
                        } else {
                            setProcessingState(false, false);

                            UiUtils.showExclamationDialog(LoginActivity.this, getString(R.string.app_name), getString(R.string.error_unable_to_login),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                            break;
                        }

                    case BusinessRules.OK:
                        Log.d(TAG, "onPostExecute: OK: ");
                        EditText username = findViewById(R.id.username);
                        rules.setLastLoggedInUsername(username.getText().toString());

                        if (rules.rememberUserPassword()) {
                            EditText password = findViewById(R.id.password);
                            rules.setLastLoggedInUserPassword(password.getText().toString());
                        } else
                            rules.clearLastLoggedInUserPassword();

                        Log.d(TAG, "onPostExecute: startActivity: ");
                        startActivity(new Intent(LoginActivity.this, MainMenuActivity.class));

                        (new Handler()).postDelayed(new Runnable() {
                            public void run() {
                                setProcessingState(false);
                                finish();
                            }
                        }, 3000);
                        break;

                    default:
                        setProcessingState(false, false);
                        UiUtils.showToast(LoginActivity.this, "Invalid username and/or password.");
                        break;
                }
            } catch (Throwable throwable) {
                Log.d(TAG, "onPostExecute: throwable: " + throwable.getMessage());
            }
        }
    }

    private void setProcessingMessage(String msg) {
        UiUtils.setTextView(LoginActivity.this, R.id.authenticating_feedback_text, msg);
    }

    private void setProcessingState(boolean isProcessing) {
        setProcessingState(isProcessing, true);
    }

    private void setProcessingState(boolean isProcessing, boolean clearCredentials) {
        this.isProcessing = isProcessing;

        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);

        if (isProcessing) {
            username.setEnabled(false);
            password.setEnabled(false);

            loginButton.setEnabled(false);
            authenticatingPanelLayout.setVisibility(View.VISIBLE);
        } else {
            if (clearCredentials) {
                username.setText("");
                password.setText("");
            }

            username.setEnabled(true);
            password.setEnabled(true);

            if (clearCredentials)
                username.requestFocus();

            loginButton.setEnabled(true);
            authenticatingPanelLayout.setVisibility(View.GONE);
        }
    }

    private EditText.OnEditorActionListener getOnEditorEnterKeyActionListener(final OnFieldEnterKeyPressListener onClickListener) {
        return new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                try {
                    if (event == null) {
                        if (actionId != EditorInfo.IME_ACTION_NEXT && actionId != EditorInfo.IME_ACTION_DONE)
                            return false; // Let system handle everything except soft enters in singleLine EditTexts
                    } else if (actionId == EditorInfo.IME_NULL) {
                        // Capture most soft enters in multi-line EditTexts and all hard enters.
                        // They supply a zero actionId and a valid KeyEvent rather than
                        // a non-zero actionId and a null event like the previous cases.

                        if (event.getAction() != KeyEvent.ACTION_DOWN)
                            return true; // We capture the event when key is 1st pressed and consume the event when key is released
                    } else
                        return false;

                    // We let the system handle it when the listener is triggered by something that wasn't an enter.

                    // Code from this point on will execute whenever the user presses enter in an attached view, regardless of position,
                    // keyboard, or singleLine status.

                    if (onClickListener != null)
                        onClickListener.onClickEvent();
                } catch (Throwable throwable) {
                    Log.d(TAG, "onEditorAction: throwable: " + throwable.getMessage());
                }

                return true;            // Consume the event
            }
        };
    }

    public interface OnFieldEnterKeyPressListener {
        void onClickEvent();
    }

    void createLoginSession(User user) {
        Log.d(TAG, "createLoginSession: RMS: getURL: " + Rms.getUrl());
        sessionManagement.createLoginSession(user.getFirstName(), user.getLastName(),
                user.getDeviceId(), user.getCompany(), user.getRmsUserId(), user.getItemType(),
                user.getAddress1(), user.getAddress2(), user.getCity(), user.getState(),
                user.getZipCode(), user.getOrgName(), user.getOrgNumber(),
                user.getFunctionalGroupName(), user.getUserGroupName(),
                user.getRole(), user.getLogin(), user.getPassword(), user.getTitle(),
                user.getCountry(), user.getEmail(), user.getTelephone(), user.getLatitude(),
                user.getLongitude(), "" + user.isSyncedUp(), user.getDriversLicenseNumber()
                , Rms.getUrl(), mIsNight);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "requestReadWritePermissions: requestPermissions: onRequestPermissionsResult: requestCode: " + requestCode);

        if (requestCode == EXTERNAL_STORAGE_PERMISSION_CODE) {
            Log.d(TAG, "requestReadWritePermissions: onRequestPermissionsResult: grantResults.length: " + grantResults.length);
            Log.d(TAG, "requestReadWritePermissions: onRequestPermissionsResult: isPewrmissionGranted: " + (grantResults.length > 0));
            Log.d(TAG, "requestReadWritePermissions: onRequestPermissionsResult: check: isPewrmissionGranted: " + (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED));
            if (grantResults.length > 0) {
                Toast.makeText(this, "External Storage Permissions Granted", Toast.LENGTH_SHORT).show();
            } else {
//                Log.d(TAG, "onRequestPermissionsResult: Please enable location permission");
                Toast.makeText(this, "Please enable External Storage permission",
                        Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == rules.BLUETOOTH_PERMISSION_REQUEST_CODE) {
//            Sep 21, 2022  -   In any case even when user granted permission or not we should open the app
//            So comment the code inside loop
            if (grantResults.length > 0) {
//                for (int i = 0; i < grantResults.length; i++) {
//                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
////                        Log.d(TAG, "onRequestPermissionsResult: Please enable permission");
//                        return;
//                    }
//                }
                LoginUserTask loginUserTask = new LoginUserTask();
                loginUserTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, username.getText().toString(), password.getText().toString());
//                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
            } else {

//                Log.d(TAG, "onRequestPermissionsResult: Please enable location permission");
                Toast.makeText(this, "Please enable permission",
                        Toast.LENGTH_SHORT).show();

                LoginUserTask loginUserTask = new LoginUserTask();
                loginUserTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, username.getText().toString(), password.getText().toString());
            }

        }


    }

    private void handleDayOrNightView() {
        Log.d(TAG, "handleDayOrNightView: ");
        if (rules.getPhoneLastBestLocation(LoginActivity.this) != null) {
            boolean isNight = rules.isItNight(new LatLng(rules.getPhoneLastBestLocation(LoginActivity.this).getLatitude(), rules.getPhoneLastBestLocation(LoginActivity.this).getLongitude()));
            Log.d(TAG, "handleDayOrNightView: isNight: " + isNight);
            mIsNight = isNight;
//            sessionManagement.setKeyIsNight(isNight);
            UiUtils.applyDarkTheme(isNight);
        }
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Now...");

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "requestReadWritePermissions: onActivityResult: requestCode: " + requestCode);
        switch (requestCode) {
            case SPEECH_REQUEST_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> arrayList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    tvVoiceInput.setText(arrayList.get(0));
                }
                if (tvVoiceInput.getText().toString().equals("login,proceed,connect,connectivity")) {
                    Intent intentMenuActivity = new Intent(getApplicationContext(), MainMenuActivity.class);
                    startActivity(intentMenuActivity);
                }
                break;
            }
        }
    }


    void requestReadWritePermissions() {
        Log.d(TAG, "requestReadWritePermissions: ");


        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "requestReadWritePermissions: permission not granted so show permission request ");
            if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_PERMISSION_CODE);

            }
        } else {
            Log.d(TAG, "requestReadWritePermissions: permission granted: so request it");
        }
//        String editTextData = editText.getText().toString();

        // getExternalStoragePublicDirectory() represents root of external storage, we are using DOWNLOADS
        // We can use following directories: MUSIC, PODCASTS, ALARMS, RINGTONES, NOTIFICATIONS, PICTURES, MOVIES
//        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        // Storing the data in file with name as geeksData.txt
//        File file = new File(folder, "geeksData.txt");
//        writeTextData(file, loginButton);
//        editText.setText("");

    }

//    public void savePrivately(View view) {
//        String editTextData = editText.getText().toString();
//
//        // Creating folder with name GeekForGeeks
//        File folder = getExternalFilesDir("GeeksForGeeks");
//
//        // Creating file with name gfg.txt
//        File file = new File(folder, "gfg.txt");
//        writeTextData(file, editTextData);
//        editText.setText("");
//    }
//
//    public void viewInformation(View view) {
//        // Creating an intent to start a new activity
//        Intent intent = new Intent(WriteExternalStorsgeAct.this, ViewInformationActivity.class);
//        startActivity(intent);
//    }
//
//    private void writeTextData(File file, String data) {
//        FileOutputStream fileOutputStream = null;
//        try {
//            fileOutputStream = new FileOutputStream(file);
//            fileOutputStream.write(data.getBytes());
//            Toast.makeText(this, "Done" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (fileOutputStream != null) {
//                try {
//                    fileOutputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    private void copyAsset(String fileName) {
        Log.d(TAG, "copyAsset: ");

        String destinationDirPatch = "";
        Log.d(TAG, "copyAsset: dirPath: Build.VERSION.SDK_INT >= Build.VERSION_CODES.R: " + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            dirPatch = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/RCOTrucks/FilteredTilesWhichWeUnzipped";
//            destinationDirPatch = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/RCOTrucks";
            destinationDirPatch = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/RCOTruck";
        } else {
//            destinationDirPatch = Environment.getExternalStorageDirectory() + "/RCOTrucks";
            destinationDirPatch = Environment.getExternalStorageDirectory() + "/RCOTruck";
        }
        Log.d(TAG, "copyAsset: dirPath: destinationDirPatch: " + destinationDirPatch);

        File destinationDir = new File(destinationDirPatch);
        Log.d(TAG, "copyAsset: dir isExists: " + destinationDir.exists());
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }
        Log.d(TAG, "copyAsset: destinationDir: " + destinationDir);

//        Log.d(TAG, "copyAsset: " + dir.getAbsolutePath());
        AssetManager assetManager = getAssets();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = assetManager.open(fileName);
            Log.d(TAG, "copyAsset: inputStream: " + inputStream);
            File outFile = new File(destinationDir, "tiles.zip");
            outputStream = new FileOutputStream(outFile);
            Log.d(TAG, "copyAsset: outputStream: " + outputStream);
            copiedFile(inputStream, outputStream);
            Toast.makeText(LoginActivity.this, "Tiles zip saved in phones memory!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.d(TAG, "copyAsset: IOException: " + e);
//            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
//                    e.printStackTrace();
                    Log.d(TAG, "copyAsset: finally: IOException: " + e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.d(TAG, "copyAsset: finally: close: IOException: " + e);
//                    e.printStackTrace();
                }
            }
        }
    }

    private void copyZoomLevel(String fileName) {
        Log.d(TAG, "copyAsset: ");

        String destinationDirPatch = "";
        Log.d(TAG, "copyAsset: dirPath: Build.VERSION.SDK_INT >= Build.VERSION_CODES.R: " + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            destinationDirPatch = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/RCOTruck";
        } else {
            destinationDirPatch = Environment.getExternalStorageDirectory() + "/RCOTruck";
        }
        Log.d(TAG, "copyAsset: dirPath: destinationDirPatch: " + destinationDirPatch);

        File destinationDir = new File(destinationDirPatch);
        Log.d(TAG, "copyAsset: dir isExists: " + destinationDir.exists());
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }
        Log.d(TAG, "copyAsset: destinationDir: " + destinationDir);

//        Log.d(TAG, "copyAsset: " + dir.getAbsolutePath());
        AssetManager assetManager = getAssets();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = assetManager.open(fileName);
            Log.d(TAG, "copyAsset: inputStream: " + inputStream);
            File outFile = new File(destinationDir, fileName);
            outputStream = new FileOutputStream(outFile);
            Log.d(TAG, "copyAsset: outputStream: " + outputStream);
            copiedFile(inputStream, outputStream);
            Toast.makeText(LoginActivity.this, "Tiles zip saved in phones memory!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.d(TAG, "copyAsset: IOException: " + e);
//            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
//                    e.printStackTrace();
                    Log.d(TAG, "copyAsset: finally: IOException: " + e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.d(TAG, "copyAsset: finally: close: IOException: " + e);
//                    e.printStackTrace();
                }
            }
        }
    }


    private void copiedFile(InputStream inputStream, OutputStream outputStream) throws IOException {
        Log.d(TAG, "copiedFile: copyAsset: ");
        byte[] buffer = new byte[32 * 1024];
        int read;
        Log.d(TAG, "copyAsset: copiedFile: buffer: size: " + buffer.length);
//        We were having a crash that this read becomes -1 so we replaced the condition with -1 instead of 1
//        while ((read = inputStream.read(buffer)) != 1) {
        while ((read = inputStream.read(buffer)) != -1) {
            Log.d(TAG, "copiedFile: read: " + read);
            outputStream.write(buffer, 0, read);
        }

    }

}
