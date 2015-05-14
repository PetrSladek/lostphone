package cz.vutbr.fit.stud.xslade12.lostphone.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import cz.vutbr.fit.stud.xslade12.lostphone.R;
import cz.vutbr.fit.stud.xslade12.lostphone.Worker;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.RegistrationMessage;
import cz.vutbr.fit.stud.xslade12.lostphone.recievers.DevicePolicyReceiver;

/**
 * Aktivita pro registraci GCM a DeviceAdminPolicy
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class MainActivity extends Activity {

    protected static final int REQUEST_CODE_ENABLE_DEVICEADMIN = 1;
    protected static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PROPERTY_GCM_ID = "gcmId";
    public static final String PROPERTY_APP_VERSION = "appVersion";



    /**
     * tlacitko registrovat/deregistrovat DeviceAdmin
     */
    private Button btnDeviceAdminToggle;
    /**
     * tlacitko registrovat/deregistrovat GCM
     */
    private Button btnGcmRegistrationToggle;

    /**
     * ID projektu z Google API konzole
     *
     * From https://console.developers.google.com/
     */
    String GCM_SENDER_ID = "941272288463";

    /**
     * Tag pro logování
     */
    static final String TAG = "LostPhone";


    DevicePolicyManager devicePolicyManager;
    ComponentName devicePolicyAdmin;

    GoogleCloudMessaging gcm;
    String gcmId;



    /**
     * Metoda pri vytvoreni Activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // DevicePolicyManager a komponenta DevicePolieReciever
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        devicePolicyAdmin = new ComponentName(this, DevicePolicyReceiver.class);

        // nastavim layout z XML
        setContentView(R.layout.activity_main);



        // tlacitko registrovat/deregistrovat DeviceAdmin
        btnDeviceAdminToggle = (Button) findViewById(R.id.btnDeviceAdminToggle);
        btnDeviceAdminToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDeviceAdminActive()) {
                    // deaktivuju devide admin
                    devicePolicyManager.removeActiveAdmin(devicePolicyAdmin);
                    btnDeviceAdminToggle.setText(R.string.btnDeviceAdminEnabled);
                } else {
                    // Aktivovat device admin
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, devicePolicyAdmin);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.admin_explanation));
                    startActivityForResult(intent, REQUEST_CODE_ENABLE_DEVICEADMIN);
                }
            }
        });

        // tlacitko registrovat/deregistrovat GCM
        btnGcmRegistrationToggle = (Button) findViewById(R.id.btnGcmRegistrationToggle);
        btnGcmRegistrationToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isGCMRegistered()) {
                    // Odregistrovani GCM
                    unregisterGCMInBackground();
                } else {
                    // Zaregistrování GCM
                    registerGCMInBackground();
                }
            }
        });


        // Overeni jestli má zarizeni pristup ke slouzbam Google Play.
        // Pokud ano pokusi se zaregistrovat GCM
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            gcmId = loadGcmId(this.getApplicationContext()); // vytahne gcmId z uloziste

            if (!isGCMRegistered()) { // kdyz  neni zaregistrovano tak zaregistrujeme
                System.out.println("RegID: - neni zaregistrovano -");
                this.setTitle("- neni zeregistrovano -");
                registerGCMInBackground();
            } else {
                System.out.println("RegID: " + gcmId);
                this.setTitle(gcmId);
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }


    }



    /**
     * Zkontrolujte zařízení, jestli má Google Play Services APK. pokud
     * Pokd ne zobrazí se dialogové okno, které umožňuje uživatelům stahovat APK z
     * Google Play Store nebo povolit v nastavení.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Ulozi registracni ID a verzi aplikace do sdílených dat aplikace
     * {@code SharedPreferences}.
     *
     * @param context Aplikační kontext
     * @param gcmId Registracni GCM ID
     */
    private void storeGcmId(Context context, String gcmId) {
        SharedPreferences prefs = getAppSharedPreferences(context);
        int appVersion = getAppVersion(context);

        Log.i(TAG, "Saving gcmId on app version " + appVersion);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_GCM_ID, gcmId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }



    /**
     * Vezme Gcm Registracni ID ze sdilenych dat aplikace
     *
     * @return GcmID, nebo prazdny string pokud registracni ID neni aktualni k verzi Aplikace
     */
    private String loadGcmId(Context context) {
        final SharedPreferences prefs = getAppSharedPreferences(context);
        String gcmId = prefs.getString(PROPERTY_GCM_ID, "");
        if (gcmId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        // Zkontroluje jestli nebyla aplikace aktualizovana; pokud ano regID nevrati protože by se aplikace mela zaregistrovat znova
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return gcmId;
    }

    /**
     * Registrace zařízení na GCM servers asynchronně.
     *
     * Zaregistruje zařízení a ulozi ziskane GcmID do sdilenych dat
     */
    private void registerGCMInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (gcm == null)
                        gcm = GoogleCloudMessaging.getInstance(MainActivity.this);

                    gcmId = gcm.register(GCM_SENDER_ID);

                    // Posle gcmID zarizeni na server, kde ho zaregistruje
                    sendGcmIdToServer();

                    // Ulozi GCM ID do sdilenych dat
                    storeGcmId(MainActivity.this.getApplicationContext(), gcmId);


                } catch (IOException ex) {
                    Log.i(TAG, "Error :" + ex.getMessage());
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                // Po zaregistrovani obnovi stav aktivity
                refreshGCMRegistered();
            }
        }.execute(null, null, null);
    }

    /**
     * Deregistrace zařízení na GCM servers asynchronně.
     *
     * Odregistruje zařízení a vymaze GcmID ze sdilenych dat
     */
    private void unregisterGCMInBackground() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground( Void... voids ) {
                try {
                    if (gcm == null)
                        gcm = GoogleCloudMessaging.getInstance(MainActivity.this);

                    gcm.unregister(); // odregistruju GCM

                    gcmId = "";
                    storeGcmId(MainActivity.this.getApplicationContext(), gcmId); // smazu registracni ID

                } catch (IOException ex) {
                    Log.i(TAG, "Error :" + ex.getMessage());
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                // Po odregistrovani obnovi stav aktivity
                refreshGCMRegistered();
            }
        }.execute();
    }


    /**
     * @return Vrati verzi aplikace z {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e); // should never happen
        }
    }

    /**
     * @return Vrati sdilena data pro tuto aplikaci {@code SharedPreferences}.
     */
    private SharedPreferences getAppSharedPreferences(Context context) {
        return getSharedPreferences("global", Context.MODE_PRIVATE);
    }


    /**
     * Posle RegistrationMessage na server pres CCS (XMPP)
     *
     * Zjisti udaje o zarizeni pripoji GcmID a zaregistruje zarizeni na serveru
     */
    private void sendGcmIdToServer() {

        // Najde google account v zarizeni
        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] accounts = manager.getAccountsByType("com.google");

        // Zjisti unikatni identifikator zarizeni
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String identifier = null;
        if (tm != null)
            identifier = tm.getDeviceId();
        if (identifier == null || identifier .length() == 0)
            identifier = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        RegistrationMessage msg = new RegistrationMessage();

        msg.setGcmId(gcmId);
        msg.setIdentifier(identifier);
        msg.setBrand(Build.BRAND);
        msg.setModel(Build.MODEL);
        msg.setGoogleAccountEmail( accounts[0].name );

        // Posle zpravu
        Worker worker = new Worker(this);
        worker.sendMessage(msg);
    }


    /**
     * Pri navraceni do aktivity
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Overi jestli je dostupne Google Play services
        checkPlayServices();

        // Aktualizuje stav aktivity
        refreshDeviceAdminEnabled();
        refreshGCMRegistered();

    }

    /**
     * Nastavi spravny text na tlacitko Registrovat/Deregistrovat GCM
     */
    public void refreshGCMRegistered() {
        if (isGCMRegistered()) {
            btnGcmRegistrationToggle.setText(R.string.btnGcmUnregistration);
        } else {
            btnGcmRegistrationToggle.setText(R.string.btnGcmRegistration);
        }
    }

    /**
     * Nastavi spravny text na tlacitko Registrovat/Deregistrovat DeviceAdmin
     */
    public void refreshDeviceAdminEnabled() {
        if (isDeviceAdminActive()) {
            btnDeviceAdminToggle.setText(R.string.btnDeviceAdminDisabled);
        } else {
            btnDeviceAdminToggle.setText(R.string.btnDeviceAdminEnabled);
        }
    }


    /**
     * Vraceni do Activity pri "cekani na vysledek"
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_ENABLE_DEVICEADMIN: // Vraceni po formuláři pro aktivaci DeviceAdmin
                    Log.v(TAG, "Enabling Policies Now");

                    refreshDeviceAdminEnabled();
                    break;
            }
        }
    }

    /**
     * @return Je aplikace aktivni jako DeviceAdmin
     */
    private boolean isDeviceAdminActive() {
        return devicePolicyManager.isAdminActive(devicePolicyAdmin);
    }

    /**
     * @return Je zarizei zaregistrovani na GCM serveru
     */
    private boolean isGCMRegistered() {
        return !gcmId.isEmpty();
    }


    /**
     * Vytvoreni menu
     * @param menu
     * @return bylo obslouzeno?
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Vytahne menu z XML
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /**
     * Kliknuti na polozku v menu
     * @param item
     * @return bylo obslouzeno?
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // ID stisknute polozky
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
