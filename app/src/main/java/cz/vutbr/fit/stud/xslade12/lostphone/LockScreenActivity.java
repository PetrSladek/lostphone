package cz.vutbr.fit.stud.xslade12.lostphone;

import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;


public class LockScreenActivity extends WithSoundActivity {

    DevicePolicyManager mDPM;
    ComponentName mDeviceAdminSample;

    protected static MediaPlayer mp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



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

        setContentView(R.layout.activity_lock_screen);

        //Set up our Lockscreen
        makeFullScreen();

        onNewIntent(getIntent());

//        lockPhone("1234");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        soundOff();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    protected int originalRingerMode;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(intent.getExtras().getBoolean("stolenMode")) {
            soundOn(R.raw.stolen);
        } else {
            soundOff();
        }
    }

    protected void lockPhone(String password) {
        mDPM.resetPassword(password, 0);
        mDPM.lockNow();
    }

    protected void unlockPhone() {



        mDPM.setPasswordQuality(mDeviceAdminSample, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
        mDPM.setPasswordMinimumLength(mDeviceAdminSample, 0);
        mDPM.resetPassword("", DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE);
        lock.disableKeyguard();

        Worker worker = new Worker(this);
        worker.setLocked(false);

//        Intent startMain = new Intent(Intent.ACTION_MAIN);
//        startMain.addCategory(Intent.CATEGORY_HOME);
//        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(startMain);

        finish();
    }



    //
    //    @Override
    //    public boolean onCreateOptionsMenu(Menu menu) {
    //        // Inflate the menu; this adds items to the action bar if it is present.
    //        getMenuInflater().inflate(R.menu.lock_screen, menu);
    //        return true;
    //    }
    //
    //    @Override
    //    public boolean onOptionsItemSelected(MenuItem item) {
    //        // Handle action bar item clicks here. The action bar will
    //        // automatically handle clicks on the Home/Up button, so long
    //        // as you specify a parent activity in AndroidManifest.xml.
    //        int id = item.getItemId();
    //        if (id == R.id.action_settings) {
    //            return true;
    //        }
    //        return super.onOptionsItemSelected(item);
    //    }


    /**
     * A simple method that sets the screen to fullscreen.  It removes the Notifications bar,
     * the Actionbar and the virtual keys (if they are on the phone)
     */
    public void makeFullScreen() {

        // over lockscreen http://stackoverflow.com/questions/3629179/android-activity-over-default-lock-screen
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON, WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED, WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);


        // Make us non-modal, so that others can receive touch events.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        // ...but notify us that it happened.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);



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
    public boolean onTouchEvent(MotionEvent event) {
        // If we've received a touch notification that the user has touched
        // outside the app, finish the activity.
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
//            finish();
            return true;
        }

        // Delegate everything else to Activity.
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        return; //Do nothing!
    }



    public void onClickBtnCallOwner(View view) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:123456789"));
        startActivity(callIntent);
    }


    public void onClickBtnUnlock(View view) {
        EditText editUnlock = (EditText) findViewById(R.id.editUnlock);
        String pin = editUnlock.getText().toString();


        if (pin.equals("1234")) {
            unlockPhone(); // zrusi heslo

        } else {

            Worker worker = new Worker(this);
            worker.passwordFailed();

            Toast.makeText(this, R.string.wrongPin, Toast.LENGTH_SHORT).show();
        }
    }


    //
    //    //here's where most of the magic happens
    //    @Override
    //    public boolean dispatchKeyEvent(KeyEvent event) {
    //        // Do this on key down
    //        boolean up = event.getAction() == KeyEvent.ACTION_UP;
    //        //flags to true if the event we are getting is the up (release)
    //        switch (event.getKeyCode()) {
    //            //case KeyEvent.KEYCODE_VOLUME_UP:
    //            case KeyEvent.KEYCODE_VOLUME_DOWN:
    //            case KeyEvent.KEYCODE_FOCUS:
    //                if (up) {
    //                    break;//break without return means pass on to other processes
    //                    //doesn't consume the press
    //                }
    //
    //                Log.v("key event", "locked key");
    //
    //                return true;
    //            //returning true means we handled the event so don't pass it to other processes
    //
    //            case KeyEvent.KEYCODE_CAMERA:
    //            case KeyEvent.KEYCODE_VOLUME_UP:
    //                Log.v("key event","wake key");
    ////                awake = true;
    ////                setBright((float) 0.1);//tell screen to go on with 10% brightness
    //                return true;
    //
    //            case KeyEvent.KEYCODE_POWER:
    //                Log.v("key event","unlock key");
    //                finish();
    //                return true;
    //
    //            case KeyEvent.KEYCODE_HOME:
    //                Toast.makeText(this, "Muhehe", Toast.LENGTH_LONG).show();
    //                return true;
    //
    //            default:
    //
    //                break;
    //        }
    //        return super.dispatchKeyEvent(event);
    //    }

}
