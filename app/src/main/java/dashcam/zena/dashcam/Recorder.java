package dashcam.zena.dashcam;

import android.app.Activity;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.view.Surface;
import android.widget.Toast;


/**
 * Created by Zena on 1/2/2015.
 */
public class Recorder extends MediaRecorder {
    private static DashCamera camera;
    private static Activity callingActivity;
    private static boolean isRunning = false;
    private static Surface surface;
    private static final int MEDIA_TYPE_VIEDO = 2;

    public Recorder (DashCamera inCamera, Activity inActivity, Surface inPreviewSurface) {
        camera = inCamera;
        surface = inPreviewSurface;
        callingActivity = inActivity;
    }
    public void activate() {
        if(camera.open()) {
                camera.startPreview();
            if (camera.unlock()) {
                try {
                    this.setCamera(camera.getCamera());
                    this.setAudioSource(MediaRecorder.AudioSource.MIC);
                    this.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                    this.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
                    this.setOutputFile(FileManager.getOutputMediaFile(MEDIA_TYPE_VIEDO).getAbsolutePath());
                    this.prepare();
                    this.start();
                    isRunning = true;
                } catch (final Exception e) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Toast toast1 = (Toast.makeText(callingActivity, "Problem with Recorder!" + e.getMessage(), Toast.LENGTH_LONG));
                            toast1.show();
                            e.printStackTrace();
                            deactivate();
                        }
                    };
                    callingActivity.runOnUiThread(runnable);
                    isRunning = false;

                }

            }

        }

    }
    public void deactivate() {
        if(isRunning){
            try {
                this.stop();
                this.reset();
                this.release();
                this.isRunning = false;
            }catch (final Exception e){
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Toast toast1 = (Toast.makeText(callingActivity, "Problem with Recorder2" + e.getMessage(), Toast.LENGTH_LONG));
                        toast1.show();
                        e.printStackTrace();
                    }
                };
                callingActivity.runOnUiThread(runnable);
            }
        }
    }

    public static boolean isRunning(){
        return isRunning;
    }
}
