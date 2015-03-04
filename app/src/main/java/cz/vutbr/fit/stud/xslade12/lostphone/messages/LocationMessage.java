package cz.vutbr.fit.stud.xslade12.lostphone.messages;

public class LocationMessage extends Message {

    public LocationMessage() {
        this.type = Message.TYPE_LOCATION;
    }

    public double lat;

    public double lng;


    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
