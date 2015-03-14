package cz.vutbr.fit.stud.xslade12.lostphone;

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

import cz.vutbr.fit.stud.xslade12.lostphone.messages.UnlockMessage;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.WrongPassMessage;

public class MyDevicePolicyReceiver extends DeviceAdminReceiver {

    private final String LOG_TAG = "ActiveDevicePolicy";

    @Override
    public void onDisabled(Context context, Intent intent) {
//        Toast.makeText(context, "Truiton's Device Admin Disabled",
//                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
//        Toast.makeText(context, "Truiton's Device Admin is now enabled",
//                Toast.LENGTH_SHORT).show();
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        CharSequence disableRequestedSeq = "Requesting to disable Device Admin";
        return disableRequestedSeq;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onPasswordChanged(Context context, Intent intent) {
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onPasswordExpiring(Context context, Intent intent) {
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {

        final Worker worker = new Worker(context);
        worker.passwordFailed();

        Toast.makeText(context, "Password failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {

        UnlockMessage msg = new UnlockMessage();
        Worker worker = new Worker(context);
        worker.sendMessage(msg);
        worker.setLocked(false);

        Toast.makeText(context, "Access Granted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLockTaskModeEntering(Context context, Intent intent, String pkg) {
        Toast.makeText(context, "LockTaskModeEntering", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLockTaskModeExiting(Context context, Intent intent) {
//        super.onLockTaskModeExiting(context, intent);
        Toast.makeText(context, "LockTaskModeExiting", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG, "MyDevicePolicyReciever Received: " + intent.getAction());
        super.onReceive(context, intent);
    }
}