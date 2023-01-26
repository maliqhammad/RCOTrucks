package com.rco.rcotrucks.model;

import android.util.Log;

import org.json.JSONException;

public class AmenitiesModel {

    String amenitiesBusinessTransflo, amenitiesFoodRestaurants, amenitiesFuelBulkDEF, amenitiesFuelBulkPropane,
            amenitiesFuelEVLevel2, amenitiesFuelEVLevel3DCFC, amenitiesFuelEVTeslaLevel2, amenitiesFuelEVTeslaLevel3DCFC,
            amenitiesFuelFastFillCNGAuto, amenitiesFuelFastFillCNGClass8, amenitiesFuelPropaneTankExchange,
            amenitiesFuelRFIDPumpStart, amenitiesOtherATM, amenitiesOtherDogPark, amenitiesOtherLaundryFacility,
            amenitiesOtherPrivateShowers, amenitiesOtherVideoGaming, amenitiesOtherWiFiBasic, amenitiesOtherWiFiPremium,
            amenitiesParkingTruckParking, amenitiesRVRVDumpService, amenitiesRVRVFriendlyParking, amenitiesRVRVHookup,
            amenitiesServiceCommercialTruckOilChange, amenitiesServiceLightMechanical, amenitiesServiceSpeedcoOnSite,
            amenitiesServiceTireServices, amenitiesServiceTirePassInLane, amenitiesServiceTirePassInServiceCenter,
            amenitiesServiceTirePassMobile, amenitiesTruckCATScales, RecordId,
            amenitiesBusinessTransfloImage = "", amenitiesFoodRestaurantsImage = "", amenitiesFuelBulkDEFImage = "", amenitiesFuelBulkPropaneImage = "",
            amenitiesFuelEVLevel2Image = "", amenitiesFuelEVLevel3DCFCImage = "", amenitiesFuelEVTeslaLevel2Image = "", amenitiesFuelEVTeslaLevel3DCFCImage = "",
            amenitiesFuelFastFillCNGAutoImage = "", amenitiesFuelFastFillCNGClass8Image = "", amenitiesFuelPropaneTankExchangeImage = "",
            amenitiesFuelRFIDPumpStartImage = "", amenitiesOtherATMImage = "", amenitiesOtherDogParkImage = "", amenitiesOtherLaundryFacilityImage = "",
            amenitiesOtherPrivateShowersImage = "", amenitiesOtherVideoGamingImage = "", amenitiesOtherWiFiBasicImage = "", amenitiesOtherWiFiPremiumImage = "",
            amenitiesParkingTruckParkingImage = "", amenitiesRVRVDumpServiceImage = "", amenitiesRVRVFriendlyParkingImage = "", amenitiesRVRVHookupImage = "",
            amenitiesServiceCommercialTruckOilChangeImage = "", amenitiesServiceLightMechanicalImage = "", amenitiesServiceSpeedcoOnSiteImage = "",
            amenitiesServiceTireServicesImage = "", amenitiesServiceTirePassInLaneImage = "", amenitiesServiceTirePassInServiceCenterImage = "",
            amenitiesServiceTirePassMobileImage = "", amenitiesTruckCATScalesImage = "",
            transflo = "", restaurants = "", bulkDEF = "", bulkPropane = "", EVLevel2 = "", EVLevel3DCFC = "",
            EVTeslaLevel2 = "", EVTeslaLevel3DCFC = "", fastFillCNGAuto = "", fastFillCNGClass8 = "", propaneTankExchange = "",
            RFIDPumpStart = "", ATM = "", dogPark = "", laundryFacility = "", privateShowers = "", videoGaming = "",
            WiFiBasic = "", WiFiPremium = "", truckParking = "", RVDumpService = "", RVFriendlyParking = "", RVHookup = "",
            commercialTruckOilChange = "", lightMechanical = "", speedCoOnSite = "", tireServices = "",
            tirePassInLane = "", tirePassInServiceCenter = "", tirePassMobile = "", CATScales = "";


    public AmenitiesModel() {
    }

    public String getAmenitiesBusinessTransflo() {
        return amenitiesBusinessTransflo;
    }

    public void setAmenitiesBusinessTransflo(String amenitiesBusinessTransflo) {
        this.amenitiesBusinessTransflo = amenitiesBusinessTransflo;
    }

    public String getAmenitiesFoodRestaurants() {
        return amenitiesFoodRestaurants;
    }

