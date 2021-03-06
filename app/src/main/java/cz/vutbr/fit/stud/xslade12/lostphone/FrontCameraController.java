package cz.vutbr.fit.stud.xslade12.lostphone;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Třída pro tajné získání fotografie z přední kamery
 * @author Petr Sládek <xslade12@stud.fit.vutbr.cz>
 */
public class FrontCameraController {

    private Context context;

    private boolean hasCamera;

    private Camera camera;
    private int cameraId;

    private Camera.PictureCallback pictureCallback;

    public FrontCameraController(Context c){
        context = c.getApplicationContext();

        // Zjisti jestli je dostupna predni kamera
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)){
            cameraId = getFrontCameraId();

            if(cameraId != -1){
                hasCamera = true;
            }else{
                hasCamera = false;
            }
        }else{
            hasCamera = false;
        }
    }

    /**
     * Je dostupna predni kamera?
     * @return
     */
    public boolean hasCamera(){
        return hasCamera;
    }

    /**
     * Otevrit predni kameru
     */
    public void open(){
        camera = null;

        if(hasCamera){
            try{
                camera = Camera.open(cameraId);
                prepareCamera();
            }
            catch(Exception e){
                hasCamera = false;
            }
        }
    }

    /**
     * Nastavi Callback po vytvoření fotografie
     * @param callback
     */
    public void setPictureCallback(Camera.PictureCallback callback) {
        pictureCallback = callback;
    }

    /**
     * Vyfot fotku (potichu)
     */
    public void takePicture(){
        takePicture(true);
    }

    /**
     * Vyfot fotku
     * @param silent s/bez stlumeni zvuku
     */
    public void takePicture(final boolean silent){
        if(hasCamera){

            if(silent) { // ztlumi zvuk
                AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                mgr.setStreamMute(AudioManager.STREAM_SYSTEM, true);
            }

            // Po vteřine vyfotí fotku
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    camera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            Log.i("Camera", "onPictureTaken");

                            // Zavola pridanej callback
                            pictureCallback.onPictureTaken(data, camera);

                            release();

                            if(silent) { // obnovy zvuk
                                AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                                mgr.setStreamMute(AudioManager.STREAM_SYSTEM, false);
                            }
                        }
                    });
                }
            }, 1000);


        }
    }

    /**
     * Uvolnění přední kamery
     */
    public void release(){
        if(camera != null){
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    /**
     * Z dosupných kamer zjistí ID přední kamery
     * @return
     */
    private int getFrontCameraId(){
        int camId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo ci = new Camera.CameraInfo();

        for(int i = 0;i < numberOfCameras;i++){
            Camera.getCameraInfo(i,ci);
            if(ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                camId = i;
            }
        }

        return camId;
    }

    /**
     * Nastavi parametry kamery a vytvori neviditelne nahledove pole
     */
    private void prepareCamera(){
        SurfaceView view = new SurfaceView(context);

        SurfaceHolder holder = view.getHolder();

        try{
            camera.setPreviewDisplay(holder);
        } catch(IOException e){
            throw new RuntimeException(e);
        }

        Camera.Parameters params = camera.getParameters();
        params.setJpegQuality(100);

        camera.setParameters(params);
        camera.startPreview();
    }


    /**
     * Vytvori unikatni soubor pro ulozeni fotografie
     * @return
     */
    public static File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"LostPhone");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath()+File.separator+"IMG_"+timeStamp+".jpg");

        return mediaFile;
    }


    /**
     * Zmensi fotografie na dano uvelikost
     * @param input raw data
     * @param width vyska
     * @param height sířka
     * @return rawjpegdata
     */
    byte[] resizeImage(byte[] input, int width, int height) {
        Bitmap original = BitmapFactory.decodeByteArray(input, 0, input.length);
        Bitmap resized = Bitmap.createScaledBitmap(original, width, height, true);

        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 100, blob);

        return blob.toByteArray();
    }

}