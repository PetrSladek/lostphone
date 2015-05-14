package cz.vutbr.fit.stud.xslade12.lostphone;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Camera;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.Telephony;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import cz.vutbr.fit.stud.xslade12.lostphone.activities.LockScreenActivity;
import cz.vutbr.fit.stud.xslade12.lostphone.activities.MainActivity;
import cz.vutbr.fit.stud.xslade12.lostphone.activities.RingingActivity;
import cz.vutbr.fit.stud.xslade12.lostphone.commands.Command;
import cz.vutbr.fit.stud.xslade12.lostphone.commands.EncryptStorageCommand;
import cz.vutbr.fit.stud.xslade12.lostphone.commands.GetLogCommand;
import cz.vutbr.fit.stud.xslade12.lostphone.commands.LocateCommand;
import cz.vutbr.fit.stud.xslade12.lostphone.commands.LockCommand;
import cz.vutbr.fit.stud.xslade12.lostphone.commands.PingCommand;
import cz.vutbr.fit.stud.xslade12.lostphone.commands.RingCommand;
import cz.vutbr.fit.stud.xslade12.lostphone.commands.WipeDataCommand;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.LocationMessage;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.LogMessage;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.Message;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.PongMessage;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.UnlockMessage;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.WrongPassMessage;
import cz.vutbr.fit.stud.xslade12.lostphone.recievers.DevicePolicyReceiver;
import cz.vutbr.fit.stud.xslade12.lostphone.services.LockScreenService;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;


