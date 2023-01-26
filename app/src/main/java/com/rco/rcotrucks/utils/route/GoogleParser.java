package com.rco.rcotrucks.utils.route;

import android.text.TextUtils;
import android.util.Log;

import androidx.core.text.HtmlCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.rco.rcotrucks.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GoogleParser extends XMLParser implements Parser {

    private static final String TAG = GoogleParser.class.getSimpleName();
    private static final String VALUE = "value";
    private static final String TEXT = "text";
    private static final String DISTANCE = "distance";
    private static final String DURATION = "duration";

    private int distance;


    private static final String OK = "OK";

    public GoogleParser(String feedUrl) {
        super(feedUrl);
    }

    /**
     * Parses a url pointing to a Google JSON object to a Route object.
     *
     * @return a Route object based on the JSON object by Haseem Saheed
     */

    public final List<Route> parse() throws RouteException {
        List<Route> routes = new ArrayList<>();

        // Turn the stream into a string
        final String result = convertStreamToString(this.getInputStream());

        if (result == null) {
            throw new RouteException("Result is null");
        }

        try {
            // Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            // Get the route object

            if (!json.getString("status").equals(OK)) {
                throw new RouteException(json);
            }

            JSONArray jsonRoutes = json.getJSONArray("routes");

            // 2022.08.10 we need to get the end address
            String endAddress = "";

            for (int i = 0; i < jsonRoutes.length(); i++) {
                Route route = new Route();

                Segment segment = new Segment();

                JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
                //Get the bounds - northeast and southwest
                final JSONObject jsonBounds = jsonRoute.getJSONObject("bounds");
                final JSONObject jsonNortheast = jsonBounds.getJSONObject("northeast");
                final JSONObject jsonSouthwest = jsonBounds.getJSONObject("southwest");

                route.setLatLgnBounds(new LatLng(jsonNortheast.getDouble("lat"), jsonNortheast.getDouble("lng")), new LatLng(jsonSouthwest.getDouble("lat"), jsonSouthwest.getDouble("lng")));

                // Get the leg, only one leg as we don't support waypoints
                final JSONObject leg = jsonRoute.getJSONArray("legs").getJSONObject(0);
                // 2022.08.10 get the route name to use it for the final destination
                String summary = jsonRoute.getString("summary");
                // Get the steps for this leg
                final JSONArray steps = leg.getJSONArray("steps");

                final int numSteps = steps.length();

                route.setName(leg.getString("start_address") + " to " + leg.getString("end_address"));

                route.setCopyright(jsonRoute.getString("copyrights"));

                route.setDurationText(leg.getJSONObject("duration").getString("text"));
                route.setDurationValue(leg.getJSONObject("duration").getInt(VALUE));

//                Aug 29, 2022  -   Only get the duration_in_traffic value when response has it
                if (leg.has("duration_in_traffic")) {

                    Log.d(TAG, "parse: duration_in_traffic: " + leg.getJSONObject("duration_in_traffic"));
                    route.setDurationWithTrafficValue(leg.getJSONObject("duration_in_traffic").getInt(VALUE));
                    route.setDurationWithTrafficText(leg.getJSONObject("duration_in_traffic").getString("text"));
                }
                route.setDistanceText(leg.getJSONObject(DISTANCE).getString("text"));
                route.setDistanceValue(leg.getJSONObject(DISTANCE).getInt(VALUE));
                route.setEndAddressText(leg.getString("end_address"));

                endAddress = leg.getString("end_address");

                route.setLength(leg.getJSONObject(DISTANCE).getInt(VALUE));

                if (!jsonRoute.getJSONArray("warnings").isNull(0)) {
                    route.setWarning(jsonRoute.getJSONArray("warnings").getString(0));
                }


                for (int y = 0; y < numSteps; y++) {

                    final JSONObject step = steps.getJSONObject(y);

                    final JSONObject start = step.getJSONObject("start_location");
                    final LatLng position = new LatLng(start.getDouble("lat"),
                            start.getDouble("lng"));
                    segment.setPoint(position);

                    final JSONObject end = step.getJSONObject("end_location");
                    final LatLng endPosition = new LatLng(end.getDouble("lat"),
                            end.getDouble("lng"));
                    segment.setEndPoint(endPosition);


                    final int length = step.getJSONObject(DISTANCE).getInt(VALUE);
                    Log.d("DirectionView", "parse: length: " + length);
                    distance += length;
                    segment.setLength(length);
                    segment.setDistance((double) distance / (double) 1000);
                    Log.d("DirectionView", "parse: distance: " + segment.getDistance());
                    segment.setDistanceText(step.getJSONObject(DISTANCE).getString(TEXT));
                    segment.setCurrentRemainingDistance((double) length);
                    segment.setDuration(step.getJSONObject(DURATION).getString(TEXT));
                    segment.setDurationValue(step.getJSONObject(DURATION).getInt(VALUE));

                    //Strip html from google directions and set as turn instruction
                    Log.d(TAG, "parse: without filtering: " + step.getString("html_instructions"));
//                    String instructions = step.getString("html_instructions").replaceAll("<(.*?)*>", "");
                    String instructions = html2text(step.getString("html_instructions"));

                    Log.d(TAG, "parse: instructions: after removing html tags: " + instructions);
                    instructions = instructions.replaceAll("Destination", " Destination");

                    String customManeuver = null, shortInstruction = null;
                    boolean isRoundabout = false, isCircle = false;
                    int exitNumber = 0;
//                    Aug 04, 2022  -   Fixed the problem regarding the round about maneuvers
                    Log.d(TAG, "setInstructionView: parse: instructions: " + instructions);

                    if (instructions.contains("At the roundabout")) {
                        isRoundabout = true;
                    } else if (instructions.contains("At the traffic circle") || instructions.contains("Exit the traffic circle")) {
                        isCircle = true;
                    } else {
//                        Aug 04 2022   -   like At Windward Cir, take the 2nd exit onto Windward Ave
                        if (instructions.startsWith("At ") && instructions.toLowerCase().contains(" cir,")) {
                            isCircle = true;
                        }
                    }
                    Log.d(TAG, "parse: isRoundabout: " + isRoundabout + " isCircle: " + isCircle);

                    if (isRoundabout || isCircle) {
                        String[] instructionsSplit = instructions.split("take the ");

                        if (instructionsSplit != null && instructionsSplit.length > 1) {
                            if (instructionsSplit[1].length() > 1) {
                                String secondPart = instructionsSplit[1];
//                                String[] splitSecondPart = secondPart.split("rd ");
                                String[] splitSecondPart = secondPart.split(" exit");
                                if (splitSecondPart != null && splitSecondPart.length > 0) {
                                    if (splitSecondPart[0] != null && splitSecondPart[0].length() > 0) {
                                        String exitNumberValue = splitSecondPart[0];
//                                        Aug 04, 2022  -   This will remove all the alphabets from string
                                        exitNumberValue = exitNumberValue.replaceAll("[^\\d.]", "");
                                        Log.d(TAG, "parse: exitNumberValue: " + exitNumberValue);
                                        exitNumber = Integer.parseInt(exitNumberValue);
                                        if (exitNumber == 1) {
                                            customManeuver = "direction_rotary_right";
                                        } else if (exitNumber == 2) {
                                            if (isCircle) {
                                                customManeuver = "direction_rotary_slight_right";
                                            } else {
                                                customManeuver = "direction_rotary_straight";
                                            }
                                        } else if (exitNumber == 3) {
                                            if (isCircle) {
                                                customManeuver = "direction_rotary_slight_left";
                                            } else {
                                                customManeuver = "direction_rotary_left";
                                            }
                                        } else if (exitNumber == 4) {
                                            customManeuver = "direction_roundabout_left";
                                        } else if (exitNumber >= 5) {
                                            customManeuver = "direction_on_ramp_sharp_left";
                                        }
                                    }
                                }
                            }
                        } else {
                            if (instructions.contains("continue straight")) {
                                customManeuver = "direction_rotary_straight";
                            }
                        }


                        instructionsSplit = instructions.split(", ");
                        if (instructionsSplit != null && instructionsSplit.length > 1) {
                            if (instructionsSplit[1].length() > 1) {
                                shortInstruction = instructionsSplit[1];
                            }
                        }

                    }
                    if (shortInstruction == null) {
                        shortInstruction = instructions;
                    }

                    boolean found = false;

                    String[] shortInstructionsSplit = shortInstruction.split(" onto ");
                    if (shortInstructionsSplit != null && shortInstructionsSplit.length > 1) {
                        if (shortInstructionsSplit[1].length() > 1) {
                            found = true;
                            shortInstruction = shortInstructionsSplit[1];
                        }
                    }

                    if (!found) {
                        shortInstructionsSplit = shortInstruction.split(" towards ");
                        if (shortInstructionsSplit != null && shortInstructionsSplit.length > 1) {
                            if (shortInstructionsSplit[1].length() > 1) {
                                found = true;
                                shortInstruction = shortInstructionsSplit[1];
                            }
                        }
                    }

                    if (!found) {
                        shortInstructionsSplit = shortInstruction.split("-turn at ");
                        if (shortInstructionsSplit != null && shortInstructionsSplit.length > 1) {
                            if (shortInstructionsSplit[1].length() > 1) {
                                found = true;
                                shortInstruction = shortInstructionsSplit[1];
                            }
                        }
                    }

                    if (!found) {
                        shortInstructionsSplit = shortInstruction.split(" on to ");
                        if (shortInstructionsSplit != null && shortInstructionsSplit.length > 1) {
                            if (shortInstructionsSplit[1].length() > 1) {
                                found = true;
                                shortInstruction = shortInstructionsSplit[1];
                            }
                        }
                    }

                    if (!found) {
                        shortInstructionsSplit = shortInstruction.split(" and stay ");
                        if (shortInstructionsSplit != null && shortInstructionsSplit.length > 1) {
                            if (shortInstructionsSplit[1].length() > 1) {
                                found = true;
                                shortInstruction = shortInstructionsSplit[1];
                            }
                        }
                    }

                    if (!found) {
                        shortInstructionsSplit = shortInstruction.split(" to stay on ");
                        if (shortInstructionsSplit != null && shortInstructionsSplit.length > 1) {
                            if (shortInstructionsSplit[1].length() > 1) {
                                found = true;
                                shortInstruction = shortInstructionsSplit[1];
                            }
                        }
                    }

                    Log.d(TAG, "parse: shortInstruction: " + shortInstruction);
                    Log.d(TAG, "parse: instruction: " + instructions);

                    if (shortInstruction.equalsIgnoreCase(instructions) && (y == (numSteps - 1))) {
                        // 2022.08.10 is the last instruction and we don't have the address
                        Log.d(TAG, "parse: sameInstruction: " + instructions);
                        if (endAddress.length() > 0) {
                            shortInstruction = endAddress;
                            //2022.08.10 getting the street from the destination ( the problem here is that the street name contains also the number)
                            String adr = getStreetFormAddress(endAddress);
                            shortInstruction = adr;
                            Log.d(TAG, "parse: endAddress 1: " + adr);
                            /*

                            2022.08.19 we should use the street name from the end address hopfely will work all the time

                            // 2022.08.10 getting the street from route summary ("summary":"26th St and Georgina Ave",)
                            String endStreetFromSummary = getEndStreetFormSummary(summary);
                            if (endStreetFromSummary != null && endStreetFromSummary.length() > 0) {
                                shortInstruction = endStreetFromSummary;
                                Log.d(TAG, "parse: endAddress 2: " + endStreetFromSummary);
                            }
                            */
                        }
                    }

                    if (shortInstruction != null) {
//                        Aug 08, 2022  -   We are already getting correct case from google api so dont convert this one
                        segment.setInstructionShort(StringUtils.capitalize(shortInstruction));
//                        segment.setInstructionShort(shortInstruction);
                    }
//                    parse: instructions:
//                    Log.d(TAG, "parse: customManeuver: " + customManeuver);

//                    July 19, 2022 -   Work around to fix the pass problem
                    if (instructions.contains("Pass")) {
                        String[] instructionsSplit = instructions.split("Pass");

                        if (instructionsSplit == null) {
                            segment.setInstruction(instructions);
                        } else {
                            if (instructionsSplit[0].length() > 0) {
                                String firstPart = instructionsSplit[0].trim();
                                segment.setInstruction(firstPart);
                            } else {
                                segment.setInstruction(instructions);
                            }
                        }
                    } else {
                        segment.setInstruction(instructions);
                    }
                    Log.d(TAG, "parse: instructions: setter one: segment.getInstruction: " + segment.getInstruction());
                    Log.d(TAG, "parse: instructions: setter one: segment.getInstructionShort: " + segment.getInstructionShort());

//                    Aug 05, 2022  -   We should save if its a round about or not
                    Log.d(TAG, "setInstructionView: parse: isRoundabout: " + isRoundabout + " isCircle: " + isCircle);
                    if (isRoundabout || isCircle) {
                        segment.setIsRoundAbout("true");
                    } else {
                        segment.setIsRoundAbout("false");
                    }
                    segment.setExitNumber(exitNumber);

                    if (customManeuver != null) {
                        Log.d(TAG, "parse: maneuver: customManeuver != null: " + customManeuver);
//                        segment.setManeuver(customManeuver, "Custom Maneuver");
                        segment.setManeuver("", "Custom Maneuver");
                    } else if (step.has("maneuver")) {
//                    Aug 02, 2022  -   Added else here
                        //Log.d(TAG, "parse: maneuver: has maneuver: " + step.getString("maneuver"));
                        Log.d(TAG, "parse: maneuver: has maneuver: " + step.getString("maneuver") + "  -> instructions: " + instructions);
                        segment.setManeuver(step.getString("maneuver"), "GoogleParser 1");
                    } else {
                        Log.d(TAG, "parse: maneuver: has not maneuver: instructions: " + instructions);
                        segment.setManeuver("", "GoogleParser 2");
                    }


                    segment.setSegmentPoints(PolyUtil.decode(step.getJSONObject("polyline").getString("points")));

                    route.addPoints(PolyUtil.decode(step.getJSONObject("polyline").getString("points")));

                    route.addSegment(segment.copy());
                }

                routes.add(route);
            }

        } catch (JSONException e) {
            throw new RouteException("JSONException. Msg: " + e.getMessage());
        }
        return routes;
    }

    public String getStreetFormAddress(String address) {
        // get the street name from address: "2170 Georgina Ave, Santa Monica, CA 90402, USA"
        String[] addressSplit = address.split(", ");
        if (addressSplit != null && addressSplit.length > 1) {
            String firstPart = addressSplit[0];

            if (firstPart != null && firstPart.length() > 0) {
                String[] addressSplit2 = firstPart.split(" ");

                // 2022.08.19 we nee to get the street name from: "2170 Georgina Ave"
                if (addressSplit2 != null && addressSplit2.length > 1) {
                    // the first value should be the number
                    String number = addressSplit2[0];
                    if (TextUtils.isDigitsOnly(number)) {
                        // we need to check if we got the number
                        Log.d(TAG, "TextUtils.isDigitsOnly: " + number + " addressSplit2:" + addressSplit2);
                        // we need to remove the number from the address
                        firstPart = firstPart.replaceAll(number + "", "");
                    }
                }
                return firstPart;
            } else {
                return firstPart;
            }
        } else {
            return null;
        }
    }

    public String getEndStreetFormSummary(String summary) {
        // get the end street from: "26th St and Georgina Ave"
        String[] addressSplit = summary.split(" and ");
        Log.d(TAG, "addressSplit: " + summary + " " + addressSplit);
        if (addressSplit != null && addressSplit.length > 1) {
            String firstPart = addressSplit[1];
            Log.d(TAG, "firstPart: " + firstPart + " ");

            return firstPart;
        } else {
            return null;
        }
    }

    /**
     * Convert an inputstream to a string.
     *
     * @param input inputstream to convert.
     * @return a String of the inputstream.
     */
    private static String convertStreamToString(final InputStream input) {
        if (input == null) return null;

        final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        final StringBuilder sBuf = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sBuf.append(line);
            }
        } catch (IOException e) {
            Log.e("Routing Error", e.getMessage());
        } finally {
            try {
                input.close();
                reader.close();
            } catch (IOException e) {
                Log.e("Routing Error", e.getMessage());
            }
        }
        return sBuf.toString();
    }

    public static String html2text(String htmlInstruction) {
//        return Jsoup.parse(html).text();

//        If you're writing for Android you can do this...

        return androidx.core.text.HtmlCompat.fromHtml(htmlInstruction, HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
    }
}
