package cz.vutbr.fit.stud.xslade12.lostphone;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;



public class MainActivity extends Activity {

    private DevicePolicyManager activeDevicePolicyManager;
    private final String LOG_TAG = "ActiveDevicePolicy";

    DevicePolicyManager truitonDevicePolicyManager;
    ComponentName truitonDevicePolicyAdmin;
    private CheckBox checkBoxDevicePolicyEnabled;
    private CheckBox checkBoxGCMRegistered;

    protected static final int REQUEST_ENABLE = 1;
    protected static final int SET_PASSWORD = 2;



    GoogleCloudMessaging gcm;
    String regid;

    public static final String PROPERTY_REG_ID = "registration_id";
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

        truitonDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        truitonDevicePolicyAdmin = new ComponentName(this, MyDevicePolicyReceiver.class);

        checkBoxDevicePolicyEnabled = (CheckBox) findViewById(R.id.checkBoxDevicePolicyEnabled);
        checkBoxGCMRegistered = (CheckBox) findViewById(R.id.checkBoxGCMRegistered);

        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(this.getApplicationContext());

            if (isGCMRegistered()) {
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


        final FrontCameraController fc = new FrontCameraController(this);
        if(fc.hasCamera()) {
            fc.open();
            fc.setPictureCallback(new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    File pictureFile = fc.getOutputMediaFile();

                    if (pictureFile == null) {
                        Log.d("FrontCAM", "Error creating media file, check storage permissions");
                        return;
                    }

                    try {
                        Log.d("FrontCAM", "File created");
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        fos.write(data);
                        fos.close();
                    } catch (FileNotFoundException e) {
                        Log.d("FrontCAM", "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d("FrontCAM", "Error accessing file: " + e.getMessage());
                    }
                }
            });
            fc.takePicture();
            fc.release();
        }

    }




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
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
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

                    msg = "Device registered, registration ID=" + regid;
                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(MainActivity.this.getApplicationContext(), regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.

//                    try {
//                        gcm.unregister();
//                    } catch (IOException exx) {
//                        msg = "Error :" + exx.getMessage();
//                    }
                }

                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
//                mDisplay.append(msg + "\n");
//                    solveCheckBoxGCMRegistered();
            }
        }.execute(null, null, null);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(DemoActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
        System.out.println("RegID: " + regid);
        this.setTitle(regid);


        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.getDeviceId();
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
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, truitonDevicePolicyAdmin);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.admin_explanation));
                    startActivityForResult(intent, REQUEST_ENABLE);
                } else {
                    truitonDevicePolicyManager.removeActiveAdmin(truitonDevicePolicyAdmin);
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
                    Log.v(LOG_TAG, "Enabling Policies Now");

//                    truitonDevicePolicyManager.setMaximumTimeToLock(truitonDevicePolicyAdmin, 30000L);
//                    truitonDevicePolicyManager.setMaximumFailedPasswordsForWipe(truitonDevicePolicyAdmin, 5);
//                    truitonDevicePolicyManager.setPasswordQuality(truitonDevicePolicyAdmin, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);
//                    truitonDevicePolicyManager.setCameraDisabled(truitonDevicePolicyAdmin, true);
//                    boolean isSufficient = truitonDevicePolicyManager.isActivePasswordSufficient();
//                    if (isSufficient) {
//                        truitonDevicePolicyManager.lockNow();
//                    } else {
////                        Intent setPasswordIntent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
////                        startActivityForResult(setPasswordIntent, SET_PASSWORD);
////                        truitonDevicePolicyManager.setPasswordExpirationTimeout(truitonDevicePolicyAdmin, 10000L);
//                    }
                    break;
            }
        }
    }

    private boolean isMyDevicePolicyReceiverActive() {
        return truitonDevicePolicyManager
                .isAdminActive(truitonDevicePolicyAdmin);
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
            Intent intent = new Intent(this, LockScreenActivity.class);
            startActivity(intent);
        } else if(view.getId() == R.id.btnStartDemoActivity) {
            Intent intent = new Intent(this, DemoActivity.class);
            startActivity(intent);
        }
    }

}
