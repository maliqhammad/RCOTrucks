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

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.SessionManagement;

import org.w3c.dom.Text;

public class CurrencyAndUMDialog extends Dialog {

    private static final String TAG = CurrencyAndUMDialog.class.getSimpleName();
    ProgressDialog progressDialog;
    SessionManagement sessionManagement;
    public Context context;
    public Dialog dialog;
    TextView usAndGallons, cansAndLiters, pesosAndLiters, cancel;
    CurrencyAndUMDialog.CurrencyOptionInterface currencyOptionInterface;

    public CurrencyAndUMDialog(@NonNull Context context, CurrencyAndUMDialog.CurrencyOptionInterface currencyOptionInterface) {
        super(context);
        this.context = context;
        this.currencyOptionInterface = currencyOptionInterface;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_layout_currency_and_um);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Log.d(TAG, "onCreate: ");

        setIds();
        initialize();
        setListener();
    }

    void setIds() {
        Log.d(TAG, "setIds: ");
        usAndGallons = findViewById(R.id.usAndGallons);
        cansAndLiters = findViewById(R.id.canAndLiters);
        pesosAndLiters = findViewById(R.id.pesosAndLiters);
        cancel = findViewById(R.id.cancel);
    }

    void initialize() {
        Log.d(TAG, "initialize: ");
        progressDialog = new ProgressDialog(context);
        sessionManagement = new SessionManagement(context);
    }

    void setListener() {
        Log.d(TAG, "setListener: ");

        usAndGallons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currencyOptionInterface.onUsAndGallonsSelection();
            }
        });
        cansAndLiters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currencyOptionInterface.onCanAndLitersSelection();
            }
        });
        pesosAndLiters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currencyOptionInterface.onPesosAndLitersSelection();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currencyOptionInterface.onCancelSelection();
            }
        });

    }


    public interface CurrencyOptionInterface {
        void onUsAndGallonsSelection();

        void onCanAndLitersSelection();

        void onPesosAndLitersSelection();

        void onCancelSelection();
    }

}


