package com.myapppark.xylophonessm;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;


import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.support.v4.content.ContextCompat.checkSelfPermission;

/**
 * Created by MyAppPark Inc. on 10/22/2017.
 */

public class RuntimePermissions
{

    static int PERMISSION_REQUEST_CODE = 200;

    //Permission grant status of both RECORD_AUDIO and EXTERNAL_STORAGE
    public static boolean checkPermission(Context context) {
        return checkPermissionRecord(context)&&checkPermissionStorage(context);
    }

    //Permission grant status of RECORD_AUDIO
    public static  boolean checkPermissionRecord(Context context)
    {
        boolean check = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int result = checkSelfPermission(context,Manifest.permission.RECORD_AUDIO);
            check = result == PackageManager.PERMISSION_GRANTED;
        }
        return check;
    }

    //Permission grant status of both EXTERNAL_STORAGE
    public static  boolean checkPermissionStorage(Context context)
    {
        boolean check = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int result = checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE);
            check = result == PackageManager.PERMISSION_GRANTED;
        }
        return check;
    }

    //Requesting permissions at runtime if not already granted
    public static void requestPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }





}
