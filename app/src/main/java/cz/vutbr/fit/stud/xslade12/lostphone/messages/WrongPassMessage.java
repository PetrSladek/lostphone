package cz.vutbr.fit.stud.xslade12.lostphone.messages;

import android.util.Base64;

import java.io.File;

import retrofit.http.Part;
import retrofit.mime.TypedFile;

public class WrongPassMessage extends Message {

    public WrongPassMessage() {
        this.type = Message.TYPE_WRONGPASS;
    }

    private File frontPhoto;



    public File getFrontPhoto() {
        return frontPhoto;
    }

    public void setFrontPhoto(File frontPhoto) {
        this.frontPhoto = frontPhoto;
    }

    public void deleteFrontPhoto() {
        this.frontPhoto = null;
    }

}
