package cz.vutbr.fit.stud.xslade12.lostphone;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import cz.vutbr.fit.stud.xslade12.lostphone.messages.RegistrationMessage;


public class MainActivity extends Activity {

    DevicePolicyManager devicePolicyManager;
    ComponentName devicePolicyAdmin;
    private CheckBox checkBoxDevicePolicyEnabled;
    private CheckBox checkBoxGCMRegistered;

    protected static final int REQUEST_ENABLE = 1;

    GoogleCloudMessaging gcm;
    String regid;

    public static final String PROPERTY_REG_ID = "gcmId";
    private static final String PROPERTY_APP_VERSION = "appVersion";


    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


    // https://console.developers.google.com/
    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "941272288463";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "LostPhone";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        devicePolicyAdmin = new ComponentName(this, MyDevicePolicyReceiver.class);

        checkBoxDevicePolicyEnabled = (CheckBox) findViewById(R.id.checkBoxDevicePolicyEnabled);
        checkBoxGCMRegistered = (CheckBox) findViewById(R.id.checkBoxGCMRegistered);

        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(this.getApplicationContext()); // vytahne regid z uloziste

            if (!isGCMRegistered()) { // kdyz  neni zaregistrovano tak zaregistrujeme
                System.out.println("RegID: - neni zaregistrovano -");
                this.setTitle("- neni zeregistrovano -");
                registerGCMInBackground();
            } else {
                System.out.println("RegID: " + regid);
                this.setTitle(regid);
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

//        final Worker worker = new Worker(this);
////        worker.passwordFailed();
////        worker.getCallLog();
//        worker.startStolenMode("pin");
//
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                worker.stopStolenMode();
//            }
//        }, 15000);
    }


    /*private void showDialog(String aTitle){
        WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        final View mView = View.inflate(getApplicationContext(), R.layout.fragment_overlay, null);
        mView.setTag(TAG);

//        int top = getApplicationContext().getResources().getDisplayMetrics().heightPixels / 2;

//        LinearLayout dialog = (LinearLayout) mView.findViewById(R.id.overlay_dialog);
//        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) dialog.getLayoutParams();
//        lp.topMargin = top;
//        lp.bottomMargin = top;
//        mView.setLayoutParams(lp);

        Button imageButton = (Button) mView.findViewById(R.id.btnUnlock);
//        lp = (ViewGroup.MarginLayoutParams) imageButton.getLayoutParams();
//        lp.topMargin = top - 58;
//        imageButton.setLayoutParams(lp);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mView.setVisibility(View.INVISIBLE);
            }
        });

        TextView title = (TextView) mView.findViewById(R.id.text);
        title.setText(aTitle);

        final WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 0,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON ,
                PixelFormat.RGBA_8888);

        mView.setVisibility(View.VISIBLE);
//        mAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.in);
//        mView.startAnimation(mAnimation);
        mWindowManager.addView(mView, mLayoutParams);

    }
*/


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
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
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        SharedPreferences prefs = getAppSharedPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

//    private void storePhoneNumber(Context context) {
//        TelephonyManager telephoneMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        String phoneNumber = telephoneMgr.getLine1Number();
//
//        Worker worker = new Worker(context);
//        worker.storePhoneNumber(phoneNumber);
//    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getAppSharedPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerGCMInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
                    }

                    regid = gcm.register(SENDER_ID);

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(MainActivity.this.getApplicationContext(), regid);

                    // Ulozi aktualni telefonni cislo (pro indikaci zmeny)
//                    storePhoneNumber(MainActivity.this.getApplicationContext());

                } catch (IOException ex) {
                    Log.i(TAG, "Error :" + ex.getMessage());
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }

                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            }
        }.execute(null, null, null);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
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
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getAppSharedPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences("global", Context.MODE_PRIVATE);
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {

        System.out.println("RegID: " + regid);
        this.setTitle(regid);


        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] accounts = manager.getAccountsByType("com.google");


        // Zjisti udaje o zarizeni jako např IMEI atp
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String identifier = null;
        if (tm != null)
            identifier = tm.getDeviceId();
        if (identifier == null || identifier .length() == 0)
            identifier = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        RegistrationMessage msg = new RegistrationMessage();

        msg.setGcmId( regid );
        msg.setIdentifier(identifier);
        msg.setBrand(Build.BRAND);
        msg.setModel(Build.MODEL);
        msg.setGoogleAccountEmail( accounts[0].name );

        Worker worker = new Worker(this);
        worker.sendMessage(msg);

    }


    @Override
    protected void onResume() {
        super.onResume();


        // Check device for Play Services APK.
        checkPlayServices();

        // Nastavi checkox a prida mu listener
        solveCheckboxDevicePolicyEnabled();

        solveCheckBoxGCMRegistered();

    }

    public void solveCheckBoxGCMRegistered() {
        if (isGCMRegistered()) {
            checkBoxGCMRegistered.setChecked(true);
        } else {
            checkBoxGCMRegistered.setChecked(false);
        }
//
//        registerGCMInBackground();
//
//        checkBoxGCMRegistered.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    registerGCMInBackground();
//                } else {
//                    try {
//                        gcm.unregister();
//                        storeRegistrationId(MainActivity.this.getApplicationContext(), "");
//                    } catch (IOException exx) {
//                    }
//                }
//            }
//        });
    }

    public void solveCheckboxDevicePolicyEnabled() {
        if (isMyDevicePolicyReceiverActive()) {
            checkBoxDevicePolicyEnabled.setChecked(true);
        } else {
            checkBoxDevicePolicyEnabled.setChecked(false);
        }
        checkBoxDevicePolicyEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, devicePolicyAdmin);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.admin_explanation));
                    startActivityForResult(intent, REQUEST_ENABLE);
                } else {
                    devicePolicyManager.removeActiveAdmin(devicePolicyAdmin);
                }
            }
        });
    }





    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ENABLE:
                    Log.v(TAG, "Enabling Policies Now");

//                    devicePolicyManager.setMaximumTimeToLock(devicePolicyAdmin, 30000L);
//                    devicePolicyManager.setMaximumFailedPasswordsForWipe(devicePolicyAdmin, 5);
//                    devicePolicyManager.setPasswordQuality(devicePolicyAdmin, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);
//                    devicePolicyManager.setCameraDisabled(devicePolicyAdmin, true);
//                    boolean isSufficient = devicePolicyManager.isActivePasswordSufficient();
//                    if (isSufficient) {
//                        devicePolicyManager.lockNow();
//                    } else {
////                        Intent setPasswordIntent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
////                        startActivityForResult(setPasswordIntent, SET_PASSWORD);
////                        devicePolicyManager.setPasswordExpirationTimeout(devicePolicyAdmin, 10000L);
//                    }
                    break;
            }
        }
    }

    private boolean isMyDevicePolicyReceiverActive() {
        return devicePolicyManager.isAdminActive(devicePolicyAdmin);
    }

    private boolean isGCMRegistered() {
        return !regid.isEmpty();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    public void onClickBtnStart(View view ){

        if(view.getId() == R.id.btnStartRingActivity) {
            Intent intent = new Intent(this, RingingActivity.class);
            startActivity(intent);
        } else if(view.getId() == R.id.btnStartLockScreenActivity) {
//            showDialog("Textik na cosi");
            Intent intent = new Intent(this, LockScreenActivity.class);
//            startActivity(intent);
        } else if(view.getId() == R.id.btnStartDemoActivity) {
            Intent intent = new Intent(this, DemoActivity.class);
            startActivity(intent);



        }
    }

}
