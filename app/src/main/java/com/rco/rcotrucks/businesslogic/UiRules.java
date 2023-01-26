package com.rco.rcotrucks.businesslogic;

import android.app.Activity;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.UiUtils;

public class UiRules {
    public static void showExclamationDataLoadErrorDialog(Activity a, String title) {
        showExclamationDataLoadErrorDialog(a, title, null);
    }

    public static void showExclamationDataLoadErrorDialog(Activity a, String title, Throwable throwable) {
        if (throwable != null)
            throwable.printStackTrace();

        UiUtils.showExclamationDialog(a, title, a.getString(R.string.error_data_load));
    }
}
