package com.rco.rcotrucks.activities.fuelreceipts.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.Serializable;

//public class ReceiptModel implements Serializable {
//    Dec 27, 2022  -   created this mutableLiveData for selecting multiple items to delete
//public class ReceiptModel extends ViewModel implements Serializable {
public class ReceiptModel extends ViewModel implements Parcelable {

    String date = "", name = "", amount = "", icon = "";
    Boolean isSelected = false;

    private String vendorState;
    private String vendorName;
    private String fuelCode;
    private String fuelType;
    private String vendorCountry;
    private String totalAmountInUSD;
    private String totalAmount;
    private String userRecordId;
    private String numberOfGallons;
    private String sortKey;
    private String objectType;
    private String objectId;
    private String recordId;
    private boolean isValid;
    private boolean isFuelReceipt;
    //        private boolean isSent;
    private int syncStatus;
    private Long idRmsRecords;
    private String[] arMatchText = new String[5];

    int tollReceiptObjectId, tollReceiptICSVRow;
    String id = "", tollReceiptObjectType = "", tollReceiptMobileRecordId = "", tollReceipt = "",
            tollReceiptOrganizationName = "", tollReceiptCompany = "", tollReceiptVendorState = "",
            tollReceiptTruckNumber = "", tollReceiptAmount = "", tollReceiptBarCode = "", tollReceiptFirstName = "",
            tollReceiptDOTNumber = "", tollReceiptCreationTime = "", tollReceiptOrganizationNumber = "",
            tollReceiptVendorName = "", tollReceiptDateTime = "", tollReceiptObjectName = "",
            tollReceiptMonth = "", tollReceiptCreationDate = "", tollReceiptYear = "", tollReceiptUserRecordId = "", tollReceiptRMSTimestamp = "",
            tollReceiptRMSCodingTimestamp = "", tollReceiptVendorCountry = "", tollReceiptRecordId = "", tollReceiptLastName = "",
            tollReceiptVehicleLicenseNumber = "", tollReceiptDay = "", tollReceiptRoadName = "";
    long dateTimeInTimeStamp;

    String fuelReceiptMobileRecordId = "", fuelReceiptOdometer = "", fuelReceiptObjectType = "",
            fuelReceiptOrganizationName = "", fuelReceiptCompany = "", fuelReceiptState = "", fuelReceiptObjectId = "",
            fuelReceiptTruckNumber = "", fuelReceiptAmount = "", fuelReceiptFirstName = "",
            fuelReceiptOrganizationNumber = "", fuelReceiptUserRecordId = "", fuelReceiptDateTime = "", fuelTypeFuelType = "",
            fuelReceiptCountry = "", fuelReceiptRecordId = "", fuelReceiptLastName = "", fuelTypeDotNumber = "",
            fuelReceiptVehicleLicenseNumber = "", fuelReceiptGallons = "", fuelReceiptSalesTax = "",
            fuelReceiptTruckStop = "", fuelReceiptICSVRow = "", fuelReceiptBarCode = "",
            fuelReceiptDOTNumber = "", fuelReceiptCreationTime = "",
            fuelReceiptObjectName = "",
            fuelReceiptMonth = "", fuelReceiptCreationDate = "", fuelReceiptYear = "", fuelReceiptRMSTimestamp = "",
            fuelReceiptRMSCodingTimestamp = "", fuelReceiptFuelType = "", month = "", day = "", year = "";


    public ReceiptModel() {
    }

    public ReceiptModel(String date, String name, String amount, String icon) {
        this.date = date;
        this.name = name;
        this.amount = amount;
        this.icon = icon;
    }

