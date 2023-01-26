package com.rco.rcotrucks.utils.route;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Segment implements Parcelable {

    private static final String TAG = Segment.class.getSimpleName();
    /**
     * Points in this segment. *
     */
    private LatLng start;
    /**
     * Points in this segment. *
     */
    private LatLng end;
    /**
     * Turn instruction to reach next segment. *
     */
    private String instruction;
    /**
     * Length of segment. *
     */
    private int length;
    /**
     * Distance covered. *
     */
    private double distance;
    private String distanceText;
    private double currentRemainingDistance;
    /**
     * Duration covered. *
     */
    private String duration;
    private double durationValue;

    /* Maneuver instructions */
    private String maneuver;

    private List<LatLng> segmentPoints;

    private Integer speedLimit;

    private boolean isVisited = false;

    private String instructionShort;
    private String isRoundAbout = "false";
    private int exitNumber = 0;
    //    Aug 05, 2022  -   1000 is the default not set value
    private float turnAngle = 1000;


    /**
     * Create an empty segment.
     */

    public Segment() {
    }

    private Segment(Parcel in) {
        start = in.readParcelable(LatLng.class.getClassLoader());
        end = in.readParcelable(LatLng.class.getClassLoader());
        instruction = in.readString();
        length = in.readInt();
        distance = in.readDouble();
        distanceText = in.readString();
        duration = in.readString();
        durationValue = in.readDouble();
        maneuver = in.readString();
        speedLimit = in.readInt();
        instructionShort = in.readString();
        isRoundAbout = in.readString();
        turnAngle = in.readFloat();
        exitNumber = in.readInt();
        // segmentPoints
        if (in.readInt() == 1) {
            segmentPoints = new ArrayList<>();
            in.readList(segmentPoints, LatLng.class.getClassLoader());
        } else {
            segmentPoints = null;
        }
    }

    /**
     * Set the turn instruction.
     *
     * @param turn Turn instruction string.
     */

    public void setInstruction(final String turn) {
        this.instruction = turn;
    }

    /**
     * Get the turn instruction to reach next segment.
     *
     * @return a String of the turn instruction.
     */

    public String getInstruction() {
        return instruction;
    }

    /**
     * Add a point to this segment.
     *
     * @param point GeoPoint to add.
     */

    public void setPoint(final LatLng point) {
        start = point;
    }

    /**
     * Add a point to this segment.
     *
     * @param point GeoPoint to add.
     */

    public void setEndPoint(final LatLng point) {
        end = point;
    }

    /**
     * Get the starting point of this
     * segment.
     *
     * @return a GeoPoint
     */

    public LatLng startPoint() {
        return start;
    }

    /**
     * Get the ending point of this
     * segment.
     *
     * @return a GeoPoint
     */

    public LatLng endPoint() {
        return end;
    }

    /**
     * Creates a segment which is a copy of this one.
     *
     * @return a Segment that is a copy of this one.
     */

    public Segment copy() {
        final Segment copy = new Segment();
        copy.start = start;
        copy.end = end;
        copy.instruction = instruction;
        copy.length = length;
        copy.distance = distance;
        copy.distanceText = distanceText;
        copy.duration = duration;
        copy.durationValue = durationValue;
        copy.maneuver = maneuver;
        copy.speedLimit = speedLimit;
        copy.isVisited = isVisited;
        copy.segmentPoints = segmentPoints;
        copy.instructionShort = instructionShort;
        copy.isRoundAbout = isRoundAbout;
        copy.turnAngle = turnAngle;
        copy.exitNumber = exitNumber;
        return copy;
    }


    /**
     * @param length the length to set
     */
    public void setLength(final int length) {
        this.length = length;
    }

    /**
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getCurrentRemainingDistance() {
        return currentRemainingDistance;
    }

    public void setCurrentRemainingDistance(double currentRemainingDistance) {
        this.currentRemainingDistance = currentRemainingDistance;
    }

    /**
     * @return the distance
     */
    public double getDistance() {
        return distance;
    }

    public String getDistanceText() {
        return distanceText;
    }

    public void setDistanceText(String distanceText) {
        this.distanceText = distanceText;
    }

    public void setManeuver(String man, String callerFunction) {
        Log.d(TAG, "setInstructionView: setManeuver: previous: maneuver: " + maneuver + " newManeuver: " + man + " context: " + callerFunction);
        maneuver = man;
    }

    public String getManeuver() {
        return maneuver;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public double getDurationValue() {
        return durationValue;
    }

    public void setDurationValue(double durationValue) {
        this.durationValue = durationValue;
    }

    public List<LatLng> getSegmentPoints() {
        return segmentPoints;
    }

    public void setSegmentPoints(List<LatLng> segmentPoints) {
        this.segmentPoints = segmentPoints;
    }

    public Integer getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(Integer speedLimit) {
        this.speedLimit = speedLimit;
        Log.d(TAG, "setSpeedLimit: speedLimit: " + speedLimit);
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(start, flags);
        dest.writeParcelable(end, flags);
        dest.writeString(instruction);
        dest.writeInt(length);
        dest.writeDouble(distance);
        dest.writeString(distanceText);
        dest.writeString(duration);
        dest.writeDouble(durationValue);
        dest.writeString(maneuver);
        dest.writeInt(speedLimit);
        dest.writeString(instructionShort);
        dest.writeString(isRoundAbout);
        dest.writeFloat(turnAngle);
        dest.writeFloat(exitNumber);
        // segmentPoint
        if (segmentPoints == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            dest.writeList(segmentPoints);
        }
    }

    public static final Creator<Segment> CREATOR = new Creator<Segment>() {
        @Override
        public Segment createFromParcel(Parcel in) {
            return new Segment(in);
        }

        @Override
        public Segment[] newArray(int size) {
            return new Segment[size];
        }
    };

    public String getInstructionShort() {
        return instructionShort;
    }

    public void setInstructionShort(String instructionShort) {
        this.instructionShort = instructionShort;
    }

    public String getIsRoundAbout() {
        return isRoundAbout;
    }

    public void setIsRoundAbout(String isRoundAbout) {
        this.isRoundAbout = isRoundAbout;
    }

    public float getTurnAngle() {
        return turnAngle;
    }

    public void setTurnAngle(float turnAngle) {
        this.turnAngle = turnAngle;
    }

    public int getExitNumber() {
        return exitNumber;
    }

    public void setExitNumber(int exitNumber) {
        this.exitNumber = exitNumber;
    }
}