/**
 * Třída zajišťující vykonávání příkazů a zasílání zpráv
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class Worker {

    /**
     * Tag pro logování
     */
    public static final String TAG = "LostPhone Worker";

    /**
     * Apliační context
     */
    protected Context context;

    /**
     * Registračníé ID GCM
     */
    protected String gcmId;

    /**
     * Sdilena data pro aplikaci
     */
    protected SharedPreferences preferences;

    /**
     * Služba pro REST API
     */
    private static ApiServiceInterface restService;

    /**
     * ID projektu z Google API konzole
     *
     * From https://console.developers.google.com/
     */
    public static final String GCM_SENDER_ID = "941272288463";

    static AtomicInteger msgId = new AtomicInteger();

    public Worker(Context context) {
        this.context = context;
        // privatni uloziste sdilenych aplikacnich dat
        this.preferences = context.getSharedPreferences("global", Context.MODE_PRIVATE);
        // Ulozene GCM ID
        this.gcmId = preferences.getString(MainActivity.PROPERTY_GCM_ID, null);
    }


    public SharedPreferences getPreferences() {
        return preferences;
    }

    public String getGcmId() {
        return gcmId;
    }

    /**
     * Zpracuje prikaz a vykona ho
     * @param command
     */
    public void proccess(Command command) {
//        Response response;
        if(command instanceof PingCommand) {
            // vrati response PONG nebo tak neco
            sendAck(command);
            processPing((PingCommand) command);
        } else if(command instanceof RingCommand) {
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
        } else if(command instanceof GetLogCommand) {
            // Spusti lokaci telefonu
            sendAck(command);
            processGetLog((GetLogCommand) command);
        } else if(command instanceof EncryptStorageCommand) {
            // Spusti zasifrovani dat
            sendAck(command);
            processEncryptStorage((EncryptStorageCommand) command);
        } else if(command instanceof WipeDataCommand) {
            // Spusti smazani dat a uvedeni do tovarniho nastaveni
            sendAck(command);
            processWipeData((WipeDataCommand) command);
        }
    }


    /**
     * Zpracuje prikaz Ping a vykona ho.
     *
     * Pošle pong zprávu
     * @param command příkaz
     */
    protected void processPing(PingCommand command) {
        PongMessage msg = new PongMessage();
        sendMessage(msg);
    }

    /**
     * Zpracuje prikaz Ring a vykona ho.
     *
     * Otevře RingingActivity s potřebnymi parametry
     * @param command příkaz
     */
    protected void processRing(RingCommand command) {
        // Pokusi se lokalizovat zarizeni
        locateDevice();

        Intent intent = new Intent(context, RingingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("closeAfter", (long) command.getCloseAfter());
        context.startActivity(intent);
    }

    /**
     * Zpracuje prikaz Lock a vykona ho.
     *
     * Uzamkne telefon
     * @param command příkaz
     */
    protected void processLock(LockCommand command) {
        startLockMode(command.getPassword(), command.getDisplayText(), command.getOwnerPhoneNumber());
    }


    /**
     * Zpracuje prikaz Locate a vykona ho.
     * @param command příkaz
     */
    protected void processLocate(LocateCommand command) {
        locateDevice();
    }

    /**
     * Zpracuje prikaz GetLog a vykona ho.
     * @param command příkaz
     */
    protected void processGetLog(GetLogCommand command) {
        LogMessage msg = new LogMessage();
        msg.setCallLog(getCallLog()); // vypis volani
        msg.setSmsLog(getSmsLog()); // vypis sms
        sendMessage(msg);
    }

    /**
     * Zpracuje prikaz EncryptStorage a vykona ho.
     * @param command příkaz
     */
    protected void processEncryptStorage(EncryptStorageCommand command) {
        storageEncrypt();
    }

    /**
     * Zpracuje prikaz WipeData a vykona ho.
     * @param command příkaz
     */
    protected void processWipeData(WipeDataCommand command) {
        wipeData();
    }

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





//    protected void sendAckOverRest(Command command) {
//        int id = command.getId();
//
//        getRestService().ackCommand(gcmId, id, new Date(), new Callback<String>() {
//            @Override
//            public void success(String s, Response response) {
//
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//
//            }
//        });
//    }


    /**
     * Posle ACK message přes GCM
     * @param command
     */
    protected void sendAck(final Command command) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {

                    Bundle data = new Bundle();
                    data.putString("ack", (new Date()).toString());
                    data.putString("id", Integer.toString(command.getId()));

                    String id = Integer.toString(msgId.incrementAndGet());
                    long timeToLive = 10000L; // seconds on GCM server

                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                    gcm.send(Worker.GCM_SENDER_ID + "@gcm.googleapis.com", id, timeToLive, data);

                } catch (IOException ex) {

                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {

            }
        }.execute(null, null, null);
    }


    /**
     * Pošle zprávu
     * @param message
     */
    public void sendMessage(Message message) {
        message.setDate(new Date());
        sendMessageOverGcm(message);
    }

    /**
     * Pošle zpravu přes HTTP REST
     * @param message
     */
    protected void sendMessageOverHttp(final Message message) {


        Callback<String> callback = new Callback<String>() {
            @Override
            public void success(String exitCode, Response response) {
                Log.i(TAG, exitCode);
                if(message instanceof WrongPassMessage) {
                    // smaze fotku ze slozky
                    try {
                        ((WrongPassMessage) message).getFrontPhoto().delete();
                    } catch (Exception ex) {

                    }
                }
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

    /**
     * Pošle zpravu přes GCM
     * @param message
     */
    protected void sendMessageOverGcm(final Message message) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    // Pres GCM se da odslat jen 4KB dat :(
                    if(message instanceof WrongPassMessage) {
                        if(isConnected()) { // jsem prepojenej, celou zpravu poslu postem
                            sendMessageOverHttp(message);
                            return null;
                        } else { // nejsem pripojenej, pres GCM poslu jen zpravu bez fotky a fotku pozdejc
                            ((WrongPassMessage) message).deleteFrontPhoto();
                        }
                    }

                    Gson gson = new Gson();
                    String jsonMessage = gson.toJson(message);

                    Bundle data = new Bundle();
                    data.putString("message", jsonMessage);

                    String id = Integer.toString(msgId.incrementAndGet());
                    long timeToLive = 10000L; // seconds on GCM server

                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                    gcm.send(Worker.GCM_SENDER_ID + "@gcm.googleapis.com", id, timeToLive, data);

                } catch (IOException ex) {

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

            }
        }.execute(null, null, null);
    }


    /**
     * Je zarízení on-line?
     * @return
     */
    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Uloz stav uzamknutí do sdilenych dat
     * @param locked
     */
    public void setLocked(boolean locked) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("locked", locked);
        editor.commit();
    }

    /**
     * Je stav zařízení zamknuto?
     * @return
     */
    public boolean isLocked() {
        return getPreferences().getBoolean("locked", false); // ve vychozim neni zamklej
    }


    /**
     * Zpravuj spatne zadani hesla
     */
    public void passwordFailed() {

        final WrongPassMessage msg = new WrongPassMessage();
        final FrontCameraController fc = new FrontCameraController(context);
        if(!fc.hasCamera()) {
            // Zariazeni nema kameru
            sendMessage(msg); // posli jen zpravu bez fotografie
            locateDevice(); // zjisti polohu zarizeni
        } else {
            fc.open(); // otevrit predni kameru
            fc.setPictureCallback(new Camera.PictureCallback() { // Callback po vyfoceni fotografie
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    try {

                        File pictureFile = fc.getOutputMediaFile();
                        if (pictureFile == null)
                            throw new IOException("Error creating media file, check storage permissions");

                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        fos.write(data);
                        fos.close();

                        msg.setFrontPhoto(pictureFile); // nastav fotku ke zprave

                    } catch (FileNotFoundException e) {
                        Log.d(TAG, "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d(TAG, "Error accessing file: " + e.getMessage());
                    } finally {
                        sendMessage(msg); // odesli zpravu
                        locateDevice(); // zjisti polohu zařízení
                    }
                }
            });
            fc.takePicture(); // vyfot fotografii
        }
    }

    /**
     * Zpracuj sprvne zadani hesla
     */
    public void passwordSuccess() {
        UnlockMessage msg = new UnlockMessage();
        this.sendMessage(msg);
        this.setLocked(false);
    }

    /**
     * Zpracuj pozadavek na zjisteni polohy zarizeni
     */
    public void locateDevice() {
        LocationController lc = new LocationController();
        lc.getLocation(context, new LocationController.LocationResult(){
            @Override
            public void gotLocation(Location location){
                if(location == null)
                    return;

                LocationMessage msg = new LocationMessage() ;
                msg.setLat( location.getLatitude() );
                msg.setLng( location.getLongitude() );

                sendMessage(msg);

            }
        });
    }

    /**
     * Vrati retezec obsahujici vypis volani
     * @return
     */
    public String getCallLog() {

        int limit = 5;

        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);

        StringBuffer sb = new StringBuffer();
