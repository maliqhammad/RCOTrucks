package com.rco.rcotrucks.businesslogic.rms;

public class Crms {
// region: Rms Codingfield Name Constant Bindings

    // region Meta Codingfield Definitions

    // "meta" codingfields for storing record level information in a field map.
    public static final String KEY_ID_RMSRECORDS = "[idrmsrecords]";
    public static final String KEY_ID_CODINGDATA = "[idcodingdata]";
    public static final String KEY_OBJECT_TYPE = "[objecttype]";
    public static final String KEY_OBJECT_ID = "[objectid]";

    // endregion Meta Codingfield Definitions

    // region common codingfields
    public static final String BARCODE = "BarCode";
    public static final String MASTER_BARCODE = "Master Barcode";
    public static final String RMS_CODING_TIMESTAMP = "RMS Coding Timestamp";
    public static final String RMS_EFILE_TIMESTAMP = "RMS Efile Timestamp";
    public static final String RMS_TIMESTAMP = "RMS Timestamp";
    public static final String CREATION_DATE = "Creation Date";
    public static final String CREATION_DATETIME = "Creation Datetime";
    public static final String CREATION_TIME = "Creation Time";
    public static final String ORGANIZATION_NAME = "Organization Name";
    public static final String ORGANIZATION_NUMBER = "Organization Number";
    public static final String RECORDID = "RecordId";
    public static final String OWNER_RECORDID = "OwnerRecordId";

    // end region common codingfields

