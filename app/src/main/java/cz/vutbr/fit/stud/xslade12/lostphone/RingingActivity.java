package cz.vutbr.fit.stud.xslade12.lostphone;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import cz.vutbr.fit.stud.xslade12.lostphone.messages.GotchaMessage;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.UnlockMessage;


public class RingingActivity extends Activity {


    public static MediaPlayer mp = null;
    public static Camera cam = null;// has to be static, otherwise onDestroy() destroys it
    public static Vibrator vib = null;



    public int originalRingerMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringing);

        makeFullScreen();

        soundOn();
        flashLightOn();
        backgroundBlinkingOn();
        vibratorOn();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        GotchaMessage msg = new GotchaMessage();
        Worker worker = new Worker(getApplicationContext());
        worker.sendMessage(msg);

        soundOff();
        flashLightOff();
        vibratorOff();

        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * A simple method that sets the screen to fullscreen.  It removes the Notifications bar,
     *   the Actionbar and the virtual keys (if they are on the phone)
     */
    public void makeFullScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON, WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED, WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

    public void onClickBtnGotcha(View view) {
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




    public void soundOn() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // zazalohuju puvodni ringerMode;
        originalRingerMode = audioManager.getRingerMode();

        // Zesilim na max a zapnu zvuky
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);



        mp = MediaPlayer.create(getApplicationContext(), R.raw.woopwoop);
        mp.setLooping(true);
        mp.start();
    }

    public void soundOff() {
        if(mp == null)
            return;

        mp.stop();
        mp.release();

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode( originalRingerMode );
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