    protected ReceiptModel(Parcel in) {
        date = in.readString();
        name = in.readString();
        amount = in.readString();
        icon = in.readString();
        byte tmpIsSelected = in.readByte();
        isSelected = tmpIsSelected == 0 ? null : tmpIsSelected == 1;
        vendorState = in.readString();
        vendorName = in.readString();
        fuelCode = in.readString();
        fuelType = in.readString();
        vendorCountry = in.readString();
        totalAmountInUSD = in.readString();
        totalAmount = in.readString();
        userRecordId = in.readString();
        numberOfGallons = in.readString();
        sortKey = in.readString();
        objectType = in.readString();
        objectId = in.readString();
        recordId = in.readString();
        isValid = in.readByte() != 0;
        isFuelReceipt = in.readByte() != 0;
        syncStatus = in.readInt();
        if (in.readByte() == 0) {
            idRmsRecords = null;
        } else {
            idRmsRecords = in.readLong();
        }
        arMatchText = in.createStringArray();
        tollReceiptObjectId = in.readInt();
        tollReceiptICSVRow = in.readInt();
        id = in.readString();
        tollReceiptObjectType = in.readString();
        tollReceiptMobileRecordId = in.readString();
        tollReceipt = in.readString();
        tollReceiptOrganizationName = in.readString();
        tollReceiptCompany = in.readString();
        tollReceiptVendorState = in.readString();
        tollReceiptTruckNumber = in.readString();
        tollReceiptAmount = in.readString();
        tollReceiptBarCode = in.readString();
        tollReceiptFirstName = in.readString();
        tollReceiptDOTNumber = in.readString();
        tollReceiptCreationTime = in.readString();
        tollReceiptOrganizationNumber = in.readString();
        tollReceiptVendorName = in.readString();
        tollReceiptDateTime = in.readString();
        tollReceiptObjectName = in.readString();
        tollReceiptMonth = in.readString();
        tollReceiptCreationDate = in.readString();
        tollReceiptYear = in.readString();
        tollReceiptUserRecordId = in.readString();
        tollReceiptRMSTimestamp = in.readString();
        tollReceiptRMSCodingTimestamp = in.readString();
        tollReceiptVendorCountry = in.readString();
        tollReceiptRecordId = in.readString();
        tollReceiptLastName = in.readString();
        tollReceiptVehicleLicenseNumber = in.readString();
        tollReceiptDay = in.readString();
        tollReceiptRoadName = in.readString();
        dateTimeInTimeStamp = in.readLong();
        fuelReceiptMobileRecordId = in.readString();
        fuelReceiptOdometer = in.readString();
        fuelReceiptObjectType = in.readString();
        fuelReceiptOrganizationName = in.readString();
        fuelReceiptCompany = in.readString();
        fuelReceiptState = in.readString();
        fuelReceiptObjectId = in.readString();
        fuelReceiptTruckNumber = in.readString();
        fuelReceiptAmount = in.readString();
        fuelReceiptFirstName = in.readString();
        fuelReceiptOrganizationNumber = in.readString();
        fuelReceiptUserRecordId = in.readString();
        fuelReceiptDateTime = in.readString();
        fuelTypeFuelType = in.readString();
        fuelReceiptCountry = in.readString();
        fuelReceiptRecordId = in.readString();
        fuelReceiptLastName = in.readString();
        fuelTypeDotNumber = in.readString();
        fuelReceiptVehicleLicenseNumber = in.readString();
        fuelReceiptGallons = in.readString();
        fuelReceiptSalesTax = in.readString();
        fuelReceiptTruckStop = in.readString();
        fuelReceiptICSVRow = in.readString();
        fuelReceiptBarCode = in.readString();
        fuelReceiptDOTNumber = in.readString();
        fuelReceiptCreationTime = in.readString();
        fuelReceiptObjectName = in.readString();
        fuelReceiptMonth = in.readString();
        fuelReceiptCreationDate = in.readString();
        fuelReceiptYear = in.readString();
        fuelReceiptRMSTimestamp = in.readString();
        fuelReceiptRMSCodingTimestamp = in.readString();
        fuelReceiptFuelType = in.readString();
        month = in.readString();
        day = in.readString();
        year = in.readString();
    }

    public static final Creator<ReceiptModel> CREATOR = new Creator<ReceiptModel>() {
        @Override
        public ReceiptModel createFromParcel(Parcel in) {
            return new ReceiptModel(in);
        }

        @Override
        public ReceiptModel[] newArray(int size) {
            return new ReceiptModel[size];
        }
    };

    public long getDateTimeInTimeStamp() {
        return dateTimeInTimeStamp;
    }