    public void setAmenitiesFoodRestaurants(String amenitiesFoodRestaurants) {
        this.amenitiesFoodRestaurants = amenitiesFoodRestaurants;
    }

    public String getAmenitiesFuelBulkDEF() {
        return amenitiesFuelBulkDEF;
    }

    public void setAmenitiesFuelBulkDEF(String amenitiesFuelBulkDEF) {
        this.amenitiesFuelBulkDEF = amenitiesFuelBulkDEF;
    }

    public String getAmenitiesFuelBulkPropane() {
        return amenitiesFuelBulkPropane;
    }

    public void setAmenitiesFuelBulkPropane(String amenitiesFuelBulkPropane) {
        this.amenitiesFuelBulkPropane = amenitiesFuelBulkPropane;
    }

    public String getAmenitiesFuelEVLevel2() {
        return amenitiesFuelEVLevel2;
    }

    public void setAmenitiesFuelEVLevel2(String amenitiesFuelEVLevel2) {
        this.amenitiesFuelEVLevel2 = amenitiesFuelEVLevel2;
    }

    public String getAmenitiesFuelEVLevel3DCFC() {
        return amenitiesFuelEVLevel3DCFC;
    }

    public void setAmenitiesFuelEVLevel3DCFC(String amenitiesFuelEVLevel3DCFC) {
        this.amenitiesFuelEVLevel3DCFC = amenitiesFuelEVLevel3DCFC;
    }

    public String getAmenitiesFuelEVTeslaLevel2() {
        return amenitiesFuelEVTeslaLevel2;
    }

    public void setAmenitiesFuelEVTeslaLevel2(String amenitiesFuelEVTeslaLevel2) {
        this.amenitiesFuelEVTeslaLevel2 = amenitiesFuelEVTeslaLevel2;
    }

    public String getAmenitiesFuelEVTeslaLevel3DCFC() {
        return amenitiesFuelEVTeslaLevel3DCFC;
    }

    public void setAmenitiesFuelEVTeslaLevel3DCFC(String amenitiesFuelEVTeslaLevel3DCFC) {
        this.amenitiesFuelEVTeslaLevel3DCFC = amenitiesFuelEVTeslaLevel3DCFC;
    }

    public String getAmenitiesFuelFastFillCNGAuto() {
        return amenitiesFuelFastFillCNGAuto;
    }

    public void setAmenitiesFuelFastFillCNGAuto(String amenitiesFuelFastFillCNGAuto) {
        this.amenitiesFuelFastFillCNGAuto = amenitiesFuelFastFillCNGAuto;
    }

    public String getAmenitiesFuelFastFillCNGClass8() {
        return amenitiesFuelFastFillCNGClass8;
    }

    public void setAmenitiesFuelFastFillCNGClass8(String amenitiesFuelFastFillCNGClass8) {
        this.amenitiesFuelFastFillCNGClass8 = amenitiesFuelFastFillCNGClass8;
    }

    public String getAmenitiesFuelPropaneTankExchange() {
        return amenitiesFuelPropaneTankExchange;
    }

    public void setAmenitiesFuelPropaneTankExchange(String amenitiesFuelPropaneTankExchange) {
        this.amenitiesFuelPropaneTankExchange = amenitiesFuelPropaneTankExchange;
    }

    public String getAmenitiesFuelRFIDPumpStart() {
        return amenitiesFuelRFIDPumpStart;
    }

    public void setAmenitiesFuelRFIDPumpStart(String amenitiesFuelRFIDPumpStart) {
        this.amenitiesFuelRFIDPumpStart = amenitiesFuelRFIDPumpStart;
    }

    public String getAmenitiesOtherATM() {
        return amenitiesOtherATM;
    }

    public void setAmenitiesOtherATM(String amenitiesOtherATM) {
        this.amenitiesOtherATM = amenitiesOtherATM;
    }

    public String getAmenitiesOtherDogPark() {
        return amenitiesOtherDogPark;
    }

    public void setAmenitiesOtherDogPark(String amenitiesOtherDogPark) {
        this.amenitiesOtherDogPark = amenitiesOtherDogPark;
    }

    public String getAmenitiesOtherLaundryFacility() {
        return amenitiesOtherLaundryFacility;
    }

