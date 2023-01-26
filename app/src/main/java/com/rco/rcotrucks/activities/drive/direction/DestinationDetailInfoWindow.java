package com.rco.rcotrucks.activities.drive.direction;

public class DestinationDetailInfoWindow {
    private String destination;
    private String photo_reference;

    public String getPhoto_reference() {
        return photo_reference;
    }

    public void setPhoto_reference(String photo_reference) {
        this.photo_reference = photo_reference;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getImage() {
        return "https://maps.googleapis.com/maps/api/place/photo" +
                "?photo_reference=" +photo_reference+
                "&maxwidth=400" +
                "&key=AIzaSyCkXY-OOuAIGFiHisd0EAaQ5m92OmG-qHg";
    }
}
