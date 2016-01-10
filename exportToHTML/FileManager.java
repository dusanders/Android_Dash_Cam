package dashcam.zena.dashcam;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Zena on 1/8/2015.
 */
public class FileManager {

    private static ArrayList<File> files;
    private static boolean saveFiles = false;
    private static int filesToSave;

    public FileManager(int inFilesToSave){
        files = new ArrayList<File>();
        filesToSave = inFilesToSave;
    }

    public static File getOutputMediaFile(int type){
        File mediaFile = null;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "DashCam");
                if(!mediaStorageDir.exists())
                    if(!mediaStorageDir.mkdirs()){
                        return null;
                    }
                String timeStamp = new SimpleDateFormat("MMMddyyyy_hhmmssaa").format(new Date());
                mediaFile = new File(mediaStorageDir.getAbsolutePath() + File.separator + "VID_" + timeStamp + ".mp4");
            }catch (final Exception e){
                return null;
            }
        }
        files.add(mediaFile);
        return mediaFile;
    }

    public static int getFileCount(){
        return files.size();
    }
    public void clean() {
        //keep our buffer count up
        if(files.size() >= filesToSave) {
            //check for oldest file
            if (files.get(0).exists()) {
                try {
                    //delete oldest
                    files.get(0).delete();
                } catch (Exception e) {
                    Log.e("FileManager", e.getMessage());
                }
            }
            //don't need to track anymore...
            files.remove(0);
        }
    }
    public void cleanAll() {
        for(int i=0; i<files.size(); i++) {
            if(files.get(i).exists()){
                try{
                    files.get(i).delete();
                }catch (Exception e){}
            }
        }
        files.clear();
    }
    public void saveFiles(){
        //purge file array so we dont delete anything in the future
        files.clear();
        saveFiles = true;
    }
    public void clearSave(){
        //purge file array so we don't delete anything in the future
        //also to save any files that were created while saveFiles = true;
        files.clear();
        saveFiles = false;
    }
    public boolean getSaveState() {
        return saveFiles;
    }
    public static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }


}
