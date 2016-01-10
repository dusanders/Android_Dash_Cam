package dashcam.zena.dashcam;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MainActivity extends ActionBarActivity {

    private static Button launchButton;
    private static Button viewFilesButton;
    private static Button saveButton;
    private static Button stopSaveButton;
    private static FrameLayout previewFrame;
    private static Surface previewSurface;
    private static Activity mainActivity;

    private static DashCamera dashCamera;
    private static Recorder recorder;

    private static ScheduledThreadPoolExecutor cameraThreads;
    private static FileManager fileManager;
    private static PowerManager powerManager;
    private static PowerManager.WakeLock wakeLock;
    private static AudioManager audioManager;

    private static boolean launched = false;


    // Following commented out for future use, see comment block in onCreate() for an explanation
    //private static AlertDialog.Builder alertBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        mainActivity = this;
        //get our custom DashCamera object
        dashCamera = new DashCamera(mainActivity);
        //get a thread pool for the dashcam operations; request 3 threads
        fileManager = new FileManager(3);
        cameraThreads = new ScheduledThreadPoolExecutor(5);
        powerManager = (PowerManager)getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Dash Cam");
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);

        /*  The following is commented out for future use of asking if user wants to delete current cache video upon exiting
                --- The default currently is automatically delete cache videos unless 'save' was set during exit.
        alertBuilder = new AlertDialog.Builder(mainActivity).setTitle("Delete current video cache?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fileManager.cleanAll();
            }
        }).setNegativeButton("No", null);
        */
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //nothing to see here....
        //already created our instance objects in onCreate() and everything else must be handled
        //  by onResume() for if a user comes back to our app.
    }


    @Override
    protected void onResume(){
        super.onResume();
        wakeLock.acquire();
        //force our orientation for video
        mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //are we returning to an already started instance?
        //get our preview back if we are.
        if(dashCamera.isPreview()) {
            try {
                previewFrame.addView(dashCamera.getCameraPreview());
            }catch (Exception e){}
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        //do nothing if the user exits, we want to keep recording video!
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement

        //exit the app
        if (id == R.id.action_settings) {
            super.onDestroy();
            wakeLock.release();
            //free up our resources
            if(recorder != null){
                //get rid of Recorder
                recorder.deactivate();
            }
            if(dashCamera.isRunning()) {
                //release our camera
                dashCamera.deactivate();
            }
            //delete cache
            if(!fileManager.getSaveState()) {
                //delete cache videos
                fileManager.cleanAll();
            }
            //shutdown threads
            cameraThreads.shutdown();
            audioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
            //GTFO!!
            System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }

    public static void launch(){
        //make sure we cant 'launch' twice!
        if(!launched) {
            //add our preview
            previewFrame.addView(dashCamera.getCameraPreview());
            //we need a delay before starting the mediaRecorder to allow views to settle
            final Handler delay = new Handler();
            //postDelay this action, wait 1000ms (1sec)
            delay.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //get our surface
                    previewSurface = dashCamera.getCameraPreview().getSurface();
                    recorder = new Recorder(dashCamera, mainActivity, previewSurface);
                    recorder.activate();
                }
            }, 1000);
            //start the recording!
            cameraThreads.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (recorder.isRunning()) {
                        recorder.deactivate();
                    }
                    //start new Recorder
                    recorder = new Recorder(dashCamera, mainActivity, previewSurface);
                    recorder.activate();
                    //if we are NOT saving files, clean them up
                    if (!fileManager.getSaveState()) {
                        fileManager.clean();
                    }
                }
            }, 2, 2, TimeUnit.MINUTES);
            launched = true;
        }
    }

    public static void viewFile(){
        //TO-DO: throw intent to view/modify files with System File Browser
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            launchButton = (Button) rootView.findViewById(R.id.launchButton);
            viewFilesButton = (Button) rootView.findViewById(R.id.viewFilesButton);
            previewFrame = (FrameLayout) rootView.findViewById(R.id.previewFrame);
            saveButton = (Button) rootView.findViewById(R.id.saveButton);
            stopSaveButton = (Button) rootView.findViewById(R.id.stopSave);

            launchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launch();
                }
            });
            viewFilesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast toast = (Toast.makeText(mainActivity, "Files are saved under Movies in File manager", Toast.LENGTH_LONG));
                    toast.show();
                    Intent getFilesIntent = new Intent();
                    getFilesIntent.setAction(Intent.ACTION_GET_CONTENT);
                    getFilesIntent.setType("file/*");
                    startActivity(getFilesIntent);
                }
            });

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recorder != null){
                        fileManager.saveFiles();
                    }
                }
            });

            stopSaveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recorder != null){
                        fileManager.clearSave();
                    }
                }
            });
            return rootView;
        }
    }
}
