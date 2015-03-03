package cz.vutbr.fit.stud.xslade12.lostphone.messages;

import java.io.File;

public class WrongPassMessage extends Message {

    public int getType() {
        return Message.TYPE_WRONGPASS;
    }

    private String password;

    private File frontPhoto;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public File getFrontPhoto() {
        return frontPhoto;
    }

    public void setFrontPhoto(File frontPhoto) {
        this.frontPhoto = frontPhoto;
    }
}
