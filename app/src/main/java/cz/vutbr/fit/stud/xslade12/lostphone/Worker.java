package cz.vutbr.fit.stud.xslade12.lostphone;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import cz.vutbr.fit.stud.xslade12.lostphone.commands.Command;
import cz.vutbr.fit.stud.xslade12.lostphone.commands.LocateCommand;
import cz.vutbr.fit.stud.xslade12.lostphone.commands.LockCommand;
import cz.vutbr.fit.stud.xslade12.lostphone.commands.PingCommand;
import cz.vutbr.fit.stud.xslade12.lostphone.commands.RingCommand;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.LocationMessage;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.Message;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.PongMessage;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.UnlockMessage;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.WrongPassMessage;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;
import retrofit.client.Response;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import retrofit.mime.MultipartTypedOutput;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
import retrofit.mime.TypedString;


public class Worker {

//    static final String HTTP_ENDPOINT = "http://lostphone.dev/gate/";

    static final String TAG = "LostPhone-Worker";
    Context context;
    SharedPreferences preferences;

    public Worker(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public void proccess(Command command) {
//        Response response;
        if(command instanceof RingCommand) {
            // spusti Ringin
            sendAck(command);
            processRing((RingCommand) command);
        } else if(command instanceof LockCommand) {
            // spusti LockScreen a nastavi PIN
            sendAck(command);
            processLock((LockCommand) command);
        } else if(command instanceof LocateCommand) {
            // Spusti lokaci telefonu
            sendAck(command);
            processLocate((LocateCommand) command);
        } else if(command instanceof PingCommand) {
            // vrati response PONG nebo tak neco
            sendAck(command);
            processPong((PingCommand) command);
        }
    }


    protected void sendAck(Command command) {
        int id = command.getId();

        getRestService().ackCommand(id, new Date(), new Callback<String>() {
            @Override
            public void success(String s, Response response) {

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }



    protected void processPong(PingCommand command) {
        PongMessage msg = new PongMessage();
        sendMessage(msg);
    }
    protected void processRing(RingCommand command) {
        Intent intent = new Intent(context, RingingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Todo nejdriv poslat request a pak to teprve spustit

//        sendResponse(response);
    }
    protected void processLock(LockCommand command) {
//        Response response = command.createResponse();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("locked", true);
        editor.commit();

        DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
//        ComponentName mDPA = new ComponentName(context, MyDevicePolicyReceiver.class);

        context.startService(new Intent(context, LockScreenService.class));


//        Intent intent = new Intent(context, LockScreenActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // aby se dala otevrit ze service
//        context.startActivity(intent);

        // todo save ownerNumber
        mDPM.resetPassword(command.getPassword(), 0);
        mDPM.lockNow();
//        mDPM.resetPassword("", 0);

//        context.startService(new Intent(context, OverlayService.class));

        //
//        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
//        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE);
//
//        lock.reenableKeyguard();
//        lock.disableKeyguard()

        Intent intent = new Intent(context, LockScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // aby se dala otevrit ze service
        context.startActivity(intent);

        // Todo nejdriv poslat request a pak to teprve spustit
//        sendResponse(response);
    }
    protected void processLocate(LocateCommand command) {
//        final Response response = command.createResponse();

        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(context, new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
                if(location == null) {
                    //  poslat zpravu i kdyz neprislo
                    Log.i("GPS", " - NENALEZENO - ");
                    return;
                }
                Log.i("GPS", String.valueOf(location.getLatitude()) + " / " + String.valueOf(location.getLongitude()));

                LocationMessage msg = new LocationMessage() ;
                msg.setLat( location.getLatitude() );
                msg.setLng( location.getLongitude() );

                sendMessage(msg);

            }
        });
    }



    private static ApiServiceInterface restService;
    protected ApiServiceInterface getRestService() {
        if(restService != null)
            return restService;

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ApiServiceInterface.ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(new AndroidLog(TAG))
                .build();

        restService = restAdapter.create(ApiServiceInterface.class);
        return restService;
    }



    protected void sendMessage(Message message) {
        message.setDate(new Date());
//        postData(message);

        Callback<String> callback = new Callback<String>() {
            @Override
            public void success(String message, Response response) {
                Log.i(TAG, message);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.i(TAG, "Error:  " + error.getMessage());
            }
        };

        if(message instanceof WrongPassMessage) {
            getRestService().createWrongPassMessage(
                    new TypedString(String.valueOf(message.getType())),
                    new TypedString(message.getDate().toString()),
                    new TypedFile("image/jpeg", ((WrongPassMessage) message).getFrontPhoto()),
                    callback
                    );
        } else {
            getRestService().createMessage(message, callback);
        }



    }
/*
    public void postData(Message message) {
        // Create a new HttpClient and Post Header
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(HTTP_ENDPOINT);

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("id", null));
//            nameValuePairs.add(new BasicNameValuePair("requestId", response.getRequest().getId().toString() ));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse httpResponse = httpClient.execute(httpPost);

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
    }
*/

}
