package com.myapppark.xylophonessm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by ChaitanyaKLK on 10/13/2017.
 */

public class SavedMusicCustomAdapter extends BaseAdapter {

    private static LayoutInflater inflater=null;
    static ArrayList<String> thisMusicFiles;
    static Context context;
    MediaPlayer player ;
    SeekBar seekbar;
    Activity savedMusicActivity;
    android.os.Handler mHandler = new android.os.Handler();
    static TextView selectedTextView;

    public SavedMusicCustomAdapter(SavedMusicActivity savedMusic, ArrayList<String> musicFiles)
    {
        savedMusicActivity = savedMusic;
        thisMusicFiles = musicFiles;
        context = savedMusic.getApplicationContext();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return thisMusicFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder
    {
        TextView tv;
        Button btn_popup;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final ViewHolder holder=new ViewHolder();
        convertView = inflater.inflate(R.layout.list_item_saved_music,null);
        holder.tv = (TextView)convertView.findViewById(R.id.tv_FileName);
        holder.tv.setText(thisMusicFiles.get(position));
        holder.tv.setTextColor(Color.GRAY);
        //final View finalConvertView = convertView;
        holder.tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic(thisMusicFiles.get(position));
                selectedTextView = holder.tv;
                setListItemAsSelected(selectedTextView);
                Log.d("Position", String.valueOf(position));
            }
        });


        holder.btn_popup = (Button)convertView.findViewById(R.id.btn_popup);
        holder.btn_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedTextView=holder.tv;
                setListItemAsSelected(selectedTextView);
                PopupMenu popmenu = new PopupMenu(context,v);
                MenuInflater inflater = popmenu.getMenuInflater();
                inflater.inflate(R.menu.actions,popmenu.getMenu());
                popmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id=item.getItemId();
                        switch (id)
                        {
                            case R.id.item_rename: Log.d("Rename File: ",String.valueOf(position));renameFile(position);break;
                            case R.id.item_delete: Log.d("Delete File: ",String.valueOf(position));deleteFile(position);break;
                            case R.id.item_share: Log.d("Share File: ",String.valueOf(position));shareFile(position);break;
                        }
                        return true;
                    }
                });
                popmenu.show();
                popmenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        resetListItemAsNotSelected(holder.tv);
                    }
                });
            }
        });




        return convertView;
    }



    //Changes the color of selected list item
    private void setListItemAsSelected(TextView tv) {
       tv.setTextColor(Color.WHITE);
        tv.setBackgroundColor(Color.parseColor("#40e0d0"));//turquoise
    }

    //Resets the color of list item
    private void resetListItemAsNotSelected(TextView tv)
    {
        tv.setTextColor(Color.GRAY);
        tv.setBackgroundColor(Color.WHITE);
    }

    //Renames a file
    private void renameFile(final int position) {
        final File file = new File(Environment.getExternalStorageDirectory() + "/XyloMusic/" + thisMusicFiles.get(position));
        final AlertDialog.Builder renameDialog = new AlertDialog.Builder(savedMusicActivity);
        final EditText filename = new EditText(savedMusicActivity);
        renameDialog.setTitle("Rename");
        filename.setText(thisMusicFiles.get(position).substring(0,thisMusicFiles.get(position).length()-4));
        filename.setSelection(0,filename.getText().toString().length());
        filename.setSelection(filename.getText().toString().length());
        renameDialog.setView(filename);
        renameDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final File newFile = new File(Environment.getExternalStorageDirectory() + "/XyloMusic/" + filename.getText()+".mp3");
                 if(hasSymbols(filename.getText().toString())) {
                     Toast.makeText(savedMusicActivity,savedMusicActivity.getString(R.string.toast_filename_restrictions), Toast.LENGTH_SHORT).show();
                     renameFile(position);
                 }
                 else if(thisMusicFiles.contains(filename.getText()+".mp3"))
                 {
                     Toast.makeText(savedMusicActivity, savedMusicActivity.getString(R.string.toast_filename_exists), Toast.LENGTH_SHORT).show();
                     renameFile(position);
                 }
                 else if(filename.getText().toString().length()>25)
                 {
                     Toast.makeText(savedMusicActivity, savedMusicActivity.getString(R.string.toast_filename_length), Toast.LENGTH_SHORT).show();
                     renameFile(position);
                 }
                 else {
                     file.renameTo(newFile);
                     savedMusicActivity.runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             SavedMusicActivity saved = new SavedMusicActivity();
                             saved.updateList_Renamed(position,filename.getText()+".mp3");
                         }
                     });
                 }

            }
        });
        renameDialog.setNegativeButton("CANCEL",null);
        renameDialog.show();


    }

    //Checks if file name has symbols or spaces
    private boolean hasSymbols(String s) {
        Pattern p = Pattern.compile("[^a-zA-Z0-9]");
        boolean containSymbols = p.matcher(s).find();
        return containSymbols;
    }

    //Deletes a file
    private void deleteFile(final int position) {
        final File file = new File(Environment.getExternalStorageDirectory() + "/XyloMusic/" + thisMusicFiles.get(position));
        final String item = thisMusicFiles.get(position);
        // ASK FOR CONFIRMATION
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(savedMusicActivity)
                    .setTitle("Confirm")
                    .setMessage("Are you sure you want to delete "+thisMusicFiles.get(position))
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean deleted = file.delete();
                            if(deleted) {
                                Toast.makeText(context,savedMusicActivity.getString(R.string.toast_file_deleted), Toast.LENGTH_SHORT).show();
                                savedMusicActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SavedMusicActivity saved = new SavedMusicActivity();
                                        saved.updateList_Deleted(position);
                                    }
                                });
                            }
                        }
                    })
                    .setNegativeButton("NO",null);deleteDialog.show();

            // UPDATE LIST ON DELETING A FILE

    }

    //Share file to other apps
    private void shareFile(int position) {
        File file = new File(Environment.getExternalStorageDirectory() + "/XyloMusic/" + thisMusicFiles.get(position));
        Uri uri = Uri.parse("file://"+file.getAbsolutePath());
        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);

        //whatsappIntent.putExtra(Intent.EXTRA_TEXT,thisMusicFiles.get(position).toString());
        //whatsappIntent.setType("text/plain");
        whatsappIntent.putExtra(Intent.EXTRA_STREAM,uri);
        whatsappIntent.setType("audio/*");
        whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        whatsappIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            savedMusicActivity.startActivity(Intent.createChooser(whatsappIntent,"File Share"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context,savedMusicActivity.getString(R.string.toast_no_supported_apps),Toast.LENGTH_SHORT).show();
        }

    }

    //Plays selected music
    private void playMusic(String fileName) {
        try{
            FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory() + "/XyloMusic/" + fileName);
            player = new MediaPlayer();

            if (player.isPlaying())
                player.stop();


            player.setDataSource(fis.getFD());
            player.prepare();
            player.start();
            showSeekDialog(fileName,fis);
            updateSeekbar();

        }
         catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Seekbar showing the status of the music being played.
    private void showSeekDialog(String fileName, FileInputStream fileStream) {

        final AlertDialog.Builder seekDialog = new AlertDialog.Builder(savedMusicActivity);
        LayoutInflater inflater = savedMusicActivity.getLayoutInflater();
        View view = inflater.inflate(R.layout.seekbar_and_button,null);


        seekbar = (SeekBar) view.findViewById(R.id.seekBar);
        seekbar.setMax(player.getDuration());
        seekbar.setProgress(0);

        final Button btn_PlayPause = (Button) view.findViewById(R.id.btn_Play_Pause);
        btn_PlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(player.isPlaying())
               {
                   player.pause();
                   btn_PlayPause.setBackground(savedMusicActivity.getResources().getDrawable(R.drawable.play_icon));
               }
               else {
                   btn_PlayPause.setBackground(savedMusicActivity.getResources().getDrawable(R.drawable.pause_icon));
                   player.start();
                   updateSeekbar();
               }
            }
        });

        seekDialog.setTitle("Playing: "+fileName);
        seekDialog.setView(view);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(player!=null && fromUser)
                    player.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btn_PlayPause.setBackground(savedMusicActivity.getResources().getDrawable(R.drawable.play_icon));
            }
        });

        seekDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                player.stop();
                resetListItemAsNotSelected(selectedTextView);
            }
        });



        seekDialog.show();

    }

    //Updates seekbar according to play time of music file
    private void updateSeekbar() {
        savedMusicActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(player.isPlaying()) {
                    seekbar.setProgress(player.getCurrentPosition());
                    mHandler.postDelayed(this, 100);
                }
            }
        });
    }


}
