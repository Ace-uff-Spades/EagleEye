package com.example.droneapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.parrot.arsdk.ardiscovery.ARDiscoveryService
import com.parrot.arsdk.ardiscovery.ARDiscoveryService.LocalBinder
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver
import com.parrot.drone.groundsdk.GroundSdk
import com.parrot.drone.groundsdk.ManagedGroundSdk
import com.parrot.drone.groundsdk.Ref
import com.parrot.drone.groundsdk.device.DeviceState
import com.parrot.drone.groundsdk.device.Drone
import com.parrot.drone.groundsdk.device.instrument.BatteryInfo
import com.parrot.drone.groundsdk.device.pilotingitf.Activable
import com.parrot.drone.groundsdk.device.pilotingitf.ManualCopterPilotingItf
import com.parrot.drone.groundsdk.facility.AutoConnection


class MainActivity : AppCompatActivity() {



    internal lateinit var takeOffLandBt: Button

    private var droneBatteryInfoRef: Ref<BatteryInfo>? = null

    private lateinit var  groundSdk: GroundSdk

    private var drone: Drone? = null

    private var droneStateRef: Ref<DeviceState>? = null

    private var mArdiscoveryService: ARDiscoveryService? = null

    private var mArdiscoveryServiceConnection: ServiceConnection? = null

    /** Drone state text view. */
    internal lateinit var droneStateTxt: TextView
    /** Drone battery level text view. */
    internal lateinit var droneBatteryTxt: TextView

    private var pilotingItfRef: Ref<ManualCopterPilotingItf>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Get user interface instances.
        droneBatteryTxt = findViewById<TextView>(R.id.droneBatteryTxt)
        takeOffLandBt = findViewById<Button>(R.id.takeOffLandBt)
        droneStateTxt = findViewById<TextView>(R.id.droneStateTxt)
        takeOffLandBt.setOnClickListener {onTakeOffLandClick()}

        // Initialize user interface default values.
        droneStateTxt.text = DeviceState.ConnectionState.DISCONNECTED.toString()

        groundSdk = ManagedGroundSdk.obtainSession(this)
    }

    private fun initDiscoveryService() {
        // create the service connection
        if (mArdiscoveryServiceConnection == null) {
            mArdiscoveryServiceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    mArdiscoveryService = (service as LocalBinder).service
                    startDiscovery()
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    mArdiscoveryService = null
                }
            }
        }
        if (mArdiscoveryService == null) {
            // if the discovery service doesn't exists, bind to it
            val i = Intent(applicationContext, ARDiscoveryService::class.java)
            applicationContext.bindService(i, mArdiscoveryServiceConnection!!, Context.BIND_AUTO_CREATE)
        } else {
            // if the discovery service already exists, start discovery
            startDiscovery()
        }
    }

    private fun startDiscovery() {
        mArdiscoveryService?.start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart()
    {
        super.onStart();
        Log.e("START:", "ONSTART() METHOD RUNNING!")
        // Monitor the auto connection facility.
        groundSdk.getFacility(AutoConnection::class.java) {
            // Called when the auto connection facility is available and when it changes.

            it?.let{
                // Start auto connection.
                Log.e("AUTO CONNECTION RUNNING:", it.status.toString())
                if (it.status != AutoConnection.Status.STARTED){
                    it.start()
                }
                if (drone?.uid != it.drone?.uid) {
                    if(drone != null) {
                        // Stop monitoring the old drone.
                        stopDroneMonitors()
                        resetDroneUi()
                    }

                    // Monitor the new drone.
                    drone = it.drone
                    Log.e("DRONE FOUND:", drone?.uid)
                    if(drone != null) {
                        startDroneMonitors()
                    }
                }
            }
        }
    }



    private fun startDroneMonitors() {
        monitorDroneState()
        monitorDroneBatteryLevel()
        monitorPilotingInterface()
    }

    private fun stopDroneMonitors() {
        droneStateRef?.close()
        droneStateRef = null

        droneBatteryInfoRef?.close()
        droneBatteryInfoRef = null

        pilotingItfRef?.close()
        pilotingItfRef = null
    }

    private fun monitorDroneState() {
        // Monitor current drone state.
        droneStateRef = drone?.getState {
            // Called at each drone state update.

            it?.let {
                // Update drone connection state view.
                droneStateTxt.text = it.connectionState.toString()
            }
        }
    }

    private fun monitorDroneBatteryLevel() {
        // Monitor the battery info instrument.
        droneBatteryInfoRef = drone?.getInstrument(BatteryInfo::class.java) {
            // Called when the battery info instrument is available and when it changes.

            it?.let {
                // Update drone battery level view.
                droneBatteryTxt.text = getString(it.batteryLevel)
            }
        }
    }

    private fun resetDroneUi() {
        // Reset drone user interface views.
        droneStateTxt.text = DeviceState.ConnectionState.DISCONNECTED.toString()
        droneBatteryTxt.text = ""
        takeOffLandBt.isEnabled = false
    }

    private fun onTakeOffLandClick() {
        // Get the piloting interface from its reference.
        pilotingItfRef?.get()?.let { itf ->
            // Do the action according to the interface capabilities
            if (itf.canTakeOff()) {
                // Take off
                itf.takeOff()
            } else if (itf.canLand()) {
                // Land
                itf.land()
            }
        }
    }

    private fun monitorPilotingInterface() {
        // Monitor a piloting interface.
        pilotingItfRef = drone?.getPilotingItf(ManualCopterPilotingItf::class.java) {
            // Called when the manual copter piloting Interface is available
            // and when it changes.

            // Disable the button if the piloting interface is not available.
            if (it == null) {
                takeOffLandBt.isEnabled = false
            } else {
                managePilotingItfState(it)
            }
        }
    }

    private fun managePilotingItfState(itf: ManualCopterPilotingItf) {
        when(itf.state) {
            Activable.State.UNAVAILABLE -> {
                // Piloting interface is unavailable.
                takeOffLandBt.isEnabled = false
            }

            Activable.State.IDLE -> {
                // Piloting interface is idle.
                takeOffLandBt.isEnabled = false

                // Activate the interface.
                itf.activate()
            }

            Activable.State.ACTIVE -> {
                // Piloting interface is active.

                when {
                    itf.canTakeOff() -> {
                        // Drone can take off.
                        takeOffLandBt.isEnabled = true
                        takeOffLandBt.text = "take off"
                    }
                    itf.canLand() -> {
                        // Drone can land.
                        takeOffLandBt.isEnabled = true
                        takeOffLandBt.text = "land"
                    }
                    else -> // Disable the button.
                        takeOffLandBt.isEnabled = false
                }
            }
        }
    }

}
