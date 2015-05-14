package cz.vutbr.fit.stud.xslade12.lostphone.messages;

/**
 * Zpráva o tom, ze vypršel čas prozvánění
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class RingingTimeoutMessage extends Message {

    public RingingTimeoutMessage() {
        this.type = Message.TYPE_RINGINGTIMEOUT;
    }

}
