package com.rco.rcotrucks.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;

public class SessionManagement {

    private static final String TAG = SessionManagement.class.getSimpleName();
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "RCOTrucks";
    private static final String IS_LOGIN = "IsLoggedIn";
    private static final String IS_ENABLE = "IsEnable";

    public static final String KEY_FIRST_NAME = "FirstName";
    public static final String KEY_LAST_NAME = "LastName";
    public static final String KEY_DEVICE_ID = "DeviceId";
    public static final String KEY_COMPANY = "Company";
    public static final String KEY_RMS_USER_ID = "RMSUserId";
    public static final String KEY_ITEM_TYPE = "ItemType";
    public static final String KEY_ADDRESS_ONE = "AddressOne";
    public static final String KEY_ADDRESS_TWO = "AddressTwo";
    public static final String KEY_CITY = "City";
    public static final String KEY_STATE = "State";
    public static final String KEY_ZIP_CODE = "ZipCode";
    public static final String KEY_ORG_NAME = "ORGName";
    public static final String KEY_ORG_NUMBER = "ORGNumber";
    public static final String KEY_FUNCTIONAL_GROUP_NAME = "FunctionalGroupName";
    public static final String KEY_USER_GROUP_NAME = "UserGroupName";
    public static final String KEY_ROLE = "Role";
    public static final String KEY_LOGIN = "Login";
    public static final String KEY_PASSWORD = "Password";
    public static final String KEY_TITLE = "Title";
    public static final String KEY_COUNTRY = "Country";
    public static final String KEY_EMAIL = "Email";
    public static final String KEY_TELEPHONE = "Telephone";
    public static final String KEY_LATITUDE = "Latitude";
    public static final String KEY_LONGITUDE = "Longitude";
    public static final String KEY_IS_SYNCED_UP = "IsSyncedUp";
    public static final String KEY_DRIVER_LICENSE_NUMBER = "DriversLicenseNumber";
    public static final String KEY_LAST_SELECTED_SERVER_URL = "LastSelectedServerURL";
    public static final String KEY_IS_NIGHT = "IsNight";

//    orgName='Henry Avocado',
//    orgNumber='15',
//    functionalGroupName='Truck Drivers',
//    userGroupName='',
//    role='["DriverELDEnt"]',
//    login='naeem',
//    password='naeem',
//    title='',
//    country='',
//    email='naeem@rco.com',
//    telephone='123456789',
//    latitude='0',
//    longitude='0',
//    isSyncedUp=false,
//    driversLicenseNumber='NAE48573',
//    homeURL='',
//    fax='null',
//    directoryName='null',
//    clientLogon='',
//    clientPassword='',
//    itemDiscount='null',
//    serviceDiscount='null',
//    jobType='',
//    reference='null',
//    salesRep='null',
//    driversLicenseState='CA',
//    location='',
//    customerNumber='',
//    dateOfHire='',
//    dateOfBirth='',
//    metricSystem='0',
//    truckRule='',
//    status='',
//    setCodingTimestamp='0',
//    truckCycleDay='',
//    drivingModeList=personal,
//    yard moves,
//    extras={Region Number=null, Office Number=null, Region Name=null, MobilePhone=, Office Name=null},
//    userRights=[],
//    isManager=null,
//    timecardSubmitters=null