    // region Truck DVIR Detail
    public static final String ABOVE_DEFECTS_CORRECTED = "Above Defects Corrected";
    public static final String ABOVE_DEFECTS_NO_CORRECTIONS_NEEDED = "Above Defects No Corrections Needed";
    public static final String ADDRESS = "Address";
    public static final String AIR_COMPRESSOR = "Air Compressor";
    public static final String AIR_LINES = "Air Lines";
    public static final String BATTERY = "Battery";
    public static final String BRAKES = "Brakes";
    public static final String BRAKE_ACCESSORIES = "Brake Accessories";
    public static final String CARBURETOR = "Carburetor";
    public static final String LOGISTICS_CARRIER = "Logistics Carrier";
    public static final String CLUTCH = "Clutch";
    public static final String CONDITION_VEHICLE_SATISFACTORY = "Condition Vehicle Satisfactory";
    public static final String DATE = "Date";
    public static final String DATETIME = "DateTime";
    public static final String DEFROSTER = "Defroster";
    public static final String DRIVER_RECORDID = "DriverRecordId";
    public static final String DRIVERS_SIGNATURE_NO_CORRECTIONS_NEEDED = "Drivers Signature No Corrections Needed";
    public static final String DRIVERS_SIGNATURE_NO_CORRECTIONS_NEEDED_DATE = "Drivers Signature No Corrections Needed Date";
    public static final String DRIVERS_SIGNATURE_VEHICLE_SATISFACTORY = "Drivers Signature Vehicle Satisfactory";
    public static final String DRIVER_FIRST_NAME = "Driver First Name";
    public static final String DRIVER_LAST_NAME = "Driver Last Name";
    public static final String DRIVE_LINE = "Drive Line";
    public static final String FIFTH_WHEEL = "Fifth Wheel";
    public static final String FRONT_AXLE = "Front Axle";
    public static final String FUEL_TANKS = "Fuel Tanks";
    public static final String HEATER = "Heater";
    public static final String HORN = "Horn";
    public static final String INSURANCE = "Insurance";
    public static final String LIGHTS = "Lights";
    public static final String LOCATION = "Location";
    public static final String MECHANICS_SIGNATURE = "Mechanics Signature";
    public static final String MECHANICS_SIGNATURE_DATE = "Mechanics Signature Date";
    public static final String MECHANIC_FIRST_NAME = "Mechanic First Name";
    public static final String MECHANIC_LAST_NAME = "Mechanic Last Name";
    public static final String MECHANIC_RECORDID = "Mechanic RecordId";
    public static final String MIRRORS = "Mirrors";
    public static final String MOBILERECORDID = "MobileRecordId";
    public static final String ODOMETER = "Odometer";
    public static final String OIL_PRESSURE = "Oil Pressure";
    public static final String ON_BOARD_RECORDER = "On Board Recorder";
    public static final String OTHER = "Other";
    public static final String RADIATOR = "Radiator";
    public static final String REAR_END = "Rear End";
    public static final String REFLECTORS = "Reflectors";
    public static final String REGISTRATION = "Registration";
    public static final String REMARKS = "Remarks";
    public static final String SAFETY_EQUIPMENT = "Safety Equipment";
    public static final String SPRINGS = "Springs";
    public static final String STARTER = "Starter";
    public static final String STEERING = "Steering";
    public static final String TACHOGRAPH = "Tachograph";
    public static final String TIME = "Time";
    public static final String TIRES = "Tires";
    public static final String TRAILER1_BRAKES = "Trailer1 Brakes";
    public static final String TRAILER1_BRAKE_CONNECTIONS = "Trailer1 Brake Connections";
    public static final String TRAILER1_COUPLING_CHAINS = "Trailer1 Coupling Chains";
    public static final String TRAILER1_COUPLING_KING_PIN = "Trailer1 Coupling (King) Pin";
    public static final String TRAILER1_DOORS = "Trailer1 Doors";
    public static final String TRAILER1_HITCH = "Trailer1 Hitch";
    public static final String TRAILER1_LANDING_GEAR = "Trailer1 Landing Gear";
    public static final String TRAILER1_LIGHTS_ALL = "Trailer1 Lights - All";
    public static final String TRAILER1_NUMBER = "Trailer1 Number";
    public static final String TRAILER1_OTHER = "Trailer1 Other";
    public static final String TRAILER1_REEFER_HOS = "Trailer1 Reefer HOS";
    public static final String TRAILER1_ROOF = "Trailer1 Roof";
    public static final String TRAILER1_SPRINGS = "Trailer1 Springs";
    public static final String TRAILER1_TARPAULIN = "Trailer1 Tarpaulin";
    public static final String TRAILER1_TIRES = "Trailer1 Tires";
    public static final String TRAILER1_WHEELS = "Trailer1 Wheels";
    public static final String TRAILER2_BRAKES = "Trailer2 Brakes";
    public static final String TRAILER2_BRAKE_CONNECTIONS = "Trailer2 Brake Connections";
    public static final String TRAILER2_COUPLING_CHAINS = "Trailer2 Coupling Chains";
    public static final String TRAILER2_COUPLING_KING_PIN = "Trailer2 Coupling (King) Pin";
    public static final String TRAILER2_DOORS = "Trailer2 Doors";
    public static final String TRAILER2_HITCH = "Trailer2 Hitch";
    public static final String TRAILER2_LANDING_GEAR = "Trailer2 Landing Gear";
    public static final String TRAILER2_LIGHTS_ALL = "Trailer2 Lights - All";
    public static final String TRAILER2_NUMBER = "Trailer2 Number";
    public static final String TRAILER2_OTHER = "Trailer2 Other";
    public static final String TRAILER2_REEFER_HOS = "Trailer2 Reefer HOS";
    public static final String TRAILER2_ROOF = "Trailer2 Roof";
    public static final String TRAILER2_SPRINGS = "Trailer2 Springs";
    public static final String TRAILER2_TARPAULIN = "Trailer2 Tarpaulin";
    public static final String TRAILER2_TIRES = "Trailer2 Tires";
    public static final String TRAILER2_WHEELS = "Trailer2 Wheels";
    public static final String TRANSMISSION = "Transmission";
    public static final String TRUCK_NUMBER = "Truck Number";
    public static final String VEHICLE_LICENSE_NUMBER = "Vehicle License Number";
    public static final String WHEELS = "Wheels";
    public static final String WINDOWS = "Windows";
    public static final String WINDSHIELD_WIPERS = "Windshield Wipers";

