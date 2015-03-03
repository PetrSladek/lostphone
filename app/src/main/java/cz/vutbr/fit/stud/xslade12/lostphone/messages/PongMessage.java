package cz.vutbr.fit.stud.xslade12.lostphone.messages;

public class PongMessage extends Message {

    public int getType() {
        return Message.TYPE_PONG;
    }

    public String text = "PONG";

}
