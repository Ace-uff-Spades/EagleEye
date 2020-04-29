package com.example.droneapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.R.layout;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.arcontroller.ARDeviceControllerStreamListener;
import com.parrot.arsdk.arcontroller.ARFeatureARDrone3;
import com.parrot.arsdk.arcontroller.ARFeatureCommon;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardatatransfer.ARDATATRANSFER_ERROR_ENUM;
import com.parrot.arsdk.ardatatransfer.ARDataTransferException;
import com.parrot.arsdk.ardatatransfer.ARDataTransferManager;
import com.parrot.arsdk.ardatatransfer.ARDataTransferMedia;
import com.parrot.arsdk.ardatatransfer.ARDataTransferMediasDownloader;
import com.parrot.arsdk.ardatatransfer.ARDataTransferMediasDownloaderAvailableMediaListener;
import com.parrot.arsdk.ardatatransfer.ARDataTransferMediasDownloaderCompletionListener;
import com.parrot.arsdk.ardatatransfer.ARDataTransferMediasDownloaderProgressListener;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceNetService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;
import com.parrot.arsdk.armedia.ARMediaObject;
import com.parrot.arsdk.arsal.ARSALPrint;
import com.parrot.arsdk.arutils.ARUtilsException;
import com.parrot.arsdk.arutils.ARUtilsFtpConnection;
import com.parrot.arsdk.arutils.ARUtilsManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ARDiscoveryServicesDevicesListUpdatedReceiverDelegate, ARDeviceControllerStreamListener, ARDataTransferMediasDownloaderAvailableMediaListener,ARDataTransferMediasDownloaderCompletionListener, ARDataTransferMediasDownloaderProgressListener,ARDeviceControllerListener{

    public static final String TAG = "DroneDiscoverer";

    private final Context mContext;
    private ARDiscoveryDeviceService myDeviceService;
    private ARDiscoveryServicesDevicesListUpdatedReceiver mArdiscoveryServicesDevicesListUpdatedReceiver;
    private ARDiscoveryService mArdiscoveryService;
    private ServiceConnection mArdiscoveryServiceConnection;
    private ARDiscoveryDevice mARDiscoveryDevice;
    public static ARDeviceController mARDeviceController;
    private ARCONTROLLER_DEVICE_STATE_ENUM mState;
    private BluetoothController mBluetoothController;
    private MavlinkController mMavlinkThread;

    private static final int DEVICE_PORT = 21;
    private static final String MEDIA_FOLDER = "internal_000";

    private AsyncTask<Void, Float, ArrayList<ARMediaObject>> getMediaAsyncTask;
    private Handler mFileTransferThreadHandler;
    private HandlerThread mFileTransferThread;

    private boolean isRunning = false;
    private boolean isDownloading = false;

    private final Object lock = new Object();

    private ARDataTransferManager dataTransferManager;
    private ARUtilsManager ftpListManager;
    private ARUtilsManager ftpQueueManager;
    private ArrayList<Integer> mediaToDownload;

    public int didMediaDownload;
    private final int CreateList = 0;
    private boolean mediaDoneDownloading;
    public ExifInterface pictureExif;

    TextView viewer,viewer2;
    Button findNetworks,connectDrone,takeOffBtn,landBtn,connect,findDevice,sendPic,takePic, moveActivity,tiltDown,stopMavlink;
    ListView wifiViewer,resultViewer;
    Bitmap bitmap;
    Cord[] cords;

    public MainActivity() {
        mediaDoneDownloading = false;
        this.mArdiscoveryServicesDevicesListUpdatedReceiver = null;
        this.mContext = null;
        mARDiscoveryDevice = null;
        myDeviceService = null;
        mState = ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_STOPPED;
        getMediaAsyncTask = null;
        mediaToDownload = new ArrayList<>();
        bitmap = null;
        didMediaDownload=0;
        //THIS IS WHERE THE COORDINGATES OF THE INITIAL AREA TO FLY THROUGH GOES
<<<<<<< HEAD
        cords = new Cord[9];
        cords[0] = new Cord(40.344862, -74.560916, 4, 0);
        cords[1] = new Cord(40.344870, -74.560589, 4, 0);
        cords[2] = new Cord(40.344878, -74.560032, 4, 0);
        cords[3] = new Cord(40.344854, -74.559480, 4, 0);
        cords[4] = new Cord(40.344862, -74.560916, 4, 0);
        cords[5] = new Cord(40.344547, -74.559496, 4, 0);
        cords[6] = new Cord(40.344563, -74.560005, 4, 0);
        cords[7] = new Cord(40.344575, -74.560499, 4, 0);
        cords[8] = new Cord(40.344596, -74.561041, 4, 0);
=======
        cords = new Cord[4];
        cords[0] = new Cord(40.3442014, -74.5614818, 6, 0);
        cords[1] = new Cord(40.3443000, -74.5613500, 6, 30);
        cords[2] = new Cord(40.3443419, -74.5612575, 6, 30);
        cords[3] = new Cord(40.3444000, -74.5612000, 6, 30);

>>>>>>> d6e0dc2ea01908ff3f8d6bc6a2dd321a433f921d
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultViewer = findViewById(R.id.resultViewer);
        Handler mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                switch(message.what) {
                    case (CreateList):

                        CustomAdapter customAdapter = new CustomAdapter();
                        resultViewer.setAdapter(customAdapter);

                        //Create the Return Mavlink File with the updated locationList
                        /*
                        cords = new Cord[mBluetoothController.locationList.size()];
                        for(int i = 0; i<cords.length; i++) {
                            cords[i] = mBluetoothController.locationList.get(i);
                        }
                        mMavlinkThread = new MavlinkController(cords,getApplicationContext());
                        int result = mMavlinkThread.start();
                        if(result == 1){
                            Log.e(TAG, "SUCCESSFULLY WENT TO PERSON OF INTEREST");
                        }
                        else if(result == 0){
                            Log.e(TAG, "NOT SUCCESSFULL ON RETURN FLIGHTPLAN");
                        }
                        
                         */

                        break;
                }
                return false;
            }
        });
        mBluetoothController = new BluetoothController(mHandler,getApplicationContext());
