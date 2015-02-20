package cz.vutbr.fit.stud.xslade12.lostphone;

import android.content.Context;

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


public class Worker {

    static final int TYPE_PING      = 0x0000;
    static final int TYPE_LOCK      = 0x0001;
    static final int TYPE_RING      = 0x0002;
    static final int TYPE_LOCATE    = 0x0003;

    static final String HTTP_ENDPOINT = "http://lostphone.dev/gate/";

    public static void run(Context context, Request request) {
        switch (request.getType()) {
            case Request.TYPE_PING:
                // vrati response PONG nebo tak neco
            break;
            case Request.TYPE_LOCK:
                // spusti LockScreen a nastavi PIN
            break;
            case Request.TYPE_RING:
                // spusti Ringin
            break;
            case Request.TYPE_LOCATE:
                // Spusti lokaci telefonu
            break;
        }


        // TODO vratit nejakej response
    }


    public void postData() {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(HTTP_ENDPOINT);

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("id", "12345"));
            nameValuePairs.add(new BasicNameValuePair("stringdata", "Hi"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
    }


}
