package com.example.droneapplication;

import android.os.Environment;
import android.os.HandlerThread;
import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_MAVLINK_START_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCommand;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARFeatureCommon;
import com.parrot.arsdk.ardatatransfer.ARDATATRANSFER_ERROR_ENUM;
import com.parrot.arsdk.ardatatransfer.ARDataTransferManager;
import com.parrot.arsdk.ardatatransfer.ARDataTransferUploader;
import com.parrot.arsdk.ardatatransfer.ARDataTransferUploaderCompletionListener;
import com.parrot.arsdk.ardatatransfer.ARDataTransferUploaderProgressListener;
import com.parrot.arsdk.arutils.ARUtilsManager;

public class UploadListener implements ARDataTransferUploaderProgressListener, ARDataTransferUploaderCompletionListener {

    protected final ARFeatureCommon featureCommon;

    //private ARDataTransferUploader uploader = Move_Activity.uploader;
    //private ARUtilsManager uploadManager = Move_Activity.uploadManager;
    //private ARDataTransferManager dataTransferManager = MainActivity.dataTransferManager;
    //public static HandlerThread uploadHandlerThread = Move_Activity.uploadHandlerThread;
    //protected static String MAVLINK_STORAGE_DIRECTORY = Move_Activity.MAVLINK_STORAGE_DIRECTORY;

    public UploadListener(final ARFeatureCommon featureCommon) {
        this.featureCommon = featureCommon;
    }

    @Override
    public void didUploadComplete(Object arg, final ARDATATRANSFER_ERROR_ENUM error) {
/*
        final Object lock = new Object();

        synchronized (lock) {
            new Thread() {
                @Override
                public void run() {
                    synchronized (lock) {

                        uploader.cancelThread();
                        uploader.dispose();
                        uploader = null;

                        uploadManager.closeWifiFtp();
                        uploadManager.dispose();
                        uploadManager = null;

                        dataTransferManager.dispose();
                        dataTransferManager = null;



                        Log.e(MainActivity.TAG, "Feature Common: " + featureCommon.toString() + "\nError: " + error.toString());

                        if (featureCommon != null && error == ARDATATRANSFER_ERROR_ENUM.ARDATATRANSFER_OK) {
                            Log.e(MainActivity.TAG, "Executing Mavlink FLightplan");
                            ARCONTROLLER_ERROR_ENUM er = featureCommon.sendMavlinkStart("flightplan.mavlink", ARCOMMANDS_COMMON_MAVLINK_START_TYPE_ENUM.ARCOMMANDS_COMMON_MAVLINK_START_TYPE_FLIGHTPLAN);
                            Log.e(MainActivity.TAG, er.toString());
                        }

                        uploadHandlerThread.quit();
                        uploadHandlerThread = null;
                    }
                }
            }.start();
        }
        */
    }

    @Override
    public void didUploadProgress(Object arg, float percent) {

    }
}

