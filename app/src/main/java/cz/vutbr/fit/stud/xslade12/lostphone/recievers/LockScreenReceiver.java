package cz.vutbr.fit.stud.xslade12.lostphone.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cz.vutbr.fit.stud.xslade12.lostphone.activities.LockScreenActivity;
import cz.vutbr.fit.stud.xslade12.lostphone.Worker;

public class LockScreenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        //If the screen was just turned on or it just booted up, start your Lock Activity
        if(action.equals(Intent.ACTION_SCREEN_ON) || action.equals(Intent.ACTION_BOOT_COMPLETED))
        {
//            DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            Worker worker = new Worker(context);
            if(!worker.isLocked()) {
                return;
            }

            Intent i = new Intent(context, LockScreenActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
