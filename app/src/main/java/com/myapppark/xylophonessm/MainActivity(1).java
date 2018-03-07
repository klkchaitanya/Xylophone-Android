package com.myapppark.xylophonessm;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends Activity {
    // Helpful Constants
    private final int NR_OF_SIMULTANEOUS_SOUNDS = 2;
    private final float LEFT_VOLUME = 1.0f;
    private final float RIGHT_VOLUME = 1.0f;
    private final int NO_LOOP = 0;
    private final int PRIORITY = 0;
    private final float NORMAL_PLAY_RATE = 1.0f;
    static boolean micRecording = false;

    // TODO: Add member variables here
    private SoundPool sPool;

    //Xylophone Music notes Buttons
    Button cButton,dButton,eButton,fButton,gButton,aButton,bButton,c1Button;
    //Xylophone Menu Buttons
    Button btn_record, btn_viewSaved;
    //Recorder Menu Chronometer and Buttons
    Chronometer chrono;
    Button btn_start, btn_stop;
    //Variables to load note ids to soundpool
    int c_Id,d_Id,e_Id,f_Id,g_Id,a_Id,b_Id,c1_Id;
    //Varialbles to track if note is playing or not
    static boolean cPlaying,dPlaying,ePlaying,fPlaying,gPlaying,aPlaying,bPlaying,c1Playing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        hideRecorderMenu();
        
        cButton=(Button)findViewById(R.id.c_key);
        dButton=(Button)findViewById(R.id.d_key);
        eButton=(Button)findViewById(R.id.e_key);
        fButton=(Button)findViewById(R.id.f_key);
        gButton=(Button)findViewById(R.id.g_key);
        aButton=(Button)findViewById(R.id.a_key);
        bButton=(Button)findViewById(R.id.b_key);
        c1Button=(Button)findViewById(R.id.c1_key);

        //Initially setting all notes playing status as false.
        cPlaying = false;dPlaying= false;ePlaying= false;fPlaying= false;
        gPlaying= false;aPlaying= false;bPlaying= false;c1Playing= false;


        //Xylophone Menu Buttons
        btn_record=(Button)findViewById(R.id.btn_record);
        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideXylophoneMenu();
            }
        });

        btn_viewSaved=(Button)findViewById(R.id.btn_viewSaved);
        btn_viewSaved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canviewSaved()) {
                    SavedMusicActivity saved = new SavedMusicActivity();
                    if(saved.getMusicFiles().size()>0) {
                        Intent in = new Intent(MainActivity.this, SavedMusicActivity.class);
                        startActivity(in);
                    }
                    else
                    {
                        Toast.makeText(getBaseContext(),getString(R.string.toast_no_music_saved),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        //Recorder Menu Chronometer and Buttons
        chrono = (Chronometer)findViewById(R.id.chrono);

        btn_start=(Button)findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(canRecordingStart()) {
                        if (getMicrophoneAvailable(getBaseContext())) {
                                startRecording();
                            } else {
                            Toast.makeText(getBaseContext(), getString(R.string.toast_mic_busy), Toast.LENGTH_SHORT).show();
                        }
                    }

            }
        });

        btn_stop=(Button)findViewById(R.id.btn_stop);
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Button","STOP Button");
                stopRecording();
            }
        });




        // TODO: Create a new SoundPool
        sPool = new SoundPool(7, AudioManager.STREAM_MUSIC,0);

        // TODO: Load and get the IDs to identify the sounds
       c_Id=sPool.load(getApplicationContext(),R.raw.note1_c,1);
       d_Id=sPool.load(getApplicationContext(),R.raw.note2_d,1);
       e_Id=sPool.load(getApplicationContext(),R.raw.note3_e,1);
       f_Id=sPool.load(getApplicationContext(),R.raw.note4_f,1);
       g_Id=sPool.load(getApplicationContext(),R.raw.note5_g,1);
       a_Id=sPool.load(getApplicationContext(),R.raw.note6_a,1);
       b_Id=sPool.load(getApplicationContext(),R.raw.note7_b,1);
       c1_Id=sPool.load(getApplicationContext(),R.raw.note8_c,1);
        
    }

    //Touch and Multitouch Finger Actions on Activity
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //Log.d("Dispatch","Touched");
        if(ev.getActionMasked()==MotionEvent.ACTION_DOWN) {
            int pointerId = ev.getPointerId(ev.getActionIndex());
            Log.d("MotionEvent", "MAIN DOWN"+" ON "+whichRectangle(ev,pointerId));
            playMusic(whichRectangle(ev,pointerId));
        }
        if(ev.getActionMasked()==MotionEvent.ACTION_POINTER_DOWN) {
            int pointerId = ev.getPointerId(ev.getActionIndex());
            Log.d("MotionEvent", pointerId+" POINTER DOWN"+" ON "+whichRectangle(ev,pointerId));
            playMusic(whichRectangle(ev,pointerId));
        }
        if(ev.getActionMasked()==MotionEvent.ACTION_UP) {
            int pointerId = ev.getPointerId(ev.getActionIndex());
            Log.d("MotionEvent", "MAIN UP"+" FROM "+whichRectangle(ev,pointerId));
            makeFalse(whichRectangle(ev,pointerId));
        }
        if(ev.getActionMasked()==MotionEvent.ACTION_POINTER_UP) {
            int pointerId = ev.getPointerId(ev.getActionIndex());
            Log.d("MotionEvent", pointerId+" POINTER UP"+" FROM "+whichRectangle(ev,pointerId));
            Log.d("Num of Pointers: ",String.valueOf(ev.getPointerCount()));
            Log.d("Action Index: ",String.valueOf(ev.getActionIndex()));
            makeFalse(whichRectangle(ev,ev.getActionIndex()));
            //setUntouchedFalse(ev);
        }

        if(ev.getActionMasked()==MotionEvent.ACTION_MOVE)
            for(int i=0;i<ev.getPointerCount();i++)
            {
                //int pointerId = ev.getPointerId(ev.findPointerIndex(i));
                playMusic(whichRectangle(ev,i));
                if(whichRectangle(ev,i).equals("null"))
                    setUntouchedFalse(ev);
            }

        return super.dispatchTouchEvent(ev);
    }

    //Finding untouched keys and making them not playing
    private void setUntouchedFalse(MotionEvent ev) {
        ArrayList<String> notes = new ArrayList<String>();
        notes.add("C");notes.add("D");notes.add("E");notes.add("F");
        notes.add("G");notes.add("A");notes.add("B");notes.add("C1");
        ArrayList<String> touched = new ArrayList<String>();
        for(int i=0;i<ev.getPointerCount();i++)
        {
            int pointer = 0;
            try {
                pointer = ev.getPointerId(ev.findPointerIndex(i));
            }catch(Exception e){Log.d("Error","Pointer out of Range");}
            if(!whichRectangle(ev,pointer).equals("null"))
                touched.add(whichRectangle(ev,pointer));
        }

        for(int i=0;i<notes.size();i++)
        {
            if(!touched.contains(notes.get(i)))
                switch (notes.get(i))
                {
                    case "C":cPlaying=false;break;
                    case "D":dPlaying=false;break;
                    case "E":ePlaying=false;break;
                    case "F":fPlaying=false;break;
                    case "G":gPlaying=false;break;
                    case "A":aPlaying=false;break;
                    case "B":bPlaying=false;break;
                    case "C1":c1Playing=false;break;
                }
        }
    }

    //Makes untouched keys playing = false
    private void makeFalse(String s) {
        switch(s) {
            case "C": cPlaying = false;break;
            case "D": dPlaying = false;break;
            case "E": ePlaying = false;break;
            case "F":fPlaying=false;break;
            case "G":gPlaying=false;break;
            case "A":aPlaying=false;break;
            case "B":bPlaying=false;break;
            case "C1":c1Playing=false;break;
        }
    }

    //Finding to which rectangle/note button the finger coordinates belong to.
    public String whichRectangle(MotionEvent ev,int pointer)
    {
        String rect = "null";
        int x=0;
        int y=0;

        if(ev.getPointerCount()>1)
        {
            // Log.d("POINTER",String.valueOf(pointer));
            try {
                x = (int) ev.getX(pointer);
                y = (int) ev.getY(pointer);
            }catch(Exception e)
            {
                Log.d("Error","Pointer Out of Range");
            }

        }
        else
        {
            x =(int)ev.getRawX();
            y =(int)ev.getRawY();
        }

        int[] location1 = new int[2];
        cButton.getLocationOnScreen(location1);
        int val = location1[1] - cButton.getTop();

        Rect rect1 = new Rect(cButton.getLeft(), cButton.getTop()+val, cButton.getRight(), cButton.getBottom()+val);
        Rect rect2 = new Rect(dButton.getLeft(), dButton.getTop()+val, dButton.getRight(), dButton.getBottom()+val);
        Rect rect3 = new Rect(eButton.getLeft(), eButton.getTop()+val, eButton.getRight(), eButton.getBottom()+val);
        Rect rect4 = new Rect(fButton.getLeft(), fButton.getTop()+val, fButton.getRight(), fButton.getBottom()+val);
        Rect rect5 = new Rect(gButton.getLeft(), gButton.getTop()+val, gButton.getRight(), gButton.getBottom()+val);
        Rect rect6 = new Rect(aButton.getLeft(), aButton.getTop()+val, aButton.getRight(), aButton.getBottom()+val);
        Rect rect7 = new Rect(bButton.getLeft(), bButton.getTop()+val, bButton.getRight(), bButton.getBottom()+val);
        Rect rect8 = new Rect(c1Button.getLeft(), c1Button.getTop()+val, c1Button.getRight(), c1Button.getBottom()+val);



        if(rect1.contains(x,y))
            return "C";
        else if(rect2.contains(x,y))
            return "D";
        else if(rect3.contains(x,y))
            return "E";
        else if(rect4.contains(x,y))
            return "F";
        else if(rect5.contains(x,y))
            return "G";
        else if(rect6.contains(x,y))
            return "A";
        else if(rect7.contains(x,y))
            return "B";
        else if(rect8.contains(x,y))
            return "C1";

        return rect;
    }

    //Checks if that notes is already playing, and plays if its not.
    public void playMusic(String s)
    {
        switch(s) {
            case "C":
                if(!cPlaying) {
                    Log.d("Playing ","C");
                    sPool.play(c_Id, 1.0f, 1.0f, 0, 0, 1.0f);
                    cPlaying = true;
                }break;
            case "D":
                if(!dPlaying) {
                    Log.d("Playing ","D");
                    sPool.play(d_Id, 1.0f, 1.0f, 0, 0, 1.0f);
                    dPlaying = true;
                }break;
            case "E":
                if(!ePlaying) {
                    Log.d("Playing ","E");
                    sPool.play(e_Id, 1.0f, 1.0f, 0, 0, 1.0f);
                    ePlaying = true;
                }break;
            case "F":
                if(!fPlaying) {
                    Log.d("Playing ","F");
                    sPool.play(f_Id, 1.0f, 1.0f, 0, 0, 1.0f);
                    fPlaying = true;
                }break;
            case "G":
                if(!gPlaying) {
                    Log.d("Playing ","G");
                    sPool.play(g_Id, 1.0f, 1.0f, 0, 0, 1.0f);
                    gPlaying = true;
                }break;
            case "A":
                if(!aPlaying) {
                    Log.d("Playing ","A");
                    sPool.play(a_Id, 1.0f, 1.0f, 0, 0, 1.0f);
                    aPlaying = true;
                }break;
            case "B":
                if(!bPlaying) {
                    Log.d("Playing ","B");
                    sPool.play(b_Id, 1.0f, 1.0f, 0, 0, 1.0f);
                    bPlaying = true;
                }break;
            case "C1":
                if(!c1Playing) {
                    Log.d("Playing ","C1");
                    sPool.play(c1_Id, 1.0f, 1.0f, 0, 0, 1.0f);
                    c1Playing = true;
                }break;
        }
    }


    //Checks if all permissions are granted or not
    private boolean canRecordingStart() {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!RuntimePermissions.checkPermission(getApplicationContext())) {
                RuntimePermissions.requestPermission(MainActivity.this);
                return false;
            } else {
                return true;
            }
        }
        else
            return true;
    }

    //Checks if there are any music files saved or not.
    private boolean canviewSaved() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!RuntimePermissions.checkPermissionStorage(getApplicationContext())) {
                RuntimePermissions.requestPermission(MainActivity.this);
                return false;
            } else {
                return true;
            }
        }
        else
            return true;
    }

    //Sets Chronometer,Buttons and Recording status
    public void startRecording()
    {
        resetChronometer();
        chrono.start();
        RecordAudio.startRecording();
        btn_start.setEnabled(false);
        btn_stop.setEnabled(true);
        micRecording = true;

    }

    //Stops Chronometer and changes Recording status
    public void stopRecording()
    {
        resetChronometer();
        hideRecorderMenu();
        chrono.stop();
        RecordAudio.stopRecording();
        micRecording=false;
        Toast.makeText(getBaseContext(),getString(R.string.toast_file_saved),Toast.LENGTH_SHORT).show();
    }


    //Resets Chronometer
    private void resetChronometer() {
        chrono.setBase(SystemClock.elapsedRealtime());
    }

    //Hides Record and ViewSaved button layout
    private void hideXylophoneMenu() {
        LinearLayout xylophoneMenu = (LinearLayout)findViewById(R.id.layout_xylophoneMenu);
        xylophoneMenu.setVisibility(View.GONE);
        LinearLayout recorderMenu = (LinearLayout)findViewById(R.id.layout_recorderMenu);
        recorderMenu.setVisibility(View.VISIBLE);

        btn_start.setEnabled(true);
        btn_stop.setEnabled(false);

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Record Music");
        alert.setMessage("Select 'START' to record. Use medium to high volume for better results.");
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alert.show();
    }

    //Hides Recorder Start and Stop button layout
    private void hideRecorderMenu() {
        LinearLayout recorderMenu = (LinearLayout)findViewById(R.id.layout_recorderMenu);
        recorderMenu.setVisibility(View.GONE);
        LinearLayout xylophoneMenu = (LinearLayout)findViewById(R.id.layout_xylophoneMenu);
        xylophoneMenu.setVisibility(View.VISIBLE);
    }


    //Checks if permissions granted and notifies user
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == RuntimePermissions.PERMISSION_REQUEST_CODE) {
            if(grantResults.length>0){
                boolean recordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if(recordAccepted&&storageAccepted)
                    Toast.makeText(getApplicationContext(),getString(R.string.toast_permission_granted),Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(),getString(R.string.toast_need_permission),Toast.LENGTH_LONG).show();
            }
        }
    }


    //On Application Paused, stops recording if active.
    @Override
    protected void onPause() {
        if(micRecording) {
            stopRecording();
            micRecording = false;
        }
        super.onPause();
    }

    //On Back button of Android Pressed
    @Override
    public void onBackPressed() {

        LinearLayout recorderMenu = (LinearLayout)findViewById(R.id.layout_recorderMenu);
        if(recorderMenu.getVisibility()== View.VISIBLE)
            hideRecorderMenu();
        else
            super.onBackPressed();

    }

    //returns whether the microphone is available or not
    public static boolean getMicrophoneAvailable(Context context) {
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(new File(context.getCacheDir(), "MediaUtil#micAvailTestFile").getAbsolutePath());
        boolean available = true;
        try {
            recorder.prepare();
            recorder.start();
        }
        catch (Exception exception) {
            available = false;
        }
        recorder.release();
        return available;
    }

}