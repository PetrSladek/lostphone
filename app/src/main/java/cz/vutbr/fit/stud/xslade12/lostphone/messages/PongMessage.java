package cz.vutbr.fit.stud.xslade12.lostphone.messages;

/**
 * Testovaci zprava
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class PongMessage extends Message {

    public PongMessage() {
        this.type = Message.TYPE_PONG;
    }

}
