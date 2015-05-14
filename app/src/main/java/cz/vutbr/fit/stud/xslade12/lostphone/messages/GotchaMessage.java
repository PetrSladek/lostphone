package cz.vutbr.fit.stud.xslade12.lostphone.messages;

/**
 * Zpráva o tom, že bylo zařízení po prozvonení nalezeno
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class GotchaMessage extends Message {

    public GotchaMessage() {
        this.type = Message.TYPE_GOTCHA;
    }
}
