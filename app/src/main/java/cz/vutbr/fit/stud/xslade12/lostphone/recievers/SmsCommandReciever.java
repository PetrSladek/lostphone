package cz.vutbr.fit.stud.xslade12.lostphone.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import cz.vutbr.fit.stud.xslade12.lostphone.Worker;

/**
 * Created by Peggy on 14.3.2015.
 */
public class SmsCommandReciever extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Worker worker = new Worker(context);

        SmsMessage msg;
        if (intent.getExtras() != null)
        {
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");
            for (int i=0; i < pdus.length; i++){
                msg = SmsMessage.createFromPdu((byte[])pdus[i]);

                if(msg.getMessageBody().equals("CMD STOLEN")) {

                    //generate a 4 digit integer 1000 <10000
                    int randomPIN = (int)(Math.random()*9000)+1000;

                    String pin = String.valueOf(randomPIN); // nahodny PIN odemknout pujde jen timto pinem nebo nasledujicim prikazem
                    worker.startStolenMode(pin);

                    // Posle SMS s odemikacim pinem zpet

                    String message = "PIN pro odekmnuti je: " + pin;
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(msg.getOriginatingAddress(), null, message, null, null);

                } else if(msg.getMessageBody().equals("CMD STOLEN STOP")) {
                    worker.stopStolenMode();
                }

            }
        }
    }
}