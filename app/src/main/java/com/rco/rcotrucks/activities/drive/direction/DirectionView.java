package com.rco.rcotrucks.activities.drive.direction;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;

import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.ImageUtils;
import com.rco.rcotrucks.utils.TTS;
import com.rco.rcotrucks.utils.UiUtils;
import com.rco.rcotrucks.utils.Utils;
import com.rco.rcotrucks.utils.route.Route;
import com.rco.rcotrucks.utils.route.Segment;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DirectionView extends LinearLayout {

    private static final String TAG = DirectionView.class.getSimpleName();
    private static final int VOICE_INSTRUCTION_LONG_DISTANCE = 122 /*instruction speech before 122 meters = 400 feet*/;
    private static final int VOICE_INSTRUCTION_SHORT_DISTANCE = 45 /*instruction speech before 122 meters = 150 feet*/;
    private TextView tvInstruction, tvDistance;
    private ImageView ivInstruction;
    Guideline guideline;

    private TextView hours, miles, exit_btn;
    private ImageView direction_btn;
    ConstraintLayout directionViewBottomBarCL;

    private TTS tts;
    private List<Segment> segments;
    private String destination;
    Segment closestSteps = null;
    Segment nextClosestSteps = null;
    String previousInstruction = "", previousQuarterInstruction = "";
    boolean muteVoice = false, isTablet = false;
    int remainingDistanceToNextStep = 0;
    Map<Integer, Integer> segmentsDurationMap;

    private OnHandleBottomView onHandleBottomView;

    LinkedHashMap<String, String> streets = new LinkedHashMap<String, String>();

//    public DirectionView(Context context) {
//        super(context);
////        Dec 26, 2022  -
////        initView(context, attrs);
////        tts = new TTS(context, Locale.US);
////        initView(context);
//    }

    public DirectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

//        Dec 26, 2022  -   I think so this is somehow having problem while rendering tablet and phone drive view
        tts = new TTS(context, Locale.US);
        initView(context, attrs);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initialize();
    }

    private void initView(Context context, AttributeSet attrs) {
//    private void initView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View container = inflater.inflate(R.layout.layout_direction, this, true);

        setIds(container);
        initialize();
        getRootView().setVisibility(GONE);
        fillAbbreviation();

        setListener();
    }

    void setIds(View container) {
        tvInstruction = container.findViewById(R.id.tv_instruction);
        tvDistance = container.findViewById(R.id.tv_distance);
        ivInstruction = container.findViewById(R.id.iv_instruction);
        guideline = container.findViewById(R.id.guideline);

        hours = container.findViewById(R.id.hours);
        miles = container.findViewById(R.id.miles);
        exit_btn = container.findViewById(R.id.exit_btn);
        direction_btn = container.findViewById(R.id.direction_btn);
        directionViewBottomBarCL = container.findViewById(R.id.directionViewBottomBarCL);
    }

    void initialize() {

        Log.d(TAG, "initialize: ");
        isTablet = getResources().getBoolean(R.bool.isTablet);
        int orientation = getResources().getConfiguration().orientation;
        if (isTablet) {
            if (guideline == null) {
                return;
            }

            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Log.d(TAG, "initialize: ORIENTATION_LANDSCAPE: set to 40");
                guideline.setGuidelinePercent(0.50F);
            } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                Log.d(TAG, "initialize: ORIENTATION_PORTRAIT: set to 65");
                guideline.setGuidelinePercent(0.65F);
            }
        }
    }

    void setListener() {

        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
//                    July 25, 2022 -   Roy told to remove confirmation dialog in mail July 24, 2022
//                    UiUtils.showOkCancelDialog(getContext(), "Warning", android.R.drawable.ic_dialog_alert,
//                            "Would you like to exit this Route?", false, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "onClick: onHandleBottomView: " + onHandleBottomView);

//                    July 25, 2022 -
                    resetInstructionViews();
                    if (onHandleBottomView != null) {
                        onHandleBottomView.iDismiss();
                    }
//                                }
//                            });


                } catch (Throwable throwable) {

                    if (onHandleBottomView != null)
                        onHandleBottomView.iDismiss();
                }
            }
        });

        direction_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onHandleBottomView != null) {
                    onHandleBottomView.iDirection();
                }
            }
        });

