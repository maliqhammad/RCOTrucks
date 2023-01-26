package com.rco.rcotrucks.businesslogic;

import com.rco.rcotrucks.utils.StringUtils;

import java.util.ArrayList;

public class EldEvent {
    public String RecordId;

    public String objectId;
    public String objectType;

    public String trucklogsobjectId;
    public String trucklogsobjectType;

    public String eldlogsobjectId;
    public String eldlogsobjectType;

    public String MobileRecordId;
    public String RmsCodingTimestamp;
    public String RmsTimestamp;

    public String Id;
    public String EventType;
    public String Detail;
    public String OrganizationName;
    public String OrganizationNumber;
    public String EldUsername;
    public String EventCode;
    public String RecordStatus;
    public String RecordOrigin;
    public String TruckNumber;
    public String Vin;
    public String LocalizationDescription;
    public String LatitudeString;
    public String LongitudeString;
    public String DstSinceLastValidCoords;
    public String VehicleMiles;
    public String EngineHours;
    public String OrderNumberCmv;
    public String OrderNumberUser;
    public String SequenceId;
    public String EventCodeDescription;
    public String DiagnosticIndicator;
    public String MalfunctionIndicator;
    public String Annotation;
    public String RecordOriginId;
    public String CheckData;
    public String CheckSum;
    public String MalfunctionDiagnosticCode;
    public String MalfunctionDiagnosticDescp;
    public String DriverLastName;
    public String DriverFirstName;
    public String DriverRecordId;
    public String EditReason;
    public Double EventSeconds;
    public String ShiftStart;
    public String CreationDate;
    public String Odometer;

    public String Sent = "0";

    public EldEvent() {

    }

    public EldEvent(EldEvent e) {
        this.RecordId = e.RecordId;
        this.objectId = e.objectId;
        this.objectType = e.objectType;
        this.trucklogsobjectId = e.trucklogsobjectId;
        this.trucklogsobjectType = e.trucklogsobjectType;
        this.eldlogsobjectId = e.eldlogsobjectId;
        this.eldlogsobjectType = e.eldlogsobjectType;
        this.MobileRecordId = e.MobileRecordId;
        this.RmsCodingTimestamp = e.RmsCodingTimestamp;
        this.RmsTimestamp = e.RmsTimestamp;
        this.Id = e.Id;
        this.EventType = e.EventType;
        this.Detail = e.Detail;
        this.OrganizationName = e.OrganizationName;
        this.OrganizationNumber = e.OrganizationNumber;
        this.EldUsername = e.EldUsername;
        this.EventCode = e.EventCode;
        this.RecordStatus = e.RecordStatus;
        this.RecordOrigin = e.RecordOrigin;
        this.TruckNumber = e.TruckNumber;
        this.Vin = e.Vin;
        this.LocalizationDescription = e.LocalizationDescription;
        this.LatitudeString = e.LatitudeString;
        this.LongitudeString = e.LongitudeString;
        this.DstSinceLastValidCoords = e.DstSinceLastValidCoords;
        this.VehicleMiles = e.VehicleMiles;
        this.EngineHours = e.EngineHours;
        this.OrderNumberCmv = e.OrderNumberCmv;
        this.OrderNumberUser = e.OrderNumberUser;
        this.SequenceId = e.SequenceId;
        this.EventCodeDescription = e.EventCodeDescription;
        this.DiagnosticIndicator = e.DiagnosticIndicator;
        this.MalfunctionIndicator = e.MalfunctionIndicator;
        this.Annotation = e.Annotation;
        this.RecordOriginId = e.RecordOriginId;
        this.CheckData = e.CheckData;
        this.CheckSum = e.CheckSum;
        this.MalfunctionDiagnosticCode = e.MalfunctionDiagnosticCode;
        this.MalfunctionDiagnosticDescp = e.MalfunctionDiagnosticDescp;
        this.DriverLastName = e.DriverLastName;
        this.DriverFirstName = e.DriverFirstName;
        this.DriverRecordId = e.DriverRecordId;
        this.EditReason = e.EditReason;
        this.EventSeconds = e.EventSeconds;
        this.ShiftStart = e.ShiftStart;
        this.CreationDate = e.CreationDate;
        this.Sent = e.Sent;
        this.Odometer = e.Odometer;
    }

    public ArrayList<EldEventContent> eldContent;

//    public Double getEventSecondsValue() {
//        try {
//            return Double.parseDouble(EventSeconds);
//        } catch (Throwable throwable) {
//            if (throwable != null)
//                throwable.printStackTrace();
//        }
//
//        return 0d;
//    }

    //    public Long getEventSeconds() {
//        try {
//            if(EventSeconds.contains(".")) {
//                String splitEventSecond = EventSeconds.substring(0, EventSeconds.indexOf("."));
//                return Long.parseLong(splitEventSecond);
//            }else{
//                return Long.parseLong(EventSeconds);
//            }
//        } catch (Throwable throwable) {
//            if (throwable != null)
//                throwable.printStackTrace();
//        }
//
//        return 0l;
//    }

    public long getEventSecondsValue() {
        return (long) (double) EventSeconds;
    }


    public Double getEventSeconds() {
        return EventSeconds;
    }

    public String getOdometer() {
        try {
            if (StringUtils.isNullOrWhitespaces(Odometer))
                return "0";

            Double d = Double.parseDouble(Odometer);

            if (d == null)
                return "0";

            Long l = d.longValue();

            if (l == null)
                return "0";

            return l.toString();
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return Odometer;
        }
    }

    public Long getIdAsLong() {
        return Long.parseLong(Id);
    }

    @Override
    public String toString() {
        BusinessRules.EventCode eventCodeEnum;

        try {
            Integer eventCode = Integer.parseInt(EventCode);
            Integer eventType = Integer.parseInt(EventType);

            String eventCodeEnumStr = "";

            try {
                eventCodeEnum = BusinessRules.EventCode.values()[eventCode - 1];
                eventCodeEnumStr = eventCodeEnum.toString();
            } catch (Throwable throwable) {
                eventCodeEnumStr = eventCode.toString();
            }

            if (eventType == BusinessRules.EventType.AN_INTERMEDIATE_LOG.getValue() &&
                    eventCode == BusinessRules.EventCode.INTERMEDIATE_LOG_WITH_REDUCED_LOCATION_PRECISION.ordinal()) {
                eventCodeEnumStr = "INTERMEDIATE_LOG_WITH_REDUCED_LOCATION_PRECISION";
            }

            return Id + ": EventCode: " + EventCode + " (" + eventCodeEnumStr + ")" + ", Shift Start: " + ShiftStart + ", CD: " + CreationDate;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }
        return Id + ": EventCode: " + EventCode + ", Shift Start: " + ShiftStart + ", CD: " + CreationDate;
    }


}
