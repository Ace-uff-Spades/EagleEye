package com.example.droneapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARFeatureARDrone3;
import com.parrot.arsdk.arsal.ARSALPrint;

import androidx.appcompat.app.AppCompatActivity;

public class Move_Activity extends AppCompatActivity {

    private static final String TAG = "Move_Activity";

    EditText xcord, ycord, zcord, psicord;

    TextView displayConfirmation;

    Button moveRelativeLoc, flightplanActivity;

    Button moveForward, moveBackward, moveLeft, moveRight, moveUp, moveDown;

    public static ARDeviceController mARDeviceController = MainActivity.mARDeviceController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move);
      //  Toolbar toolbar = findViewById(R.id.toolbar);

        displayConfirmation = findViewById(R.id.displayCord);
        displayConfirmation.setMovementMethod(new ScrollingMovementMethod());

        flightplanActivity = findViewById(R.id.flightplanActivity);
        moveForward = findViewById(R.id.moveForward);
        moveBackward = findViewById(R.id.movebackward);
        moveRight = findViewById(R.id.moveRight);
        moveLeft = findViewById(R.id.moveLeft);
        moveUp = findViewById(R.id.moveUp);
        moveDown = findViewById(R.id.moveDown);


        flightplanActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMavlinkActivity();
            }
        });
        moveForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                move("f");
            }
        });
        moveBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                move("b");
            }
        });
        moveRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                move("r");
            }
        });
        moveLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                move("l");
            }
        });
        moveUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                move("u");
            }
        });
        moveDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                move("d");
            }
        });

        /*
        moveRelativeLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveRelativeLocation(xcord.getText().toString(),ycord.getText().toString(),zcord.getText().toString(),psicord.getText().toString());
            }
        });
        */
    }
    private void moveRelativeLocation(String xc, String yc, String zc, String psic){
        double x = Double.parseDouble(xc);
        double y = Double.parseDouble(yc);
        double z = Double.parseDouble(zc);
        double psi = Double.parseDouble(psic)*(Math.PI/180.0);
        String confirmationText = "(" + x + "," + y + "," + z + "," + psi + ")";
        addToLogs(confirmationText);

        ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState = getPilotingState();
        if (ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING.equals(flyingState))
        {
            ARCONTROLLER_ERROR_ENUM error = mARDeviceController.getFeatureARDrone3().sendPilotingMoveBy((float)x,(float)y,(float)z,(float)psi);

            if (!error.equals(ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK))
            {
                ARSALPrint.e(TAG, "Error while moving to location: " + error);
                addToLogs("ERROR WHILE MOVING: " + error.toString());
            }
        }
    }

    public void move(String direction){
        switch(direction) {
            case ("f"):
                moveRelativeLocation("1.0", "0.0", "0.0", "0.0");
                addToLogs("Moving Forward");
                break;
            case ("b"):
                moveRelativeLocation("-1.0", "0.0", "0.0", "0.0");
                addToLogs("Moving Backward");
                break;
            case("l"):
                moveRelativeLocation("0.0", "-1.0", "0.0", "0.0");
                addToLogs("Moving Left");
                break;
            case("r"):
                moveRelativeLocation("0.0", "1.0", "0.0", "0.0");
                addToLogs("Moving Right");
                break;
            case("u"):
                moveRelativeLocation("0.0", "0.0", "-1.0", "0.0");
                addToLogs("Moving Up");
                break;
            case("d"):
                moveRelativeLocation("0.0", "0.0", "1.0", "0.0");
                addToLogs("Moving Down");
                break;
        }
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

    public void addToLogs(String newLog){
        String oldLogs = displayConfirmation.getText().toString();
        displayConfirmation.setText(new SpannableString(String.format("%s\n%s", oldLogs, newLog)));
    }


    public void openMavlinkActivity(){
        Intent intent = new Intent(this, Mavlink_Activity.class);
        startActivity(intent);
    }
}