    public void setAmenitiesOtherLaundryFacility(String amenitiesOtherLaundryFacility) {
        this.amenitiesOtherLaundryFacility = amenitiesOtherLaundryFacility;
    }

    public String getAmenitiesOtherPrivateShowers() {
        return amenitiesOtherPrivateShowers;
    }

    public void setAmenitiesOtherPrivateShowers(String amenitiesOtherPrivateShowers) {
        this.amenitiesOtherPrivateShowers = amenitiesOtherPrivateShowers;
    }

    public String getAmenitiesOtherVideoGaming() {
        return amenitiesOtherVideoGaming;
    }

    public void setAmenitiesOtherVideoGaming(String amenitiesOtherVideoGaming) {
        this.amenitiesOtherVideoGaming = amenitiesOtherVideoGaming;
    }

    public String getAmenitiesOtherWiFiBasic() {
        return amenitiesOtherWiFiBasic;
    }

    public void setAmenitiesOtherWiFiBasic(String amenitiesOtherWiFiBasic) {
        this.amenitiesOtherWiFiBasic = amenitiesOtherWiFiBasic;
    }

    public String getAmenitiesOtherWiFiPremium() {
        return amenitiesOtherWiFiPremium;
    }

    public void setAmenitiesOtherWiFiPremium(String amenitiesOtherWiFiPremium) {
        this.amenitiesOtherWiFiPremium = amenitiesOtherWiFiPremium;
    }

    public String getAmenitiesParkingTruckParking() {
        return amenitiesParkingTruckParking;
    }

    public void setAmenitiesParkingTruckParking(String amenitiesParkingTruckParking) {
        this.amenitiesParkingTruckParking = amenitiesParkingTruckParking;
    }

    public String getAmenitiesRVRVDumpService() {
        return amenitiesRVRVDumpService;
    }

    public void setAmenitiesRVRVDumpService(String amenitiesRVRVDumpService) {
        this.amenitiesRVRVDumpService = amenitiesRVRVDumpService;
    }

    public String getAmenitiesRVRVFriendlyParking() {
        return amenitiesRVRVFriendlyParking;
    }

    public void setAmenitiesRVRVFriendlyParking(String amenitiesRVRVFriendlyParking) {
        this.amenitiesRVRVFriendlyParking = amenitiesRVRVFriendlyParking;
    }

    public String getAmenitiesRVRVHookup() {
        return amenitiesRVRVHookup;
    }

    public void setAmenitiesRVRVHookup(String amenitiesRVRVHookup) {
        this.amenitiesRVRVHookup = amenitiesRVRVHookup;
    }

    public String getAmenitiesServiceCommercialTruckOilChange() {
        return amenitiesServiceCommercialTruckOilChange;
    }

    public void setAmenitiesServiceCommercialTruckOilChange(String amenitiesServiceCommercialTruckOilChange) {
        this.amenitiesServiceCommercialTruckOilChange = amenitiesServiceCommercialTruckOilChange;
    }

    public String getAmenitiesServiceLightMechanical() {
        return amenitiesServiceLightMechanical;
    }

    public void setAmenitiesServiceLightMechanical(String amenitiesServiceLightMechanical) {
        this.amenitiesServiceLightMechanical = amenitiesServiceLightMechanical;
    }

    public String getAmenitiesServiceSpeedcoOnSite() {
        return amenitiesServiceSpeedcoOnSite;
    }

    public void setAmenitiesServiceSpeedcoOnSite(String amenitiesServiceSpeedcoOnSite) {
        this.amenitiesServiceSpeedcoOnSite = amenitiesServiceSpeedcoOnSite;
    }

    public String getAmenitiesServiceTireServices() {
        return amenitiesServiceTireServices;
    }

    public void setAmenitiesServiceTireServices(String amenitiesServiceTireServices) {
        this.amenitiesServiceTireServices = amenitiesServiceTireServices;
    }

    public String getAmenitiesServiceTirePassInLane() {
        return amenitiesServiceTirePassInLane;
    }

    public void setAmenitiesServiceTirePassInLane(String amenitiesServiceTirePassInLane) {
        this.amenitiesServiceTirePassInLane = amenitiesServiceTirePassInLane;
    }

    public String getAmenitiesServiceTirePassInServiceCenter() {
        return amenitiesServiceTirePassInServiceCenter;
    }

    public void setAmenitiesServiceTirePassInServiceCenter(String amenitiesServiceTirePassInServiceCenter) {
        this.amenitiesServiceTirePassInServiceCenter = amenitiesServiceTirePassInServiceCenter;
    }

