package cz.vutbr.fit.stud.xslade12.lostphone.recievers;

import android.annotation.TargetApi;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cz.vutbr.fit.stud.xslade12.lostphone.Worker;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.UnlockMessage;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.WrongPassMessage;

/**
 * Broadcast reciever reagující na zmenu ocharen politky zarizeni nebo na spatne/spravne zadane heslo.
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class DevicePolicyReceiver extends DeviceAdminReceiver {

    private final String LOG_TAG = "ActiveDevicePolicy";

    /**
     * Aplikace byla zrusena jako spravce zarizeni
     * @param context
     * @param intent
     */
    @Override
    public void onDisabled(Context context, Intent intent) {
    }

    /**
     * Aplikace byla aktivovana jako spravce zarizeni
     * @param context
     * @param intent
     */
    @Override
    public void onEnabled(Context context, Intent intent) {
    }

    /**
     * Text ktery se zobrazi uzivateli pokud chce deaktivovat apliakci jako spravce zarizeni.
     * @param context
     * @param intent
     * @return
     */
    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        CharSequence disableRequestedSeq = "Po deaktivování nebude aplikace pracovat správně!";
        return disableRequestedSeq;
    }

    /**
     * Kdyz je zmeneno heslo
     * @param context
     * @param intent
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onPasswordChanged(Context context, Intent intent) {
    }

    /**
     * Kdyz vypresela platnost hesla
     * @param context
     * @param intent
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onPasswordExpiring(Context context, Intent intent) {
    }

    /**
     * Kdyz bylo zadano spatne heslo
     * @param context
     * @param intent
     */
    @Override
    public void onPasswordFailed(Context context, Intent intent) {

        // Posleme zpavu o tom ze se nekdo snazi dostat do zarizeni
        final Worker worker = new Worker(context);
        worker.passwordFailed();

    }

    /**
     * Kdyz bylo zadano spravne heslo
     * @param context
     * @param intent
     */
    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {


        // Posleme zpravu o tom ze nekdo odemkl zarizeni
        Worker worker = new Worker(context);
        worker.passwordSuccess();

    }

    @Override
    public void onLockTaskModeEntering(Context context, Intent intent, String pkg) {
    }

    @Override
    public void onLockTaskModeExiting(Context context, Intent intent) {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }
}