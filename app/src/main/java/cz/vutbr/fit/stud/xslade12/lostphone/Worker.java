package cz.vutbr.fit.stud.xslade12.lostphone;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

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
    static final int TYPE_RING      = 0x0001;
    static final int TYPE_LOCK      = 0x0002;
    static final int TYPE_LOCATE    = 0x0003;

    static final String HTTP_ENDPOINT = "http://lostphone.dev/gate/";

    Context context;

    public Worker(Context context) {
        this.context = context;
    }

    public void proccess(Request request) {
        Response response;
        if(request.isType(Request.TYPE_RING))
            // spusti Ringin
            response = processRing(request);
        else if(request.isType(Request.TYPE_LOCK))
            // spusti LockScreen a nastavi PIN
            response = processLock(request);
        else if(request.isType(Request.TYPE_LOCATE))
            // Spusti lokaci telefonu
            response = processLocate(request);
        //else if(request.isType(Request.TYPE_PING))
        else {
            // vrati response PONG nebo tak neco
            response = processPong(request);
        }

        sendResponse(response);
    }


    protected Response processPong(Request request) {
        Response response = request.createResponse();
        return response;
    }
    protected Response processRing(Request request) {
        Response response = request.createResponse();

//        Intent intent = new Intent(context, RingingActivity.class);
//        context.startActivity(intent);



        Intent dialogIntent = new Intent(context, RingingActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(dialogIntent);




        // Todo nejdriv poslat request a pak to teprve spustit

        return response;
    }
    protected Response processLock(Request request) {
        Response response = request.createResponse();

        Intent intent = new Intent(context, LockScreenActivity.class);
        context.startActivity(intent);
        // Todo nejdriv poslat request a pak to teprve spustit

        return response;
    }
    protected Response processLocate(Request request) {
        Response response = request.createResponse();
        return response;
    }



    protected void sendResponse(Response response) {
        postData(response);
    }


    public void postData(Response response) {
        // Create a new HttpClient and Post Header
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(HTTP_ENDPOINT);

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("id", null));
            nameValuePairs.add(new BasicNameValuePair("requestId", response.getRequest().getId().toString() ));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse httpResponse = httpClient.execute(httpPost);

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
    }


}
