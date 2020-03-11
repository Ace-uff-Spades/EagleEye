package com.example.droneapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.R.layout;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DroneDiscoverer";

    private final Context mContext;

    private final ARDiscoveryServicesDevicesListUpdatedReceiver mArdiscoveryServicesDevicesListUpdatedReceiver;

    private ARDiscoveryService mArdiscoveryService;

    private ServiceConnection mArdiscoveryServiceConnection;

    TextView viewer;

    Button findNetworks;

    ListView wifiViewer;

    private final ARDiscoveryServicesDevicesListUpdatedReceiverDelegate mDiscoveryDelegate =
            new ARDiscoveryServicesDevicesListUpdatedReceiverDelegate() {

                @Override
                public void onServicesDevicesListUpdated() {
                    viewer.setText("got inside");
                    if (mArdiscoveryService != null) {
                        List<ARDiscoveryDeviceService> deviceList = mArdiscoveryService.getDeviceServicesArray();
                        String [] deviceListString = {"potato,cheese,milk,doodh"};
                        /*int x=1;
                        deviceListString[0]=deviceList.size()+"";
                        for(ARDiscoveryDeviceService device1:deviceList)
                        {
                            deviceListString[x]=device1.toString();
                            x++;
                        }
                         */
                        ArrayAdapter <String> adapter = new ArrayAdapter(getApplicationContext(),layout.simple_list_item_1,deviceListString);
                        wifiViewer.setAdapter(adapter);
                        viewer.setText("finished");
                        // Do what you want with the device list
                    }
                }
            };

    public MainActivity() {
        this.mArdiscoveryServicesDevicesListUpdatedReceiver = null;
        this.mContext = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findNetworks = findViewById(R.id.findNetworks);
        wifiViewer = findViewById(R.id.wifiViewer);
        viewer = findViewById(R.id.viewer);
        ARSDK.loadSDKLibs();
        implementListeners();
        mDiscoveryDelegate.onServicesDevicesListUpdated();
        initDiscoveryService();
    }

    private void implementListeners()
    {
        findNetworks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerReceivers();
            }
        });
    }

    private void initDiscoveryService()
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

    private void startDiscovery()
    {
        if (mArdiscoveryService != null)
        {
            mArdiscoveryService.start();
        }
    }

    private void registerReceivers()
    {
        viewer.setText("reached here");
        ARDiscoveryServicesDevicesListUpdatedReceiver receiver =
                new ARDiscoveryServicesDevicesListUpdatedReceiver(mDiscoveryDelegate);
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastMgr.registerReceiver(receiver,
                new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
    }

    private ARDiscoveryDevice createDiscoveryDevice(@NonNull ARDiscoveryDeviceService service) {
        ARDiscoveryDevice device = null;
        try {
            device = new ARDiscoveryDevice(mContext, service);
        } catch (ARDiscoveryException e) {
            Log.e(TAG, "Exception", e);
        }

        return device;
    }

    private void unregisterReceivers()
    {
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());

        localBroadcastMgr.unregisterReceiver(mArdiscoveryServicesDevicesListUpdatedReceiver);
    }

    private void closeServices()
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
}
