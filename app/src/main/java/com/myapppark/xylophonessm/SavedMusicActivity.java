package com.myapppark.xylophonessm;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class SavedMusicActivity extends AppCompatActivity {

    static ListView list;
    static SavedMusicCustomAdapter savedMusicAdapter;
    static ArrayList<String> Files;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_saved_music);
        list = (ListView)findViewById(R.id.listview_savedMusic);
        //Get saved file names
        Files = getMusicFiles();
        //Calling a custom adapter and setting it to list view
        savedMusicAdapter = new SavedMusicCustomAdapter(this, Files);
        list.setAdapter(savedMusicAdapter);

    }


    //Gets all the music file names from XyloMusic directory to an ArrayList
    public ArrayList<String> getMusicFiles()
    {
        ArrayList<String> fileNames=new ArrayList<String>();
        RecordAudio.createDirectoryIfNecessary("XyloMusic");
        File parentDir = new File(Environment.getExternalStorageDirectory()+"/XyloMusic/");
        File[] files = parentDir.listFiles();
        for(int i=0;i<files.length;i++) {
            fileNames.add(files[i].getName().toString());
        }
        return fileNames;
    }

    //Updates/Refreshes the list after a file deletion
    public static void updateList_Deleted(int pos)
    {
        Files.remove(pos);
        savedMusicAdapter.notifyDataSetChanged();
        Log.d("Updated","Updated");
    }

    //Updates/Refreshes the list after a file rename
    public static void updateList_Renamed(int pos, String newName)
    {
        Files.set(pos,newName);
        savedMusicAdapter.notifyDataSetChanged();
        Log.d("Updated","Updated");
    }

    @Override
    protected void onPause() {
        //finish();
        super.onPause();
    }
}
