package cz.vutbr.fit.stud.xslade12.lostphone;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

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
    static final int TYPE_RING      = 0x0001;
    static final int TYPE_LOCK      = 0x0002;
    static final int TYPE_LOCATE    = 0x0003;

    /**
     * Unique ID
     */
    protected Integer id;

    /**
     * Type of request
     */
    protected Integer type;


    /**
     * Get Unique ID
     * @return Integer
     */
    public Integer getId() {
        return id;
    }

    /**
     * Set Unique ID
     * @return void
     */
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public boolean isType(Integer type) {
        return this.getType() == type;
    }



    public static Request createFromBundle(Bundle data) {
        Request request = new Request();
        request.setId( Integer.valueOf(data.getString("id"))); // default type 0
        request.setType(Integer.valueOf(data.getString("type"))); // default type PING

        return request;
    }

    public Response createResponse() {
        Response response = new Response();
        response.setRequest(this);
        return response;
    }

}
