package cz.vutbr.fit.stud.xslade12.lostphone;

import android.content.Context;
import android.os.Bundle;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Request {

    static final int TYPE_PING      = 0x0000;
    static final int TYPE_LOCK      = 0x0001;
    static final int TYPE_RING      = 0x0002;
    static final int TYPE_LOCATE    = 0x0003;

    protected int type;



    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }



    public static Request createFromBundle(Bundle data) {
        Request request = new Request();
        request.setType( data.getInt("type", TYPE_PING) ); // default type PING

        return request;
    }
}
