package cz.vutbr.fit.stud.xslade12.lostphone.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import cz.vutbr.fit.stud.xslade12.lostphone.messages.GotchaMessage;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.RingingTimeoutMessage;


public class WithSoundActivity extends Activity {


    protected static MediaPlayer mp = null;

    protected int originalRingerMode;
    protected boolean originalBluetoothEnabled;


    public void soundOn(int resource) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // zazalohuju puvodni ringerMode;
        originalRingerMode = audioManager.getRingerMode();

        // Zesilim na max a zapnu zvuky
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);



        mp = MediaPlayer.create(getApplicationContext(), resource);
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


    public void bluetoothOff() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
            return;

        originalBluetoothEnabled = bluetoothAdapter.isEnabled();
        setBluetooth(false); // disable bluetooth
    }

     public void bluetoothOn() {
         setBluetooth(originalBluetoothEnabled); // disable bluetooth
     }


    public static boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
            return false;
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        }
        else if(!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }

}