    public String getAmenitiesServiceTirePassMobile() {
        return amenitiesServiceTirePassMobile;
    }

    public void setAmenitiesServiceTirePassMobile(String amenitiesServiceTirePassMobile) {
        this.amenitiesServiceTirePassMobile = amenitiesServiceTirePassMobile;
    }

    public String getAmenitiesTruckCATScales() {
        return amenitiesTruckCATScales;
    }

    public void setAmenitiesTruckCATScales(String amenitiesTruckCATScales) {
        this.amenitiesTruckCATScales = amenitiesTruckCATScales;
    }

    public String getRecordId() {
        return RecordId;
    }

    public void setRecordId(String recordId) {
        RecordId = recordId;
    }

    public String getAmenitiesBusinessTransfloImage() {
        return amenitiesBusinessTransfloImage;
    }

    public void setAmenitiesBusinessTransfloImage(String amenitiesBusinessTransfloImage) {
        this.amenitiesBusinessTransfloImage = amenitiesBusinessTransfloImage;
    }

    public String getAmenitiesFoodRestaurantsImage() {
        return amenitiesFoodRestaurantsImage;
    }

    public void setAmenitiesFoodRestaurantsImage(String amenitiesFoodRestaurantsImage) {
        this.amenitiesFoodRestaurantsImage = amenitiesFoodRestaurantsImage;
    }

    public String getAmenitiesFuelBulkDEFImage() {
        return amenitiesFuelBulkDEFImage;
    }

    public void setAmenitiesFuelBulkDEFImage(String amenitiesFuelBulkDEFImage) {
        this.amenitiesFuelBulkDEFImage = amenitiesFuelBulkDEFImage;
    }

    public String getAmenitiesFuelBulkPropaneImage() {
        return amenitiesFuelBulkPropaneImage;
    }

    public void setAmenitiesFuelBulkPropaneImage(String amenitiesFuelBulkPropaneImage) {
        this.amenitiesFuelBulkPropaneImage = amenitiesFuelBulkPropaneImage;
    }

    public String getAmenitiesFuelEVLevel2Image() {
        return amenitiesFuelEVLevel2Image;
    }

    public void setAmenitiesFuelEVLevel2Image(String amenitiesFuelEVLevel2Image) {
        this.amenitiesFuelEVLevel2Image = amenitiesFuelEVLevel2Image;
    }

    public String getAmenitiesFuelEVLevel3DCFCImage() {
        return amenitiesFuelEVLevel3DCFCImage;
    }

    public void setAmenitiesFuelEVLevel3DCFCImage(String amenitiesFuelEVLevel3DCFCImage) {
        this.amenitiesFuelEVLevel3DCFCImage = amenitiesFuelEVLevel3DCFCImage;
    }

    public String getAmenitiesFuelEVTeslaLevel2Image() {
        return amenitiesFuelEVTeslaLevel2Image;
    }

    public void setAmenitiesFuelEVTeslaLevel2Image(String amenitiesFuelEVTeslaLevel2Image) {
        this.amenitiesFuelEVTeslaLevel2Image = amenitiesFuelEVTeslaLevel2Image;
    }

    public String getAmenitiesFuelEVTeslaLevel3DCFCImage() {
        return amenitiesFuelEVTeslaLevel3DCFCImage;
    }

    public void setAmenitiesFuelEVTeslaLevel3DCFCImage(String amenitiesFuelEVTeslaLevel3DCFCImage) {
        this.amenitiesFuelEVTeslaLevel3DCFCImage = amenitiesFuelEVTeslaLevel3DCFCImage;
    }

    public String getAmenitiesFuelFastFillCNGAutoImage() {
        return amenitiesFuelFastFillCNGAutoImage;
    }

    public void setAmenitiesFuelFastFillCNGAutoImage(String amenitiesFuelFastFillCNGAutoImage) {
        this.amenitiesFuelFastFillCNGAutoImage = amenitiesFuelFastFillCNGAutoImage;
    }

    public String getAmenitiesFuelFastFillCNGClass8Image() {
        return amenitiesFuelFastFillCNGClass8Image;
    }

    public void setAmenitiesFuelFastFillCNGClass8Image(String amenitiesFuelFastFillCNGClass8Image) {
        this.amenitiesFuelFastFillCNGClass8Image = amenitiesFuelFastFillCNGClass8Image;
    }