    // Constructor
    public SessionManagement(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(String firstName, String lastName, String deviceId, String company,
                                   String rmsUserId, String itemType, String addressOne, String addressTwo,
                                   String city, String state, String zipCode, String orgName, String orgNumber,
                                   String functionalGroupName, String userGroupName, String role, String login,
                                   String password, String title, String country, String email, String telephone,
                                   String latitude, String longitude, String isSyncedUp,
                                   String driversLicenseNumber, String lastSelectedServerUrl, boolean isNight) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
        editor.putBoolean(IS_ENABLE, true);

        // Storing name in pref
        editor.putString(KEY_FIRST_NAME, firstName);
        editor.putString(KEY_LAST_NAME, lastName);
        editor.putString(KEY_DEVICE_ID, deviceId);
        editor.putString(KEY_COMPANY, company);
        editor.putString(KEY_RMS_USER_ID, rmsUserId);
        editor.putString(KEY_ITEM_TYPE, itemType);
        editor.putString(KEY_ADDRESS_ONE, addressOne);
        editor.putString(KEY_ADDRESS_TWO, addressTwo);
        editor.putString(KEY_CITY, city);
        editor.putString(KEY_STATE, state);
        editor.putString(KEY_ZIP_CODE, zipCode);
        editor.putString(KEY_ORG_NAME, orgName);
        editor.putString(KEY_ORG_NUMBER, orgNumber);
        editor.putString(KEY_FUNCTIONAL_GROUP_NAME, functionalGroupName);
        editor.putString(KEY_USER_GROUP_NAME, userGroupName);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_LOGIN, login);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_TITLE, title);
        editor.putString(KEY_COUNTRY, country);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TELEPHONE, telephone);
        editor.putString(KEY_LATITUDE, latitude);
        editor.putString(KEY_LONGITUDE, longitude);
        editor.putString(KEY_IS_SYNCED_UP, isSyncedUp);
        editor.putString(KEY_DRIVER_LICENSE_NUMBER, driversLicenseNumber);
        editor.putString(KEY_LAST_SELECTED_SERVER_URL, lastSelectedServerUrl);
        editor.putBoolean(KEY_IS_NIGHT, isNight);
        // commit changes
        editor.commit();

        Log.d(TAG, "createLoginSession: isUserLoggedIn: " + isLoggedIn());
    }


    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     */
    public boolean checkLogin() {
        // Check login status
        if (!this.isLoggedIn()) {
            return false;
        }
        return true;
    }


    /**
     * Get stored session data
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();

        user.put(KEY_FIRST_NAME, pref.getString(KEY_FIRST_NAME, null));
        user.put(KEY_LAST_NAME, pref.getString(KEY_LAST_NAME, null));
        user.put(KEY_DEVICE_ID, pref.getString(KEY_DEVICE_ID, null));
        user.put(KEY_COMPANY, pref.getString(KEY_COMPANY, null));
        user.put(KEY_RMS_USER_ID, pref.getString(KEY_RMS_USER_ID, null));
        user.put(KEY_ITEM_TYPE, pref.getString(KEY_ITEM_TYPE, null));
        user.put(KEY_ADDRESS_ONE, pref.getString(KEY_ADDRESS_ONE, null));
        ;
        user.put(KEY_ADDRESS_TWO, pref.getString(KEY_ADDRESS_TWO, null));
        user.put(KEY_CITY, pref.getString(KEY_CITY, null));
        user.put(KEY_STATE, pref.getString(KEY_STATE, null));
        user.put(KEY_ZIP_CODE, pref.getString(KEY_ZIP_CODE, null));
        user.put(KEY_ORG_NAME, pref.getString(KEY_ORG_NAME, null));
        user.put(KEY_ORG_NUMBER, pref.getString(KEY_ORG_NUMBER, null));
        user.put(KEY_FUNCTIONAL_GROUP_NAME, pref.getString(KEY_FUNCTIONAL_GROUP_NAME, null));
        user.put(KEY_USER_GROUP_NAME, pref.getString(KEY_USER_GROUP_NAME, null));
        user.put(KEY_ROLE, pref.getString(KEY_ROLE, null));
        user.put(KEY_LOGIN, pref.getString(KEY_LOGIN, null));
        user.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD, null));
        user.put(KEY_TITLE, pref.getString(KEY_TITLE, null));
        user.put(KEY_COUNTRY, pref.getString(KEY_COUNTRY, null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        user.put(KEY_TELEPHONE, pref.getString(KEY_TELEPHONE, null));
        user.put(KEY_LATITUDE, pref.getString(KEY_LATITUDE, null));
        user.put(KEY_LONGITUDE, pref.getString(KEY_LONGITUDE, null));
        user.put(KEY_IS_SYNCED_UP, pref.getString(KEY_IS_SYNCED_UP, null));
        user.put(KEY_DRIVER_LICENSE_NUMBER, pref.getString(KEY_DRIVER_LICENSE_NUMBER, null));
        user.put(KEY_LAST_SELECTED_SERVER_URL, pref.getString(KEY_LAST_SELECTED_SERVER_URL, null));
        return user;
    }


    public void logoutUser() {

        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }

    /**
     * Quick check for login
     **/
    // Get Login State
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    public String getUserFirstName() {

        return pref.getString(KEY_FIRST_NAME, null);
    }

    public String getUserLastName() {

        return pref.getString(KEY_LAST_NAME, null);
    }

    public String getUserPassword() {

        return pref.getString(KEY_PASSWORD, null);
    }

    public String getKeyDeviceId() {
        return pref.getString(KEY_DEVICE_ID, null);
    }

    public String getKeyCompany() {
        return pref.getString(KEY_DEVICE_ID, null);
    }

    public String getKeyRmsUserId() {
        return pref.getString(KEY_DEVICE_ID, null);
    }

    public String getKeyItemType() {
        return pref.getString(KEY_DEVICE_ID, null);
    }

    public String getKeyAddressOne() {
        return pref.getString(KEY_DEVICE_ID, null);
    }

    public String getKeyAddressTwo() {
        return pref.getString(KEY_DEVICE_ID, null);
    }

    public String getKeyCity() {
        return pref.getString(KEY_DEVICE_ID, null);
    }

    public String getKeyState() {
        return pref.getString(KEY_DEVICE_ID, null);
    }

    public String getKeyZipCode() {
        return pref.getString(KEY_DEVICE_ID, null);
    }

    public String getKeyOrgName() {
        return pref.getString(KEY_ORG_NAME, null);
    }

    public String getKeyOrgNumber() {
        return pref.getString(KEY_ORG_NUMBER, null);
    }

    public String getKeyFunctionalGroupName() {
        return pref.getString(KEY_FUNCTIONAL_GROUP_NAME, null);
    }

    public String getKeyUserGroupName() {
        return pref.getString(KEY_USER_GROUP_NAME, null);
    }

    public String getKeyRole() {
        return pref.getString(KEY_ROLE, null);
    }

    public String getKeyLogin() {
        return pref.getString(KEY_LOGIN, null);
    }

    public String getKeyPassword() {
        return pref.getString(KEY_PASSWORD, null);
    }

    public String getKeyTitle() {
        return pref.getString(KEY_TITLE, null);
    }

    public String getKeyCountry() {
        return pref.getString(KEY_COUNTRY, null);
    }

    public String getKeyEmail() {
        return pref.getString(KEY_EMAIL, null);
    }

    public String getKeyTelephone() {
        return pref.getString(KEY_TELEPHONE, null);
    }

    public String getKeyLatitude() {
        return pref.getString(KEY_LATITUDE, null);
    }

    public String getKeyLongitude() {
        return pref.getString(KEY_LONGITUDE, null);
    }

    public String getKeyIsSyncedUp() {
        return pref.getString(KEY_DEVICE_ID, null);
    }

    public String getKeyDriverLicenseNumber() {
        return pref.getString(KEY_DRIVER_LICENSE_NUMBER, null);
    }

    public String getKeyLastSelectedServerUrl() {
        return pref.getString(KEY_LAST_SELECTED_SERVER_URL, null);
    }

    public boolean getKeyIsNight() {
        return pref.getBoolean(KEY_IS_NIGHT, false);
    }


    public void setKeyFirstName(String firstName) {
        editor.putString(KEY_FIRST_NAME, firstName);
        editor.commit();
    }

    public void setKeyLastName(String lastName) {
        editor.putString(KEY_LAST_NAME, lastName);
        editor.commit();
    }

    public void setKeyDeviceId(String deviceId) {
        editor.putString(KEY_DEVICE_ID, deviceId);
        editor.commit();
    }

    public void setKeyCompany(String company) {
        editor.putString(KEY_COMPANY, company);
        editor.commit();
    }

    public void setKeyRmsUserId(String rmsUserId) {
        editor.putString(KEY_COMPANY, rmsUserId);
        editor.commit();
    }

    public void setKeyItemType(String itemType) {
        editor.putString(KEY_ITEM_TYPE, itemType);
        editor.commit();
    }

    public void setKeyAddressOne(String addressOne) {
        editor.putString(KEY_ADDRESS_ONE, addressOne);
        editor.commit();
    }

    public void setKeyAddressTwo(String addressTwo) {
        editor.putString(KEY_ADDRESS_TWO, addressTwo);
        editor.commit();
    }

    public void setKeyCity(String city) {
        editor.putString(KEY_CITY, city);
        editor.commit();
    }

    public void setKeyState(String state) {
        editor.putString(KEY_STATE, state);
        editor.commit();
    }

    public void setKeyZipCode(String zipCode) {
        editor.putString(KEY_ZIP_CODE, zipCode);
        editor.commit();
    }

    public void setKeyOrgName(String orgName) {
        editor.putString(KEY_ORG_NAME, orgName);
        editor.commit();
    }

    public void setKeyOrgNumber(String orgNumber) {
        editor.putString(KEY_ORG_NUMBER, orgNumber);
        editor.commit();
    }

    public void setKeyFunctionalGroupName(String functionalGroupName) {
        editor.putString(KEY_FUNCTIONAL_GROUP_NAME, functionalGroupName);
        editor.commit();
    }

    public void setKeyUserGroupName(String userGroupName) {
        editor.putString(KEY_USER_GROUP_NAME, userGroupName);
        editor.commit();
    }

    public void setKeyRole(String role) {
        editor.putString(KEY_ROLE, role);
        editor.commit();
    }

    public void setKeyLogin(String login) {
        editor.putString(KEY_LOGIN, login);
        editor.commit();
    }

    public void setKeyPassword(String password) {
        editor.putString(KEY_PASSWORD, password);
        editor.commit();
    }

    public void setKeyTitle(String title) {
        editor.putString(KEY_TITLE, title);
        editor.commit();
    }

    public void setKeyCountry(String country) {
        editor.putString(KEY_COUNTRY, country);
        editor.commit();
    }

    public void setKeyEmail(String email) {
        editor.putString(KEY_EMAIL, email);
        editor.commit();
    }

    public void setKeyTelephone(String telephone) {
        editor.putString(KEY_TELEPHONE, telephone);
        editor.commit();
    }

    public void setKeyLatitude(String latitude) {

        editor.putString(KEY_LATITUDE, latitude);
        editor.commit();
    }

    public void setKeyLongitude(String longitude) {
        editor.putString(KEY_LONGITUDE, longitude);
        editor.commit();
    }

    public void setKeyIsSyncedUp(String isSyncedUp) {
        editor.putString(KEY_IS_SYNCED_UP, isSyncedUp);
        editor.commit();
    }

    public void setKeyDriverLicenseNumber(String driverLicenseNumber) {
        editor.putString(KEY_DRIVER_LICENSE_NUMBER, driverLicenseNumber);
        editor.commit();
    }

    public void setKeyLastSelectedServerUrl(String lastSelectedServerUrl) {
        editor.putString(KEY_LAST_SELECTED_SERVER_URL, lastSelectedServerUrl);
        editor.commit();
    }

    public void setKeyIsNight(Boolean isNight) {
        editor.putBoolean(KEY_IS_NIGHT, isNight);
        editor.commit();
    }
}
