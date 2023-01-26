package com.rco.rcotrucks.businesslogic;

import java.util.HashMap;
import android.util.Pair;

public class WindRose {
    private HashMap<Pair<Double, Double>, String> ranges;
    private Double windRoseIncrement = 11.25;
    private Double angle = 0.0;

    public WindRose() {
        ranges = new HashMap<>();

        ranges.put(new Pair(360-windRoseIncrement, angle+windRoseIncrement), "N"); angle += (windRoseIncrement * 2);
        ranges.put(new Pair(angle-windRoseIncrement, angle+windRoseIncrement), "NNE"); angle += (windRoseIncrement * 2);
        ranges.put(new Pair(angle-windRoseIncrement, angle+windRoseIncrement), "NE"); angle += (windRoseIncrement * 2);
        ranges.put(new Pair(angle-windRoseIncrement, angle+windRoseIncrement), "ENE"); angle += (windRoseIncrement * 2);
        ranges.put(new Pair(angle-windRoseIncrement, angle+windRoseIncrement), "E"); angle += (windRoseIncrement * 2);
        ranges.put(new Pair(angle-windRoseIncrement, angle+windRoseIncrement), "ESE"); angle += (windRoseIncrement * 2);
        ranges.put(new Pair(angle-windRoseIncrement, angle+windRoseIncrement), "SE"); angle += (windRoseIncrement * 2);
        ranges.put(new Pair(angle-windRoseIncrement, angle+windRoseIncrement), "SSE"); angle += (windRoseIncrement * 2);
        ranges.put(new Pair(angle-windRoseIncrement, angle+windRoseIncrement), "S"); angle += (windRoseIncrement * 2);
        ranges.put(new Pair(angle-windRoseIncrement, angle+windRoseIncrement), "SSW"); angle += (windRoseIncrement * 2);
        ranges.put(new Pair(angle-windRoseIncrement, angle+windRoseIncrement), "SW"); angle += (windRoseIncrement * 2);
        ranges.put(new Pair(angle-windRoseIncrement, angle+windRoseIncrement), "WSW"); angle += (windRoseIncrement * 2);
        ranges.put(new Pair(angle-windRoseIncrement, angle+windRoseIncrement), "W"); angle += (windRoseIncrement * 2);
        ranges.put(new Pair(angle-windRoseIncrement, angle+windRoseIncrement), "WNW"); angle += (windRoseIncrement * 2);
        ranges.put(new Pair(angle-windRoseIncrement, angle+windRoseIncrement), "NW"); angle += (windRoseIncrement * 2);
        ranges.put(new Pair(angle-windRoseIncrement, angle+windRoseIncrement), "NNW"); angle += (windRoseIncrement * 2);
    }

    public String getWindRoseRepresentation(String v) {
        return getWindRoseRepresentation(Double.valueOf(v));
    }

    public String getWindRoseRepresentation(Double v) {
        if (v < windRoseIncrement)
            return "N";

        Object[] keys = ranges.keySet().toArray();

        for (int i=0; i<ranges.size(); i++) {
            Pair<Double, Double> key = (Pair<Double, Double>) keys[i];
            Double start = key.first;
            Double end = key.second;

            if (v >= start && v < end)
                return (String) ranges.values().toArray()[i];
        }

        return v.toString();
    }
}