    public String getAmenitiesFuelPropaneTankExchangeImage() {
        return amenitiesFuelPropaneTankExchangeImage;
    }

    public void setAmenitiesFuelPropaneTankExchangeImage(String amenitiesFuelPropaneTankExchangeImage) {
        this.amenitiesFuelPropaneTankExchangeImage = amenitiesFuelPropaneTankExchangeImage;
    }

    public String getAmenitiesFuelRFIDPumpStartImage() {
        return amenitiesFuelRFIDPumpStartImage;
    }

    public void setAmenitiesFuelRFIDPumpStartImage(String amenitiesFuelRFIDPumpStartImage) {
        this.amenitiesFuelRFIDPumpStartImage = amenitiesFuelRFIDPumpStartImage;
    }

    public String getAmenitiesOtherATMImage() {
        return amenitiesOtherATMImage;
    }

    public void setAmenitiesOtherATMImage(String amenitiesOtherATMImage) {
        this.amenitiesOtherATMImage = amenitiesOtherATMImage;
    }

    public String getAmenitiesOtherDogParkImage() {
        return amenitiesOtherDogParkImage;
    }

    public void setAmenitiesOtherDogParkImage(String amenitiesOtherDogParkImage) {
        this.amenitiesOtherDogParkImage = amenitiesOtherDogParkImage;
    }

    public String getAmenitiesOtherLaundryFacilityImage() {
        return amenitiesOtherLaundryFacilityImage;
    }

    public void setAmenitiesOtherLaundryFacilityImage(String amenitiesOtherLaundryFacilityImage) {
        this.amenitiesOtherLaundryFacilityImage = amenitiesOtherLaundryFacilityImage;
    }

    public String getAmenitiesOtherPrivateShowersImage() {
        return amenitiesOtherPrivateShowersImage;
    }

    public void setAmenitiesOtherPrivateShowersImage(String amenitiesOtherPrivateShowersImage) {
        this.amenitiesOtherPrivateShowersImage = amenitiesOtherPrivateShowersImage;
    }

    public String getAmenitiesOtherVideoGamingImage() {
        return amenitiesOtherVideoGamingImage;
    }

    public void setAmenitiesOtherVideoGamingImage(String amenitiesOtherVideoGamingImage) {
        this.amenitiesOtherVideoGamingImage = amenitiesOtherVideoGamingImage;
    }

    public String getAmenitiesOtherWiFiBasicImage() {
        return amenitiesOtherWiFiBasicImage;
    }

    public void setAmenitiesOtherWiFiBasicImage(String amenitiesOtherWiFiBasicImage) {
        this.amenitiesOtherWiFiBasicImage = amenitiesOtherWiFiBasicImage;
    }

    public String getAmenitiesOtherWiFiPremiumImage() {
        return amenitiesOtherWiFiPremiumImage;
    }

    public void setAmenitiesOtherWiFiPremiumImage(String amenitiesOtherWiFiPremiumImage) {
        this.amenitiesOtherWiFiPremiumImage = amenitiesOtherWiFiPremiumImage;
    }

    public String getAmenitiesParkingTruckParkingImage() {
        return amenitiesParkingTruckParkingImage;
    }

    public void setAmenitiesParkingTruckParkingImage(String amenitiesParkingTruckParkingImage) {
        this.amenitiesParkingTruckParkingImage = amenitiesParkingTruckParkingImage;
    }

    public String getAmenitiesRVRVDumpServiceImage() {
        return amenitiesRVRVDumpServiceImage;
    }

    public void setAmenitiesRVRVDumpServiceImage(String amenitiesRVRVDumpServiceImage) {
        this.amenitiesRVRVDumpServiceImage = amenitiesRVRVDumpServiceImage;
    }

    public String getAmenitiesRVRVFriendlyParkingImage() {
        return amenitiesRVRVFriendlyParkingImage;
    }

    public void setAmenitiesRVRVFriendlyParkingImage(String amenitiesRVRVFriendlyParkingImage) {
        this.amenitiesRVRVFriendlyParkingImage = amenitiesRVRVFriendlyParkingImage;
    }

    public String getAmenitiesRVRVHookupImage() {
        return amenitiesRVRVHookupImage;
    }