<<<<<<< HEAD
=======

        //Creating Mavlink Controller
>>>>>>> d6e0dc2ea01908ff3f8d6bc6a2dd321a433f921d
        tiltDown = findViewById(R.id.tiltDown);
        findNetworks = findViewById(R.id.findNetworks);
        connectDrone = findViewById(R.id.connectBtn);
        wifiViewer = findViewById(R.id.wifiViewer);
        viewer = findViewById(R.id.viewer);
        takeOffBtn = findViewById(R.id.takeOffBtn);
        landBtn = findViewById(R.id.landBtn);
        viewer2 = findViewById(R.id.viewer2);
        connect = findViewById(R.id.connect);
        sendPic = findViewById(R.id.sendPic);
        takePic = findViewById(R.id.takePic);
        findDevice=findViewById(R.id.findDevice);
        moveActivity = findViewById(R.id.move_activity);
        stopMavlink = findViewById(R.id.StopMavlink);
        ARSDK.loadSDKLibs();
        implementListeners();
    }

    @SuppressLint("WrongThread")
    public void implementListeners()
    {
        stopMavlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mMavlinkThread.stopMavlink();
                } catch (ARControllerException e) {
                    Log.e(TAG, "COULDN'T STOP MAVLINK");
                }
            }
        });
        tiltDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mARDeviceController.getFeatureARDrone3().setCameraOrientation((byte)-50,(byte)0);
            }
        });
        //Drone takes a picture
        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mARDeviceController.getFeatureARDrone3().sendMediaRecordPictureV2();
            }
        });

        //Drone downloads the picture and sends it to raspberryPi
        sendPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewer.setText("Fetching Media");
                //createDataTransferManager();
                //fetchMediasList();
                ArrayList<Integer> list = new ArrayList<>();
                for(int x=0;x<1;x++)
                    list.add(x);
                try {
                    sendMedias();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ARDataTransferException e) {
                    e.printStackTrace();
                }
            }
        });

        //App tries to find raspberry pi on bluetooth radar; raspberry pi has to be paired already
        findDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewer2.setText("Finding Device");
                if(mBluetoothController.findRaspberryPi())
                    viewer2.setText("Found!");
                else
                    viewer2.setText("Not Found");
            }
        });

        //App connects to raspberry pi, cannot work unless findDevice is called before
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBluetoothController.connect())
                    viewer2.setText("Connected!");
                else
                    viewer2.setText("Not Connected");
            }
        });

        //Searches for drone's wifi network; has to be connected on phones wifi for this to work
        findNetworks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initDiscoveryService();
                registerReceivers();
                onServicesDevicesListUpdated();
            }
        });

        //Connects to drone after drone is found on wifi, cannot work unless findNetworks is called before
        connectDrone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDeviceController();