//        sb.append("Posledni volani:");
        while (cursor.moveToNext()) {
            if(cursor.getPosition() >= limit)
                break;
            String  cellNumber = cursor.getString(number);
            String  callType = cursor.getString(type);
            String  callDate = cursor.getString(date);
            Date    callDayTime = new Date(Long.valueOf(callDate));
            String  callDuration = cursor.getString(duration);
            String  direction = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    direction = "OUTGOING";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    direction = "INCOMING";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    direction = "MISSED";
                    break;
            }
            sb.append(cellNumber + "|"+ direction + "|" + callDayTime+ "|" + callDuration + "\n");
        }
        cursor.close();
        return sb.toString();
    }

    /**
     * Vrati retezec obsahujici vypis SMS
     * @return
     */
    public String getSmsLog() {
        int limit = 5;

        Cursor cursor = context.getContentResolver().query(/*Telephony.Sms.CONTENT_URI*/ Uri.parse("content://sms"), null, null, null, null);
        int address = cursor.getColumnIndex(Telephony.Sms.ADDRESS);
        int type = cursor.getColumnIndex(Telephony.Sms.TYPE);
        int date = cursor.getColumnIndex(Telephony.Sms.DATE);
        int body = cursor.getColumnIndex(Telephony.Sms.BODY);

        StringBuffer sb = new StringBuffer();
//        sb.append("Posledni volani:");
        while (cursor.moveToNext()) {
            if(cursor.getPosition() >= limit)
                break;

            String  cellAddress = cursor.getString(address); // number
            String  callType = cursor.getString(type);
            String  callDate = cursor.getString(date);
            String  cellBody = cursor.getString(body);

            Date    callDayTime = new Date(Long.valueOf(callDate));
            String  box = null;
            int boxcode = Integer.parseInt(callType);
            switch (boxcode) {
                case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                    box = "OUTBOX";
                    break;
                case Telephony.Sms.MESSAGE_TYPE_INBOX:
                    box = "INBOX";
                    break;
                case Telephony.Sms.MESSAGE_TYPE_DRAFT:
                    box = "DRAFT";
                    break;
            }
            sb.append(cellAddress + "|"+ box + "|" + callDayTime+ "|" + cellBody.replace("\n", "/n") + "\n");
        }
        cursor.close();
        return sb.toString();

    }

    /**
     * Zamkne zařízení
     * @param password heslo
     */
    public void startLockMode(String password) {
        startLockMode(password, null, null, false);
    }

    /**
     * Zamkne zařízení
     * @param password heslo
     * @param displayText text na displej
     * @param ownerPhoneNumber telefoní číslo majitele
     */
    public void startLockMode(String password, String displayText, String ownerPhoneNumber) {
        startLockMode(password, displayText, ownerPhoneNumber, false);
    }

    /**
     * Zamkne zařízení
     * @param password heslo
     * @param displayText text na displej
     * @param ownerPhoneNumber telefoní číslo majitele
     * @param stolenMode Mod ukradení? (spustí zvuk)
     */
    public void startLockMode(String password, String displayText, String ownerPhoneNumber, Boolean stolenMode) {
        setLocked(true);

        DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        context.startService(new Intent(context, LockScreenService.class));

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("password", password);
        editor.putString("displayText", displayText);
        editor.putString("ownerPhoneNumber", ownerPhoneNumber.replace(" ",""));
        editor.commit();

        mDPM.resetPassword(password, 0);
        mDPM.lockNow();

        Intent intent = new Intent(context, LockScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // aby se dala otevrit ze service
        intent.putExtra("stolenMode", stolenMode);
        context.startActivity(intent);
    }

    /**
     * Spustí mod ukradeni
     * @param password
     */
    public void startStolenMode(String password) {
        startLockMode(password, null, null, true);
    }

    /**
     * Vypne mod ukradeni
     */
    public void stopStolenMode() {
        // posle intent na ukonceni zvuku v lockactivity

        Intent intent = new Intent(context, LockScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // aby se pustil onNewIntent
        intent.putExtra("stolenMode", false); // Vypnu stolen Mode (zvuk has been stolen)
        context.startActivity(intent);

    }

    /**
     * Zasifruje data na karťe
     */
    public void storageEncrypt() {
        DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName devicePolicyAdmin = new ComponentName(context, DevicePolicyReceiver.class);

        mDPM.setStorageEncryption(devicePolicyAdmin, true);
    }

    /**
     * Uvede zarizeni do tovarniho nastaveni
     */
    public void wipeData() {
        DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDPM.wipeData( DevicePolicyManager.WIPE_EXTERNAL_STORAGE );
    }

}
