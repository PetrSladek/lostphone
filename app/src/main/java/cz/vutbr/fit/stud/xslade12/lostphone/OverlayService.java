package cz.vutbr.fit.stud.xslade12.lostphone;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class OverlayService extends Service {

    private static final String TAG = OverlayService.class.getSimpleName();
    WindowManager mWindowManager;
    View mView;
//    Animation mAnimation;


    @Override
    public void onCreate() {
        super.onCreate();

        showDialog("muhehee onCreate()");

//        mView = View.inflate(getApplicationContext(), R.layout.fragment_overlay, null);
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
//                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
//                PixelFormat.TRANSLUCENT);
//        params.gravity = Gravity.RIGHT | Gravity.TOP;
//        params.setTitle("Load Average");
//        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
//        wm.addView(mView, params);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        showDialog("muhehee onStartCommand()");



//        WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
//        View mView = View.inflate(getApplicationContext(), R.layout.fragment_overlay, null);
//        WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0,
//                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
//                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//    /* | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON */,
//                PixelFormat.RGBA_8888);
//
//        mWindowManager.addView(mView, mLayoutParams);


//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Delete entry")
//                .setMessage("Are you sure you want to delete this entry?")
//                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // continue with delete
//                    }
//                })
//                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // do nothing
//                    }
//                })
//                .setIcon(android.R.drawable.ic_dialog_alert);
////                .show();
//        AlertDialog dialog = builder.create();
//        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//
//        dialog.show();

//        registerOverlayReceiver();
        return super.onStartCommand(intent, flags, startId);
    }

    private void showDialog(String aTitle){
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        mView = View.inflate(getApplicationContext(), R.layout.fragment_overlay, null);
        mView.setTag(TAG);

//        int top = getApplicationContext().getResources().getDisplayMetrics().heightPixels / 2;

//        LinearLayout dialog = (LinearLayout) mView.findViewById(R.id.overlay_dialog);
//        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) dialog.getLayoutParams();
//        lp.topMargin = top;
//        lp.bottomMargin = top;
//        mView.setLayoutParams(lp);

        Button imageButton = (Button) mView.findViewById(R.id.btnUnlock);
//        lp = (ViewGroup.MarginLayoutParams) imageButton.getLayoutParams();
//        lp.topMargin = top - 58;
//        imageButton.setLayoutParams(lp);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mView.setVisibility(View.INVISIBLE);
            }
        });

        TextView title = (TextView) mView.findViewById(R.id.displayText);
        title.setText(aTitle);

        final WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 0,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON ,
                PixelFormat.RGBA_8888);

        mView.setVisibility(View.VISIBLE);
//        mAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.in);
//        mView.startAnimation(mAnimation);
        mWindowManager.addView(mView, mLayoutParams);

    }

    private void hideDialog(){
        if(mView != null && mWindowManager != null){
            mWindowManager.removeView(mView);
            mView = null;
        }
    }

    @Override
    public void onDestroy() {
        unregisterOverlayReceiver();
        super.onDestroy();
    }

    private void registerOverlayReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(overlayReceiver, filter);
    }

    private void unregisterOverlayReceiver() {
        hideDialog();
        unregisterReceiver(overlayReceiver);
    }


    private BroadcastReceiver overlayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                showDialog("Esto es una prueba y se levanto desde");
            }
            else if (action.equals(Intent.ACTION_USER_PRESENT)) {
                hideDialog();
            }
            else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                hideDialog();
            }
        }
    };
}
