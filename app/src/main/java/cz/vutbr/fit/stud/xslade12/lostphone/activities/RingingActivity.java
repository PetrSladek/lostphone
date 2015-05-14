package cz.vutbr.fit.stud.xslade12.lostphone.activities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import cz.vutbr.fit.stud.xslade12.lostphone.R;
import cz.vutbr.fit.stud.xslade12.lostphone.Worker;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.GotchaMessage;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.RingingTimeoutMessage;

/**
 * Aktivita obrazovku prozvonení
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class RingingActivity extends WithSoundActivity {

    protected static Worker worker;

    /**
     * Instance kamery
     */
    protected static Camera cam = null; // has to be static, otherwise onDestroy() destroys it
    /**
     * Instance vibratoru
     */
    protected static Vibrator vib = null;

    /**
     * Tag pro logování
     */
    static final String TAG = "LostPhone";

    /**
     * Udalost pri vytvoreni activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        worker = new Worker(this);

        // Nastaveni XML s layoutem
        setContentView(R.layout.activity_ringing);

        // Nastav na fullscreen, pres bezny lockscreen a rozsvit displej
        makeFullScreen();

        // Zapnout zvuk sireny
        soundOn(R.raw.sirena);
        // Rozsvitit blask diodu
        flashLightOn();
        // Rozblikat pozadi activity
        backgroundBlinkingOn();
        // Zacit vybrovat
        vibratorOn();
        // vypnu bluetooth kvuli např zaplemu Handsfree
        bluetoothOff();

        // Pokud je nastaven parametr za jak dlouho se má vypnout, nastavi se casovac
        long closeAfter = getIntent().getLongExtra("closeAfter", 0);
        if(closeAfter > 0) {
            // Naplanuj vypnuti na x sekund
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    // Posle zpravu o tom ze prozvanani koncilo
                    worker.sendMessage(new RingingTimeoutMessage());
                    RingingActivity.this.finish();
                }
            }, closeAfter);
        }
    }

    /**
     * Pri zruseni activity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Vypne zvuk
        soundOff();
        // Vypne blesk diodu
        flashLightOff();
        // Vypne vybrace
        vibratorOff();
        // opet povolim Bluetooth jestli bylo predtim zapnute
        bluetoothOn();

        // Ukonci proces
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * Nastavi parametry okna na fullscreen, pres lockscreen a rozsviti displej
     */
    public void makeFullScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON, WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED, WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

    /**
     * Obsluha tlacitka "Mam te"
     * @param view
     */
    public void onClickBtnGotcha(View view) {
        worker.sendMessage( new GotchaMessage() );
        finish();
    }


    /**
     * Zapnuti sekcence vibraci
     */
    public void vibratorOn() {
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Start without a delay
        // Vibrate for 100 milliseconds
        // Sleep for 1000 milliseconds
        long[] pattern = {0, 1000, 1000};

        vib.vibrate(pattern, 0);
    }

    /**
     * Ukonceni vybraci
     */
    public void vibratorOff() {
        if(vib == null)
            return;
        vib.cancel();
    }


    /**
     * Zapnuti sekcence prechodu barev na pozadi
     */
    public void backgroundBlinkingOn() {
        LinearLayout background = (LinearLayout) findViewById(R.id.background);

        final int DELAY = 100;

        ColorDrawable f1 = new ColorDrawable(Color.MAGENTA);
        ColorDrawable f2 = new ColorDrawable(Color.WHITE);
        ColorDrawable f3 = new ColorDrawable(Color.GREEN);
        ColorDrawable f4 = new ColorDrawable(Color.WHITE);

        AnimationDrawable anim = new AnimationDrawable();
        anim.addFrame(f1, DELAY);
        anim.addFrame(f2, DELAY);
        anim.addFrame(f3, DELAY);
        anim.addFrame(f4, DELAY);
        anim.setOneShot(false);

        //background.setBackgroundDrawable(anim); // This method is deprecated in API 16
        background.setBackground(anim); // Use this method if you're using API 16
        anim.start();
    }

    /**
     * Rozsviti blesk diodu
     */
    public void flashLightOn() {

        try {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cam = Camera.open();
                Camera.Parameters p = cam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
                cam.startPreview();
            } else {
                Log.i(TAG, "Camera flash light not available.");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Zhasne blesk diodu
     */
    public void flashLightOff() {
        try {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cam.stopPreview();
                cam.release();
                cam = null;
            } else {
                Log.i(TAG, "Camera flash light not available.");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


}
