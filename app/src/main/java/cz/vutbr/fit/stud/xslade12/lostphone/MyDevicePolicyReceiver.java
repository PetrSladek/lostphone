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
        Toast.makeText(context, "Truiton's Device Admin Disabled",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        Toast.makeText(context, "Truiton's Device Admin is now enabled",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        CharSequence disableRequestedSeq = "Requesting to disable Device Admin";
        return disableRequestedSeq;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        Toast.makeText(context, "Device password is now changed",
                Toast.LENGTH_SHORT).show();
        DevicePolicyManager localDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName localComponent = new ComponentName(context,MyDevicePolicyReceiver.class);
        localDPM.setPasswordExpirationTimeout(localComponent, 0L);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onPasswordExpiring(Context context, Intent intent) {
        // This would require API 11 an above
        Toast.makeText(context,"Truiton's Device password is going to expire, please change to a new password",Toast.LENGTH_LONG).show();

        DevicePolicyManager localDPM = (DevicePolicyManager) context
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName localComponent = new ComponentName(context, MyDevicePolicyReceiver.class);
        long expr = localDPM.getPasswordExpiration(localComponent);
        long delta = expr - System.currentTimeMillis();
        boolean expired = delta < 0L;
        if (expired) {
            localDPM.setPasswordExpirationTimeout(localComponent, 10000L);
            Intent passwordChangeIntent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
            passwordChangeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(passwordChangeIntent);
        }
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {

        final WrongPassMessage msg = new WrongPassMessage();
        final Worker worker = new Worker(context);

        final FrontCameraController fc = new FrontCameraController(context);
        if(!fc.hasCamera()) {
            worker.sendMessage(msg); // kdyz neni fotak posli jen zpravu
        } else {
            fc.open();
            fc.setPictureCallback(new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    File pictureFile = fc.getOutputMediaFile();
                    try {
                        if (pictureFile == null) {
                            throw new IOException("Error creating media file, check storage permissions");
                        }

                        Log.d("FrontCAM", "File created");
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        fos.write(data);
                        fos.close();

                        msg.setFrontPhoto(pictureFile); // nastav fotku ke zprave

                    } catch (FileNotFoundException e) {
                        Log.d("FrontCAM", "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d("FrontCAM", "Error accessing file: " + e.getMessage());
                    } finally {
                        worker.sendMessage(msg); // odesli zpravu
                     }
                }
            });
            fc.takePicture();
            fc.release();
        }


        Toast.makeText(context, "Password failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {

        UnlockMessage msg = new UnlockMessage();
        Worker worker = new Worker(context);
        worker.sendMessage(msg);


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