    // endregion Truck DVIR Detail and common codingfields

    // region Signature

    public static final String DESCRIPTION = "Description";
    public static final String DOCUMENT_DATE = "Document Date";
    public static final String DOCUMENT_TITLE = "Document Title";
    public static final String DOCUMENT_TYPE = "Document Type";
    public static final String EXPIRATION_DATE = "Expiration Date";
    public static final String ISSUING_AUTHORITY = "Issuing Authority";
    public static final String ITEMTYPE = "ItemType";
    public static final String PARENT_OBJECTID = "ParentObjectId";
    public static final String PARENT_OBJECTTYPE = "ParentObjectType";
    public static final String REVIEWED_BY = "Reviewed By";
    public static final String REVIEWED_DATE = "Reviewed Date";
    public static final String SIGNATURE_DATE = "Signature Date";
    public static final String SIGNATURE_NAME = "Signature Name";

    // endregion Signature

    // region Fuel Receipt
//    Nov 24, 2022  -   + Toll Receipt

    public static final String COMPANY = "Company";
    public static final String DAY = "Day";
    public static final String DOT_NUMBER = "DOT Number";
    public static final String DRIVER_LICENSE_NUMBER = "Driver License Number";
    public static final String FIRST_NAME = "First Name";
    public static final String FUEL_CODE = "Fuel Code";
    public static final String FUEL_TYPE = "Fuel Type";
    public static final String HOUR = "Hour";
    public static final String LAST_NAME = "Last Name";
    public static final String MINUTES = "Minutes";
    public static final String MONTH = "Month";
    public static final String NUMBER_OF_GALLONS_PURCHASED = "Number of Gallons Purchased";
    public static final String PRICE_PER_GALLON = "Price per Gallon";
    public static final String PURCHASERS_NAME = "Purchasers Name";
    public static final String QUARTER = "Quarter";
    public static final String REFUND = "Refund";
    public static final String SALES_TAX = "Sales Tax";
    public static final String TOTAL_AMOUNT_OF_SALE_IN_USD = "Total Amount of Sale in USD";
    public static final String VENDOR_ADDRESS = "Vendor Address";
    public static final String VENDOR_CITY = "Vendor City";
    public static final String VENDOR_COUNTRY = "Vendor Country";
    public static final String VENDOR_NAME = "Vendor Name";
    public static final String VENDOR_STATE = "Vendor State";
    public static final String YEAR = "Year";
//    Aug 02, 2022  -   Added a new record as recommended by Roy
    public static final String USER_RECORD_ID = "UserRecordId";
    public static final String TOTAL_AMOUNT = "Amount";
    public static final String TOLL_ROAD_NAME = "Road Name";


    // endregion Fuel Receipt

    // region IFTA Event
    public static final String COUNTRY = "Country";
    public static final String EMPLOYEE_ID = "Employee Id";
    public static final String IFTA_YES_OR_NO = "IFTA Yes or No";
    public static final String JURISDICTION_ID = "Jurisdiction ID";
    public static final String LONGITUDE = "Longitude";
    public static final String LATITUDE = "Latitude";
    public static final String MILES = "Miles";
    public static final String MOBILETIMESTAMP = "MobileTimestamp";
    public static final String ODOMETER_START = "Odometer Start";
    public static final String STATE = "State";
    public static final String STATUS = "Status";
    public static final String TRIP_PERMIT_YES_OR_NO = "Trip Permit Yes or No";
    public static final String IFTA_TAX_EXEMPT_ROAD_YES_OR_NO = "IFTA Tax Exempt Road Yes or No";


    // endregion IFTA Event

    // endregion: Rms Codingfield Name Constant Bindings