    public void setDateTimeInTimeStamp(long dateTimeInTimeStamp) {
        this.dateTimeInTimeStamp = dateTimeInTimeStamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getVendorState() {
        return vendorState;
    }

    public void setVendorState(String vendorState) {
        this.vendorState = vendorState;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getFuelCode() {
        return fuelCode;
    }

    public void setFuelCode(String fuelCode) {
        this.fuelCode = fuelCode;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getVendorCountry() {
        return vendorCountry;
    }

    public void setVendorCountry(String vendorCountry) {
        this.vendorCountry = vendorCountry;
    }

    public String getTotalAmountInUSD() {
        return totalAmountInUSD;
    }

    public void setTotalAmountInUSD(String totalAmountInUSD) {
        this.totalAmountInUSD = totalAmountInUSD;
    }

    public String getNumberOfGallons() {
        return numberOfGallons;
    }

    public void setNumberOfGallons(String numberOfGallons) {
        this.numberOfGallons = numberOfGallons;
    }

    public String getSortKey() {
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    public Long getIdRmsRecords() {
        return idRmsRecords;
    }

    public void setIdRmsRecords(Long idRmsRecords) {
        this.idRmsRecords = idRmsRecords;
    }

    public String[] getArMatchText() {
        return arMatchText;
    }

    public void setArMatchText(String[] arMatchText) {
        this.arMatchText = arMatchText;
    }

    public boolean isFuelReceipt() {
        return isFuelReceipt;
    }

    public void setFuelReceipt(boolean fuelReceipt) {
        isFuelReceipt = fuelReceipt;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getUserRecordId() {
        return userRecordId;
    }

    public void setUserRecordId(String userRecordId) {
        this.userRecordId = userRecordId;
    }

    public int getTollReceiptObjectId() {
        return tollReceiptObjectId;
    }

    public void setTollReceiptObjectId(int tollReceiptObjectId) {
        this.tollReceiptObjectId = tollReceiptObjectId;
    }

    public int getTollReceiptICSVRow() {
        return tollReceiptICSVRow;
    }

    public void setTollReceiptICSVRow(int tollReceiptICSVRow) {
        this.tollReceiptICSVRow = tollReceiptICSVRow;
    }

    public String getTollReceiptObjectType() {
        return tollReceiptObjectType;
    }

    public void setTollReceiptObjectType(String tollReceiptObjectType) {
        this.tollReceiptObjectType = tollReceiptObjectType;
    }

    public String getTollReceiptMobileRecordId() {
        return tollReceiptMobileRecordId;
    }

    public void setTollReceiptMobileRecordId(String tollReceiptMobileRecordId) {
        this.tollReceiptMobileRecordId = tollReceiptMobileRecordId;
    }

    public String getTollReceipt() {
        return tollReceipt;
    }

    public void setTollReceipt(String tollReceipt) {
        this.tollReceipt = tollReceipt;
    }

    public String getTollReceiptOrganizationName() {
        return tollReceiptOrganizationName;
    }

    public void setTollReceiptOrganizationName(String tollReceiptOrganizationName) {
        this.tollReceiptOrganizationName = tollReceiptOrganizationName;
    }

    public String getTollReceiptCompany() {
        return tollReceiptCompany;
    }

    public void setTollReceiptCompany(String tollReceiptCompany) {
        this.tollReceiptCompany = tollReceiptCompany;
    }

    public String getTollReceiptVendorState() {
        return tollReceiptVendorState;
    }

    public void setTollReceiptVendorState(String tollReceiptVendorState) {
        this.tollReceiptVendorState = tollReceiptVendorState;
    }

    public String getTollReceiptTruckNumber() {
        return tollReceiptTruckNumber;
    }

    public void setTollReceiptTruckNumber(String tollReceiptTruckNumber) {
        this.tollReceiptTruckNumber = tollReceiptTruckNumber;
    }

    public String getTollReceiptAmount() {
        return tollReceiptAmount;
    }

    public void setTollReceiptAmount(String tollReceiptAmount) {
        this.tollReceiptAmount = tollReceiptAmount;
    }

    public String getTollReceiptBarCode() {
        return tollReceiptBarCode;
    }

    public void setTollReceiptBarCode(String tollReceiptBarCode) {
        this.tollReceiptBarCode = tollReceiptBarCode;
    }

    public String getTollReceiptFirstName() {
        return tollReceiptFirstName;
    }

    public void setTollReceiptFirstName(String tollReceiptFirstName) {
        this.tollReceiptFirstName = tollReceiptFirstName;
    }

    public String getTollReceiptDOTNumber() {
        return tollReceiptDOTNumber;
    }

    public void setTollReceiptDOTNumber(String tollReceiptDOTNumber) {
        this.tollReceiptDOTNumber = tollReceiptDOTNumber;
    }

    public String getTollReceiptCreationTime() {
        return tollReceiptCreationTime;
    }

    public void setTollReceiptCreationTime(String tollReceiptCreationTime) {
        this.tollReceiptCreationTime = tollReceiptCreationTime;
    }

    public String getTollReceiptOrganizationNumber() {
        return tollReceiptOrganizationNumber;
    }

    public void setTollReceiptOrganizationNumber(String tollReceiptOrganizationNumber) {
        this.tollReceiptOrganizationNumber = tollReceiptOrganizationNumber;
    }

    public String getTollReceiptVendorName() {
        return tollReceiptVendorName;
    }

    public void setTollReceiptVendorName(String tollReceiptVendorName) {
        this.tollReceiptVendorName = tollReceiptVendorName;
    }

    public String getTollReceiptDateTime() {
        return tollReceiptDateTime;
    }

    public void setTollReceiptDateTime(String tollReceiptDateTime) {
        this.tollReceiptDateTime = tollReceiptDateTime;
    }

    public String getTollReceiptObjectName() {
        return tollReceiptObjectName;
    }

    public void setTollReceiptObjectName(String tollReceiptObjectName) {
        this.tollReceiptObjectName = tollReceiptObjectName;
    }

    public String getTollReceiptMonth() {
        return tollReceiptMonth;
    }

    public void setTollReceiptMonth(String tollReceiptMonth) {
        this.tollReceiptMonth = tollReceiptMonth;
    }

    public String getTollReceiptCreationDate() {
        return tollReceiptCreationDate;
    }

    public void setTollReceiptCreationDate(String tollReceiptCreationDate) {
        this.tollReceiptCreationDate = tollReceiptCreationDate;
    }

    public String getTollReceiptYear() {
        return tollReceiptYear;
    }

    public void setTollReceiptYear(String tollReceiptYear) {
        this.tollReceiptYear = tollReceiptYear;
    }

    public String getTollReceiptRMSTimestamp() {
        return tollReceiptRMSTimestamp;
    }

    public void setTollReceiptRMSTimestamp(String tollReceiptRMSTimestamp) {
        this.tollReceiptRMSTimestamp = tollReceiptRMSTimestamp;
    }

    public String getTollReceiptRMSCodingTimestamp() {
        return tollReceiptRMSCodingTimestamp;
    }

    public void setTollReceiptRMSCodingTimestamp(String tollReceiptRMSCodingTimestamp) {
        this.tollReceiptRMSCodingTimestamp = tollReceiptRMSCodingTimestamp;
    }

    public String getTollReceiptVendorCountry() {
        return tollReceiptVendorCountry;
    }

    public void setTollReceiptVendorCountry(String tollReceiptVendorCountry) {
        this.tollReceiptVendorCountry = tollReceiptVendorCountry;
    }

    public String getTollReceiptRecordId() {
        return tollReceiptRecordId;
    }

    public void setTollReceiptRecordId(String tollReceiptRecordId) {
        this.tollReceiptRecordId = tollReceiptRecordId;
    }

    public String getTollReceiptLastName() {
        return tollReceiptLastName;
    }

    public void setTollReceiptLastName(String tollReceiptLastName) {
        this.tollReceiptLastName = tollReceiptLastName;
    }

    public String getTollReceiptVehicleLicenseNumber() {
        return tollReceiptVehicleLicenseNumber;
    }

    public void setTollReceiptVehicleLicenseNumber(String tollReceiptVehicleLicenseNumber) {
        this.tollReceiptVehicleLicenseNumber = tollReceiptVehicleLicenseNumber;
    }

    public String getTollReceiptDay() {
        return tollReceiptDay;
    }

    public void setTollReceiptDay(String tollReceiptDay) {
        this.tollReceiptDay = tollReceiptDay;
    }

    public String getTollReceiptRoadName() {
        return tollReceiptRoadName;
    }

    public void setTollReceiptRoadName(String tollReceiptRoadName) {
        this.tollReceiptRoadName = tollReceiptRoadName;
    }

    public String getTollReceiptUserRecordId() {
        return tollReceiptUserRecordId;
    }

    public void setTollReceiptUserRecordId(String tollReceiptUserRecordId) {
        this.tollReceiptUserRecordId = tollReceiptUserRecordId;
    }

    public String getFuelReceiptMobileRecordId() {
        return fuelReceiptMobileRecordId;
    }

    public void setFuelReceiptMobileRecordId(String fuelReceiptMobileRecordId) {
        this.fuelReceiptMobileRecordId = fuelReceiptMobileRecordId;
    }

    public String getFuelReceiptOdometer() {
        return fuelReceiptOdometer;
    }

    public void setFuelReceiptOdometer(String fuelReceiptOdometer) {
        this.fuelReceiptOdometer = fuelReceiptOdometer;
    }

    public String getFuelReceiptObjectType() {
        return fuelReceiptObjectType;
    }

    public void setFuelReceiptObjectType(String fuelReceiptObjectType) {
        this.fuelReceiptObjectType = fuelReceiptObjectType;
    }

    public String getFuelReceiptOrganizationName() {
        return fuelReceiptOrganizationName;
    }

    public void setFuelReceiptOrganizationName(String fuelReceiptOrganizationName) {
        this.fuelReceiptOrganizationName = fuelReceiptOrganizationName;
    }

    public String getFuelReceiptCompany() {
        return fuelReceiptCompany;
    }

    public void setFuelReceiptCompany(String fuelReceiptCompany) {
        this.fuelReceiptCompany = fuelReceiptCompany;
    }

    public String getFuelReceiptState() {
        return fuelReceiptState;
    }

    public void setFuelReceiptState(String fuelReceiptState) {
        this.fuelReceiptState = fuelReceiptState;
    }

    public String getFuelReceiptObjectId() {
        return fuelReceiptObjectId;
    }

    public void setFuelReceiptObjectId(String fuelReceiptObjectId) {
        this.fuelReceiptObjectId = fuelReceiptObjectId;
    }

    public String getFuelReceiptTruckNumber() {
        return fuelReceiptTruckNumber;
    }

    public void setFuelReceiptTruckNumber(String fuelReceiptTruckNumber) {
        this.fuelReceiptTruckNumber = fuelReceiptTruckNumber;
    }

    public String getFuelReceiptAmount() {
        return fuelReceiptAmount;
    }

    public void setFuelReceiptAmount(String fuelReceiptAmount) {
        this.fuelReceiptAmount = fuelReceiptAmount;
    }

    public String getFuelReceiptFirstName() {
        return fuelReceiptFirstName;
    }

    public void setFuelReceiptFirstName(String fuelReceiptFirstName) {
        this.fuelReceiptFirstName = fuelReceiptFirstName;
    }

    public String getFuelReceiptOrganizationNumber() {
        return fuelReceiptOrganizationNumber;
    }

    public void setFuelReceiptOrganizationNumber(String fuelReceiptOrganizationNumber) {
        this.fuelReceiptOrganizationNumber = fuelReceiptOrganizationNumber;
    }

    public String getFuelReceiptUserRecordId() {
        return fuelReceiptUserRecordId;
    }

    public void setFuelReceiptUserRecordId(String fuelReceiptUserRecordId) {
        this.fuelReceiptUserRecordId = fuelReceiptUserRecordId;
    }

    public String getFuelReceiptDateTime() {
        return fuelReceiptDateTime;
    }

    public void setFuelReceiptDateTime(String fuelReceiptDateTime) {
        this.fuelReceiptDateTime = fuelReceiptDateTime;
    }

    public String getFuelTypeFuelType() {
        return fuelTypeFuelType;
    }

    public void setFuelTypeFuelType(String fuelTypeFuelType) {
        this.fuelTypeFuelType = fuelTypeFuelType;
    }

    public String getFuelReceiptCountry() {
        return fuelReceiptCountry;
    }

    public void setFuelReceiptCountry(String fuelReceiptCountry) {
        this.fuelReceiptCountry = fuelReceiptCountry;
    }

    public String getFuelReceiptRecordId() {
        return fuelReceiptRecordId;
    }

    public void setFuelReceiptRecordId(String fuelReceiptRecordId) {
        this.fuelReceiptRecordId = fuelReceiptRecordId;
    }

    public String getFuelReceiptLastName() {
        return fuelReceiptLastName;
    }

    public void setFuelReceiptLastName(String fuelReceiptLastName) {
        this.fuelReceiptLastName = fuelReceiptLastName;
    }

    public String getFuelTypeDotNumber() {
        return fuelTypeDotNumber;
    }

    public void setFuelTypeDotNumber(String fuelTypeDotNumber) {
        this.fuelTypeDotNumber = fuelTypeDotNumber;
    }

    public String getFuelReceiptVehicleLicenseNumber() {
        return fuelReceiptVehicleLicenseNumber;
    }

    public void setFuelReceiptVehicleLicenseNumber(String fuelReceiptVehicleLicenseNumber) {
        this.fuelReceiptVehicleLicenseNumber = fuelReceiptVehicleLicenseNumber;
    }

    public String getFuelReceiptGallons() {
        return fuelReceiptGallons;
    }

    public void setFuelReceiptGallons(String fuelReceiptGallons) {
        this.fuelReceiptGallons = fuelReceiptGallons;
    }

    public String getFuelReceiptSalesTax() {
        return fuelReceiptSalesTax;
    }

    public void setFuelReceiptSalesTax(String fuelReceiptSalesTax) {
        this.fuelReceiptSalesTax = fuelReceiptSalesTax;
    }

    public String getFuelReceiptTruckStop() {
        return fuelReceiptTruckStop;
    }

    public void setFuelReceiptTruckStop(String fuelReceiptTruckStop) {
        this.fuelReceiptTruckStop = fuelReceiptTruckStop;
    }

    public String getFuelReceiptICSVRow() {
        return fuelReceiptICSVRow;
    }

    public void setFuelReceiptICSVRow(String fuelReceiptICSVRow) {
        this.fuelReceiptICSVRow = fuelReceiptICSVRow;
    }

    public String getFuelReceiptBarCode() {
        return fuelReceiptBarCode;
    }

    public void setFuelReceiptBarCode(String fuelReceiptBarCode) {
        this.fuelReceiptBarCode = fuelReceiptBarCode;
    }

    public String getFuelReceiptDOTNumber() {
        return fuelReceiptDOTNumber;
    }

    public void setFuelReceiptDOTNumber(String fuelReceiptDOTNumber) {
        this.fuelReceiptDOTNumber = fuelReceiptDOTNumber;
    }

    public String getFuelReceiptCreationTime() {
        return fuelReceiptCreationTime;
    }

    public void setFuelReceiptCreationTime(String fuelReceiptCreationTime) {
        this.fuelReceiptCreationTime = fuelReceiptCreationTime;
    }

    public String getFuelReceiptObjectName() {
        return fuelReceiptObjectName;
    }

    public void setFuelReceiptObjectName(String fuelReceiptObjectName) {
        this.fuelReceiptObjectName = fuelReceiptObjectName;
    }

    public String getFuelReceiptMonth() {
        return fuelReceiptMonth;
    }

    public void setFuelReceiptMonth(String fuelReceiptMonth) {
        this.fuelReceiptMonth = fuelReceiptMonth;
    }

    public String getFuelReceiptCreationDate() {
        return fuelReceiptCreationDate;
    }

    public void setFuelReceiptCreationDate(String fuelReceiptCreationDate) {
        this.fuelReceiptCreationDate = fuelReceiptCreationDate;
    }

    public String getFuelReceiptYear() {
        return fuelReceiptYear;
    }

    public void setFuelReceiptYear(String fuelReceiptYear) {
        this.fuelReceiptYear = fuelReceiptYear;
    }

    public String getFuelReceiptRMSTimestamp() {
        return fuelReceiptRMSTimestamp;
    }

    public void setFuelReceiptRMSTimestamp(String fuelReceiptRMSTimestamp) {
        this.fuelReceiptRMSTimestamp = fuelReceiptRMSTimestamp;
    }

    public String getFuelReceiptRMSCodingTimestamp() {
        return fuelReceiptRMSCodingTimestamp;
    }

    public void setFuelReceiptRMSCodingTimestamp(String fuelReceiptRMSCodingTimestamp) {
        this.fuelReceiptRMSCodingTimestamp = fuelReceiptRMSCodingTimestamp;
    }

    public String getFuelReceiptFuelType() {
        return fuelReceiptFuelType;
    }

    public void setFuelReceiptFuelType(String fuelReceiptFuelType) {
        this.fuelReceiptFuelType = fuelReceiptFuelType;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Boolean isSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    //    Dec 27, 2022  -   created this mutableLiveData for selecting multiple items to delete
// initialize variables
    MutableLiveData<String> mutableLiveData = new MutableLiveData<>();

    // create set text method
    public void setText(String s) {
        mutableLiveData.setValue(s);
    }

    // create get text method
    public MutableLiveData<String> getText() {
        return mutableLiveData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeString(name);
        dest.writeString(amount);
        dest.writeString(icon);
        dest.writeByte((byte) (isSelected == null ? 0 : isSelected ? 1 : 2));
        dest.writeString(vendorState);
        dest.writeString(vendorName);
        dest.writeString(fuelCode);
        dest.writeString(fuelType);
        dest.writeString(vendorCountry);
        dest.writeString(totalAmountInUSD);
        dest.writeString(totalAmount);
        dest.writeString(userRecordId);
        dest.writeString(numberOfGallons);
        dest.writeString(sortKey);
        dest.writeString(objectType);
        dest.writeString(objectId);
        dest.writeString(recordId);
        dest.writeByte((byte) (isValid ? 1 : 0));
        dest.writeByte((byte) (isFuelReceipt ? 1 : 0));
        dest.writeInt(syncStatus);
        if (idRmsRecords == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(idRmsRecords);
        }
        dest.writeStringArray(arMatchText);
        dest.writeInt(tollReceiptObjectId);
        dest.writeInt(tollReceiptICSVRow);
        dest.writeString(id);
        dest.writeString(tollReceiptObjectType);
        dest.writeString(tollReceiptMobileRecordId);
        dest.writeString(tollReceipt);
        dest.writeString(tollReceiptOrganizationName);
        dest.writeString(tollReceiptCompany);
        dest.writeString(tollReceiptVendorState);
        dest.writeString(tollReceiptTruckNumber);
        dest.writeString(tollReceiptAmount);
        dest.writeString(tollReceiptBarCode);
        dest.writeString(tollReceiptFirstName);
        dest.writeString(tollReceiptDOTNumber);
        dest.writeString(tollReceiptCreationTime);
        dest.writeString(tollReceiptOrganizationNumber);
        dest.writeString(tollReceiptVendorName);
        dest.writeString(tollReceiptDateTime);
        dest.writeString(tollReceiptObjectName);
        dest.writeString(tollReceiptMonth);
        dest.writeString(tollReceiptCreationDate);
        dest.writeString(tollReceiptYear);
        dest.writeString(tollReceiptUserRecordId);
        dest.writeString(tollReceiptRMSTimestamp);
        dest.writeString(tollReceiptRMSCodingTimestamp);
        dest.writeString(tollReceiptVendorCountry);
        dest.writeString(tollReceiptRecordId);
        dest.writeString(tollReceiptLastName);
        dest.writeString(tollReceiptVehicleLicenseNumber);
        dest.writeString(tollReceiptDay);
        dest.writeString(tollReceiptRoadName);
        dest.writeLong(dateTimeInTimeStamp);
        dest.writeString(fuelReceiptMobileRecordId);
        dest.writeString(fuelReceiptOdometer);
        dest.writeString(fuelReceiptObjectType);
        dest.writeString(fuelReceiptOrganizationName);
        dest.writeString(fuelReceiptCompany);
        dest.writeString(fuelReceiptState);
        dest.writeString(fuelReceiptObjectId);
        dest.writeString(fuelReceiptTruckNumber);
        dest.writeString(fuelReceiptAmount);
        dest.writeString(fuelReceiptFirstName);
        dest.writeString(fuelReceiptOrganizationNumber);
        dest.writeString(fuelReceiptUserRecordId);
        dest.writeString(fuelReceiptDateTime);
        dest.writeString(fuelTypeFuelType);
        dest.writeString(fuelReceiptCountry);
        dest.writeString(fuelReceiptRecordId);
        dest.writeString(fuelReceiptLastName);
        dest.writeString(fuelTypeDotNumber);
        dest.writeString(fuelReceiptVehicleLicenseNumber);
        dest.writeString(fuelReceiptGallons);
        dest.writeString(fuelReceiptSalesTax);
        dest.writeString(fuelReceiptTruckStop);
        dest.writeString(fuelReceiptICSVRow);
        dest.writeString(fuelReceiptBarCode);
        dest.writeString(fuelReceiptDOTNumber);
        dest.writeString(fuelReceiptCreationTime);
        dest.writeString(fuelReceiptObjectName);
        dest.writeString(fuelReceiptMonth);
        dest.writeString(fuelReceiptCreationDate);
        dest.writeString(fuelReceiptYear);
        dest.writeString(fuelReceiptRMSTimestamp);
        dest.writeString(fuelReceiptRMSCodingTimestamp);
        dest.writeString(fuelReceiptFuelType);
        dest.writeString(month);
        dest.writeString(day);
        dest.writeString(year);
    }
}
