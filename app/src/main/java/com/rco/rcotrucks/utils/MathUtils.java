package com.rco.rcotrucks.utils;

import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class MathUtils {
    public static final String TAG = "MathUtils";

    public static int round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.intValue();
    }
    public static float roundFloat(float value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    public static double getDoubleValue(String text) {
        try {
            if (!StringUtils.isNullOrWhitespaces(text))
                return Double.parseDouble(text);
            else return 0;
        } catch (Exception e) {
            Log.d(TAG, "getDoubleValue() ***** Error.  text=" + text + ", " + e);
        }
        return 0;
    }

    public static float getFloatValue(String text) {
        try {
            return Float.parseFloat(text);
        } catch (Exception e) {
        }
        return 0;
    }

    public static String roundTo2DecimalCases(String value) {
        try {
            DecimalFormat df = new DecimalFormat("#0.00");
            return df.format(Double.parseDouble(value));
        } catch (Exception e) {
            return value;
        }

    }

    public static String roundTo1DecimalCases(String value) {
        try {
            DecimalFormat df = new DecimalFormat("#0.0");
            return df.format(Double.parseDouble(value));
        } catch (Exception e) {
            return value;
        }

    }

    public static Integer sumDigits(String value) {
        Log.d(TAG, "generateEldTransferFileName: sumDigits: value: "+value);
        if (value == null)
            return null;

        Integer sum = 0;

        for (int i=0; i<value.length(); i++) {
            String ch = value.substring(i, i+1);
            Integer chInt = Integer.parseInt(ch);

            sum += chInt;
        }

        return sum;
    }
}
