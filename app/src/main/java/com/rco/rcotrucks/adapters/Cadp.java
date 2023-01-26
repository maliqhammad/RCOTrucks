package com.rco.rcotrucks.adapters;

public class Cadp {
    // These constants for Adapters
    // Recycler View ViewHolder and related constants.
    public static final int VIEW_TAG_OBJECTID = 1;
    public static final int VIEW_TAG_OBJECTTYPE = 2;

    public static String KEY_IDRMSRECORDS = "idRmsRecords";

    public static final int VIEWTYPE_HDR = 1;
    public static final int VIEWTYPE_FLD = 2;
    public static final int VIEWTYPE_LBL_FLD = 3;
    public static final int VIEWTYPE_HDR_FLD = 4;
    public static final int VIEWTYPE_LBL_CHK = 5;
    public static final int VIEWTYPE_HDR_CMT = 6;
    public static final int VIEWTYPE_HDR_FLD_SIG = 7;
    public static final int VIEWTYPE_HDR_FLD_SPIN = 8;
    public static final int VIEWTYPE_HDR_SPIN_HID = 9;
    public static final int VIEWTYPE_EXTRA = 10;
    public static final int VIEWTYPE_PIC = 11;

//    public static final int EDIT_MODE_COMBINED = 0;
    public static final int EDIT_MODE_EDITABLE = 1;
    public static final int EDIT_MODE_READONLY = 3;
    public static final int EDIT_MODE_LOOKUP = 4;

    public static final int COMBINE_TYPE_NONE = 0;
    public static final int COMBINE_TYPE_FIRST_LAST_NAME = 1;
    public static final int COMBINE_TYPE_LAST_FIRST_NAME = 2;
    public static final int COMBINE_TYPE_CODE_OF_SELECTION = 3;


    public static final Integer BITMAP_CLASS_SIGNATURE = 1;
    public static final Integer BITMAP_CLASS_RECORD_CONTENT = 2;
    public static final Integer BITMAP_TYPE_DRIVER_SIGNATURE = 1;
    public static final Integer BITMAP_TYPE_MECHANIC_SIGNATURE = 2;
    public static final Integer BITMAP_TYPE_NONE = 3;

    public static final String HTML_ELEM_TYPE_BITMAP = "bitmap";
    public static final String HTML_ELEM_TYPE_TEXT = "text";
    public static final String HTML_ELEM_TYPE_SPAN = "span";
    public static final String HTML_ELEM_TYPE_CHECKBOX = "checkbox";

    public static final String HTML_FORMAT_DATE = "date";
    public static final String HTML_FORMAT_TIME_FROM_DATE = "timefromdate";
    public static final String HTML_FORMAT_AMPM_FROM_DATE = "ampmfromdate";

    public static final String EXTRA_MESSAGE = "extramessage";

    public static final String FOLDER_RELATIVE_PATH_PRINTFILES = "printfiles";

    public static final int SYNC_STATUS_PENDING_UPDATE = 0;
    public static final int SYNC_STATUS_SENT = 1;
    public static final int SYNC_STATUS_MARKED_FOR_DELETE = 2;


    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_OBJECT_ID = "object_id";
    public static final String EXTRA_OBJECT_TYPE = "object_type";

    public static final String EXTRA_EVALUATION = "evaluation";
    public static final String EXTRA_EVALUATION_TYPE = "evaluation_type";
    public static final String EXTRA_PREVIEW = "preview_spf";
    public static final String EXTRA_SIGNATURE = "signature_spf";
    public static final String EXTRA_SIGNATURE_TYPE = "signature_spf_type";
    public static final String EXTRA_LASTACTIVITY = "last_activity";
    public static final String EXTRA_PREVIEW_URL = "preview_url";

    public static final String SQLITE_VAL_TRUE = "1";
}
