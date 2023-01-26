package com.rco.rcotrucks.model;

import java.io.Serializable;

public class PretripModel implements Serializable {

    String id = "", objectId = "", objectType = "", mobileRecordId = "", functionalGroupName = "", organizationName = "", organizationNumber = "",
            dateTime = "", latitude = "", longitude = "", firstName = "", lastName = "", recordId = "", vehicleLicenseNumber = "",
            airCompressor = "", airLines = "", battery = "", brakeAccessories = "", brakes = "", carburetor = "", clutch = "", defroster = "",
            driveLine = "", fifthWheel = "", frontalAxle = "", fuelTanks = "", heater = "", horn = "", lights = "", mirrors = "", oilPressure = "",
            onBoardRecorder = "", radiator = "", rearEnd = "", reflectors = "", safetyEquipment = "", springs = "", starter = "", steering = "",
            tachograph = "", tires = "", transmission = "", wheels = "", windows = "", windShieldWipers = "", others = "", trailer1 = "",
            trailer1BreakConnections = "", trailer1Breaks = "", trailer1CouplingPin = "", trailer1CouplingChains = "", trailer1Doors = "",
            trailer1Hitch = "", trailer1LandingGear = "", trailer1LightsAll = "", trailer1Roof = "", trailer1Springs = "", trailer1Tarpaulin = "",
            trailer1Tires = "", trailer1Wheels = "", trailer1Others = "", trailer2 = "", trailer2BreakConnections = "", trailer2Breaks = "",
            trailer2CouplingPin = "", trailer2CouplingChains = "", trailer2Doors = "", trailer2Hitch = "", trailer2LandingGear = "",
            trailer2LightsAll = "", trailer2Roof = "", trailer2Springs = "", trailer2Tarpaulin = "", trailer2Tires = "", trailer2Wheels = "",
            trailer2Others = "", remarks = "", conditionVehicleIsSatisfactory = "", driversSignatureVehicleSatisfactory = "", aboveDefectsCorrected = "",
            aboveDefectsNoCorrectionNeeded = "", mechanicsSignatureDate = "", driversSignatureNoCorrectionNeeded = "",
            driversSignatureNoCorrectionNeededDate = "", truckNumber = "", carrier = "", address = "", odometer = "", mechanicFirstName = "",
            mechanicLastName = "", mechanicRecordId = "", trailer1ReeferHOS = "", trailer2ReeferHOS = "",
            registration = "", insurance = "", rmsTimestamp = "", sent = "", mechanicNameAndSignature = "", driverNameAndSignature = "",
            mechanicSignatureBitmap="", driverSignatureBitmap="", driverSignatureDate="";
    Boolean isSelected = false;
    long dateTimeInTimeStamp;


    public PretripModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getMobileRecordId() {
        return mobileRecordId;
    }

    public void setMobileRecordId(String mobileRecordId) {
        this.mobileRecordId = mobileRecordId;
    }

    public String getFunctionalGroupName() {
        return functionalGroupName;
    }

