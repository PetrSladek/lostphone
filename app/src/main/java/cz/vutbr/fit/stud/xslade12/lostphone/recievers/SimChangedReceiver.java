package cz.vutbr.fit.stud.xslade12.lostphone.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import cz.vutbr.fit.stud.xslade12.lostphone.Worker;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.SimStateChangedMessage;

/**
 * Broadcast reciever reagující na zmenu stavu SIM karty
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class SimChangedReceiver extends BroadcastReceiver {

    Worker worker;
    TelephonyManager telephoneMgr;

    @Override
    public void onReceive(Context context, Intent intent) {

        worker = new Worker(context);

        // Ziská stav SIM karty
        telephoneMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telephoneMgr.getSimState();


        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                Log.i("SimStateListener", "Sim State absent");
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                Log.i("SimStateListener", "Sim State network locked");
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                Log.i("SimStateListener", "Sim State pin required");
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                Log.i("SimStateListener", "Sim State puk required");
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                Log.i("SimStateListener", "Sim State unknown");
                break;
            case TelephonyManager.SIM_STATE_READY:
                Log.i("SimStateListener", "Sim State ready");
                String phoneNumber = telephoneMgr.getLine1Number();
                Log.i("SimStateListener", phoneNumber);

                // Zaslat udaje o simkarte
                sendSimStateChangedMessage();
        }
    }


    /**
     * Získá informace o SIM kartě s odešle je zprávou na server
     */
    protected void sendSimStateChangedMessage() {
        SimStateChangedMessage msg = new SimStateChangedMessage();

        msg.setPhoneNumber( telephoneMgr.getLine1Number() ); // http://cs.wikipedia.org/wiki/MSISDN
        msg.setNetworkOperator ( telephoneMgr.getNetworkOperator() ); // ze site na ktere jsem
        msg.setNetworkOperatorName ( telephoneMgr.getNetworkOperatorName() ); // ze site na ktere jsem
        msg.setNetworkCountryIso( telephoneMgr.getNetworkCountryIso() );// ze site na ktere jsem
        msg.setImei(telephoneMgr.getDeviceId()); // IMEI
        msg.setSimCountryIso( telephoneMgr.getSimCountryIso() ); // ze site ktere patri SIM karta
        msg.setSimOperator( telephoneMgr.getSimOperator() );// ze site ktere patri SIM karta
        msg.setSimOperatorName( telephoneMgr.getSimOperatorName() );// ze site ktere patri SIM karta
        msg.setSimSerialNumber( telephoneMgr.getSimSerialNumber() );// ze site ktere patri SIM karta
        msg.setSubscriberId( telephoneMgr.getSubscriberId() ); // http://cs.wikipedia.org/wiki/International_Mobile_Subscriber_Identity


        worker.sendMessage(msg);
    }



}
