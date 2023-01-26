package com.rco.rcotrucks.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.rco.rcotrucks.BuildConfig;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.SessionManagement;
import com.rco.rcotrucks.utils.UiUtils;

public class PickupDialog extends Dialog {

    private static final String TAG = PickupDialog.class.getSimpleName();
    ProgressDialog progressDialog;
    SessionManagement sessionManagement;
    public Context context;
    public Dialog dialog;
    EditText trailerNumber;
    String trailerNumberValue = "";
    TextView yes, cancel, pickupOrDropTitle;
    ImageView clearTrailerNumberIcon;
    PickupInterface pickupInterface;


    public PickupDialog(@NonNull Context context, PickupInterface pickupInterface) {
        super(context);
        this.context = context;
        this.pickupInterface = pickupInterface;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_layout_pickup_drop);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Log.d(TAG, "onCreate: ");

        setIds();
        initialize();
        setListener();
    }

    void setIds() {
        Log.d(TAG, "setIds: ");
        pickupOrDropTitle = findViewById(R.id.pickupOrDropTitle);
        trailerNumber = findViewById(R.id.trailerNumber);
        clearTrailerNumberIcon = findViewById(R.id.clearTrailerNumberIcon);
        yes = findViewById(R.id.yes);
        cancel = findViewById(R.id.cancel);
    }

    void initialize() {
        Log.d(TAG, "initialize: ");
        progressDialog = new ProgressDialog(context);
        sessionManagement = new SessionManagement(context);
        pickupOrDropTitle.setText(R.string.pickup_trailer);
        trailerNumber.requestFocus();
    }

    void setListener() {
        Log.d(TAG, "setListener: ");

        trailerNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    Log.d(TAG, "afterTextChanged: input: " + trailerNumber.getText().toString());
                    if (trailerNumber.getText().length() > 0) {

                        if (clearTrailerNumberIcon.getVisibility() == View.GONE) {

                            clearTrailerNumberIcon.setVisibility(View.VISIBLE);
                        }
                    } else {

                        if (clearTrailerNumberIcon.getVisibility() == View.VISIBLE) {

                            clearTrailerNumberIcon.setVisibility(View.GONE);
                        }
                    }
                } catch (Throwable throwable) {
                    Log.d(TAG, "afterTextChanged: throwable: " + throwable.getMessage());
                }
            }
        });

        clearTrailerNumberIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (trailerNumber.getText().length() > 0) {

                    trailerNumber.setText("");
                }
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticate();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickupInterface.onCancelPressed();
                UiUtils.closeKeyboard(trailerNumber);
                dismiss();
            }
        });

    }

    void authenticate() {
        if (validate()) {
        } else {
            pickupInterface.onYesPressed(trailerNumberValue);
            UiUtils.closeKeyboard(trailerNumber);
            dismiss();
        }
    }

    boolean validate() {

        trailerNumberValue = trailerNumber.getText().toString();

        boolean valid = false;

        if (trailerNumberValue.isEmpty()) {
            trailerNumber.setError("Please enter trailer number");
            valid = true;
        }
        return valid;
    }

    public interface PickupInterface {
        void onYesPressed(String trailerNumber);

        void onCancelPressed();
    }

}

