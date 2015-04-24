package cz.vutbr.fit.stud.xslade12.lostphone.activities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Vibrator;
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


public class RingingActivity extends WithSoundActivity {

    protected static Worker worker;

    protected static Camera cam = null;// has to be static, otherwise onDestroy() destroys it
    protected static Vibrator vib = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringing);

        worker = new Worker(this);

        makeFullScreen();

        soundOn(R.raw.sirena);
        flashLightOn();
        backgroundBlinkingOn();
        vibratorOn();

        bluetoothOff(); // vypnu bluetooth kvuli např zaplemu Handsfree

        long closeAfter = getIntent().getLongExtra("closeAfter", 0);
        if(closeAfter > 0) {
            // Naplanuj vypnuti na x sekund
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                worker.sendMessage(new RingingTimeoutMessage());
                RingingActivity.this.finish();
                }
            }, closeAfter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        soundOff();
        flashLightOff();
        vibratorOff();

        bluetoothOn(); // opet povolim Bluetooth jestli bylo predtim zapnute

        android.os.Process.killProcess(android.os.Process.myPid());
    }


    public void makeFullScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON, WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED, WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

    public void onClickBtnGotcha(View view) {
        worker.sendMessage( new GotchaMessage() );
        finish();
    }


    public void vibratorOn() {
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Start without a delay
        // Vibrate for 100 milliseconds
        // Sleep for 1000 milliseconds
        long[] pattern = {0, 1000, 1000};

        vib.vibrate(pattern, 0);
    }

    public void vibratorOff() {
        if(vib == null)
            return;
        vib.cancel();
    }



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


    public void flashLightOn() {

        try {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cam = Camera.open();
                Camera.Parameters p = cam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
                cam.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Exception flashLightOn()", Toast.LENGTH_SHORT).show();
        }
    }

    public void flashLightOff() {
        try {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cam.stopPreview();
                cam.release();
                cam = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Exception flashLightOff", Toast.LENGTH_SHORT).show();
        }
    }


}
