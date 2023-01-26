package com.rco.rcotrucks.utils;

import java.util.ArrayList;

public class ArrayUtils {
    public static String[] toStringArray(ArrayList<String> values) {
        if (values == null)
            return null;

        String[] result = new String[values.size()];

        for (int i=0; i<values.size(); i++)
            result[i] = values.get(i);

        return result;
    }

    public static ArrayList<String> toArray(String... values) {
        if (values == null)
            return null;

        ArrayList<String> result = new ArrayList<>();

        for (String v: values)
            result.add(v);

        return result;
    }
}
