package cz.vutbr.fit.stud.xslade12.lostphone;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
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


public class Worker {

    static final String HTTP_ENDPOINT = "http://lostphone.dev/gate/";

    Context context;

    public Worker(Context context) {
        this.context = context;
    }

    public void proccess(Request request) {
//        Response response;
        if(request.isType(Request.TYPE_RING))
            // spusti Ringin
            processRing(request);
        else if(request.isType(Request.TYPE_LOCK))
            // spusti LockScreen a nastavi PIN
            processLock(request);
        else if(request.isType(Request.TYPE_LOCATE))
            // Spusti lokaci telefonu
            processLocate(request);
        //else if(request.isType(Request.TYPE_PING))
        else {
            // vrati response PONG nebo tak neco
            processPong(request);
        }
    }


    protected void processPong(Request request) {
        Response response = request.createResponse();

        sendResponse(response);
    }
    protected void processRing(Request request) {
        Response response = request.createResponse();

//        Intent intent = new Intent(context, RingingActivity.class);
//        context.startActivity(intent);

        Intent intent = new Intent(context, RingingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Todo nejdriv poslat request a pak to teprve spustit

        sendResponse(response);
    }
    protected void processLock(Request request) {
        Response response = request.createResponse();


        DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
//        ComponentName mDPA = new ComponentName(context, MyDevicePolicyReceiver.class);

        context.startService(new Intent(context, LockScreenService.class));

        mDPM.resetPassword("heslo", 0);
        mDPM.lockNow();

        //
//        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
//        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE);
//
//        lock.reenableKeyguard();
//        lock.disableKeyguard()

//        Intent intent = new Intent(context, LockScreenActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // aby se dala otevrit ze service
//        context.startActivity(intent);

        // Todo nejdriv poslat request a pak to teprve spustit
        sendResponse(response);
    }
    protected void processLocate(Request request) {
        final Response response = request.createResponse();

        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(context, new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
                if(location == null) {
                    Log.i("GPS", " - NENALEZENO - ");
                    return;
                }
                Log.i("GPS", String.valueOf(location.getLatitude() + " / " + String.valueOf(location.getLongitude())));

                // TODO set Lat and Lng to response .. poslat i kdyz neprislo
                sendResponse(response);
            }
        });
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
