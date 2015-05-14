package cz.vutbr.fit.stud.xslade12.lostphone.messages;

/**
 * Zprava o tom ze bylo zarizeni uspěšně odemčeno
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class UnlockMessage extends Message {

    public UnlockMessage() {
        this.type = Message.TYPE_UNLOCK;
    }


}