    public void setAmenitiesRVRVHookupImage(String amenitiesRVRVHookupImage) {
        this.amenitiesRVRVHookupImage = amenitiesRVRVHookupImage;
    }

    public String getAmenitiesServiceCommercialTruckOilChangeImage() {
        return amenitiesServiceCommercialTruckOilChangeImage;
    }

    public void setAmenitiesServiceCommercialTruckOilChangeImage(String amenitiesServiceCommercialTruckOilChangeImage) {
        this.amenitiesServiceCommercialTruckOilChangeImage = amenitiesServiceCommercialTruckOilChangeImage;
    }

    public String getAmenitiesServiceLightMechanicalImage() {
        return amenitiesServiceLightMechanicalImage;
    }

    public void setAmenitiesServiceLightMechanicalImage(String amenitiesServiceLightMechanicalImage) {
        this.amenitiesServiceLightMechanicalImage = amenitiesServiceLightMechanicalImage;
    }

    public String getAmenitiesServiceSpeedcoOnSiteImage() {
        return amenitiesServiceSpeedcoOnSiteImage;
    }

    public void setAmenitiesServiceSpeedcoOnSiteImage(String amenitiesServiceSpeedcoOnSiteImage) {
        this.amenitiesServiceSpeedcoOnSiteImage = amenitiesServiceSpeedcoOnSiteImage;
    }

    public String getAmenitiesServiceTireServicesImage() {
        return amenitiesServiceTireServicesImage;
    }

    public void setAmenitiesServiceTireServicesImage(String amenitiesServiceTireServicesImage) {
        this.amenitiesServiceTireServicesImage = amenitiesServiceTireServicesImage;
    }

    public String getAmenitiesServiceTirePassInLaneImage() {
        return amenitiesServiceTirePassInLaneImage;
    }

    public void setAmenitiesServiceTirePassInLaneImage(String amenitiesServiceTirePassInLaneImage) {
        this.amenitiesServiceTirePassInLaneImage = amenitiesServiceTirePassInLaneImage;
    }

    public String getAmenitiesServiceTirePassInServiceCenterImage() {
        return amenitiesServiceTirePassInServiceCenterImage;
    }

    public void setAmenitiesServiceTirePassInServiceCenterImage(String amenitiesServiceTirePassInServiceCenterImage) {
        this.amenitiesServiceTirePassInServiceCenterImage = amenitiesServiceTirePassInServiceCenterImage;
    }

    public String getAmenitiesServiceTirePassMobileImage() {
        return amenitiesServiceTirePassMobileImage;
    }

    public void setAmenitiesServiceTirePassMobileImage(String amenitiesServiceTirePassMobileImage) {
        this.amenitiesServiceTirePassMobileImage = amenitiesServiceTirePassMobileImage;
    }

    public String getAmenitiesTruckCATScalesImage() {
        return amenitiesTruckCATScalesImage;
    }

    public void setAmenitiesTruckCATScalesImage(String amenitiesTruckCATScalesImage) {
        this.amenitiesTruckCATScalesImage = amenitiesTruckCATScalesImage;
    }

    public String getTransflo() {
        return transflo;
    }

    public void setTransflo(String transflo) {
        this.transflo = transflo;
    }

    public String getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(String restaurants) {
        this.restaurants = restaurants;
    }

    public String getBulkDEF() {
        return bulkDEF;
    }

    public void setBulkDEF(String bulkDEF) {
        this.bulkDEF = bulkDEF;
    }

    public String getBulkPropane() {
        return bulkPropane;
    }

    public void setBulkPropane(String bulkPropane) {
        this.bulkPropane = bulkPropane;
    }

    public String getEVLevel2() {
        return EVLevel2;
    }

    public void setEVLevel2(String EVLevel2) {
        this.EVLevel2 = EVLevel2;
    }

    public String getEVLevel3DCFC() {
        return EVLevel3DCFC;
    }

    public void setEVLevel3DCFC(String EVLevel3DCFC) {
        this.EVLevel3DCFC = EVLevel3DCFC;
    }

    public String getEVTeslaLevel2() {
        return EVTeslaLevel2;
    }

    public void setEVTeslaLevel2(String EVTeslaLevel2) {
        this.EVTeslaLevel2 = EVTeslaLevel2;
    }

    public String getEVTeslaLevel3DCFC() {
        return EVTeslaLevel3DCFC;
    }

