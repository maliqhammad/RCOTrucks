package com.rco.rcotrucks.businesslogic;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.UiUtils;

public class SyncTaskForFragment extends AsyncTask<String, Integer, Integer> {
    private SyncTask.IRefreshTaskMethods refreshTaskMethods;
    private SyncTask.IRollbackOnError rollbackOnError;
    private boolean isProcessing;
    private Activity activity;
    private String msg;
    private String rollbackErrorMsg;


    public SyncTaskForFragment(Activity activity,SyncTask.IRefreshTaskMethods refreshTaskMethods, String msg) {
        this.activity = activity;
        this.isProcessing = false;
        this.refreshTaskMethods = refreshTaskMethods;
        this.msg = msg;

//        UiUtils.setTextView(fragment, R.id.loading_feedback_text, msg);
    }

    @Override
    protected void onPreExecute() {
        try {
            setProcessingState(true);
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    @Override
    protected Integer doInBackground(String... params) {
        try {
            refreshTaskMethods.executeSyncItems();

            try {

            } catch (Throwable throwable) {
                if (throwable != null)
                    throwable.printStackTrace();
            }

            return BusinessRules.OK;
        } catch (UnsupportedOperationException ex) {
            if (rollbackOnError != null)
                rollbackOnError.rollbackOnError();

            return BusinessRules.ROLLBACK_ERROR;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return BusinessRules.UNABLE_TO_SYNC;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        try {
            setProcessingMessage(msg);
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        try {
            try {
                setProcessingState(false, false);
            } catch (Throwable throwable) {
                if (throwable != null)
                    throwable.printStackTrace();
            }

            switch (result) {
                case BusinessRules.ROLLBACK_ERROR:
                    UiUtils.showToast(activity, rollbackErrorMsg);
                    break;

                case BusinessRules.UNABLE_TO_SYNC:
                    if (rollbackOnError != null)
                        rollbackOnError.rollbackOnError();

                    UiUtils.showToast(activity, msg + " failed.");
                    break;

                case BusinessRules.OK:
                default:
                    refreshTaskMethods.loadScreen();
                    break;
            }
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    public String getRollbackErrorMsg() {
        return rollbackErrorMsg;
    }

    public void setUnsupportedOperationErrorMsg(String v) {
        rollbackErrorMsg = v;
    }

    private void setProcessingMessage(String msg) {
        UiUtils.setTextView(activity, R.id.loading_feedback_text, msg);
    }

    private void setProcessingState(boolean isProcessing) {
        setProcessingState(isProcessing, true);
    }

    private void setProcessingState(boolean isProcessing, boolean clearCredentials) {
        this.isProcessing = isProcessing;

        try {
            EditText searchBox = activity.findViewById(R.id.et_search);
            searchBox.setEnabled(!isProcessing);
        } catch (Throwable throwable) {
            /*if (throwable != null)
                throwable.printStackTrace();*/
        }

        if (isProcessing) {
            try {
                activity.findViewById(R.id.loading_panel).setVisibility(View.VISIBLE);
            } catch (Throwable throwable) {
                /*if (throwable != null)
                    throwable.printStackTrace();*/
            }

            try {
                activity.findViewById(R.id.search_bar_panel).setEnabled(false);
                activity.findViewById(R.id.search_bar_loading_panel).setVisibility(View.VISIBLE);
            } catch (Throwable throwable) {
                /*if (throwable != null)
                    throwable.printStackTrace();*/
            }
        } else {
            try {
                activity.findViewById(R.id.loading_panel).setVisibility(View.GONE);
            } catch (Throwable throwable) {
                /*if (throwable != null)
                    throwable.printStackTrace();*/
            }

            try {
                activity.findViewById(R.id.search_bar_panel).setEnabled(true);
                activity.findViewById(R.id.search_bar_loading_panel).setVisibility(View.GONE);
            } catch (Throwable throwable) {
                /*if (throwable != null)
                    throwable.printStackTrace();*/
            }
        }
    }

    public interface IRefreshTaskMethods {
        void loadScreen();
        void executeSyncItems() throws Exception;
    }

    public interface IRollbackOnError {
        void rollbackOnError();
    }
}
