package com.example.droneapplication;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_FLIGHTPLANSTATE_COMPONENTSTATELISTCHANGED_COMPONENT_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_MAVLINK_START_TYPE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARFeatureARDrone3;
import com.parrot.arsdk.arsal.ARSALPrint;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Move_Activity extends AppCompatActivity {

    EditText xcord, ycord, zcord, psicord;

    TextView displayConfirmation;

    Button auto;

    Button moveRelativeLoc;
    private ARDeviceController mARDeviceController = MainActivity.mARDeviceController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move);
      //  Toolbar toolbar = findViewById(R.id.toolbar);
        xcord = findViewById(R.id.xvalue);
        ycord = findViewById(R.id.yvalue);
        zcord = findViewById(R.id.zvalue);
        psicord = findViewById(R.id.psivalue);
        displayConfirmation = findViewById(R.id.displayCord);

        moveRelativeLoc = findViewById(R.id.moveRelativeLoc);
        moveRelativeLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveRelativeLocation(xcord.getText().toString(),ycord.getText().toString(),zcord.getText().toString(),psicord.getText().toString());
            }
        });

        auto = findViewById(R.id.automatic);
        auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activateFlightPlan();
            }
        });
    }

    private void moveRelativeLocation(String xc, String yc, String zc, String psic){
        double x = Double.parseDouble(xc);
        double y = Double.parseDouble(yc);
        double z = Double.parseDouble(zc);
        double psi = Double.parseDouble(psic)*(Math.PI/180.0);
        String confirmationText = "(" + x + "," + y + "," + z + "," + psi + ")";
        displayConfirmation.setText(confirmationText);

        ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState = getPilotingState();
        if (ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING.equals(flyingState))
        {
            ARCONTROLLER_ERROR_ENUM error = mARDeviceController.getFeatureARDrone3().sendPilotingMoveBy((float)x,(float)y,(float)z,(float)psi);

            if (!error.equals(ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK))
            {
                ARSALPrint.e(MainActivity.TAG, "Error while moving to location: " + error);
            }
        }
    }

    private void activateFlightPlan(){
        Log.e(MainActivity.TAG, "Activating Flight Plan");
        mARDeviceController.getFeatureCommon().sendMavlinkStart((String)"./Flightplan", (ARCOMMANDS_COMMON_MAVLINK_START_TYPE_ENUM) ARCOMMANDS_COMMON_MAVLINK_START_TYPE_ENUM.ARCOMMANDS_COMMON_MAVLINK_START_TYPE_FLIGHTPLAN);
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

}
