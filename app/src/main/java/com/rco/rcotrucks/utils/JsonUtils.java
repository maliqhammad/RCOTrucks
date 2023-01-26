package com.rco.rcotrucks.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class JsonUtils {
    public static final String TAG = "JsonUtils";

    public static class JsonWrapper {
        private JSONObject json;

        public JsonWrapper(JSONObject json) {
            this.json = json;
        }

        public String get(String name) {
            return get(name, null);
        }

        public String get(String name, String defaultValue) {
            String v = defaultValue;
//            try {
                v = json.optString(name, defaultValue);
//            } catch (JSONException e) {
//                Log.d(TAG, "get() **** Error getting JSON object. Probable cause: element with name: " + name + " not found. " + e.toString());
////                e.printStackTrace();
//            }
            return v;
        }

        public JSONObject getJsonObj(String name) {
            JSONObject v = null;

//            try {
                v = json.optJSONObject(name);
//            } catch (JSONException e) {
//                Log.d(TAG, "getJsonObj() **** Error getting JSON object. Probable cause: element with name: " + name + " not found." + e.toString());
//            }
            return v;
        }
    }

    /**
     *
     [{"LobjectId":11,"objectType":"NRT524","errorCode":"","errorMessage":"","csvDataFilePath":"",
     "iCsvRow":-1,"mobileRecordId":"IFTAEvent-18029-austin-ON-1597147100.297781",
     "mapCodingInfo":{"Week":"33","BarCode":"000002502967","Employee Id":"66432","Odometer":"0","Fuel Type":"Ethanol","Organization Name":"Rise West","MobileRecordId":"IFTAEvent-18029-austin-ON-1597147100.297781","Day":"11","Trip Permit Yes or No":"no","Month":"08","Year":"2020","Creation Date":"2020-08-11 00:00:00.000","DateTime":"2020-08-11 14:58:20.000","Company":"Austin, Alexander","Quarter":"2","Organization Number":"8","Miles":"1048","Fuel Code":"03","Country":"US","Jurisdiction ID Exited":"VT","IFTA Yes or No":"yes","Hour":"14","State":"Alabama","RMS Coding Timestamp":"1597147102621","ObjectName":"2020-08-11 14:58, ifta-tp, Austin, Alexander, Alabama","Minutes":"58","RMS Timestamp":"1597147102621","Last Name":"Austin","First Name":"Alexander"}}      */
    public static class JsonRecFiltXWrapper {
        public long LobjectId = -1;
        public String objectId = "";
        public String objectType = "";
        public String errorCode = "";
        public String errorMessage = "";
        public long ixRow = -1;
        public String mobileRecordId = "";
        public JsonWrapper codingInfo;


        public JsonRecFiltXWrapper(JSONObject jsonRow) {
            JsonWrapper rec = new JsonWrapper(jsonRow);
            objectId = rec.get("LobjectId");
            if (objectId != null && objectType.length() > 0) LobjectId = Long.parseLong(objectId);
            objectType = rec.get("objectType");
            errorCode = rec.get("errorCode");
            errorMessage = rec.get("errorMessage");
            ixRow = Long.parseLong(rec.get("iCsvRow", "-1"));
            mobileRecordId = rec.get("mobileRecordId");
            codingInfo = new JsonWrapper(rec.getJsonObj("mapCodingInfo"));
        }

        public String getCoding(String codingfieldName) {
            return codingInfo.get(codingfieldName);
        }

        public String getCoding(String codingfieldName, String defaultValue) {
            return codingInfo.get(codingfieldName, defaultValue);
        }
    }
}
