package cz.vutbr.fit.stud.xslade12.lostphone.messages;

/**
 * Zpráva o tom, ze se změnil stav SIM karty
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class SimStateChangedMessage extends Message {

    public SimStateChangedMessage() {
        this.type = Message.TYPE_SIMSTATECHANGED;
    }


    /**
     * IMEI
     *
     * International Mobile Equipment Identity. Jde o unikátní číslo přidělené výrobcem mobilnímu telefonu.
     */
    protected String imei; //

    /**
     * IMSI
     *
     * International Mobile Subscriber Identity. Jde o unikátní číslo přidělené mobilním operátorem pro SIM kartu v mobilní síti GSM nebo UMTS. Může být použito v dalších sítích jako např. CDMA.
     */
    protected String subscriberId;


    /**
     * MSISDN
     *
     * Mobile Subscriber ISDN Number je celosvětově jednoznačné číslo, které identifikuje účastníka ve veřejné telefonní síti
     */
    protected String phoneNumber;

    /**
     * Operátor přihlášené sítě
     */
    protected String networkOperator;

    /**
     * Název operátora přihlášené sítě
     */
    protected String networkOperatorName;

    /**
     * ISO kód Země operátora přihlášené sítě
     */
    protected String networkCountryIso;

    /**
     * Operátor sítě vyddavajici SIM kartu
     */
    protected String simOperator;

    /**
     * Název operátora sítě vyddavajici SIM kartu
     */
    protected String simOperatorName;

    /**
     * Seriové číslo SIM karty
     */
    protected String simSerialNumber;


    /**
     * ISO kód Země operátora vydávající SIM kartu
     */
    protected String simCountryIso;



    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNetworkOperator() {
        return networkOperator;
    }

    public void setNetworkOperator(String networkOperator) {
        this.networkOperator = networkOperator;
    }

    public String getNetworkOperatorName() {
        return networkOperatorName;
    }

    public void setNetworkOperatorName(String networkOperatorName) {
        this.networkOperatorName = networkOperatorName;
    }

    public String getNetworkCountryIso() {
        return networkCountryIso;
    }

    public void setNetworkCountryIso(String networkCountryIso) {
        this.networkCountryIso = networkCountryIso;
    }

    public String getSimOperator() {
        return simOperator;
    }

    public void setSimOperator(String simOperator) {
        this.simOperator = simOperator;
    }

    public String getSimOperatorName() {
        return simOperatorName;
    }

    public void setSimOperatorName(String simOperatorName) {
        this.simOperatorName = simOperatorName;
    }

    public String getSimCountryIso() {
        return simCountryIso;
    }

    public void setSimCountryIso(String simCountryIso) {
        this.simCountryIso = simCountryIso;
    }

    public String getSimSerialNumber() {
        return simSerialNumber;
    }

    public void setSimSerialNumber(String simSerialNumber) {
        this.simSerialNumber = simSerialNumber;
    }


}
