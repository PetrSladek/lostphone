package cz.vutbr.fit.stud.xslade12.lostphone.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;

import cz.vutbr.fit.stud.xslade12.lostphone.Worker;
import cz.vutbr.fit.stud.xslade12.lostphone.messages.WrongPassMessage;

/**
 * Broadcast reciever reagující na zmenu stavu síte
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Zjistim jestli jsem pripojen k internetu
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = (networkInfo != null && networkInfo.isConnected());
        if (!isConnected) // pokud ne, koncim
            return;

        // sem znovu pripojenej, pokusim se odeslat vsechny neodeslany fotky
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"LostPhone");
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File directory, String fileName) {
                return fileName.endsWith(".jpg");
            }
        };


        Worker worker = new Worker(context);
        if(mediaStorageDir.listFiles(filter).length > 0) {
            for (final File file : mediaStorageDir.listFiles(filter)) {
                if (!file.isFile())
                    continue;

                // odesleme fotku pres REST API
                WrongPassMessage msg = new WrongPassMessage();
                msg.setFrontPhoto(file);
                worker.sendMessage(msg);
            }
        }


    }
}
