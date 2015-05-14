package cz.vutbr.fit.stud.xslade12.lostphone.messages;

/**
 * Zpráva o tom, kde se zařízení nachází
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class LocationMessage extends Message {

    public LocationMessage() {
        this.type = Message.TYPE_LOCATION;
    }

    protected double lat;

    protected double lng;


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