//        directionViewBottomBarCL.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//            @Override
//            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                Log.d(TAG, "onLayoutChange: left: " + left + " top: " + top + " right: " + right);
//                int height = bottom - top;
//                int width = right - left;
//                Log.i(TAG, "onLayoutChange: height is" + height);
//                Log.i(TAG, "onLayoutChange: width is " + width);
//            }
//        });

    }

    public void setupBottomInstruction(Route displayedRoute, LatLng phoneLastBestLatLng,
                                       double timeStampWhenEnteringOnASegment, double timeStampWhenEnteringOnARoute,
                                       OnHandleBottomView onHandleBottomView) {
        this.onHandleBottomView = onHandleBottomView;
        Log.d(TAG, "setupBottomInstruction: remainingCurrentSegmentDistance: " + remainingCurrentSegmentDistance);
        Log.d(TAG, "setupBottomInstruction: timeStampWhenEnteringOnARoute: " + timeStampWhenEnteringOnARoute);

        if (displayedRoute != null) {
            int stepsCount = displayedRoute.getSegments().size();
            int firstStep = displayedRoute.getCurrentStep();
//            Log.d(TAG, "setupBottomInstruction: stepsCount: " + stepsCount + " firstStep: " + firstStep);
//            Log.d(TAG, "setupBottomInstruction: getSegmentsDurationMap: segmentsDurationMap: " + getSegmentsDurationMap());
            if (getSegmentsDurationMap() != null) {
                Log.d(TAG, "setupBottomInstruction: getSegmentsDurationMap: size: " + getSegmentsDurationMap().size());
            }

            int restDistance = 0;
            int restDuration = 0;
            for (int i = firstStep; i < stepsCount; i++) {
                Segment step = displayedRoute.getSegments().get(i);
//                Log.d(TAG, "setupBottomInstruction: step: duration: " + step.getDuration());
                int stepDurationWithTraffic = -1;
//                if (getSegmentsDurationMap() != null) {
//                July 27, 2022 -   check that the index is less than total size of segmentsDurationMap content list
                if (getSegmentsDurationMap() != null && (i < getSegmentsDurationMap().size())) {
                    stepDurationWithTraffic = getSegmentsDurationMap().get(i);
                }

//                Log.d(TAG, "setupBottomInstruction: step: stepDuration: " + stepDurationWithTraffic);
//                Log.d(TAG, "setupBottomInstruction: stepDuration: " + stepDurationWithTraffic);
                if (i == firstStep) {
                    double middleDistance;
                    if (phoneLastBestLatLng == null) {
                        middleDistance = SphericalUtil.computeDistanceBetween(displayedRoute.getPoints().get(0), step.endPoint());
                    } else {
                        middleDistance = SphericalUtil.computeDistanceBetween(phoneLastBestLatLng, step.endPoint());
                    }

//                    double distanceRatio = middleDistance / step.getLength();
//                    July 26, 2022 -
                    double distanceRatioNew = remainingCurrentSegmentDistance / step.getLength();


//                        July 12, 2022 -   We changed to use the remaining distance that we are showing from top instructions bar AND at the start we should set
//                        the remaining distance to the total distance of the step
//                        restDistance += middleDistance;
                    if (remainingCurrentSegmentDistance == 0 && i == 0) {
                        Log.d(TAG, "setupBottomInstruction: remainingCurrentSegmentDistance == 0 && i==0: ");
                        restDistance = step.getLength();
//                            July 25, 2022 -   We need to get the duration with traffic from the hashmap
                        if (stepDurationWithTraffic > 0) {
                            restDuration += stepDurationWithTraffic;
                        } else {
                            restDuration += step.getDurationValue();
                        }
//                        Log.d(TAG, "setupBottomInstruction: restDuration: " + getSegmentsDurationMap());
                    } else {
//                        Log.d(TAG, "setupBottomInstruction: else: " + getSegmentsDurationMap());
                        restDistance += remainingCurrentSegmentDistance;
                        double delta = 0.0;
                        if (timeStampWhenEnteringOnASegment > 0) {
                            delta = DateUtils.getTimestampInDouble() - timeStampWhenEnteringOnASegment;
                        } else {
//                            July 25, 2022 -
                            if (stepDurationWithTraffic > 0) {
                                delta = stepDurationWithTraffic;
                            } else {
                                delta = step.getDurationValue();
                            }
                        }
//                        Log.d(TAG, "setupBottomInstruction: timeStampWhenEnteringOnASegment: " + timeStampWhenEnteringOnASegment + " delta: " + delta);

//                            July 25, 2022 -   We need to get the duration with traffic from the hashmap
                        double remainingTime, timeRatio, stepDuration;
//                        Log.d(TAG, "setupBottomInstruction: remainingTimeEstimated: " + remainingTimeEstimated);
//                        July 26, 2022 -


                        if (stepDurationWithTraffic > 0) {
                            stepDuration = stepDurationWithTraffic;
                            remainingTime = stepDurationWithTraffic - delta;
                        } else {
                            stepDuration = step.getDurationValue();
                            remainingTime = step.getDurationValue() - delta;
                        }
                        timeRatio = remainingTime / stepDuration;

//                        Log.d(TAG, "setupBottomInstruction: delta: timeRatio: "+timeRatio+" distanceRatioNew: "+distanceRatioNew+" mul: "+(distanceRatioNew * 0.9));

//                        July 26, 2022 -   For making it more accurate, we should increase from 95% to 97% but that will generate more calls to Google API
                        if ((timeRatio < (distanceRatioNew * 0.95))) {
//                            Log.d(TAG, "setupBottomInstruction: delta: if ((timeRatio > (distanceRatioNew * 0.95)))");

//                                July 12, 2022 -   TODO This is the case when we are in traffic and the duration takes longer than it should
//                                We will add a work to set it to zero to ignore the step duration
//                                Should we make another call to google to get the duration updates?
                            restDuration += step.getDurationValue() * distanceRatioNew;
                            if (onHandleBottomView != null) {
//                                Log.d(TAG, "setupBottomInstruction: call: reloadCurrentSegment: ");
                                onHandleBottomView.reloadCurrentSegment();
                            }

                        } else {
                            restDuration += remainingTime;
                        }

                        Log.d(TAG, "setupBottomInstruction: delta : " + delta + " restDuration: " + restDuration);
                    }


//                        July 12, 2022
//                        Todo We need a solution for the case when we are stuck in the traffic
                    Log.d(TAG, "setupBottomInstruction: i == firstStep: restDistance: " + restDistance + " restDuration: " + restDuration);
                } else {
                    restDistance += step.getLength();
                    if (stepDurationWithTraffic > 0) {
                        restDuration += stepDurationWithTraffic;
                    } else {
                        restDuration += step.getDurationValue();
                    }
                    Log.d(TAG, "setupBottomInstruction: else: i == firstStep: restDistance: " + restDistance + " restDuration: " + restDuration);
                }

            }
            Log.d(TAG, "setupBottomInstruction: restDistance: " + restDistance + " restDuration: " + restDuration);

//                July 15, 2022 -   Adding duration in traffic also in parenthesis() for comparison
            double deltaWithTrafficInSeconds = 0;
            if (timeStampWhenEnteringOnARoute > 0) {
                deltaWithTrafficInSeconds = DateUtils.getTimestampInDouble() - timeStampWhenEnteringOnARoute;
            }

            int restDurationWithTrafficInSeconds = 0;
            int restDurationWithTrafficInMinutes = (int) ((displayedRoute.getDurationWithTrafficValue() - deltaWithTrafficInSeconds) / 60);
            restDurationWithTrafficInSeconds = (int) (displayedRoute.getDurationWithTrafficValue() - deltaWithTrafficInSeconds);
            Log.d(TAG, "setupBottomInstruction: restDurationWithTrafficInMinutes: " + restDurationWithTrafficInMinutes);

//                July 15, 2022 -   TODO In the case when minutes==0, may be show the seconds or should we force it to 1 mint?
            int restDurationInMinutes = (int) restDuration / 60;

            Log.d(TAG, "setupBottomInstruction: restDurationInMinutes: " + restDurationInMinutes);

//                July 20, 2022 -   For now, We will use the duration only with traffic
            boolean useTrafficDuration = false;
//            Oct 25, 2022  -   Hiding as Roy recommended (It was initially meant for testing)
            boolean showSeconds = false;

            if (useTrafficDuration) {
                if (restDurationWithTrafficInMinutes > 0) {
                    if (showSeconds) {
                        hours.setText(restDurationWithTrafficInMinutes + " min" + " (" + restDurationWithTrafficInSeconds + " sec)");
                    } else {
                        hours.setText(restDurationWithTrafficInMinutes + " min");
                    }
                } else {
                    hours.setText(restDurationWithTrafficInSeconds + " sec");
                }
            } else {
                if (restDurationInMinutes > 0) {
//                    hours.setText(restDurationInMinutes + " min" + " (" + restDuration + " sec)");
                    hours.setText(restDurationInMinutes + " min");
                } else {
                    hours.setText((int) restDuration + " sec");
                }
            }


            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
//                cal.add(Calendar.SECOND, restDurationInMinutes);
            cal.add(Calendar.MINUTE, restDurationInMinutes);
//                String arrivalTime = DateUtils.getDateTime(cal.getTime(), DateUtils.FORMAT_DATE_HH_MM_A);
            String arrivalTime = DateUtils.getDateTime(cal.getTime(), DateUtils.FORMAT_DATE_hh_mm_a);

//            Log.d(TAG, "setupBottomInstruction: restDistance: and arrival Time: " + Utils.convertDistanceInMiles(restDistance) + " - " + arrivalTime);
            miles.setText(Utils.convertDistanceInMiles(restDistance) + " - " + arrivalTime);
        } else {
            Log.d(TAG, "setupBottomInstruction: else: setting hours as empty strings");
            hours.setText("");
            miles.setText("");
        }
    }

    public void setRoute(Route route, String destination) {
        this.destination = destination;
        segments = route.getSegments();
        Log.d(TAG, "setRoute: playInstructionForStep: segments: size: " + segments.size());
    }

    public void setupInstruction(LatLng lastLocation, int currentSegmentMaxSpeed) {
        if (segments != null) {

            Log.d(TAG, "setupInstruction: segments: size: " + segments.size() + " lastLocation: " + lastLocation);
            for (int i = 0; i < segments.size(); i++) {
                Segment segment = segments.get(i);

//            Log.d(TAG, "setupInstruction: each segment: " + segment.getSegmentPoints());
//            Log.d(TAG, "setupInstruction: each segment: https://api.mapbox.com/directions/v5/mapbox/driving/"
//                    + segment.startPoint().longitude + "," + segment.startPoint().latitude + ";" +
//                    +segment.endPoint().longitude + "," + segment.endPoint().latitude + "?annotations=maxspeed&overview=full&geometries=geojson&access_token=pk.eyJ1IjoiZHJhZ29zYmIiLCJhIjoiY2tiZzR4YXM1MGlrcjJxa2M4M2t4c3ZldCJ9.-orOgLEYWRv2Hlpy4YJ3xg");

                Log.d(TAG, "setupInstruction: segment: " + segment);
                if (segment != null) {

//        July 06, 2022 -   the different changed from 5 to 50 because when we are loaded instructions initially, at that time
//                the instructions are not loaded because in case of 5 meters the closest Steps will be null and eventually
//                we cannot show the instructions on instructions layout
//                Will this effect the rerouting when we need to detect the closed step or geofencing?


//        July 12, 2022 -   We change it back from 50 to 5, because we believe this tolerance has caused the rerouting
//        on 22 street not to update position and also show wrong instructions (Show the next instructions not the current insructions)
                    Log.d(TAG, "setupInstruction: segments: 0th index: " + segment.getSegmentPoints());
                    if (PolyUtil.isLocationOnPath(lastLocation, segment.getSegmentPoints(), true, 5)) {
//                    if (PolyUtil.isLocationOnPath(lastLocation, segment.getSegmentPoints(), true, 50)) {

                        closestSteps = segment;
//                        Log.d(TAG, "setupInstruction: each closestSteps: " + closestSteps);
                        if (i < segments.size() - 1) {
                            nextClosestSteps = segments.get(i + 1);
                        } else {
                            nextClosestSteps = closestSteps;
                        }
                    }
                }
            }

            setInstructionView(lastLocation, currentSegmentMaxSpeed);
        }
    }

    public int getRemainingDistanceToNextStep() {
        return remainingDistanceToNextStep;
    }

    public void handleDestinationReached() {
        tvInstruction.setText("You have arrived at " + destination);
        tvDistance.setText("");
        speakInstruction("You have arrived at your destination.");
    }

    double remainingCurrentSegmentDistance = 0.0;

    public void setInstructionView(LatLng lastLocation, int currentSegmentMaxSpeed) {
        Log.d(TAG, "setInstructionView: closestSteps: " + closestSteps + " nextClosestSteps: " + nextClosestSteps);

//        Aug 02, 2022  -   If we are away more than five meters from the road then we might not get the closest step but
//        we are forcefully assigning the first(zeroth) index.
        if (closestSteps == null) {
            if (segments != null && segments.size() > 0) {
                Log.d(TAG, "setInstructionView: segments: size: " + segments.size() + " instruction: " + segments.get(0).getInstruction());
                closestSteps = segments.get(0);
                if (segments.size() > 1) {
                    nextClosestSteps = segments.get(1);
                } else {
                    nextClosestSteps = closestSteps;
                }
            }
        }
        if (closestSteps != null) {

//            Log.d(TAG, "setupInstruction: setInstructionView: distanceText: " + closestSteps.getDistanceText() + " destination: " + destination + " segments: " + segments);
            remainingCurrentSegmentDistance = closestSteps.getCurrentRemainingDistance();

//            Log.d(TAG, "setInstructionView: remainingCurrentSegmentDistance: " + remainingCurrentSegmentDistance + " closestSteps.getDistanceText(): " + closestSteps.getDistanceText());
            tvDistance.setText(closestSteps.getDistanceText());

//            Aug 05, 2022  -   We comment it out
//            String manoeuvreImg = ImageUtils.FetchDirectionImage(closestSteps.getManeuver());
            String manoeuvreImg = null;
            String instruction = "";
            String shortInstruction = "";

            if (closestSteps == nextClosestSteps) {

                instruction = destination;
//                tvInstruction.setText();
                Log.d(TAG, "setInstructionView: instruction: if: instruction: " + instruction + " closestSteps: " + closestSteps.getInstruction());
                String maneuver = "";
                if (closestSteps.getInstruction().contains("Destination") && closestSteps.getInstruction().contains("on the left")) {
                    maneuver = "direction_arrive_left";
                } else if (closestSteps.getInstruction().contains("Destination") && closestSteps.getInstruction().contains("on the right")) {
                    maneuver = "direction_arrive_right";
                } else {
                    maneuver = "";
                }

                manoeuvreImg = ImageUtils.FetchDirectionImage(maneuver);
                Log.d(TAG, "setInstructionView: fetch: manoeuvreImg: " + manoeuvreImg);
            } else if (nextClosestSteps != null) {

                instruction = headingInstruction(nextClosestSteps.getInstruction());
                Log.d(TAG, "setInstructionView: instruction: else: " + instruction);


                shortInstruction = nextClosestSteps.getInstructionShort();
//                tvInstruction.setText(headingInstruction(nextClosestSteps.getInstruction()));
                manoeuvreImg = ImageUtils.FetchDirectionImage(nextClosestSteps.getManeuver());
                Log.d(TAG, "setInstructionView: fetch: nextClosestSteps!=null: " + nextClosestSteps.getManeuver() + " manoeuvreImg: " + manoeuvreImg);
            } else if (destination != null) {
//                July 22, 2022 -   We don't know why this condition is being used
//            } else if (segments.size() == 1 && destination != null) {
                Log.d(TAG, "setInstructionView: instruction: else if destination != null: destination: " + destination);
                instruction = destination;
//                tvInstruction.setText(destination);
            }

            Log.d(TAG, "setInstructionView: manoeuvreImg: " + manoeuvreImg);

//            Log.d(TAG, "setInstructionView: closestSteps.getIsRoundAbout(): " + closestSteps.getIsRoundAbout());
//            Log.d(TAG, "setInstructionView: closestSteps.getTurnAngle(): " + closestSteps.getTurnAngle());
//            Aug 05, 2022  -
//            if (nextClosestSteps != null && nextClosestSteps.getIsRoundAbout().equalsIgnoreCase("true")) {
//                manoeuvreImg = ImageUtils.FetchDirectionImageForRoundAbout((int) nextClosestSteps.getTurnAngle());
//            }
//            Log.d(TAG, "setInstructionView: manoeuvreImg: " + manoeuvreImg);


            if (instruction == null || instruction.isEmpty()) {
                instruction = "null or empty";
            }

//            Aug 04, 2022  -   We should use the short instruction if its available (Short is the refine instruction we did in Google parser)
            if (!shortInstruction.isEmpty()) {
//                Oct 10, 2022  -   Refine the instruction that we set on instructions text view
                refineInstructions(shortInstruction);
//                tvInstruction.setText(shortInstruction);
            } else {
                refineInstructions(instruction);
//                tvInstruction.setText(instruction);
            }

            Log.d(TAG, "setInstructionView: manoeuvreImg: before assigning to widget: " + manoeuvreImg);
            if (manoeuvreImg == null) {
//                ivInstruction.setImageDrawable(null);
            } else {
                int resImage = getResources().getIdentifier(manoeuvreImg, "drawable", getContext().getPackageName());
                ivInstruction.setImageResource(resImage);
            }

            String voiceInstruction;
            if (nextClosestSteps != null) {
//                Log.d(TAG, "setInstructionView: nextClosestSteps != null : closestSteps.getInstruction(): " + closestSteps.getInstruction());
//                Log.d(TAG, "setInstructionView: nextClosestSteps != null : closestSteps.getInstructionShort(): " + closestSteps.getInstructionShort());
//                Log.d(TAG, "setInstructionView: nextClosestSteps != null : nextClosestSteps.getInstruction(): " + nextClosestSteps.getInstruction());
//                Log.d(TAG, "setInstructionView: nextClosestSteps != null : nextClosestSteps.getInstructionShort(): " + nextClosestSteps.getInstructionShort());

                //2022.08.19 we should use the short instruction because it might have been formatted voiceInstruction = setupVoiceInstruction(closestSteps.getInstruction(), nextClosestSteps.getInstruction(), nextClosestSteps.getManeuver());

                if (nextClosestSteps.getManeuver().contains("_")) {
                    // 2022.08.24 we should use the nirmal instruction if we are close to a roundabout
                    voiceInstruction = nextClosestSteps.getInstruction();
                } else {
                    voiceInstruction = setupVoiceInstruction(closestSteps.getInstruction(), nextClosestSteps.getInstructionShort(), nextClosestSteps.getManeuver());
                }
//                Log.d(TAG, "setInstructionView: nextClosestSteps != null : voiceInstruction: " + voiceInstruction);
            } else {
                voiceInstruction = "Head on " + destination;
            }
//            Log.d(TAG, "setupInstruction: setInstructionView: voiceInstruction: " + voiceInstruction);

            double distanceInNextStep = SphericalUtil.computeDistanceBetween(lastLocation, closestSteps.endPoint());

//            July 08, 2022 -
            remainingDistanceToNextStep = ((int) distanceInNextStep);
            Log.d(TAG, "setInstructionView: remainingDistanceToNextStep: " + remainingDistanceToNextStep);
            double distanceToCurrentStep = SphericalUtil.computeDistanceBetween(closestSteps.startPoint(), closestSteps.endPoint());

            int voiceInstructionLimit = VOICE_INSTRUCTION_LONG_DISTANCE;
            if (distanceToCurrentStep < 100)
                voiceInstructionLimit = VOICE_INSTRUCTION_SHORT_DISTANCE;


            Log.d(TAG, "setupInstruction: setInstructionView: distanceToNextStep : " + distanceInNextStep + " voiceInstructionLimit: " + voiceInstructionLimit + " currentSegmentMaxSpeed: " + currentSegmentMaxSpeed);
            if (distanceInNextStep < voiceInstructionLimit) {
                // 2022.08.24 we should calulate the distance 10% grater than the length of the step
                double distance = voiceInstructionLimit * 1.1;
                boolean canPlayInstruction = false;

                if (closestSteps.getLength() > distance) {
                    // 2022.08.24 we should show the turn message only if the length of the step is > the speak limit
                    canPlayInstruction = true;
                    Log.d(TAG, "setupInstruction: setInstructionView: canPlayInstruction: distance: " + distance);
                }
                if (previousInstruction.isEmpty() || !previousInstruction.equals(voiceInstruction)) {
//                        July 22, 2022 -   Comment out for testing
                    if (canPlayInstruction) {
                        Log.d(TAG, "setupInstruction: setInstructionView: canPlayInstruction: " + voiceInstruction);
                        speakInstruction(voiceInstruction);
                        previousInstruction = voiceInstruction;
                    }
                }
            } else {
//                June 27, 2022 -   We should check the 1/4 mile on a highway/freeway
                Log.d(TAG, "setupInstruction: setInstructionView: ");
//                Aug 02, 2022  -   We should not play quarter of a mile message if the segment length is smaller than the half(0.5) of a mile
                int stepLengthLimit = 800, quarterOfAMile = 420;

//                Aug 04, 2022  -   Fix the problem regarding quarter of a mile message that was played at the beginning
//                Aug 01, 2022  -   Use get length as it will return the total length of a segment
//                if ((distanceInNextStep < stepLengthLimit) && (closestSteps.getLength() > stepLengthLimit)) {
                if ((distanceInNextStep < quarterOfAMile) && (closestSteps.getLength() > stepLengthLimit)) {
                    Log.d(TAG, "setupInstruction: setInstructionView: distance is less than quarter and max speed also exceeds 70 km");
                    String voiceInstructionChanged = null;
                    if (nextClosestSteps.getManeuver() != null && nextClosestSteps.getManeuver().contains("_")) {
                        // 2022.08.19 if we have a custom manouvre then we should use the instruction
                        voiceInstructionChanged = "in quarter of a mile " + nextClosestSteps.getInstruction();
                    } else {
                        voiceInstructionChanged = "in quarter of a mile " + voiceInstruction;
                    }
                    Log.d(TAG, "setupInstruction: setInstructionView: voiceInstructionChanged: " + voiceInstructionChanged
                            + " previousQuarterInstruction: " + previousQuarterInstruction);
                    if (previousQuarterInstruction.isEmpty() || !previousQuarterInstruction.equalsIgnoreCase(voiceInstructionChanged)) {
                        Log.d(TAG, "setInstructionView: now Speak above instruction");
//                        July 22, 2022 -   Comment out for testing
                        speakInstruction(voiceInstructionChanged);
                        previousQuarterInstruction = voiceInstructionChanged;
                    }
                }
            }
        } else {
//            Aug 02, 2022  -   Adding extra log for debugging information
            tvInstruction.setText("closest Step is null.");
        }
    }

