package cz.vutbr.fit.stud.xslade12.lostphone.messages;

import java.io.File;

import retrofit.http.Part;
import retrofit.mime.TypedFile;

public class WrongPassMessage extends Message {

    public WrongPassMessage() {
        this.type = Message.TYPE_WRONGPASS;
    }

//
//    private String password;

    private File frontPhoto;

//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }

    public File getFrontPhoto() {
        return frontPhoto;
    }

    public void setFrontPhoto(File frontPhoto) {
        this.frontPhoto = frontPhoto;
    }
}
