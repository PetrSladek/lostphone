package cz.vutbr.fit.stud.xslade12.lostphone;

import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import cz.vutbr.fit.stud.xslade12.lostphone.commands.Command;
import cz.vutbr.fit.stud.xslade12.lostphone.commands.LocateCommand;
import cz.vutbr.fit.stud.xslade12.lostphone.commands.LockCommand;
import cz.vutbr.fit.stud.xslade12.lostphone.commands.PingCommand;
import cz.vutbr.fit.stud.xslade12.lostphone.commands.RingCommand;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.LocationMessage;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.Message;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.PongMessage;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.WrongPassMessage;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;


public class Worker {

//    static final String HTTP_ENDPOINT = "http://lostphone.dev/gate/";

    static final String TAG = "LostPhone-Worker";
    Context context;
    String gcmId;
    SharedPreferences preferences;

    public Worker(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences("global", Context.MODE_PRIVATE);
        this.gcmId = preferences.getString(MainActivity.PROPERTY_REG_ID, null);
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

        getRestService().ackCommand(gcmId, id, new Date(), new Callback<String>() {
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
        // Pokusi se lokalizovat zarizeni
        locateDevice();

        Intent intent = new Intent(context, RingingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("closeAfter", (long) command.getCloseAfter());
        context.startActivity(intent);
    }
    protected void processLock(LockCommand command) {

        setLocked(true);

        DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        context.startService(new Intent(context, LockScreenService.class));


//        Intent intent = new Intent(context, LockScreenActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // aby se dala otevrit ze service
//        context.startActivity(intent);

        // todo save ownerNumber
        mDPM.resetPassword(command.getPassword(), 0);
        mDPM.lockNow();
//        mDPM.resetPassword("", 0);

//        context.startService(new Intent(context, OverlayService.class));

//        //
//        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
//        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE);
//
//        lock.disableKeyguard();

        Intent intent = new Intent(context, LockScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // aby se dala otevrit ze service
        context.startActivity(intent);

        // Todo nejdriv poslat request a pak to teprve spustit
//        sendResponse(response);
    }
    protected void processLocate(LocateCommand command) {
        locateDevice();
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
                    gcmId,
                    new TypedString(String.valueOf(message.getType())),
                    new TypedString(message.getDate().toString()),
                    new TypedFile("image/jpeg", ((WrongPassMessage) message).getFrontPhoto()),
                    callback
                    );
        } else {
            getRestService().createMessage(gcmId, message, callback);
        }

    }


    public void setLocked(boolean locked) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putBoolean("locked", locked);
        editor.commit();
    }


    public void passwordFailed() {
        final WrongPassMessage msg = new WrongPassMessage();
        final FrontCameraController fc = new FrontCameraController(context);
        if(!fc.hasCamera()) {
            sendMessage(msg); // kdyz neni fotak posli jen zpravu
        } else {
            fc.open();
            fc.setPictureCallback(new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    Toast.makeText(context, "onPictureTaken", Toast.LENGTH_LONG).show();
                    try {
                        File pictureFile = fc.getOutputMediaFile();
                        if (pictureFile == null)
                            throw new IOException("Error creating media file, check storage permissions");

                        Log.d("FrontCAM", "File created");
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        fos.write(data);
                        fos.close();

                        msg.setFrontPhoto(pictureFile); // nastav fotku ke zprave

                    } catch (FileNotFoundException e) {
                        Log.d("FrontCAM", "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d("FrontCAM", "Error accessing file: " + e.getMessage());
                    } finally {
                        sendMessage(msg); // odesli zpravu
                    }
                }
            });
            fc.takePicture();
            fc.release();
        }
    }


    public void locateDevice() {
        LocationController lc = new LocationController();
        lc.getLocation(context, new LocationController.LocationResult(){
            @Override
            public void gotLocation(Location location){
                if(location == null) {
                    // TODO message nenalezeno
                    return;
                }

                LocationMessage msg = new LocationMessage() ;
                msg.setLat( location.getLatitude() );
                msg.setLng( location.getLongitude() );

                sendMessage(msg);

            }
        });
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