//    When doing the route text in navigation window.
//    Seventh street => 7th Street
//    One hundred and twenty second avenue => 122nd Ave
//    On the audio instructions change I-10 E to I-10 East
//    Change single letters at end E – East, W – West, N – north, S – south
//    Lets try and get drive side panel with new ui next week.
//    Will add amenities to loves, pilot, ta.


    private String setupVoiceInstruction(String firstInstruction, String secondInstruction, String secondManeuver) {
        Log.d(TAG, "setupVoiceInstruction: firstInstruction: " + firstInstruction
                + " secondInstruction: " + secondInstruction
                + " secondManeuver: " + secondManeuver);

        StringBuilder formattedInstruction = new StringBuilder();

        if (secondInstruction != null && secondManeuver != null) {
            String formatedManeuver = secondManeuver.replace("-", " ") + " ";
            formattedInstruction.append(formatedManeuver);
            String formatedSecondInstruction = "";
            if (secondInstruction.contains("onto")) {
                formatedSecondInstruction = secondInstruction.substring(secondInstruction.indexOf("onto"));
                formatedSecondInstruction = formatedSecondInstruction.replace("onto", "on");
                //2022.08.19 } else if (secondInstruction.contains("on") ) { we need to fix the issue regarding " continue straight to stay on 26th St" to "ontinue straight to stay on 26th St"
            } else if (secondInstruction.contains(" on ")) {
                Log.d(TAG, "setupVoiceInstruction: formattedInstruction: contains: " + secondInstruction);

                formatedSecondInstruction = secondInstruction.substring(secondInstruction.indexOf(" on "));

                Log.d(TAG, "setupVoiceInstruction: formattedInstruction: contains: " + formatedSecondInstruction);

            } else if (secondInstruction.contains("towards")) {
                formatedSecondInstruction = secondInstruction.substring(secondInstruction.indexOf("towards"));
            } else {
                //2022.08.19 we should use the full instruction if it does not need to be formatted. It was an issue regarding the "turn left" and was not saying "on Georgina"
                formatedSecondInstruction = "on " + secondInstruction;
            }

            if (formatedSecondInstruction.contains("Destination")) {
                String destinationRemoved = formatedSecondInstruction.substring(0, formatedSecondInstruction.indexOf("Destination"));

                formattedInstruction.append(destinationRemoved);
            } else {
                formattedInstruction.append(formatedSecondInstruction);
            }
        } else {
            formattedInstruction.append(firstInstruction);
        }

//        June 28, 2022 -
//        I think split it with "for" because previous instructions are not that meaningful so added like
//        Previous was      =>  turn right on Esparta Way
//        New is            =>  for Esparta Way turn right
        Log.d(TAG, "setupVoiceInstruction: comparison: formattedInstruction: " + formattedInstruction);
//        String newFormattedInstruction = formattedInstruction.toString().replace("on", "for");
//        Log.d(TAG, "setupVoiceInstruction: newFormattedInstruction: " + newFormattedInstruction);
//        String[] splitRequestIdAndEventType = newFormattedInstruction.split("for");
//        if (splitRequestIdAndEventType != null && splitRequestIdAndEventType.length > 1) {
//            newFormattedInstruction = "for" + splitRequestIdAndEventType[1] + " " + splitRequestIdAndEventType[0].trim();
//            Log.d(TAG, "setupVoiceInstruction: comparison: newFormattedInstruction: final String: " + newFormattedInstruction);
//            return newFormattedInstruction;
//        }

        Log.d(TAG, "setupVoiceInstruction: formattedInstruction: " + formattedInstruction);
        return voiceInstruction(formattedInstruction.toString());
    }

    private String headingInstruction(String instruction) {
        Log.d(TAG, "headingInstruction: instruction: " + instruction);
        if (!instruction.contains("onto"))
            return instruction;

//        July 25, 2022 -   Show instructions in camelcase and not in lower case
//        String formattedInstruction = instruction.toLowerCase().substring(instruction.indexOf("onto"));
        String formattedInstruction = instruction.substring(instruction.indexOf("onto"));
        Log.d(TAG, "headingInstruction: instruction: " + formattedInstruction);
        formattedInstruction = formattedInstruction.replace("onto ", "");
        if (formattedInstruction.contains("destination"))
            formattedInstruction = formattedInstruction.substring(0, formattedInstruction.indexOf("destination"));


//        July 25, 2022 -   commented today because we want to show instructions as we are getting them from the api
//        for (String key : streets.keySet()) {
//            if (formattedInstruction.toLowerCase().contains(" " + key.toLowerCase())) {
//                String value = stringToUpperCase(streets.get(key));
//                formattedInstruction = stringToUpperCase(formattedInstruction).replace(key.toLowerCase(), value);
//                break;
//            }
//        }
        return formattedInstruction;
    }

    private String voiceInstruction(String instruction) {
//        String formattedInstruction = instruction.toLowerCase();
//        for (String key : streets.keySet()) {
//            if (formattedInstruction.toLowerCase().contains(" " + key.toLowerCase())) {
//                String value = stringToUpperCase(streets.get(key));
//                formattedInstruction = stringToUpperCase(formattedInstruction).replace(key.toLowerCase(), value);
//                break;
//            }
//        }
//        return formattedInstruction;
        return instruction;
    }

    public void speakInstruction(String message) {
        Log.d(TAG, "setupVoiceInstruction: speakInstruction: message: " + message);
        String tempMessage = message.replace(" St", " Street").replace(" Pl", " Place");

        if (tempMessage.contains(" Driv")) {
            tempMessage = message.replace(" Driv", " Drive");
        } else if (tempMessage.contains(" Drv")) {
            tempMessage = message.replace(" Drv", " Drive");
        } else if (!tempMessage.contains(" Dr.") && tempMessage.contains(" Dr")) {
            tempMessage = message.replace(" Dr", " Drive");
        }

        if (message.equalsIgnoreCase(mPreviousInstructionPlayed)) {
            return;
        } else {
            mPreviousInstructionPlayed = message;
        }

        if (muteVoice)
            return;
        tts.speak(tempMessage);
    }

    public void onDestroy() {
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }

        if (tts != null) {
            tts.shutdown();
        }

        tts = null;
    }

    public void setMuteVoice(boolean muteVoice) {
        this.muteVoice = muteVoice;
        Log.d(TAG, "handleVoice: setMuteVoice: muteVoice: " + muteVoice);
        if (muteVoice) {
            muteVoice();
        }
    }

    private void muteVoice() {
        Log.d(TAG, "handleVoice: muteVoice: stop test to speech: ");
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }
    }

    private String stringToUpperCase(String street) {
        return street.substring(0, 1).toUpperCase() + street.substring(1).toLowerCase();
    }

    public void setPreviousQuarterInstruction(String previousQuarterInstruction) {
        this.previousQuarterInstruction = previousQuarterInstruction;
    }

    public void resetInstructionViews() {

        tvInstruction.setText("Empty");
        tvDistance.setText("");
        hours.setText("");
        miles.setText("");
        mPreviousInstructionPlayed = "";

        ivInstruction.setImageURI(null);
        direction_btn.setImageURI(null);

        segments = null;
        closestSteps = null;
        nextClosestSteps = null;
    }

    String mPreviousInstructionPlayed = "";

    public void playInstructionForStep(int stepIndex) {

        Log.d(TAG, "playInstructionForStep: onLocationChanged: segmentPosition: stepIndex: " + stepIndex);
        Log.d(TAG, "playInstructionForStep: stepIndex: " + stepIndex + " segments: " + segments);
        if (segments != null) {
            Log.d(TAG, "playInstructionForStep: segments: " + segments.size());
            if (stepIndex < segments.size()) {
                Segment segment = segments.get(stepIndex);
                Segment nextSegment = null;
                if ((stepIndex + 1) < segments.size()) {
                    nextSegment = segments.get(stepIndex + 1);
                    String message = "In " + segment.getDistanceText() + " " + nextSegment.getInstruction();
                    Log.d(TAG, "playInstructionForStep: message: " + message);
                    speakInstruction(message);
                } else {
                    nextSegment = segments.get((segments.size() - 1));
//                    Log.d(TAG, "playInstructionForStep: distanceText: "+segment.getDistanceText()+" instruction: "+segment.getInstruction()+" nextSegment: instrcution: "+nextSegment.getInstruction());
//                    String message = "In " + segment.getDistanceText() + " " + segment.getInstruction();
                    String message = "In " + segment.getDistanceText() + " you will arrive at the destination";
                    Log.d(TAG, "else: playInstructionForStep: message: " + message);
                    speakInstruction(message);
                }
            } else {
                Log.d(TAG, "playInstructionForStep: else when stepIndex < segments.size()");
            }
        }

    }

    public String generatePlayMessageForIndex(int stepIndex) {

        Log.d(TAG, "playInstructionForStep: onLocationChanged: segmentPosition: stepIndex: " + stepIndex);
        Log.d(TAG, "playInstructionForStep: stepIndex: " + stepIndex + " segments: " + segments);
        if (segments != null) {
            Log.d(TAG, "playInstructionForStep: segments: " + segments.size());
            if (stepIndex < segments.size()) {
                Segment segment = segments.get(stepIndex);
                Segment nextSegment = null;
                if ((stepIndex + 1) < segments.size()) {
                    nextSegment = segments.get(stepIndex + 1);
                    String message = "In " + segment.getDistanceText() + " " + nextSegment.getInstruction();
                    Log.d(TAG, "playInstructionForStep: message: " + message);
                    return message;
                } else {
                    nextSegment = segments.get((segments.size() - 1));
//                    Log.d(TAG, "playInstructionForStep: distanceText: "+segment.getDistanceText()+" instruction: "+segment.getInstruction()+" nextSegment: instrcution: "+nextSegment.getInstruction());
//                    String message = "In " + segment.getDistanceText() + " " + segment.getInstruction();
                    String message = "In " + segment.getDistanceText() + " you will arrive at the destination";
                    Log.d(TAG, "else: playInstructionForStep: message: " + message);
                    return message;
                }
            }
        }
        return null;
    }

    public Map<Integer, Integer> getSegmentsDurationMap() {
        return segmentsDurationMap;
    }

    public void setSegmentsDurationMap(Map<Integer, Integer> segmentsDurationMap) {
        this.segmentsDurationMap = segmentsDurationMap;

    }

    private void fillAbbreviation() {
        streets.put("ALY", "ALLEY");
        streets.put("ANX", "ANEX");
        streets.put("ARC", "ARCADE");
        streets.put("ARCADE", "ARCADE");
        streets.put("AVE", "AVENUE");
// streets.put("AV","AVENUE");
        streets.put("BYU", "BAYOU");
        streets.put("BCH", "BEACH");
        streets.put("BND", "BEND");
        streets.put("BLF", "BLUFF");
        streets.put("BLFS", "BLUFFS");
        streets.put("BTM", "BOTTOM");
        streets.put("BLVD", "BOULEVARD");
        streets.put("BR", "BRANCH");
        streets.put("BRG", "BRIDGE");
        streets.put("BRK", "BROOK");
        streets.put("BRKS", "BROOKS");
        streets.put("BG", "BURG");
        streets.put("BGS", "BURGS");
        streets.put("BYP", "BYPASS");
        streets.put("CP", "CAMP");
        streets.put("CYN", "CANYON");
        streets.put("CPE", "CAPE");
        streets.put("CSWY", "CAUSEWAY");
        streets.put("CTR", "CENTER");
        streets.put("CTRS", "CENTERS");
        streets.put("CIR", "CIRCLE");
        streets.put("CIRS", "CIRCLES");
        streets.put("CLF", "CLIFF");
        streets.put("CLFS", "CLIFFS");
        streets.put("CLB", "CLUB");
        streets.put("CMN", "COMMON");
        streets.put("CMNS", "COMMONS");
        streets.put("COR", "CORNER");
        streets.put("CORS", "CORNERS");
        streets.put("CASE", "COURSE");
        streets.put("CT", "COURT");
        streets.put("CTS", "COURTS");
        streets.put("CV", "COVE");
        streets.put("CVS", "COVES");
        streets.put("CRK", "CREEK");
        streets.put("CRES", "CRESCENT");
        streets.put("CAST", "CREST");
        streets.put("XING", "CROSSING");
        streets.put("XRD", "CROSSROAD");
        streets.put("XRDS", "CROSSROADS");
        streets.put("CURV", "CURVE");
        streets.put("DL", "DALE");
        streets.put("DM", "DAM");
        streets.put("DV", "DIVIDE");
        streets.put("DR", "DRIVE");
        streets.put("DRS", "DRIVES");
        streets.put("EST", "ESTATE");
        streets.put("ESTS", "ESTATES");
        streets.put("EXPY", "EXPRESSWAY");
        streets.put("EXT", "EXTENSION");
        streets.put("EXTS", "EXTENSIONS");
        streets.put("FALL", "FALL");
        streets.put("FALS", "FALLS");
        streets.put("FRY", "FERRY");
        streets.put("FLD", "FIELD");
        streets.put("FLDS", "FIELDS");
        streets.put("FLT", "FLAT");
        streets.put("FLTS", "FLATS");
        streets.put("FRD", "FORD");
        streets.put("FADS", "FORDS");
        streets.put("FAST", "FOREST");
        streets.put("FRG", "FORGE");
        streets.put("FAGS", "FORGES");
        streets.put("FRK", "FORK");
        streets.put("FRKS", "FORKS");
        streets.put("FT", "FORT");
        streets.put("FWY", "FREEWAY");
        streets.put("GRDN", "GARDEN");
        streets.put("GDNS", "GARDENS");
        streets.put("GRDNS", "GARDENS");
        streets.put("GTWY", "GATEWAY");
        streets.put("GLN", "GLEN");
        streets.put("GLNS", "GLENS");
        streets.put("GRN", "GREEN");
        streets.put("GRNS", "GREENS");
        streets.put("GRV", "GROVE");
        streets.put("GRVS", "GROVES");
        streets.put("HBR", "HARBOR");
        streets.put("HBRS", "HARBORS");
        streets.put("HVN", "HAVEN");
// streets.put("HT", "HEIGHTS");
// streets.put("HTS", "HEIGHTS");
        streets.put("HWY", "HIGHWAY");
        streets.put("HL", "HILL");
        streets.put("HLS", "HILLS");
        streets.put("HOLW", "HOLLOW");
        streets.put("INLT", "INLET");
        streets.put("IS", "ISLAND");
        streets.put("ISS", "ISLANDS");
        streets.put("ISLE", "ISLE");
        streets.put("JCT", "JUNCTION");
        streets.put("JCTS", "JUNCTIONS");
        streets.put("KY", "KEY");
        streets.put("KYS", "KEYS");
        streets.put("KNL", "KNOLL");
        streets.put("KNLS", "KNOLLS");
        streets.put("LK", "LAKE");
        streets.put("LKS", "LAKES");
        streets.put("LAND", "LAND");
        streets.put("LNDG", "LANDING");
        streets.put("LANE", "LANE");
        streets.put("LN", "LANE");
        streets.put("LGT", "LIGHT");
        streets.put("LGTS", "LIGHTS");
        streets.put("LF", "LOAF");
        streets.put("LCK", "LOCK");
        streets.put("LCKS", "LOCKS");
        streets.put("LDG", "LODGE");
        streets.put("LOOP", "LOOP");
        streets.put("MALL", "MALL");
        streets.put("MNR", "MANOR");
        streets.put("MNRS", "MANORS");
        streets.put("MDW", "MEADOW");
        streets.put("MDWS", "MEADOWS");
        streets.put("MEWS", "MEWS");
        streets.put("ML", "MILL");
        streets.put("MLS", "MILLS");
        streets.put("MSN", "MISSION");
        streets.put("MTWY", "MOTORWAY");
        streets.put("MT", "MOUNT");
        streets.put("MTN", "MOUNTAIN");
        streets.put("MTNS", "MOUNTAINS");
        streets.put("NCK", "NECK");
        streets.put("ORCH", "ORCHARD");
        streets.put("OVAL", "OVAL");
        streets.put("OPAS", "OVERPASS");
        streets.put("PARK", "PARK");
        streets.put("PARKS", "PARK");
        streets.put("PKWY", "PARKWAY");
        streets.put("PKWYS", "PARKWAYS");
        streets.put("PASS", "PASS");
        streets.put("PSGE", "PASSAGE");
        streets.put("PATH", "PATH");
        streets.put("PIKE", "PIKE");
        streets.put("PNE", "PINE");
        streets.put("PNES", "PINES");
        streets.put("PL", "PLACE");
        streets.put("PLN", "PLAIN");
        streets.put("PLNS", "PLAINS");
        streets.put("PLZ", "PLAZA");
        streets.put("PT", "POINT");
        streets.put("PTS", "POINTS");
        streets.put("PAT", "PORT");
        streets.put("PATS", "PORTS");
        streets.put("PR", "PRAIRIE");
        streets.put("RADL", "RADIAL");
        streets.put("RAMP", "RAMP");
        streets.put("RNCH", "RANCH");
        streets.put("RPD", "RAPID");
        streets.put("RPDS", "RAPIDS");
        streets.put("RST", "REST");
        streets.put("RDG", "RIDGE");
        streets.put("RDGS", "RIDGES");
        streets.put("RIV", "RIVER");
        streets.put("RD", "ROAD");
        streets.put("RDS", "ROADS");
        streets.put("RTE", "ROUTE");
        streets.put("ROW", "ROW");
        streets.put("RUE", "RUE");
        streets.put("RUN", "RUN");
        streets.put("SHL", "SHOAL");
        streets.put("SHLS", "SHOALS");
        streets.put("SHR", "SHORE");
        streets.put("SHRS", "SHORES");
        streets.put("SKWY", "SKYWAY");
        streets.put("SPG", "SPRING");
        streets.put("SPGS", "SPRINGS");
        streets.put("SPUR", "SPUR");
        streets.put("SPURS", "SPUR");
        streets.put("SQ", "SQUARE");
        streets.put("SAS", "SQUARES");
        streets.put("STA", "STATION");
        streets.put("STRA", "STRAVENUE");
        streets.put("STRM", "STREAM");
        streets.put("ST", "STREET");
        streets.put("STR", "STREET");
        streets.put("STS", "STREETS");
        streets.put("SMT", "SUMMIT");
        streets.put("TER", "TERRACE");
        streets.put("TRWY", "THROUGHWAY");
        streets.put("TRCE", "TRACE");
        streets.put("TRAK", "TRACK");
        streets.put("TRFY", "TRAFFICWAY");
        streets.put("TRL", "TRAIL");
        streets.put("TRLR", "TRAILER");
        streets.put("TUNL", "TUNNEL");
        streets.put("TPKE", "TURNPIKE");
        streets.put("UPAS", "UNDERPASS");
        streets.put("UN", "UNION");
        streets.put("UNS", "UNIONS");
        streets.put("VLY", "VALLEY");
        streets.put("VLYS", "VALLEYS");
        streets.put("VW", "VIEW");
        streets.put("VWS", "VIEWS");
        streets.put("VLG", "VILLAGE");
        streets.put("VLGS", "VILLAGES");
        streets.put("VL", "VILLE");
        streets.put("VIS", "VISTA");
        streets.put("WALK", "WALK");
        streets.put("WALKS", "WALK");
        streets.put("WALL", "WALL");
        streets.put("WAY", "WAY");
        streets.put("WYs", "WAYS");
        streets.put("WL", "WELL");
        streets.put("WLS", "WELLS");
    }

    public interface OnHandleBottomView {
        void iDismiss();

        void iDirection();

        void reloadCurrentSegment();
    }

    public String getmPreviousInstructionPlayed() {
        return mPreviousInstructionPlayed;
    }

    public void setmPreviousInstructionPlayed(String mPreviousInstructionPlayed) {
        this.mPreviousInstructionPlayed = mPreviousInstructionPlayed;
    }

    public void setManeuverImage(int source) {
        Log.d(TAG, "setInstructionView: setManeuverImage: source: " + source);
        ivInstruction.setImageResource(source);
        Log.d(TAG, "setManeuverImage: ivInstruction: ");
    }

    void refineInstructions(String instruction) {
//        When doing the route text in navigation window.
//        Seventh street à 7th Street
//        One hundred and twenty second avenue to 122nd Ave
//        On the audio instructions change I-10 E to I-10 East
//        Change single letters at end E – East, W – West, N – north, S – south

        String refinedInstruction = instruction.replace("Seventh street", "7th Street")
                .replace("One hundred and twenty second avenue", "122nd Ave")
                .replace(" E ", " East ")
                .replace(" W ", " West ")
                .replace(" N ", " North ")
                .replace(" S ", " South ");

        if (refinedInstruction.endsWith(" E")) {
            refinedInstruction = refinedInstruction.replace(" E", " East");
        }

        if (refinedInstruction.endsWith(" W")) {
            refinedInstruction = refinedInstruction.replace(" W", " West");
        }

        if (refinedInstruction.endsWith(" N")) {
            refinedInstruction = refinedInstruction.replace(" N", " North");
        }

        if (refinedInstruction.endsWith(" S")) {
            refinedInstruction = refinedInstruction.replace(" S", " South");
        }


        Log.d(TAG, "refineInstructions: words to digits: instruction: " + refinedInstruction);
        Log.d(TAG, "refineInstructions: words to digits: " + UiUtils.replaceNumbers(instruction));


//        On the audio instructions change I-10 E to I-10 East
//        Change single letters at end E – East, W – West, N – north, S – south
        tvInstruction.setText(refinedInstruction);
    }
}
