package cz.vutbr.fit.stud.xslade12.lostphone.messages;

import java.util.Date;

public abstract class Message {

    static final int TYPE_PONG              = 0x0000; // odpoved na PING, pouze testovaci
    static final int TYPE_REGISTRATION      = 0x0001;
    static final int TYPE_GOTCHA            = 0x0002;
    static final int TYPE_RINGINGTIMEOUT    = 0x0003;
    static final int TYPE_UNLOCK            = 0x0004;
    static final int TYPE_WRONGPASS         = 0x0005;
    static final int TYPE_LOCATION          = 0x0006;

    protected int type;

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
