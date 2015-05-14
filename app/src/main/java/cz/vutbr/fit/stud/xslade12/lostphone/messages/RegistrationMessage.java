package cz.vutbr.fit.stud.xslade12.lostphone.messages;

/**
 * Zpráva o tom, kde se zařízení nachází
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class RegistrationMessage extends Message {

    public RegistrationMessage() {
        this.type = Message.TYPE_REGISTRATION;
    }

    /**
     * Unikatni identifikator zarizeni
     */
    protected String identifier;

    /**
     * Pridelene GCM ID
     */
    protected String gcmId;

    /**
     * Google Account
     */
    protected String googleAccountEmail;

    /**
     * Značka/Výrobce zařízení
     */
    protected String brand;

    /**
     * Model zařízení
     */
    protected String model;


    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getGcmId() {
        return gcmId;
    }

    public void setGcmId(String gcmId) {
        this.gcmId = gcmId;
    }

    public String getGoogleAccountEmail() {
        return googleAccountEmail;
    }

    public void setGoogleAccountEmail(String googleAccountEmail) {
        this.googleAccountEmail = googleAccountEmail;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
