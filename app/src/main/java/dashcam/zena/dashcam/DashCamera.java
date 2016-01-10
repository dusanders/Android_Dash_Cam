package dashcam.zena.dashcam;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.view.SurfaceHolder;
import android.widget.Toast;

/**
 * Created by Zena on 1/2/2015.
 */
public class DashCamera {
    private static Camera camera;
    private static Activity callingActivity;
    private static CameraPreview cameraPreview;
    private static SurfaceHolder previewHolder;
    private static boolean isOpen = false;
    private static boolean isUnlocked = false;
    private static boolean isPreview = false;

    public DashCamera(Activity inActivity){
        callingActivity = inActivity;
        cameraPreview = new CameraPreview(inActivity, this);
        open();
    }



    public static boolean open(){
        if(isOpen)
            return true;
        try{
            camera = android.hardware.Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            parameters.set("cam_mode", 1);
            parameters.setPreviewSize(1920, 1080);
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            parameters.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            camera.setParameters(parameters);
        }catch (final Exception e){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Toast toast = (Toast.makeText(callingActivity, "Problem with DashCam1: " + e.getMessage(), Toast.LENGTH_LONG));
                    toast.show();
                    e.printStackTrace();
                }
            };
            callingActivity.runOnUiThread(runnable);
        }
        isOpen = true;
        return true;
    }





    public static boolean unlock(){
        if(isUnlocked)
            return true;
        try{
            camera.unlock();
        }catch (final Exception e){ Runnable runnable = new Runnable() {
                @Override
                public void run() {
                Toast toast = (Toast.makeText(callingActivity, "Problem with DashCam2: " + e.getMessage(), Toast.LENGTH_LONG));
                toast.show();
                    e.printStackTrace();
               }
            };
            callingActivity.runOnUiThread(runnable);
        }
        isUnlocked = true;
        return true;
    }





    public static boolean startPreview(){
        if(isPreview())
            return true;
        try {
            if(callingActivity.hasWindowFocus()) {
                camera.setPreviewDisplay(previewHolder);
                camera.startPreview();
            }
        }catch (final Exception e){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Toast toast = (Toast.makeText(callingActivity, "Problem with DashCam3: " + e.getMessage(), Toast.LENGTH_LONG));
                    toast.show();
                    e.printStackTrace();
                }
            };
            callingActivity.runOnUiThread(runnable);
        }
        isPreview = true;
        return true;
    }



    public  CameraPreview getCameraPreview(){return cameraPreview;}

    public static void setPreviewHolder(SurfaceHolder inSurfaceHolder){
        previewHolder = inSurfaceHolder;
    }

    public static SurfaceHolder getPreviewHolder(){return previewHolder;}

    public Camera getCamera(){return camera;}
    public static boolean isUnlocked(){return isUnlocked;}
    public static boolean isOpen(){return isOpen;}
    public static boolean isPreview() {return isPreview;}


    public static boolean isRunning() {
        //using lock/unlock for choice because this acts as app 'ownership'
        // of the Camera, don't want to be a hog.
        if(isPreview()){
            return true;
        }
        return false;
    }



    public void deactivate() {
        if(isUnlocked())
            lock();
        if(isPreview())
            stopPreview();
        if(isOpen())
            camera.release();
        isOpen = false;
        isUnlocked = false;
        isPreview = false;
    }



    public boolean lock() {
        try {
            camera.lock();
        }catch (final Exception e){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Toast toast1 = (Toast.makeText(callingActivity, "Problem with Rlock" + e.getMessage(), Toast.LENGTH_LONG));
                    toast1.show();
                    e.printStackTrace();
                }
            };
            callingActivity.runOnUiThread(runnable);
            return false;
        }
        return true;
    }



    public boolean stopPreview(){
        try{
            isPreview = false;
            camera.stopPreview();
        }catch (final Exception e){
            return false;
        }
        return true;
    }

}