    // region: Rms Record Type Constant Bindings

//    public static final String MAP_ASSET = "Map Asset";
//    public static final String REST_AREA = "Rest Area";
    public static final String R_TRUCK_DVIR_DETAIL = "Truck DVIR Detail";
    public static final String R_TRUCK_DVIR_HEADER = "Truck DVIR Header";
    public static final String R_SIGNATURE = "Signature";
    public static final String R_FUEL_RECEIPT = "Fuel Receipt";
    public static final String R_TOLL_RECEIPT = "Toll Receipt";
    public static final String R_IFTA_EVENT = "IFTA Event";
//    public static final String USER = "User";

    // endregion: Rms Record Type Constant Bindings

    // region: Rms Datatypes

    public static final String D_BOOLEAN = "Boolean";
    public static final String D_DATE= "Date";
    public static final String D_STRING = "String";
    public static final String D_DECIMAL = "Decimal";
    public static final String D_NUMERIC = "Numeric";
    public static final String D_SIGNATURE = "Signature";
    public static final String D_LIST = "List";
    public static final String D_GROUP = "Group";
    public static final String D_HOLD = "Hold";
    public static final String D_RETENTION = "Retention";
    public static final String D_RETENTION_FORMULA = "RetentionFormula";
    public static final String D_TRIGGER = "Trigger";
    public static final String D_LINK = "Link";
    public static final String D_WORKFLOW = "Workflow";
    public static final String D_AUTO = "NumberVal";
    public static final String D_DATETIME = "DateTime";
//    public static final String D_IMAGE = "Image";

    // endregion: Rms Datatypes

    // region: CodingData table columns

    public static final String COL_STRING = "String";
    public static final String COL_NUMBERVAL = "NumberVal";
    public static final String COL_SIGNATURE = "Signature";

    // endregion: CodingData table columns


    // region Codingfield values

    public static final String V_DRIVER_SIGNATURE = "DriverSignature";
    public static final String V_MECHANIC_SIGNATURE = "MechanicSignature";
    public static final String V_FUEL_RECEIPT = "FuelReceipt";

    // region Codingfield values

    // region Setting constants

    public static final String SETTING_MAX_RMS_TIMESTAMP = "maxtimestampdvir";
    public static final String SETTING_MAX_IFTA_EVENT_TIMESTAMP = "maxtimestampiftaevent";

    // endregion Setting constants

    // region ItemType constants

    public static final String ITEMTYPE_TRUCK_DVIR_DETAIL_SIGNATURE = "Truck DVIR Detail Signature";
    // endregion ItemType constants

    // region REST call codes

    public static final String REST_CSV_OPERATION_DELETE = "D";
    public static final String REST_CSV_OPERATION_INSERT = "I";
    public static final String REST_CSV_OPERATION_UPDATE = "U";
    public static final String REST_CSV_OPERATION_NOP = "N";

    public static final String REST_RESPONSE_MEMBER_OPERATION = "operation";
    public static final String REST_RESPONSE_MEMBER_ERROR_CODE = "errorCode";
    public static final String REST_RESPONSE_MEMBER_ERROR_MESSAGE = "errorMessage";

    public static final String REST_RESPONSE_ERROR_CODE_SUCCESS = "SUCCESS";
    public static final String REST_RESPONSE_ERROR_CODE_RECORD_NOT_FOUND = "RECORD_NOT_FOUND";

    // endregion REST CSV codes

    // region Forms

    public static final String FORM_Medical_Form_Signature = "Driver Medical Form";
    public static final String FORM_Driver_Skill_Performance_Evaluation_Form_Signature = "Driver Skill Performance Evaluation";
    public static final String FORM_Driver_Employment_Record_Form_Signature = "Driver Employment Record";
    public static final String FORM_Driver_Application_Header_Signature = "Driver Application Header";
    public static final String FORM_Driver_Violations_Header_Signature = "Driver Violations Header";
    // endregion Forms

}
