package com.rco.rcotrucks.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rco.rcotrucks.utils.TextUtils;

public abstract class AppBaseFragment extends Fragment {
    private ProgressDialog mProgressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected void showLoading(String message){
        if (mProgressDialog == null || TextUtils.isNullOrEmpty(message))
            return;

        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    protected void hideLoading() {
        if (mProgressDialog == null)
            return;

        mProgressDialog.dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
