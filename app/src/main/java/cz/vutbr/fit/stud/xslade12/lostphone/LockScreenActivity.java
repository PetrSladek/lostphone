package cz.vutbr.fit.stud.xslade12.lostphone;

import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class LockScreenActivity extends WithSoundActivity {

    DevicePolicyManager mDPM;
    ComponentName mDeviceAdminSample;
    Worker worker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        worker = new Worker(this);

        // home key is locked since then
//        homeKeyLocker.unlock();
//        // home key is unlocked since

//        startService(new Intent(this, LockScreenService.class));
//
//        super.onCreate(savedInstanceState);
//        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
//        alertDialog.setTitle("your title");
//        alertDialog.setMessage("your message");
//        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
//
//        alertDialog.show();
//
//



        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);;
        mDeviceAdminSample = new ComponentName(this, DevicePolicyReceiver.class);

        // http://stackoverflow.com/questions/3594532/how-to-programmaticaly-lock-screen-in-android
        // http://stackoverflow.com/questions/4545079/lock-the-android-device-programatically
////
//            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
//            KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
////
//            lock.reenableKeyguard();
        //lock.disableKeyguard()
        // ---

        //Set up our Lockscreen
        makeFullScreen();

        setContentView(R.layout.activity_lock_screen);

        String text = worker.getPreferences().getString("displayText", null);
        TextView textView = (TextView) findViewById(R.id.displayText);
        textView.setText( text );

        // your text box
        EditText editUnlock = (EditText) findViewById(R.id.editUnlock);
        final Button btnUnlock = (Button) findViewById(R.id.btnUnlock);

        editUnlock.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    btnUnlock.performClick();
                    return true;
                }
                return false;
            }
        });


        processIntent(getIntent());

    }

    /**
     * A simple method that sets the screen to fullscreen.  It removes the Notifications bar,
     * the Actionbar and the virtual keys (if they are on the phone)
     */
    public void makeFullScreen() {

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
////
////        over lockscreen http://stackoverflow.com/questions/3629179/android-activity-over-default-lock-screen
////        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
//
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
////        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON, WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED, WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
////
////        //Make us non-modal, so that others can receive touch events.
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
////        //...but notify us that it happened.
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
////
//
//        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//        getWindow().setFormat(PixelFormat.TRANSLUCENT);


        getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);


//        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
//                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
//                PixelFormat.TRANSLUCENT);
//
//        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
//
//        ViewGroup mTopView = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_lock_screen, null);
//        getWindow().setAttributes(params);
//        wm.addView(mTopView, params);




//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SYSTEM);

//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        if (Build.VERSION.SDK_INT < 19) { //View.SYSTEM_UI_FLAG_IMMERSIVE is only on API 19+
//            this.getWindow().getDecorView()
//                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        } else {
//            this.getWindow().getDecorView()
//                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
//        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        soundOff();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        processIntent(intent);
    }

    protected void processIntent(Intent intent) {
        if(intent.getExtras() != null && intent.getExtras().getBoolean("stolenMode", false)) {
            soundOn(R.raw.stolen);
        } else {
            soundOff();
        }
    }

    protected void unlockPhone() {

        mDPM.setPasswordQuality(mDeviceAdminSample, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
        mDPM.setPasswordMinimumLength(mDeviceAdminSample, 0);
        mDPM.resetPassword("", DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE);
        lock.disableKeyguard();

        worker.passwordSuccess();

//        android.os.Process.killProcess(android.os.Process.myPid());
        finish();
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        // If we've received a touch notification that the user has touched
//        // outside the app, finish the activity.
//        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
////            finish();
//            return true;
//        }
//
//        // Delegate everything else to Activity.
//        return super.onTouchEvent(event);
//    }

    @Override
    public void onBackPressed() {
        return; //Do nothing!
    }



    public void onClickBtnCallOwner(View view) {

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE);
        lock.disableKeyguard();

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + worker.getPreferences().getString("ownerPhoneNumber", null)));
        startActivityForResult(callIntent, 666);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 666) {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE);
            lock.reenableKeyguard();
        }


    }

    public void onClickBtnUnlock(View view) {
        EditText editUnlock = (EditText) findViewById(R.id.editUnlock);
        String pin = editUnlock.getText().toString();

        if (pin.equals( worker.getPreferences().getString("password", null) )) {

            unlockPhone(); // zrusi heslo

        } else {
            worker.passwordFailed();

            editUnlock.setText(""); // vymaze input s pinem
            Toast.makeText(this, R.string.wrongPin, Toast.LENGTH_LONG).show();
        }
    }


    //here's where most of the magic happens
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Do this on key down
        boolean up = event.getAction() == KeyEvent.ACTION_UP;
        //flags to true if the event we are getting is the up (release)
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_FOCUS:
                return true;
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_VOLUME_UP:
                return true;
            case KeyEvent.KEYCODE_POWER:
                finish();
                return true;
            case KeyEvent.KEYCODE_HOME:
                return true;
            default:
            break;
        }
        return super.dispatchKeyEvent(event);
    }

}
