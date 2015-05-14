package cz.vutbr.fit.stud.xslade12.lostphone.messages;

import java.util.Date;

/**
 * Abstraktni predek vsech zpráv
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public abstract class Message {

    // Typy zprav
    static final int TYPE_PONG              = 0x0000; // odpoved na PING, pouze testovaci
    static final int TYPE_REGISTRATION      = 0x0001; // registracni zprava
    static final int TYPE_GOTCHA            = 0x0002; // mam te
    static final int TYPE_RINGINGTIMEOUT    = 0x0003; // prozvoneni vyprselo
    static final int TYPE_UNLOCK            = 0x0004; // zarizeni odemceno
    static final int TYPE_WRONGPASS         = 0x0005; // spatne zadane heslo
    static final int TYPE_LOCATION          = 0x0006; // pozice zarizeni
    static final int TYPE_SIMSTATECHANGED   = 0x0007; // zmenen stav SIM karty
    static final int TYPE_LOG               = 0x0008; // seznam volani a sms

    /**
     * Typ zpravy
     */
    protected int type;

    /**
     * Datum odeslani
     */
    protected Date date;

    public int getType() {
        return type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
