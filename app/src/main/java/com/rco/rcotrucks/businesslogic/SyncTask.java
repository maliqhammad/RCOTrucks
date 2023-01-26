package com.rco.rcotrucks.businesslogic;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.UiUtils;

public class SyncTask extends AsyncTask<String, Integer, Integer> {
    private static final String TAG = SyncTask.class.getSimpleName();
    private IRefreshTaskMethods refreshTaskMethods;
    private IRollbackOnError rollbackOnError;
    private boolean isProcessing;
    private Activity a;
    private String msg;
    private String rollbackErrorMsg;

    public SyncTask(Activity a, IRefreshTaskMethods refreshTaskMethods, IRollbackOnError rollbackOnError, String msg) {
        this(a, refreshTaskMethods, msg);
        this.rollbackOnError = rollbackOnError;
    }

    public SyncTask(Activity a, IRefreshTaskMethods refreshTaskMethods, String msg) {
        this.a = a;
        this.isProcessing = false;
        this.refreshTaskMethods = refreshTaskMethods;
        this.msg = msg;
        Log.d(TAG, "SyncTask: ");
        UiUtils.setTextView(a, R.id.loading_feedback_text, msg);
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
                    UiUtils.showToast(a, rollbackErrorMsg);
                    break;

                case BusinessRules.UNABLE_TO_SYNC:
                    if (rollbackOnError != null)
                        rollbackOnError.rollbackOnError();

                    UiUtils.showToast(a, msg + " failed.");
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
        UiUtils.setTextView(a, R.id.loading_feedback_text, msg);
    }

    private void setProcessingState(boolean isProcessing) {
        setProcessingState(isProcessing, true);
    }

    private void setProcessingState(boolean isProcessing, boolean clearCredentials) {
        this.isProcessing = isProcessing;

        try {
            EditText searchBox = a.findViewById(R.id.et_search);
            searchBox.setEnabled(!isProcessing);
        } catch (Throwable throwable) {
            /*if (throwable != null)
                throwable.printStackTrace();*/
        }

        if (isProcessing) {
            try {
                a.findViewById(R.id.loading_panel).setVisibility(View.VISIBLE);
            } catch (Throwable throwable) {
                /*if (throwable != null)
                    throwable.printStackTrace();*/
            }

            try {
                a.findViewById(R.id.search_bar_panel).setEnabled(false);
                a.findViewById(R.id.search_bar_loading_panel).setVisibility(View.VISIBLE);
            } catch (Throwable throwable) {
                /*if (throwable != null)
                    throwable.printStackTrace();*/
            }
        } else {
            try {
                a.findViewById(R.id.loading_panel).setVisibility(View.GONE);
            } catch (Throwable throwable) {
                /*if (throwable != null)
                    throwable.printStackTrace();*/
            }

            try {
                a.findViewById(R.id.search_bar_panel).setEnabled(true);
                a.findViewById(R.id.search_bar_loading_panel).setVisibility(View.GONE);
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
