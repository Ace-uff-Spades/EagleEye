package com.example.harmitbadyal.firstapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    Button buttonOn,buttonOff,buttonNextPage;
    BluetoothAdapter myBluetoothAdapter;
    Intent btEnablingIntent;
    int requestForEnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonOn = (Button)(findViewById(R.id.onBlue));
        buttonOff = (Button)(findViewById(R.id.offBlue));
        buttonNextPage = (Button)(findViewById(R.id.nextPage));
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestForEnable = 1;
        bluetoothOn();
        bluetoothOff();
        nextPage();
    }

    private void nextPage()
    {
        buttonNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(this,ActivityScan.class);
                //startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        if(requestCode==requestForEnable)
        {
            if(resultCode==RESULT_OK)
                Toast.makeText(getApplicationContext(),"Bluetooth is enabled",Toast.LENGTH_LONG).show();
            else if(resultCode==RESULT_CANCELED)
                Toast.makeText(getApplicationContext(),"Bluetooth enabling canceled ",Toast.LENGTH_LONG).show();

        }
    }

    private void bluetoothOff()
    {
        buttonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myBluetoothAdapter.isEnabled())
                    myBluetoothAdapter.disable();
            }
        });
    }

    private void bluetoothOn()
    {
        buttonOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myBluetoothAdapter==null)
                    Toast.makeText(getApplicationContext(),"Phone does not support bluetooth",Toast.LENGTH_LONG).show();
                else
                {
                    if(!myBluetoothAdapter.isEnabled())
                    {
                        startActivityForResult(btEnablingIntent,requestForEnable);
                    }
                }
            }
        });
    }


}
