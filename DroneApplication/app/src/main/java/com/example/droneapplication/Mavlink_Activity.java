package com.example.droneapplication;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.os.HandlerThread;
import android.view.View;
import android.widget.Button;
import android.widget.Scroller;
import android.widget.TextView;

import com.parrot.arsdk.arcommands.ARCOMMANDS_ANIMATION_PLAY_MODE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_GPSSETTINGS_HOMETYPE_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED_AXIS_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED_SENSORNAME_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_FLIGHTPLANSTATE_COMPONENTSTATELISTCHANGED_COMPONENT_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKPLAYERRORSTATECHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_MAVLINK_START_TYPE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARFeatureARDrone3;
import com.parrot.arsdk.arcontroller.ARFeatureCommon;
import com.parrot.arsdk.ardatatransfer.ARDATATRANSFER_ERROR_ENUM;
import com.parrot.arsdk.ardatatransfer.ARDATATRANSFER_UPLOADER_RESUME_ENUM;
import com.parrot.arsdk.ardatatransfer.ARDataTransferException;
import com.parrot.arsdk.ardatatransfer.ARDataTransferManager;
import com.parrot.arsdk.ardatatransfer.ARDataTransferUploader;
import com.parrot.arsdk.ardatatransfer.ARDataTransferUploaderCompletionListener;
import com.parrot.arsdk.ardatatransfer.ARDataTransferUploaderProgressListener;
import com.parrot.arsdk.armavlink.ARMAVLINK_ERROR_ENUM;
import com.parrot.arsdk.armavlink.ARMavlinkException;
import com.parrot.arsdk.armavlink.ARMavlinkFileGenerator;
import com.parrot.arsdk.armavlink.ARMavlinkMissionItem;
import com.parrot.arsdk.armavlink.MAV_ROI;
import com.parrot.arsdk.armavlink.MAV_VIEW_MODE_TYPE;
import com.parrot.arsdk.arutils.ARUTILS_ERROR_ENUM;
import com.parrot.arsdk.arutils.ARUtilsManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class Mavlink_Activity extends AppCompatActivity {

    private static final String TAG = "Mavlink_Activity";

    TextView Logs, calibrateCheck, gpsFixedCheck, mavlinkFile;

    Button  writeMavlinkFile, runFlightPlan, calibrate, stopFlightPlan, gpsFixed;

    public static ARDeviceController mARDeviceController = MainActivity.mARDeviceController;

    private ARDataTransferManager dataTransferManager;

    private ARUtilsManager uploadManager;

    private ARDataTransferUploader uploader;

    private HandlerThread uploadHandlerThread;

    private ARMavlinkFileGenerator MavController;

    private static final int DEVICE_PORT = 61;

    protected static String MAVLINK_STORAGE_DIRECTORY;

    private ARDATATRANSFER_ERROR_ENUM uploadError;

    File mavFile;

    final String filename = "flightplan.mavlink5";

    private final String productIP = "192.168.42.1";

<<<<<<< HEAD
=======
    //Constructor
    /*public Mavlink_Activity() throws InterruptedException, ARControllerException {
        calibrate();
        fixGPS();
        //File controller
        try {
            MavController = new ARMavlinkFileGenerator();
        } catch (ARMavlinkException e) {
            Log.e(TAG, "COULDN'T CREATE MAVLINK CONTROLLER");
        }
    }
*/

>>>>>>> d6e0dc2ea01908ff3f8d6bc6a2dd321a433f921d
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mavlink);

        gpsFixedCheck = findViewById(R.id.gpsFixedCheck);
        Logs = findViewById(R.id.log);
        Logs.setMovementMethod(new ScrollingMovementMethod());

        calibrateCheck = findViewById(R.id.calibrateCheck);
        mavlinkFile = findViewById(R.id.mavlinkFile);

        gpsFixed = findViewById(R.id.gpsFix);
        gpsFixed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    fixGPS();
                } catch (ARControllerException e) {
                    Log.e(TAG, "ERROR FIXING GPS: " + e.getMessage());
                    addToLogs("ERROR FIXING GPS: " + e.getMessage());
                }
            }
        });

        writeMavlinkFile = findViewById(R.id.writeMavfile);
        writeMavlinkFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    createMavlinkFile();
                    createFlightPlan();
                    transmitMavlinkFile(mARDeviceController.getFeatureCommon(), mavFile.getPath());
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    addToLogs(e.getMessage());
                }
            }
        });

        runFlightPlan = findViewById(R.id.runFlightPlan);
        runFlightPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    runMavlink(filename);
                } catch (ARControllerException e) {
                    addToLogs(e.toString());
                }
            }});
        stopFlightPlan = findViewById(R.id.stopFlightplan);
        stopFlightPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    stopMavlink();
                } catch (ARControllerException e) {
                    Log.e(TAG, "ERROR STOPPING MAVLINK FILE: " + e.getMessage());
                    addToLogs("ERROR STOPPING MAVLINK FILE: " + e.getMessage());
                }
            }
        });

        calibrate = findViewById(R.id.calibrate);
        calibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    calibrate();
                } catch (ARControllerException | InterruptedException e) {
                    Log.e(TAG, "CALIBRATION ERROR: " + e.getMessage());
                    addToLogs("CALIBRATION ERROR: " + e.getMessage());
                }
            }
        });
        //File controller
        try {
            MavController = new ARMavlinkFileGenerator();
        } catch (ARMavlinkException e) {
            Log.e(TAG, "COULDN'T CREATE MAVLINK CONTROLLER");
        }
    }

    /**
     * Calibrate is the main calibration method. Checks if the drones needs calibration.
     * If it does, then sends a singal to drone to start calibration
     * Calls the calibrating method which prompts what axis to calibrate.
     * If successful then returns. If not then prints out all the motors and their status.
     * @throws ARControllerException
     * @throws InterruptedException
     */
    private void calibrate() throws ARControllerException, InterruptedException {

        List<ARCONTROLLER_DICTIONARY_KEY_ENUM> commandListCamera = new ArrayList<ARCONTROLLER_DICTIONARY_KEY_ENUM>();
        commandListCamera.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_CAMERASETTINGSSTATE_CAMERASETTINGSCHANGED);
        int MaxTilt = calibrateCamera(commandListCamera);
        commandListCamera.clear();
        commandListCamera.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_ORIENTATION);
        int currentTilt = calibrateCamera(commandListCamera);


        List<ARCONTROLLER_DICTIONARY_KEY_ENUM> commandListCalibartion = new ArrayList<ARCONTROLLER_DICTIONARY_KEY_ENUM>();
        commandListCalibartion.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_CAMERASETTINGSSTATE_CAMERASETTINGSCHANGED);
        commandListCalibartion.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_ORIENTATION);
        commandListCalibartion.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONREQUIREDSTATE);
        commandListCalibartion.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTARTEDCHANGED);
        commandListCalibartion.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED);
        commandListCalibartion.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED);

        for(ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey : commandListCalibartion) {
            ARControllerDictionary elementDictionary = mARDeviceController.getCommandElements(commandKey);

            if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONREQUIREDSTATE) && (elementDictionary != null)) {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null) {
                    byte required = (byte) ((Integer) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONREQUIREDSTATE_REQUIRED)).intValue();
                    Log.e(TAG, "CALIBRATION IS REQUIRED: " + required);
                    if(required == 1){
                        calibrateCheck.setText("0");
                        addToLogs("CALIBRATION IS REQUIRED.");
                        ARCONTROLLER_ERROR_ENUM calibrationError = mARDeviceController.getFeatureCommon().sendCalibrationMagnetoCalibration((byte) 1);
                        Log.e(TAG, "CALIBRATION ERROR: " + calibrationError);
                    }
                    if(required == 0){
                        addToLogs("ALREADY CALIBRATED.");
                        calibrateCheck.setText("1");
                        return;
                    }
                }
            }

            if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTARTEDCHANGED) && (elementDictionary != null)){
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null) {
                    byte started = (byte)((Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTARTEDCHANGED_STARTED)).intValue();
                    Log.e(TAG, "CALIBRATION STARTED: " + started);
                    addToLogs("CALIBRATION STARTED: " + started);
                }
            }

            if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED) && (elementDictionary != null)){
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null) {
                    ARCOMMANDS_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED_AXIS_ENUM axis = ARCOMMANDS_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED_AXIS_ENUM.getFromValue((Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED_AXIS));
                    if(calibrating()){
                        Log.e(TAG, "CALIBRATION SUCCESSFUL");
                        addToLogs("CALIBRATION SUCCESSFUL");
                        calibrateCheck.setText("1");
                        return;
                    }
                }
            }

            if (commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED){
                if ((elementDictionary != null) && (elementDictionary.size() > 0)) {
                    Iterator<ARControllerArgumentDictionary<Object>> itr = elementDictionary.values().iterator();
                    while (itr.hasNext()) {
                        ARControllerArgumentDictionary<Object> args = itr.next();
                        if (args != null) {
                            ARCOMMANDS_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED_SENSORNAME_ENUM sensorName = ARCOMMANDS_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED_SENSORNAME_ENUM.getFromValue((Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED_SENSORNAME));
                            byte sensorState = (byte)((Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED_SENSORSTATE)).intValue();
                            Log.e(TAG, "SENSOR: " + sensorName + " STATE: " + sensorState + "\n");
                        }
                    }
                } else {
                    Log.e(TAG, "SENSOR LIST IS EMPTY");
                }
            }
        }


    }
    public boolean calibrating() throws InterruptedException, ARControllerException {

        ARControllerDictionary dict = null;
        String axisString = "";
        try{
            dict = mARDeviceController.getCommandElements(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED);
        } catch (ARControllerException e) {
            e.printStackTrace();
        }

        if(dict != null){
            ARControllerArgumentDictionary<Object> args = dict.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
            if (args != null) {
                ARCOMMANDS_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED_AXIS_ENUM axis = ARCOMMANDS_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED_AXIS_ENUM.getFromValue((Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED_AXIS));
                while(axis.getValue() != 3){
                    axis = ARCOMMANDS_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED_AXIS_ENUM.getFromValue((Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED_AXIS));
                    switch(axis.getValue()){
                        case(0):
                            axisString = "x axis";
                            break;
                        case(1):
                            axisString = "y axis";
                            break;
                        case(2):
                            axisString = "z axis";
                    }

                    Log.e(TAG, "CALIBRATING " + axisString);
                    addToLogs("CALIBRATE " + axisString);
                    Thread.sleep(5000);

                    //Check if the status of the axis changed
                    ARControllerDictionary dictChanged = null;
                    try{
                        dictChanged = mARDeviceController.getCommandElements(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATECHANGED);
                    } catch (ARControllerException e) {
                        e.printStackTrace();
                    }
                    if (dictChanged != null) {
                        ARControllerArgumentDictionary<Object> argsChanged = dictChanged.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                        if (argsChanged != null) {
                            byte xAxisCalibration = (byte) ((Integer) argsChanged.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATECHANGED_XAXISCALIBRATION)).intValue();
                            byte yAxisCalibration = (byte) ((Integer) argsChanged.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATECHANGED_YAXISCALIBRATION)).intValue();
                            byte zAxisCalibration = (byte) ((Integer) argsChanged.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATECHANGED_ZAXISCALIBRATION)).intValue();
                            byte calibrationFailed = (byte) ((Integer) argsChanged.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATECHANGED_CALIBRATIONFAILED)).intValue();
                            if (calibrationFailed != 1) {
                                Log.e(TAG, "X axis Calibration: " + xAxisCalibration + "\n" + "Y axis Calibration: " + yAxisCalibration + "\n" + "Z axis Calibration: " + zAxisCalibration);
                                addToLogs("X axis Calibration: " + xAxisCalibration + "\n" + "Y axis Calibration: " + yAxisCalibration + "\n" + "Z axis Calibration: " + zAxisCalibration);
                            } else if (xAxisCalibration == 1) {
                                // Because Xaxis is the last calibration you do. So if this calibrates then all the other ones are good too.
                                return true;
                            }
                        }
                    }

                    dict = mARDeviceController.getCommandElements(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED);
                    args = dict.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                }
                return true;
            }
        }

        return false;
    }
    public int calibrateCamera(List<ARCONTROLLER_DICTIONARY_KEY_ENUM> list) throws ARControllerException {


        List<ARCONTROLLER_DICTIONARY_KEY_ENUM> commandListFlightplan = list;
        //commandListFlightplan.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED);
        //commandListFlightplan.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_AVAILABILITYSTATECHANGED);
        //commandListFlightplan.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_COMPONENTSTATELISTCHANGED);

        for(ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey : commandListFlightplan) {
            ARControllerDictionary elementDictionary = mARDeviceController.getCommandElements(commandKey);
            //First move the camera down
            if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_CAMERASETTINGSSTATE_CAMERASETTINGSCHANGED) && (elementDictionary != null)) {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null) {
                    float fov = (float) ((Double) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CAMERASETTINGSSTATE_CAMERASETTINGSCHANGED_FOV)).doubleValue();
                    float panMax = (float) ((Double) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CAMERASETTINGSSTATE_CAMERASETTINGSCHANGED_PANMAX)).doubleValue();
                    float panMin = (float) ((Double) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CAMERASETTINGSSTATE_CAMERASETTINGSCHANGED_PANMIN)).doubleValue();
                    float tiltMax = (float) ((Double) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CAMERASETTINGSSTATE_CAMERASETTINGSCHANGED_TILTMAX)).doubleValue();
                    float tiltMin = (float) ((Double) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CAMERASETTINGSSTATE_CAMERASETTINGSCHANGED_TILTMIN)).doubleValue();
                    Log.e(TAG, "Tilt: " + tiltMax + " to " + tiltMin);
                    ARCONTROLLER_ERROR_ENUM er = mARDeviceController.getFeatureARDrone3().sendCameraOrientationV2((byte)tiltMin, (byte)0);
                    addToLogs("TILTING CAMERA TO: " + tiltMin + " tilt");
                    Log.e(TAG, "ERROR MOVING CAMERA: " + er.toString());
                    return (int)tiltMax;
                }
            }

            if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_ORIENTATION) && (elementDictionary != null)) {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null) {
                    byte tilt = (byte) ((Integer) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_ORIENTATION_TILT)).intValue();
                    byte pan = (byte) ((Integer) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_ORIENTATION_PAN)).intValue();
                    Log.e(TAG, "Current Tilt: " + tilt + "\nCurrent Pan: " + pan);
                    addToLogs("Camera is at: " + tilt + " tilt and " + pan + " pan");
                    return tilt;
                }
            }
        }
        return -1;
    }
    public void fixGPS() throws ARControllerException {
        List<ARCONTROLLER_DICTIONARY_KEY_ENUM> commandListGPS = new ArrayList<>();
        commandListGPS.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_GPSFIXSTATECHANGED);
        commandListGPS.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED);
        commandListGPS.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_RESETHOMECHANGED);

        for(ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey : commandListGPS) {
            ARControllerDictionary elementDictionary = mARDeviceController.getCommandElements(commandKey);
            if(commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_GPSFIXSTATECHANGED){
                ARControllerArgumentDictionary<Object> argsFixed = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (argsFixed != null) {
                    byte fixed = (byte) ((Integer) argsFixed.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_GPSFIXSTATECHANGED_FIXED)).intValue();
                    gpsFixedCheck.setText(String.format("%s", fixed));
                    List<Float> position = getDronePosition();
                    addToLogs( "GPS Postion: (" + position.get(0) + "," + position.get(1) + "," + position.get(2) + ")");
                    ARCONTROLLER_ERROR_ENUM error = mARDeviceController.getFeatureARDrone3().sendGPSSettingsHomeType(ARCOMMANDS_ARDRONE3_GPSSETTINGS_HOMETYPE_TYPE_ENUM.ARCOMMANDS_ARDRONE3_GPSSETTINGS_HOMETYPE_TYPE_TAKEOFF);
                    Log.e(TAG, "RESET HOME GPS ERROR: " + error);
                }
            }
            if(commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED){
                ARControllerArgumentDictionary<Object> argsUpdate = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (argsUpdate != null) {
                    ARCOMMANDS_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED_STATE_ENUM state = ARCOMMANDS_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED_STATE_ENUM.getFromValue((Integer) argsUpdate.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED_STATE));
                    addToLogs(String.format("GPS FIX UPDATED: %s", state.toString()));
                    Log.e(TAG, "GPS FIX UPDATED: " + state.toString());
                }
            }
            if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_RESETHOMECHANGED) && (elementDictionary != null)){
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null) {
                    double latitude = (double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_RESETHOMECHANGED_LATITUDE);
                    double longitude = (double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_RESETHOMECHANGED_LONGITUDE);
                    double altitude = (double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_RESETHOMECHANGED_ALTITUDE);
                    Log.e(TAG, "NEW HOME GPS: (" + latitude + "," + longitude + "," + altitude + ")");
                    addToLogs("NEW HOME GPS: (" + latitude + "," + longitude + "," + altitude + ")");
                }
            }
        }
    }

    public void createMavlinkFile(){
        Log.e(TAG, "CREATING MAVLINK FILE. LOADING COMMANDS....");

        //Initial drone and camera positioning
        List<Float> position = getDronePosition();
        ARMavlinkMissionItem takePanarama = ARMavlinkMissionItem.CreateMavlinkCreatePanoramaMissionItem(0,(float)-100,0,(float)30);
        ARMavlinkMissionItem takeoff = ARMavlinkMissionItem.CreateMavlinkTakeoffMissionItem(position.get(0),position.get(1), 2,0, 0);
        ARMavlinkMissionItem initialAltitude = ARMavlinkMissionItem.CreateMavlinkNavWaypointMissionItem(position.get(0),position.get(1), 5, 220);
        MavController.addMissionItem(takeoff);
        MavController.addMissionItem(initialAltitude);
        MavController.addMissionItem(takePanarama);


        //add custom flight plan here
        addMavlinkCommands(1,40.379153, -74.525307,6,30);
        addMavlinkCommands(2, 0,0,0,0);
        addMavlinkCommands( )
        deviceController.getFeatureAnimation().sendStartVerticalReveal(Log.e(TAG);


        //Land at start position
        ARMavlinkMissionItem land = ARMavlinkMissionItem.CreateMavlinkLandMissionItem(position.get(0),position.get(1),0,0);
        MavController.addMissionItem(land);
        Log.e(TAG, "COMMANDS LOADED");
    }
    /**
     *  Adds flighplan commands
     * @param command 1 is to add a waypoint. 2 is for taking a pic.
     * @param latitude latitude if command is 1
     * @param longitude longitude if command is 1
     * @param altitude altitude if command is 1 (meters)
     * @param yaw 0 degrees is north and all degrees are measured in reference to North. (degrees)
     * @throws ARMavlinkException
     */
    public void addMavlinkCommands(int command, double latitude, double longitude, double altitude, double yaw) {

        //command 1 is move to a location
        if(command == 1){
            ARMavlinkMissionItem navPoint1 = ARMavlinkMissionItem.CreateMavlinkNavWaypointMissionItem((float)latitude, (float)longitude, (float)altitude, (float)yaw);
            MavController.addMissionItem(navPoint1);
        }
        if(command == 2){
            ARMavlinkMissionItem takePic = ARMavlinkMissionItem.CreateMavlinkImageStartCaptureMissionItem(0,1,0);
            ARMavlinkMissionItem stopPic = ARMavlinkMissionItem.CreateMavlinkImageStopCaptureMissionItem();
            MavController.addMissionItem(takePic);
            MavController.addMissionItem(stopPic);
        }
    }

    /**
     * Uploads Flightplan to drone
     * @throws IOException
     */
    private void createFlightPlan() throws IOException {
        // save our mavlink file on android phone's directory
        MAVLINK_STORAGE_DIRECTORY = getApplicationContext().getFilesDir().toString();

        File file = new File(MAVLINK_STORAGE_DIRECTORY);
        Log.e(TAG, "MAVLINK STORAGE DIRECTORY: "+ getApplicationContext().getFilesDir());


        mavFile = new File(MAVLINK_STORAGE_DIRECTORY + File.separator + filename);
        boolean fileCreated = mavFile.createNewFile();
        if(!fileCreated){
            Log.e(TAG, "MAVFILE CREATION ERROR");
            boolean deleteOldFile = mavFile.delete();
            Log.e(TAG, "DELETED OLD MAVFILE: " + deleteOldFile);
            boolean createNewFile = mavFile.createNewFile();
            Log.e(TAG, "CREATING NEW MAVFILE: " + createNewFile);
        }


        //noinspection ResultOfMethodCallIgnored
        //mavFile.delete();
        MavController.CreateMavlinkFile(mavFile.getPath());
        addToLogs("CREATED MAVLINK FILE");
    }
    public void transmitMavlinkFile(final ARFeatureCommon featureCommon, String filepath) {
        addToLogs("UPLOADING MAVLINK FILE...");

        try {
            dataTransferManager = new ARDataTransferManager();
            uploader = dataTransferManager.getARDataTransferUploader();
            uploadManager = new ARUtilsManager();

            ARUTILS_ERROR_ENUM isWifiInitiated = uploadManager.initWifiFtp(productIP, DEVICE_PORT, "", "");
            if(!isWifiInitiated.equals(ARUTILS_ERROR_ENUM.ARUTILS_OK)) {
                Log.e(TAG, "WIFI INITIATED ERROR: " + isWifiInitiated.toString());
            }


            final UploadListener listener = new UploadListener(featureCommon);
            try {
                uploader.createUploader(uploadManager, "/"+filename, filepath, listener, null, listener, null, ARDATATRANSFER_UPLOADER_RESUME_ENUM.ARDATATRANSFER_UPLOADER_RESUME_FALSE);
            }catch(ARDataTransferException e){
                ARDATATRANSFER_ERROR_ENUM datatransfererror = e.getError();
                Log.e(TAG, "DATA TRANSFER ERROR: " + datatransfererror.toString());
                //Log.e(TAG, e.getMessage());
            }

            uploadHandlerThread = new HandlerThread("mavlink_uploader");
            uploadHandlerThread.start();

            Runnable uploadRunnable = uploader.getUploaderRunnable();
            Handler uploadHandler = new Handler(uploadHandlerThread.getLooper());

            uploadHandler.post(uploadRunnable);

            addToLogs("UPLOAD COMPLETE");
        } catch (Exception e) {
            Log.e(TAG, "transmitMavlinkFile exception: " + e.getMessage(), e);
        }
    }

    /**
     * Runs the Mavlink file uploaded onto the drone
     * @param filename
     * @throws ARControllerException
     */
    public void runMavlink(String filename) throws ARControllerException {

        List<ARCONTROLLER_DICTIONARY_KEY_ENUM> list = new ArrayList<ARCONTROLLER_DICTIONARY_KEY_ENUM>();

        //First check if flightplan is available
        list.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_AVAILABILITYSTATECHANGED);
        int FLIGHTPLANAVALIABLE = MavlinkFileController(list);
        //if its available then run the flightplan
        if(FLIGHTPLANAVALIABLE != 1){

            //running flightplan
            Log.d(TAG, "Executing Mavlink FLightplan: " + mavFile.getPath());
            addToLogs("EXECUTING MAVLINK FLIGHTPLAN: " + mavFile.getPath());
            ARCONTROLLER_ERROR_ENUM er = mARDeviceController.getFeatureCommon().sendMavlinkStart(filename, ARCOMMANDS_COMMON_MAVLINK_START_TYPE_ENUM.ARCOMMANDS_COMMON_MAVLINK_START_TYPE_FLIGHTPLAN);
            addToLogs("EXECUTED MAVLINK FILE. ERRORS: " + er.toString());


            //check if the piloting state changed and if there was a mavlink error running the file
            list.clear();
            list.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED);
            list.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKPLAYERRORSTATECHANGED);
            MavlinkFileController(list);
        }
        else{
            //check components of Flight plan that are not available
            list.clear();
            list.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_COMPONENTSTATELISTCHANGED);
            MavlinkFileController(list);
        }
    }
    /**
     * Stops the Mavlink file when it's running
     * @throws ARControllerException
     */
    public void stopMavlink() throws ARControllerException {
        List<ARCONTROLLER_DICTIONARY_KEY_ENUM> list = new ArrayList<ARCONTROLLER_DICTIONARY_KEY_ENUM>();

        //First check if the mavlink file is running
        list.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED);
        int mavlinkFileRunning = MavlinkFileController(list);
        if(mavlinkFileRunning == 0){
            //If Mavlink File is running then try to stop it
            ARCONTROLLER_ERROR_ENUM er = mARDeviceController.getFeatureCommon().sendMavlinkStop();
            //mARDeviceController.getFeatureARDrone3().sendPilotingEmergency();
            Log.e(TAG, "Stopping Mavlink File: " + er.toString());
            //addToLogs("CUT OUT MOTORS!!");

            //check if you stopped the mavlink file
            mavlinkFileRunning = MavlinkFileController(list);
            if(mavlinkFileRunning == 1){
                Log.e(TAG, "SUCESSFULLY STOPPED MAVLINK FILE");
                addToLogs("SUCESSFULLY STOPPED MAVLINK FILE");
            }
        }
        else if(mavlinkFileRunning == 2){
            Log.e(TAG, "MAVLINK FILE IS PAUSED");
            addToLogs("MAVLINK FILE IS PAUSED");
        }
        else if(mavlinkFileRunning == 3){
            Log.e(TAG, "MAVLINK FILE IS LOADED AND READY TO EXECUTE");
            addToLogs("MAVLINK FILE IS LOADED AND READY TO EXECUTE");
        }
        else if(mavlinkFileRunning == Integer.MIN_VALUE){
            Log.e(TAG, "TRIED TO STOP MAVLINK FILE BUT MAVLINK FILE NOT RUNNING");
        }

    }
    /**
     * Helper method for running and stopping mavlink file.
     * Retrieves status's of multiple things while mavlinkfile is executing
     * @param list
     * @return
     * @throws ARControllerException
     */
    public int MavlinkFileController(List<ARCONTROLLER_DICTIONARY_KEY_ENUM> list) throws ARControllerException {
        if (uploadError == ARDATATRANSFER_ERROR_ENUM.ARDATATRANSFER_OK) {

            List<ARCONTROLLER_DICTIONARY_KEY_ENUM> commandListFlightplan = list;
            //commandListFlightplan.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED);
            //commandListFlightplan.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_AVAILABILITYSTATECHANGED);
            //commandListFlightplan.add(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_COMPONENTSTATELISTCHANGED);

            for(ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey : commandListFlightplan) {
                ARControllerDictionary elementDictionary = mARDeviceController.getCommandElements(commandKey);
                if (commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_AVAILABILITYSTATECHANGED) {
                    ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_AVAILABILITYSTATECHANGED);
                    if (args != null) {
                        byte AvailabilityState = (byte) ((Integer) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_AVAILABILITYSTATECHANGED_AVAILABILITYSTATE)).intValue();
                        Log.e(TAG, "FLIGHTPLAN AVALIABILITY: " + AvailabilityState);
                        addToLogs("FLIGHTPLAN AVALIABILITY: " + AvailabilityState);
                        return AvailabilityState;
                    }
                }
                if (commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_COMPONENTSTATELISTCHANGED){
                    //Flightplan Components
                    Iterator<ARControllerArgumentDictionary<Object>> itr = elementDictionary.values().iterator();
                    while (itr.hasNext()) {
                        ARControllerArgumentDictionary<Object> argsFlightPlanComponents = itr.next();
                        if (argsFlightPlanComponents != null) {
                            ARCOMMANDS_COMMON_FLIGHTPLANSTATE_COMPONENTSTATELISTCHANGED_COMPONENT_ENUM component = ARCOMMANDS_COMMON_FLIGHTPLANSTATE_COMPONENTSTATELISTCHANGED_COMPONENT_ENUM.getFromValue((Integer) argsFlightPlanComponents.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_COMPONENTSTATELISTCHANGED_COMPONENT));
                            byte State = (byte) ((Integer) argsFlightPlanComponents.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_COMPONENTSTATELISTCHANGED_STATE)).intValue();
                            Log.e(TAG, "FLIGHTPLAN COMPONENTS: \nComponent: " + component + " State: " + State + "\n");
                            addToLogs("FLIGHTPLAN COMPONENTS: \nComponent: " + component + " State: " + State + "\n");
                        }
                    }
                }
                if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED) && (elementDictionary != null)){
                    ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                    if (args != null) {
                        ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_STATE_ENUM state = ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_STATE_ENUM.getFromValue((Integer) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_STATE));
                        String filepath = (String) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_FILEPATH);
                        ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_TYPE_ENUM type = ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_TYPE_ENUM.getFromValue((Integer) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_TYPE));
                        Log.e(TAG, "\nMAVLINK FILE PLAYING STATE: " + state.toString() + "\nFEATURE COMMON FILEPATH: " + filepath + "\nMAVLINK FILE PLAYING TYPE: " + type.toString());
                        addToLogs("\nMAVLINK FILE PLAYING STATE: " + state.toString() + "\nFEATURE COMMON FILEPATH: " + filepath + "\nMAVLINK FILE PLAYING TYPE: " + type.toString());
                        return state.getValue();
                    }
                }
                if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKPLAYERRORSTATECHANGED) && (elementDictionary != null)){
                    ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                    if (args != null) {
                        ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKPLAYERRORSTATECHANGED_ERROR_ENUM error = ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKPLAYERRORSTATECHANGED_ERROR_ENUM.getFromValue((Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKPLAYERRORSTATECHANGED_ERROR));
                        Log.e(TAG, "MAVLINK PLAY ERROR STATE CHANGED: " + error.toString());
                        addToLogs("MAVLINK PLAY ERROR STATE CHANGED: " + error.toString());
                    }
                }
            }
        }
        return -1;
    }

    public ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM getPilotingState()
    {
        ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState = ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.eARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_UNKNOWN_ENUM_VALUE;
        if (mARDeviceController != null)
        {
            try
            {
                ARControllerDictionary dict = mARDeviceController.getCommandElements(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED);
                if (dict != null)
                {
                    ARControllerArgumentDictionary<Object> args = dict.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                    if (args != null)
                    {
                        Integer flyingStateInt = (Integer) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE);
                        flyingState = ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.getFromValue(flyingStateInt);
                    }
                }
            }
            catch (ARControllerException e)
            {
                e.printStackTrace();
            }

            return flyingState;
        }
        return null;
    }

    public List<Float> getDronePosition()
    {

        List position = new ArrayList<Float>();
        if (mARDeviceController != null)
        {
            try
            {
                ARControllerDictionary dict = mARDeviceController.getCommandElements(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED);
                if (dict != null)
                {
                    ARControllerArgumentDictionary<Object> args = dict.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                    if (args != null)
                    {
                        Log.e(TAG, "Position: (" + args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_LATITUDE) + " "
                                + args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_LONGITUDE) + " "
                                + args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_ALTITUDE) + ")");

                        float latitude = (float)((double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_LATITUDE));
                        float longitude = (float)((double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_LONGITUDE));
                        float altitude = (float)((double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_ALTITUDE));
                        position.add(latitude);
                        position.add(longitude);
                        position.add(altitude);


                    }
                }
            }
            catch (ARControllerException e)
            {
                e.printStackTrace();
            }

            return position;
        }
        return null;
    }

    public void addToLogs(String newLog){
        String oldLogs = Logs.getText().toString();
        Logs.setText(new SpannableString(String.format("%s\n%s", oldLogs, newLog)));
    }

    private class UploadListener implements ARDataTransferUploaderProgressListener, ARDataTransferUploaderCompletionListener {

        private final ARFeatureCommon featureCommon;

        private UploadListener(final ARFeatureCommon featureCommon) {
            this.featureCommon = featureCommon;
        }

        @Override
        public void didUploadComplete(Object arg, final ARDATATRANSFER_ERROR_ENUM error) {

            uploadError = error;
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

                            uploadHandlerThread.quit();
                            uploadHandlerThread = null;

                            Log.d(TAG, "UPLOAD COMPLETION ERROR: " + error.toString()); // Return Error: Ftp error
                        }
                    }
                }.start();
            }
        }

        @Override
        public void didUploadProgress(Object arg, float percent) {

        }
    }
}
