package cz.vutbr.fit.stud.xslade12.lostphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;

import cz.vutbr.fit.stud.xslade12.lostphone.messages.WrongPassMessage;

/**
 * Created by Peggy on 25.3.2015.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = (networkInfo != null && networkInfo.isConnected());
        if (!isConnected)
            return;

        // sem znovu pripojenej, pokusim se odeslat vsechnz neodeslany fotky
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
