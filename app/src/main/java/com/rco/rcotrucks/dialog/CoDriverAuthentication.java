package com.rco.rcotrucks.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.google.android.gms.maps.model.LatLng;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.SessionManagement;

public class CoDriverAuthentication extends Dialog {

    private static final String TAG = CoDriverAuthentication.class.getSimpleName();
    ProgressDialog progressDialog;
    SessionManagement sessionManagement;
    public Context context;
    public Dialog dialog;
    EditText username, password;
    String usernameValue = "", passwordValue = "";
    TextView authenticate, cancel;
    ImageView clearUsernameIcon;
    CoDriverAuthenticationInterface coDriverAuthenticationInterface;


    public CoDriverAuthentication(@NonNull Context context, CoDriverAuthenticationInterface coDriverAuthenticationInterface) {
        super(context);
        this.context = context;
        this.coDriverAuthenticationInterface = coDriverAuthenticationInterface;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_codriver_authentication_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Log.d(TAG, "onCreate: ");

        setIds();
        initialize();
        setListener();
    }

    void setIds() {
        Log.d(TAG, "setIds: ");
        username = findViewById(R.id.username);
        clearUsernameIcon = findViewById(R.id.clearUsernameIcon);
        password = findViewById(R.id.password);
        authenticate = findViewById(R.id.authenticate);
        cancel = findViewById(R.id.cancel);
    }

    void initialize() {
        Log.d(TAG, "initialize: ");
        progressDialog = new ProgressDialog(context);
        sessionManagement = new SessionManagement(context);
    }

    void setListener() {
        Log.d(TAG, "setListener: ");

        username.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                try {
                    Log.d(TAG, "onKey: code: " + keyCode);
                    Log.d(TAG, "onKey: event: " + keyEvent.getCharacters());
                    if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        password.requestFocus();

                        return true;
                    }
                } catch (Throwable throwable) {
                    if (throwable != null)
                        throwable.printStackTrace();
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
                try {
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
                } catch (Throwable throwable) {
                    Log.d(TAG, "afterTextChanged: throwable: " + throwable.getMessage());
                }
            }
        });


        password.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                try {
                    if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        authenticate();
                        return true;
                    }

                    if (keyCode == KeyEvent.KEYCODE_ENTER)
                        return true;
                } catch (Throwable throwable) {
                    if (throwable != null)
                        throwable.printStackTrace();
                }

                return false;
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

        authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticate();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coDriverAuthenticationInterface.onCancelPressed();
                dismiss();
            }
        });

    }

    boolean validate() {

        usernameValue = username.getText().toString();
        passwordValue = password.getText().toString();

        boolean valid = false;

        if (usernameValue.isEmpty()) {
            username.setError("Please enter login");
            valid = true;
        }

        if (passwordValue.isEmpty()) {
            password.setError("Please enter password");
            valid = true;
        }
        return valid;
    }

    void authenticate() {
        if (validate()) {
        } else {
            coDriverAuthenticationInterface.onAuthentication(usernameValue, passwordValue);
            dismiss();
        }
    }

    public interface CoDriverAuthenticationInterface {
        void onAuthentication(String username, String password);

        void onCancelPressed();
    }

}