    public void setEVTeslaLevel3DCFC(String EVTeslaLevel3DCFC) {
        this.EVTeslaLevel3DCFC = EVTeslaLevel3DCFC;
    }

    public String getFastFillCNGAuto() {
        return fastFillCNGAuto;
    }

    public void setFastFillCNGAuto(String fastFillCNGAuto) {
        this.fastFillCNGAuto = fastFillCNGAuto;
    }

    public String getFastFillCNGClass8() {
        return fastFillCNGClass8;
    }

    public void setFastFillCNGClass8(String fastFillCNGClass8) {
        this.fastFillCNGClass8 = fastFillCNGClass8;
    }

    public String getPropaneTankExchange() {
        return propaneTankExchange;
    }

    public void setPropaneTankExchange(String propaneTankExchange) {
        this.propaneTankExchange = propaneTankExchange;
    }

    public String getRFIDPumpStart() {
        return RFIDPumpStart;
    }

    public void setRFIDPumpStart(String RFIDPumpStart) {
        this.RFIDPumpStart = RFIDPumpStart;
    }

    public String getATM() {
        return ATM;
    }

    public void setATM(String ATM) {
        this.ATM = ATM;
    }

    public String getDogPark() {
        return dogPark;
    }

    public void setDogPark(String dogPark) {
        this.dogPark = dogPark;
    }

    public String getLaundryFacility() {
        return laundryFacility;
    }

    public void setLaundryFacility(String laundryFacility) {
        this.laundryFacility = laundryFacility;
    }

    public String getPrivateShowers() {
        return privateShowers;
    }

    public void setPrivateShowers(String privateShowers) {
        this.privateShowers = privateShowers;
    }

    public String getVideoGaming() {
        return videoGaming;
    }

    public void setVideoGaming(String videoGaming) {
        this.videoGaming = videoGaming;
    }

    public String getWiFiBasic() {
        return WiFiBasic;
    }

    public void setWiFiBasic(String wiFiBasic) {
        WiFiBasic = wiFiBasic;
    }

    public String getWiFiPremium() {
        return WiFiPremium;
    }

    public void setWiFiPremium(String wiFiPremium) {
        WiFiPremium = wiFiPremium;
    }

    public String getTruckParking() {
        return truckParking;
    }

    public void setTruckParking(String truckParking) {
        this.truckParking = truckParking;
    }

    public String getRVDumpService() {
        return RVDumpService;
    }

    public void setRVDumpService(String RVDumpService) {
        this.RVDumpService = RVDumpService;
    }

    public String getRVFriendlyParking() {
        return RVFriendlyParking;
    }

    public void setRVFriendlyParking(String RVFriendlyParking) {
        this.RVFriendlyParking = RVFriendlyParking;
    }

    public String getRVHookup() {
        return RVHookup;
    }

    public void setRVHookup(String RVHookup) {
        this.RVHookup = RVHookup;
    }

    public String getCommercialTruckOilChange() {
        return commercialTruckOilChange;
    }

    public void setCommercialTruckOilChange(String commercialTruckOilChange) {
        this.commercialTruckOilChange = commercialTruckOilChange;
    }

    public String getLightMechanical() {
        return lightMechanical;
    }

    public void setLightMechanical(String lightMechanical) {
        this.lightMechanical = lightMechanical;
    }

    public String getSpeedCoOnSite() {
        return speedCoOnSite;
    }

    public void setSpeedCoOnSite(String speedCoOnSite) {
        this.speedCoOnSite = speedCoOnSite;
    }

    public String getTireServices() {
        return tireServices;
    }

    public void setTireServices(String tireServices) {
        this.tireServices = tireServices;
    }

    public String getTirePassInLane() {
        return tirePassInLane;
    }

    public void setTirePassInLane(String tirePassInLane) {
        this.tirePassInLane = tirePassInLane;
    }

    public String getTirePassInServiceCenter() {
        return tirePassInServiceCenter;
    }

    public void setTirePassInServiceCenter(String tirePassInServiceCenter) {
        this.tirePassInServiceCenter = tirePassInServiceCenter;
    }

    public String getTirePassMobile() {
        return tirePassMobile;
    }

    public void setTirePassMobile(String tirePassMobile) {
        this.tirePassMobile = tirePassMobile;
    }

    public String getCATScales() {
        return CATScales;
    }

    public void setCATScales(String CATScales) {
        this.CATScales = CATScales;
    }

}