    public void setFunctionalGroupName(String functionalGroupName) {
        this.functionalGroupName = functionalGroupName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationNumber() {
        return organizationNumber;
    }

    public void setOrganizationNumber(String organizationNumber) {
        this.organizationNumber = organizationNumber;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getVehicleLicenseNumber() {
        return vehicleLicenseNumber;
    }

    public void setVehicleLicenseNumber(String vehicleLicenseNumber) {
        this.vehicleLicenseNumber = vehicleLicenseNumber;
    }

    public String getAirCompressor() {
        return airCompressor;
    }

    public void setAirCompressor(String airCompressor) {
        this.airCompressor = airCompressor;
    }

    public String getAirLines() {
        return airLines;
    }

    public void setAirLines(String airLines) {
        this.airLines = airLines;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getBrakeAccessories() {
        return brakeAccessories;
    }

    public void setBrakeAccessories(String brakeAccessories) {
        this.brakeAccessories = brakeAccessories;
    }

    public String getBrakes() {
        return brakes;
    }

    public void setBrakes(String brakes) {
        this.brakes = brakes;
    }

    public String getCarburetor() {
        return carburetor;
    }

    public void setCarburetor(String carburetor) {
        this.carburetor = carburetor;
    }

    public String getClutch() {
        return clutch;
    }

    public void setClutch(String clutch) {
        this.clutch = clutch;
    }

    public String getDefroster() {
        return defroster;
    }

    public void setDefroster(String defroster) {
        this.defroster = defroster;
    }

    public String getDriveLine() {
        return driveLine;
    }

    public void setDriveLine(String driveLine) {
        this.driveLine = driveLine;
    }

    public String getFifthWheel() {
        return fifthWheel;
    }

    public void setFifthWheel(String fifthWheel) {
        this.fifthWheel = fifthWheel;
    }

    public String getFrontalAxle() {
        return frontalAxle;
    }

    public void setFrontalAxle(String frontalAxle) {
        this.frontalAxle = frontalAxle;
    }

    public String getFuelTanks() {
        return fuelTanks;
    }

    public void setFuelTanks(String fuelTanks) {
        this.fuelTanks = fuelTanks;
    }

    public String getHeater() {
        return heater;
    }

    public void setHeater(String heater) {
        this.heater = heater;
    }

    public String getHorn() {
        return horn;
    }

    public void setHorn(String horn) {
        this.horn = horn;
    }

    public String getLights() {
        return lights;
    }

    public void setLights(String lights) {
        this.lights = lights;
    }

    public String getMirrors() {
        return mirrors;
    }

    public void setMirrors(String mirrors) {
        this.mirrors = mirrors;
    }

    public String getOilPressure() {
        return oilPressure;
    }

    public void setOilPressure(String oilPressure) {
        this.oilPressure = oilPressure;
    }

    public String getOnBoardRecorder() {
        return onBoardRecorder;
    }

    public void setOnBoardRecorder(String onBoardRecorder) {
        this.onBoardRecorder = onBoardRecorder;
    }

    public String getRadiator() {
        return radiator;
    }

    public void setRadiator(String radiator) {
        this.radiator = radiator;
    }

    public String getRearEnd() {
        return rearEnd;
    }

    public void setRearEnd(String rearEnd) {
        this.rearEnd = rearEnd;
    }

    public String getReflectors() {
        return reflectors;
    }

    public void setReflectors(String reflectors) {
        this.reflectors = reflectors;
    }

    public String getSafetyEquipment() {
        return safetyEquipment;
    }

    public void setSafetyEquipment(String safetyEquipment) {
        this.safetyEquipment = safetyEquipment;
    }

    public String getSprings() {
        return springs;
    }

    public void setSprings(String springs) {
        this.springs = springs;
    }

    public String getStarter() {
        return starter;
    }

    public void setStarter(String starter) {
        this.starter = starter;
    }

    public String getSteering() {
        return steering;
    }

    public void setSteering(String steering) {
        this.steering = steering;
    }

    public String getTachograph() {
        return tachograph;
    }

    public void setTachograph(String tachograph) {
        this.tachograph = tachograph;
    }

    public String getTires() {
        return tires;
    }

    public void setTires(String tires) {
        this.tires = tires;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public String getWheels() {
        return wheels;
    }

    public void setWheels(String wheels) {
        this.wheels = wheels;
    }

    public String getWindows() {
        return windows;
    }

    public void setWindows(String windows) {
        this.windows = windows;
    }

    public String getWindShieldWipers() {
        return windShieldWipers;
    }

    public void setWindShieldWipers(String windShieldWipers) {
        this.windShieldWipers = windShieldWipers;
    }

    public String getOthers() {
        return others;
    }

    public void setOthers(String others) {
        this.others = others;
    }

    public String getTrailer1() {
        return trailer1;
    }

    public void setTrailer1(String trailer1) {
        this.trailer1 = trailer1;
    }

    public String getTrailer1BreakConnections() {
        return trailer1BreakConnections;
    }

    public void setTrailer1BreakConnections(String trailer1BreakConnections) {
        this.trailer1BreakConnections = trailer1BreakConnections;
    }

    public String getTrailer1Breaks() {
        return trailer1Breaks;
    }

    public void setTrailer1Breaks(String trailer1Breaks) {
        this.trailer1Breaks = trailer1Breaks;
    }

    public String getTrailer1CouplingPin() {
        return trailer1CouplingPin;
    }

    public void setTrailer1CouplingPin(String trailer1CouplingPin) {
        this.trailer1CouplingPin = trailer1CouplingPin;
    }

    public String getTrailer1CouplingChains() {
        return trailer1CouplingChains;
    }

    public void setTrailer1CouplingChains(String trailer1CouplingChains) {
        this.trailer1CouplingChains = trailer1CouplingChains;
    }

    public String getTrailer1Doors() {
        return trailer1Doors;
    }

    public void setTrailer1Doors(String trailer1Doors) {
        this.trailer1Doors = trailer1Doors;
    }

    public String getTrailer1Hitch() {
        return trailer1Hitch;
    }

    public void setTrailer1Hitch(String trailer1Hitch) {
        this.trailer1Hitch = trailer1Hitch;
    }

    public String getTrailer1LandingGear() {
        return trailer1LandingGear;
    }

    public void setTrailer1LandingGear(String trailer1LandingGear) {
        this.trailer1LandingGear = trailer1LandingGear;
    }

    public String getTrailer1LightsAll() {
        return trailer1LightsAll;
    }

    public void setTrailer1LightsAll(String trailer1LightsAll) {
        this.trailer1LightsAll = trailer1LightsAll;
    }

    public String getTrailer1Roof() {
        return trailer1Roof;
    }

    public void setTrailer1Roof(String trailer1Roof) {
        this.trailer1Roof = trailer1Roof;
    }

    public String getTrailer1Springs() {
        return trailer1Springs;
    }

    public void setTrailer1Springs(String trailer1Springs) {
        this.trailer1Springs = trailer1Springs;
    }

    public String getTrailer1Tarpaulin() {
        return trailer1Tarpaulin;
    }

    public void setTrailer1Tarpaulin(String trailer1Tarpaulin) {
        this.trailer1Tarpaulin = trailer1Tarpaulin;
    }

    public String getTrailer1Tires() {
        return trailer1Tires;
    }

    public void setTrailer1Tires(String trailer1Tires) {
        this.trailer1Tires = trailer1Tires;
    }

    public String getTrailer1Wheels() {
        return trailer1Wheels;
    }

    public void setTrailer1Wheels(String trailer1Wheels) {
        this.trailer1Wheels = trailer1Wheels;
    }

    public String getTrailer1Others() {
        return trailer1Others;
    }

    public void setTrailer1Others(String trailer1Others) {
        this.trailer1Others = trailer1Others;
    }

    public String getTrailer2() {
        return trailer2;
    }

    public void setTrailer2(String trailer2) {
        this.trailer2 = trailer2;
    }

    public String getTrailer2BreakConnections() {
        return trailer2BreakConnections;
    }

    public void setTrailer2BreakConnections(String trailer2BreakConnections) {
        this.trailer2BreakConnections = trailer2BreakConnections;
    }

    public String getTrailer2Breaks() {
        return trailer2Breaks;
    }

    public void setTrailer2Breaks(String trailer2Breaks) {
        this.trailer2Breaks = trailer2Breaks;
    }

    public String getTrailer2CouplingPin() {
        return trailer2CouplingPin;
    }

    public void setTrailer2CouplingPin(String trailer2CouplingPin) {
        this.trailer2CouplingPin = trailer2CouplingPin;
    }

    public String getTrailer2CouplingChains() {
        return trailer2CouplingChains;
    }

    public void setTrailer2CouplingChains(String trailer2CouplingChains) {
        this.trailer2CouplingChains = trailer2CouplingChains;
    }

    public String getTrailer2Doors() {
        return trailer2Doors;
    }

    public void setTrailer2Doors(String trailer2Doors) {
        this.trailer2Doors = trailer2Doors;
    }

    public String getTrailer2Hitch() {
        return trailer2Hitch;
    }

    public void setTrailer2Hitch(String trailer2Hitch) {
        this.trailer2Hitch = trailer2Hitch;
    }

    public String getTrailer2LandingGear() {
        return trailer2LandingGear;
    }

    public void setTrailer2LandingGear(String trailer2LandingGear) {
        this.trailer2LandingGear = trailer2LandingGear;
    }

    public String getTrailer2LightsAll() {
        return trailer2LightsAll;
    }

    public void setTrailer2LightsAll(String trailer2LightsAll) {
        this.trailer2LightsAll = trailer2LightsAll;
    }

    public String getTrailer2Roof() {
        return trailer2Roof;
    }

    public void setTrailer2Roof(String trailer2Roof) {
        this.trailer2Roof = trailer2Roof;
    }

    public String getTrailer2Springs() {
        return trailer2Springs;
    }

    public void setTrailer2Springs(String trailer2Springs) {
        this.trailer2Springs = trailer2Springs;
    }

    public String getTrailer2Tarpaulin() {
        return trailer2Tarpaulin;
    }

    public void setTrailer2Tarpaulin(String trailer2Tarpaulin) {
        this.trailer2Tarpaulin = trailer2Tarpaulin;
    }

    public String getTrailer2Tires() {
        return trailer2Tires;
    }

    public void setTrailer2Tires(String trailer2Tires) {
        this.trailer2Tires = trailer2Tires;
    }

    public String getTrailer2Wheels() {
        return trailer2Wheels;
    }

    public void setTrailer2Wheels(String trailer2Wheels) {
        this.trailer2Wheels = trailer2Wheels;
    }

    public String getTrailer2Others() {
        return trailer2Others;
    }

    public void setTrailer2Others(String trailer2Others) {
        this.trailer2Others = trailer2Others;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getConditionVehicleIsSatisfactory() {
        return conditionVehicleIsSatisfactory;
    }

    public void setConditionVehicleIsSatisfactory(String conditionVehicleIsSatisfactory) {
        this.conditionVehicleIsSatisfactory = conditionVehicleIsSatisfactory;
    }

    public String getDriversSignatureVehicleSatisfactory() {
        return driversSignatureVehicleSatisfactory;
    }

    public void setDriversSignatureVehicleSatisfactory(String driversSignatureVehicleSatisfactory) {
        this.driversSignatureVehicleSatisfactory = driversSignatureVehicleSatisfactory;
    }

    public String getAboveDefectsCorrected() {
        return aboveDefectsCorrected;
    }

    public void setAboveDefectsCorrected(String aboveDefectsCorrected) {
        this.aboveDefectsCorrected = aboveDefectsCorrected;
    }

    public String getAboveDefectsNoCorrectionNeeded() {
        return aboveDefectsNoCorrectionNeeded;
    }

    public void setAboveDefectsNoCorrectionNeeded(String aboveDefectsNoCorrectionNeeded) {
        this.aboveDefectsNoCorrectionNeeded = aboveDefectsNoCorrectionNeeded;
    }

    public String getMechanicsSignatureDate() {
        return mechanicsSignatureDate;
    }

    public void setMechanicsSignatureDate(String mechanicsSignatureDate) {
        this.mechanicsSignatureDate = mechanicsSignatureDate;
    }

    public String getDriversSignatureNoCorrectionNeeded() {
        return driversSignatureNoCorrectionNeeded;
    }

    public void setDriversSignatureNoCorrectionNeeded(String driversSignatureNoCorrectionNeeded) {
        this.driversSignatureNoCorrectionNeeded = driversSignatureNoCorrectionNeeded;
    }

    public String getDriversSignatureNoCorrectionNeededDate() {
        return driversSignatureNoCorrectionNeededDate;
    }

    public void setDriversSignatureNoCorrectionNeededDate(String driversSignatureNoCorrectionNeededDate) {
        this.driversSignatureNoCorrectionNeededDate = driversSignatureNoCorrectionNeededDate;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOdometer() {
        return odometer;
    }

    public void setOdometer(String odometer) {
        this.odometer = odometer;
    }

    public String getMechanicFirstName() {
        return mechanicFirstName;
    }

    public void setMechanicFirstName(String mechanicFirstName) {
        this.mechanicFirstName = mechanicFirstName;
    }

    public String getMechanicLastName() {
        return mechanicLastName;
    }

    public void setMechanicLastName(String mechanicLastName) {
        this.mechanicLastName = mechanicLastName;
    }

    public String getMechanicRecordId() {
        return mechanicRecordId;
    }

    public void setMechanicRecordId(String mechanicRecordId) {
        this.mechanicRecordId = mechanicRecordId;
    }

    public String getTrailer1ReeferHOS() {
        return trailer1ReeferHOS;
    }

    public void setTrailer1ReeferHOS(String trailer1ReeferHOS) {
        this.trailer1ReeferHOS = trailer1ReeferHOS;
    }

    public String getTrailer2ReeferHOS() {
        return trailer2ReeferHOS;
    }

    public void setTrailer2ReeferHOS(String trailer2ReeferHOS) {
        this.trailer2ReeferHOS = trailer2ReeferHOS;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public String getInsurance() {
        return insurance;
    }

    public void setInsurance(String insurance) {
        this.insurance = insurance;
    }

    public String getRmsTimestamp() {
        return rmsTimestamp;
    }

    public void setRmsTimestamp(String rmsTimestamp) {
        this.rmsTimestamp = rmsTimestamp;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public String getMechanicNameAndSignature() {
        return mechanicNameAndSignature;
    }

    public void setMechanicNameAndSignature(String mechanicNameAndSignature) {
        this.mechanicNameAndSignature = mechanicNameAndSignature;
    }

    public String getDriverNameAndSignature() {
        return driverNameAndSignature;
    }

    public void setDriverNameAndSignature(String driverNameAndSignature) {
        this.driverNameAndSignature = driverNameAndSignature;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public String getTruckNumber() {
        return truckNumber;
    }

    public void setTruckNumber(String truckNumber) {
        this.truckNumber = truckNumber;
    }

    public long getDateTimeInTimeStamp() {
        return dateTimeInTimeStamp;
    }

    public void setDateTimeInTimeStamp(long dateTimeInTimeStamp) {
        this.dateTimeInTimeStamp = dateTimeInTimeStamp;
    }

    public String getMechanicSignatureBitmap() {
        return mechanicSignatureBitmap;
    }

    public void setMechanicSignatureBitmap(String mechanicSignatureBitmap) {
        this.mechanicSignatureBitmap = mechanicSignatureBitmap;
    }

    public String getDriverSignatureBitmap() {
        return driverSignatureBitmap;
    }

    public void setDriverSignatureBitmap(String driverSignatureBitmap) {
        this.driverSignatureBitmap = driverSignatureBitmap;
    }

    public String getDriverSignatureDate() {
        return driverSignatureDate;
    }

    public void setDriverSignatureDate(String driverSignatureDate) {
        this.driverSignatureDate = driverSignatureDate;
    }
}
