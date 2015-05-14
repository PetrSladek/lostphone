package cz.vutbr.fit.stud.xslade12.lostphone.activities;

import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import cz.vutbr.fit.stud.xslade12.lostphone.R;
import cz.vutbr.fit.stud.xslade12.lostphone.Worker;
import cz.vutbr.fit.stud.xslade12.lostphone.recievers.DevicePolicyReceiver;

/**
 * Obrazovka uzamknutého zařízení
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class LockScreenActivity extends WithSoundActivity {

    public static final int REQUEST_CODE_CALLOWNER = 666;
    DevicePolicyManager devicePolicyManager;
    ComponentName devicePolicyAdmin;
    Worker worker;

    /**
     * Metoda spustena po vytvorení activity (viz life cyklus activity)
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        worker = new Worker(this);

        // DevicePolicyManager a komponenta DevicePolieReciever
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);;
        devicePolicyAdmin = new ComponentName(this, DevicePolicyReceiver.class);

        // Nastav na fullscreen a pres bezny lockscreen
        makeFullScreen();

        // Nastaveni XML s layoutem
        setContentView(R.layout.activity_lock_screen);

        // Pole pro zadani hesla
        EditText editUnlock = (EditText) findViewById(R.id.editUnlock);
        // Tlacitko potvrzeni
        final Button btnUnlock = (Button) findViewById(R.id.btnUnlock);

        // Stuisknutí "Enter" po vyplneněni hesla
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

        // Zpracuje intent
        processIntent(getIntent());
    }

    /**
     * Nastavi parametry okna na fullscreen
     */
    public void makeFullScreen() {

        getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

    }


    /**
     * Metorda pri zruseni activity (viz life cycle activity)
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        soundOff();
    }

    /**
     * Metoda ktera se vola pri opetovnem vyvolani activity v pripade ze uz jednou bezi
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        processIntent(intent);
    }

    /**
     * Zpracovani intentu pri spusteni nebo opetovnem spusteni
     * @param intent
     */
    protected void processIntent(Intent intent) {
        // Podle extras zapnu mood ukradeni nebi ne
        if(intent.getExtras() != null && intent.getExtras().getBoolean("stolenMode", false)) {
            soundOn(R.raw.stolen);
        } else {
            soundOff();
        }

        // Prepise text na displeji
        String text = worker.getPreferences().getString("displayText", null);
        TextView textView = (TextView) findViewById(R.id.displayText);
        textView.setText( text );
    }


    /**
     * Pretizeni stisku tlacika zpět
     */
    @Override
    public void onBackPressed() {
        return; //Nic nedelej!
    }

    /**
     * Metoda obstará odemknutí zarizeni
     */
    protected void unlockPhone() {

        // Vynuluje heslo
        devicePolicyManager.setPasswordQuality(devicePolicyAdmin, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
        devicePolicyManager.setPasswordMinimumLength(devicePolicyAdmin, 0);
        devicePolicyManager.resetPassword("", DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);

        // Vypne standarni lockscreen
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE);
        lock.disableKeyguard();

        // Posle zpravu o tom ze zarizeni bylo odemknuto
        worker.passwordSuccess();

        // Po dvou vterinach vypne activitu
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LockScreenActivity.this.finish();
            }
        }, 2000);

    }

    /**
     * Obsluha tlafitka "Zavolat majiteli"
     * @param view
     */
    public void onClickBtnCallOwner(View view) {

        // Docasne vyplne uzamknutí
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE);
        lock.disableKeyguard();

        // Vytoci cislo majitele a "pocka na vysledek"
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + worker.getPreferences().getString("ownerPhoneNumber", null)));
        startActivityForResult(callIntent, REQUEST_CODE_CALLOWNER);

    }


    /**
     * Vraceni do Activity pri "cekani na vysledek"
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CALLOWNER) { // Pokud jsme se vratili z volani majiteli, obnovime bezny lockscreen
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE);
            lock.reenableKeyguard();
        }


    }

    /**
     * Obsoluha udalosti tlacitka odemknout, po zadani PINu
     * @param view
     */
    public void onClickBtnUnlock(View view) {
        EditText editUnlock = (EditText) findViewById(R.id.editUnlock);
        String pin = editUnlock.getText().toString();

        // Pokud je spravne heslo odemkneme zarizeni
        if (pin.equals( worker.getPreferences().getString("password", null) )) {

            unlockPhone(); // zrusi heslo

        } else { // Pokud ne, oznamime chybu a zasleme zpravu o neuspesnem odemceni
            worker.passwordFailed();

            editUnlock.setText(""); // vymaze input s pinem
            Toast.makeText(this, R.string.wrongPin, Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Pretizeni udalosti HW tlacitek, aby nevypnuli lockscreen
     * @param event
     * @return
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Pri domacknuti
        boolean up = event.getAction() == KeyEvent.ACTION_UP;

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
