package cz.vutbr.fit.stud.xslade12.lostphone.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;


/**
 * Abstraktni aktivita zajistujici prehravani zvuku na pozadi
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public abstract class WithSoundActivity extends Activity {

    /**
     * Prehravac hudby
     */
    protected static MediaPlayer mediaPlayer = null;

    /**
     * Puvodni stav hlasitosti
     */
    protected int originalRingerMode;
    /**
     * Puvodni stav zapnuti BlueTooth
     */
    protected boolean originalBluetoothEnabled;


    /**
     * Zapne smycku prehravani zvuku z parametru
     *
     * + zesili hlasitost na maximum
     * @param resource
     */
    public void soundOn(int resource) {

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // zazalohuju puvodni ringerMode;
        originalRingerMode = audioManager.getRingerMode();

        // Zesilim na max a zapnu zvuky
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);

        // Spusti prehravani
        mediaPlayer = MediaPlayer.create(getApplicationContext(), resource);
        mediaPlayer.setLooping(true); // smycka
        mediaPlayer.start();
    }

    /**
     * Ukonci prehravani
     */
    public void soundOff() {
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode( originalRingerMode );
    }


    /**
     * Nastavi BlueTooth na puvodni hodnotu
     */
    public void bluetoothOff() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
            return;

        originalBluetoothEnabled = bluetoothAdapter.isEnabled();
        setBluetooth(false); // disable bluetooth
    }

    /**
     * Zapne BlueTooth
     */
     public void bluetoothOn() {
         setBluetooth(originalBluetoothEnabled); // disable bluetooth
     }


    /**
     * Nastavi Bluetoohh na vypnuto/zapnuto
     * @param enable
     * @return
     */
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

        return true;
    }

}
