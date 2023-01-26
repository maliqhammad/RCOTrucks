package com.rco.rcotrucks.activities;

import android.app.Dialog;

import com.rco.rcotrucks.utils.UiUtils;

public class DialogManager extends UiUtils {
    public static Dialog d = null;

    public static boolean existsOpenDialog() {
        return d != null && d.isShowing();
    }
}
