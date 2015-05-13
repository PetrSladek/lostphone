package cz.vutbr.fit.stud.xslade12.lostphone.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cz.vutbr.fit.stud.xslade12.lostphone.activities.LockScreenActivity;
import cz.vutbr.fit.stud.xslade12.lostphone.Worker;

/**
 * Broadcast reciever reagující na zamknuti zarizeni
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class LockScreenReceiver extends BroadcastReceiver {
    /**
     * Otevre LockScreenActivity
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // Pokud jde o udalost zapnutí displeje nebo nastartování zařízení
        if(action.equals(Intent.ACTION_SCREEN_ON) || action.equals(Intent.ACTION_BOOT_COMPLETED))
        {

            Worker worker = new Worker(context);
            if(!worker.isLocked()) { // pokud zarizeni neni aktualne zamknuté tak koncim.
                return;
            }

            // spustim LockScreen activity
            Intent i = new Intent(context, LockScreenActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
