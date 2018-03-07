package com.myapppark.xylophonessm;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by MyAppPark Inc. on 10/11/2017.
 */

public class RecordAudio
{

    static MediaRecorder mrec=null;
    static boolean startRec=false;


    //Starts recording audio via MIC
    public static void startRecording()
    {

        if(mrec==null) {
            createDirectoryIfNecessary("XyloMusic");
            mrec = new MediaRecorder();
            mrec.setAudioSource(MediaRecorder.AudioSource.MIC);
            mrec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mrec.setOutputFile(Environment.getExternalStorageDirectory() + "/XyloMusic/" + getNewFileName() + ".mp3");
            mrec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        }

        if(!startRec) {
            try {
                mrec.prepare();
                mrec.start();
                startRec = true;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Error", e.toString());
            }
        }

    }


    //Stops recording audio
    public static void stopRecording()
    {
        if(mrec!=null) {
            mrec.stop();
            mrec.reset();
            mrec.release();
            mrec = null;
            startRec=false;
        }

    }

    //Creates a unique file name using current date and time
    public static String getNewFileName()
    {
       SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
       String currentDataTime = dateFormat.format(new Date());
       Log.e("DateTime",currentDataTime);
       return "XyloRec"+currentDataTime;
       // return "Recording"+"12";

    }


    //Creates XyloMusic directory if its not exist
    public static void createDirectoryIfNecessary(String s) {
    try {
        File newFolder = new File(Environment.getExternalStorageDirectory(), s);
        if (!newFolder.exists()) {
            newFolder.mkdir();
        }
    } catch (Exception e) {
        System.out.println("e: " + e);
    }

}
}