<<<<<<< HEAD
                //Creating Mavlink Controller
                mMavlinkThread = new MavlinkController(cords);
=======
                mMavlinkThread = new MavlinkController(cords,getApplicationContext());
>>>>>>> d6e0dc2ea01908ff3f8d6bc6a2dd321a433f921d
            }
        });

        //Drone takesOff
        takeOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //takeoff();
                int result = mMavlinkThread.start();
                if(result == 0){
                    Log.e(TAG, "MAVLINK THREAD DIDN'T WORK");
                }
                else if(result == 1) {
                    createDataTransferManager();
                    fetchMediasList();
                    Log.e(TAG, "CONNECTED TO PI AND TRANSMITTING PICTURES.....");
                }
                else {
                    Log.e(TAG, "MAVLINKTHREAD RETURNED SOME SHIT THAT WE DON'T KNOW");
                }
            }
        });
        landBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                land();
            }
        });

        //Drone lands
        moveActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMoveActivity();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mARDeviceController!=null)
            mARDeviceController.dispose();
        viewer.setText("Disoposed");
    }

    public void openMoveActivity(){
        Intent intent = new Intent(this, Move_Activity.class);
        startActivity(intent);
    }

    public void takeoff()
    {
        if (ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED.equals(getPilotingState()))
        {
            ARCONTROLLER_ERROR_ENUM error = mARDeviceController.getFeatureARDrone3().sendPilotingTakeOff();

            if (!error.equals(ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK))
            {
                ARSALPrint.e(TAG, "Error while sending take off: " + error);
            }
        }
    }

    public void land()
    {
        ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState = getPilotingState();
        if (ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING.equals(flyingState))
        {
            ARCONTROLLER_ERROR_ENUM error = mARDeviceController.getFeatureARDrone3().sendPilotingLanding();

            if (!error.equals(ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK))
            {
                ARSALPrint.e(TAG, "Error while landing: " + error);
            }
        }
    }

    public void initDiscoveryService()
    {
        // create the service connection
        if (mArdiscoveryServiceConnection == null)
        {
            mArdiscoveryServiceConnection = new ServiceConnection()
            {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service)
                {
                    mArdiscoveryService = ((ARDiscoveryService.LocalBinder) service).getService();

                    startDiscovery();
                }

                @Override
                public void onServiceDisconnected(ComponentName name)
                {
                    mArdiscoveryService = null;
                }
            };
        }

        if (mArdiscoveryService == null)
        {
            // if the discovery service doesn't exists, bind to it
            Intent i = new Intent(getApplicationContext(), ARDiscoveryService.class);
            getApplicationContext().bindService(i, mArdiscoveryServiceConnection, Context.BIND_AUTO_CREATE);
        }
        else
        {
            // if the discovery service already exists, start discovery
            startDiscovery();
        }
    }

    public void startDiscovery()
    {
        if (mArdiscoveryService != null)
        {
            mArdiscoveryService.start();
        }
    }

    public void registerReceivers()
    {
        mArdiscoveryServicesDevicesListUpdatedReceiver = new ARDiscoveryServicesDevicesListUpdatedReceiver((ARDiscoveryServicesDevicesListUpdatedReceiverDelegate) this);
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastMgr.registerReceiver(mArdiscoveryServicesDevicesListUpdatedReceiver, new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
    }

    public void onServicesDevicesListUpdated() {
        viewer.setText("Fetching Devices");

        if (mArdiscoveryService != null) {

            List<ARDiscoveryDeviceService> deviceList = mArdiscoveryService.getDeviceServicesArray();

            String [] deviceListString = new String[deviceList.size()];
            int x=0;
            for(ARDiscoveryDeviceService device1:deviceList)
            {
                deviceListString[x]=device1.toString();
                x++;
            }
            ArrayAdapter <String> adapter = new ArrayAdapter(this,layout.simple_list_item_1,deviceListString);
            wifiViewer.setAdapter(adapter);
            myDeviceService = deviceList.get(0);
            viewer.setText("Found Device");
            // Do what you want with the device list
        }
    }

    public void createDeviceController(){
        mARDiscoveryDevice = createDiscoveryDevice(myDeviceService);
        try {
            mARDeviceController = new ARDeviceController(mARDiscoveryDevice);
        }
        catch(ARControllerException e)
        {
            e.printStackTrace();
        }
        unregisterReceivers();
        closeServices();
        viewer.setText(mARDeviceController.toString());
        mARDeviceController.addListener(this);
        mARDeviceController.addStreamListener(this);
        if ((mARDeviceController != null) && (ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_STOPPED.equals(mState))) {
            ARCONTROLLER_ERROR_ENUM error = mARDeviceController.start();
            if(error!=ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK)
                finish();
        }
        viewer.setText("Connected");
    }

    public ARDiscoveryDevice createDiscoveryDevice(ARDiscoveryDeviceService service)
    {
        ARDiscoveryDevice device = null;
        if ((service != null)&&
                (ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_BEBOP_2.equals(ARDiscoveryService.getProductFromProductID(service.getProductID()))))
        {
            try
            {
                device = new ARDiscoveryDevice();

                ARDiscoveryDeviceNetService netDeviceService = (ARDiscoveryDeviceNetService) service.getDevice();

                device.initWifi(ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_ARDRONE, netDeviceService.getName(), netDeviceService.getIp(), netDeviceService.getPort());
            }
            catch (ARDiscoveryException e)
            {
                e.printStackTrace();
                Log.e(TAG, "Error: " + e.getError());
            }
        }
        return device;
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

    //Connects drone memory to phones memory and creates ARdataTransferManager
    public void createDataTransferManager() {
        String productIP = "192.168.42.1";  // TODO: get this address from libARController

        ARDATATRANSFER_ERROR_ENUM result = ARDATATRANSFER_ERROR_ENUM.ARDATATRANSFER_OK;
        try
        {
            dataTransferManager = new ARDataTransferManager();
        }
        catch (ARDataTransferException e)
        {
            e.printStackTrace();
            result = ARDATATRANSFER_ERROR_ENUM.ARDATATRANSFER_ERROR;
        }

        if (result == ARDATATRANSFER_ERROR_ENUM.ARDATATRANSFER_OK)
        {
            try
            {
                ftpListManager = new ARUtilsManager();
                ftpQueueManager = new ARUtilsManager();

                ftpListManager.initWifiFtp(productIP, DEVICE_PORT, ARUtilsFtpConnection.FTP_ANONYMOUS, "");
                ftpQueueManager.initWifiFtp(productIP, DEVICE_PORT, ARUtilsFtpConnection.FTP_ANONYMOUS, "");
            }
            catch (ARUtilsException e)
            {
                e.printStackTrace();
                result = ARDATATRANSFER_ERROR_ENUM.ARDATATRANSFER_ERROR_FTP;
            }
        }
        if (result == ARDATATRANSFER_ERROR_ENUM.ARDATATRANSFER_OK)
        {
            //Directory in where media is going to be downloaded on the phone
            String externalDirectory = getApplicationContext().getExternalFilesDir(null).getPath();
            try
            {
                dataTransferManager.getARDataTransferMediasDownloader().createMediasDownloader(ftpListManager, ftpQueueManager, MEDIA_FOLDER, externalDirectory);
            }
            catch (ARDataTransferException e)
            {
                e.printStackTrace();
                result = e.getError();
            }
        }

        if (result == ARDATATRANSFER_ERROR_ENUM.ARDATATRANSFER_OK)
        {
            // create a thread for the download to run the download runnable
            mFileTransferThread = new HandlerThread("FileTransferThread");
            mFileTransferThread.start();
            mFileTransferThreadHandler = new Handler(mFileTransferThread.getLooper());
        }

        if (result != ARDATATRANSFER_ERROR_ENUM.ARDATATRANSFER_OK)
        {
            // clean up here because an error happened
        }
    }

    //Fetches list of medias that will later be downloaded, calls download medias on post execute
    @SuppressLint("StaticFieldLeak")
    private void fetchMediasList() {
        if (getMediaAsyncTask == null)
        {
            getMediaAsyncTask = new AsyncTask<Void, Float, ArrayList<ARMediaObject>>()
            {
                @Override
                protected ArrayList<ARMediaObject> doInBackground(Void... params)
                {
                    ArrayList<ARMediaObject> mediaList = null;
                    synchronized (lock)
                    {
                        ARDataTransferMediasDownloader mediasDownloader = null;
                        if (dataTransferManager != null)
                        {
                            mediasDownloader = dataTransferManager.getARDataTransferMediasDownloader();
                        }

                        if (mediasDownloader != null) {
                            try {
                                int mediaListCount = mediasDownloader.getAvailableMediasSync(false);
                                mediaList = new ArrayList<>(mediaListCount);
                                for (int i = 0; i < mediaListCount; i++) {
                                    ARDataTransferMedia currentMedia = mediasDownloader.getAvailableMediaAtIndex(i);
                                    final ARMediaObject currentARMediaObject = new ARMediaObject();
                                    currentARMediaObject.updateDataTransferMedia(getResources(), currentMedia);
                                    mediaList.add(currentARMediaObject);
                                }
                            } catch (ARDataTransferException e) {
                                e.printStackTrace();
                                mediaList = null;
                            }
                        }

                    }
                    return mediaList;
                }

                @Override
                protected void onPostExecute(ArrayList<ARMediaObject> arMediaObjects)
                {
                    //Adds the media indexes to an arrayList of the medias that you want to download
                    for(int x=0;x<arMediaObjects.size();x++)
                        mediaToDownload.add(x);

                    //Passes the arrayList to downloadmedias() to download the medias
                    try {
                        downloadMedias(mediaToDownload);
                    } catch (ARDataTransferException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
        }

        if (getMediaAsyncTask.getStatus() != AsyncTask.Status.RUNNING) {
            getMediaAsyncTask.execute();
        }
    }

    //Download the medias on to phones internal memory
    private void downloadMedias(ArrayList<Integer> mediaToDl) throws ARDataTransferException, IOException, InterruptedException {
        ARDataTransferMediasDownloader mediasDownloader = null;
        if (dataTransferManager != null)
        {
            mediasDownloader = dataTransferManager.getARDataTransferMediasDownloader();
        }

        if (mediasDownloader != null)
        {
            for (int i = 0; i < mediaToDl.size(); i++)
            {
                int mediaIndex = mediaToDl.get(i);
                ARDataTransferMedia mediaObject = null;
                try
                {
                    mediaObject = dataTransferManager.getARDataTransferMediasDownloader().getAvailableMediaAtIndex(mediaIndex);
                }
                catch (ARDataTransferException e)
                {
                    e.printStackTrace();
                }

                if (mediaObject != null)
                {
                    try
                    {
                        mediasDownloader.addMediaToQueue(mediaObject, this, null, this, null);
                    }
                    catch (ARDataTransferException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            if (!isRunning)
            {
                isRunning = true;
                Runnable downloaderQueueRunnable = mediasDownloader.getDownloaderQueueRunnable();
                mFileTransferThreadHandler.post(downloaderQueueRunnable);//imageView.setImageBitmap(mediasDownloader.getAvailableMediaAtIndex(0));
            }
        }
        isDownloading = true;
        while(true)
        {
            if(didMediaDownload==mediaToDl.size())
            {
                sendMedias();
                break;
            }
        }
        /*
        viewer.setText("Media Downloaded");
        try {
            sendMedias(mediaToDl);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
         */
    }

    //Send the media to the raspberry pi
    public void sendMedias() throws IOException, InterruptedException, ARDataTransferException {
        viewer2.setText("Sending Media");

        //Fetches the picture files in phones directory
        File externalDirectory = getApplicationContext().getExternalFilesDir(null);
        File [] folderFiles= externalDirectory.listFiles();

        //Converts files to bytes and sends it to raspberry pi via bluetooth
        for (int x = 0; x < folderFiles.length; x++) {
            Bitmap mbitmap = BitmapFactory.decodeFile(folderFiles[x].getAbsolutePath());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            mbitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
            byte[] imageBytes = stream.toByteArray();

            mBluetoothController.writeToServer(("sendpictures").getBytes());
            Thread.sleep(70);
            for (int y = 0; y < imageBytes.length; y += 700) {
                Thread.sleep(70);
                mBluetoothController.writeToServer(Arrays.copyOfRange(imageBytes, y, Math.min(y + 700, imageBytes.length)));
            }
            Thread.sleep(70);
            mBluetoothController.writeToServer("done".getBytes());
            while(!mBluetoothController.doneDownloading) { }
            mBluetoothController.doneDownloading=false;
            Thread.sleep(70);
        }

        mBluetoothController.doneSending=false;
        mBluetoothController.writeToServer("LocationData".getBytes());
        Thread.sleep(30);
        for(int x = 0; x < folderFiles.length; x++) {
            while(!mBluetoothController.doneSending){}
            mBluetoothController.doneSending=false;
            Log.e("Reached",x+"");
            pictureExif = new ExifInterface(folderFiles[x].getAbsolutePath());
            float[] latLong = new float[2];
            pictureExif.getLatLong(latLong);
            mBluetoothController.writeToServer(("(Latitude: "+latLong[0]+", Longitude "+latLong[1]+")").getBytes());
        }
        while(!mBluetoothController.doneSending){}
        mBluetoothController.writeToServer("done".getBytes());

        Thread.sleep(400);
        mBluetoothController.writeToServer("finished".getBytes());
        viewer.setText("Media Sent");

        //Deleting Images from drones memory

        /*
        for(int index=0;index<mediaToDl.size();index++)
        {
            ARDataTransferMedia mediaObject = dataTransferManager.getARDataTransferMediasDownloader().getAvailableMediaAtIndex(index);
            dataTransferManager.getARDataTransferMediasDownloader().deleteMedia(mediaObject);
        }
         */

        //Deleting Images from phones memory
        //externalDirectory.delete();
    }

    //Closes discovery service, called when application closes in onDestroy
    public void closeServices()
    {
        Log.d(TAG, "closeServices ...");

        if (mArdiscoveryService != null)
        {
            new Thread(new Runnable() {
                @Override
                public void run()
                {
                    mArdiscoveryService.stop();

                    getApplicationContext().unbindService(mArdiscoveryServiceConnection);
                    mArdiscoveryService = null;
                }
            }).start();
        }
    }

    //Closes Broadcast receiver, called when application closes in onDestroy
    public void unregisterReceivers()
    {
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());

        localBroadcastMgr.unregisterReceiver(mArdiscoveryServicesDevicesListUpdatedReceiver);
    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mBluetoothController.imageList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.customlayout,null);

            ImageView imageView = (ImageView)view.findViewById(R.id.imageView2);
            TextView textView_location = (TextView)view.findViewById(R.id.textView_location);

            imageView.setImageBitmap(mBluetoothController.imageList.get(i));
            textView_location.setText(mBluetoothController.locationList.get(i).toString());

            return view;
        }
    }

    //All the listeners
    @Override
    public void onStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARCONTROLLER_ERROR_ENUM error) {
        switch (newState) {
            case ARCONTROLLER_DEVICE_STATE_RUNNING:
                break;
            case ARCONTROLLER_DEVICE_STATE_STOPPED:
                break;
            case ARCONTROLLER_DEVICE_STATE_STARTING:
                break;
            case ARCONTROLLER_DEVICE_STATE_STOPPING:
                break;
            default:
                break;
        }
    }

    @Override
    public void onExtensionStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARDISCOVERY_PRODUCT_ENUM product, String name, ARCONTROLLER_ERROR_ENUM error) {

    }

    @Override
    public void onCommandReceived(ARDeviceController deviceController, ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, ARControllerDictionary elementDictionary) {
        if (elementDictionary != null) {
            // if the command received is a battery state changed
            if (commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED) {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);

                if (args != null) {
                    Integer batValue = (Integer) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED_PERCENT);
                    // do what you want with the battery level
                }
            }
            if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_ORIENTATION) && (elementDictionary != null)){
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null) {
                    byte tilt = (byte)((Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_ORIENTATION_TILT)).intValue();
                    byte pan = (byte)((Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_ORIENTATION_PAN)).intValue();
                }
            }
            if (commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED)
            {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null)
                {
                    Integer flyingStateInt = (Integer) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE);
                    ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState = ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.getFromValue(flyingStateInt);
                }
            }
        }
        else {
            Log.e(TAG, "elementDictionary is null");
        }

    }

    @Override
    public ARCONTROLLER_ERROR_ENUM configureDecoder(ARDeviceController deviceController, ARControllerCodec codec) {

        return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
    }

    @Override
    public ARCONTROLLER_ERROR_ENUM onFrameReceived(ARDeviceController deviceController, ARFrame frame) {
        return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
    }

    @Override
    public void onFrameTimeout(ARDeviceController deviceController) {

    }

    @Override
    public void didMediaComplete(Object arg, ARDataTransferMedia media, ARDATATRANSFER_ERROR_ENUM error) {
        didMediaDownload++;
    }

    @Override
    public void didMediaProgress(Object arg, ARDataTransferMedia media, float percent) {
    }

    @Override
    public void didMediaAvailable(Object arg, final ARDataTransferMedia media, final int index) {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {

            }
        });
    }
}
