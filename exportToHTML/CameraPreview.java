package dashcam.zena.dashcam;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import java.lang.annotation.Annotation;

/**
 * Created by Zena on 12/26/2014.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

    private static SurfaceHolder mainHolder;
    private static DashCamera camera;
    private static Surface previewSurface;

    public CameraPreview(Activity inContext, DashCamera inCamera){
        super(inContext);
        camera = inCamera;
        mainHolder = getHolder();
        mainHolder.addCallback(this);
        mainHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        previewSurface = mainHolder.getSurface();
    }

    public static SurfaceHolder holder(){
        return mainHolder;
    }
    public static Surface getSurface(){return previewSurface;}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera.setPreviewHolder(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

}
