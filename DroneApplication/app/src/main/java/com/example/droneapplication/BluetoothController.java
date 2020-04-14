package com.example.droneapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Handler;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;

public class BluetoothController extends AppCompatActivity{

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice btDevice;
    SendReceive sendReceive;
    Handler mHandler;
    public ArrayList<Bitmap> imageList;
    public ArrayList<Cord> locationList;
    public ExifInterface pictureExif;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private Context mApplicationContext;
    private final int CreateList = 0;
    public boolean doneDownloading;
    public boolean doneSending;
    private static final UUID MY_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    public BluetoothController(Handler Handler2,Context mApplicationContext)
    {
        doneDownloading = false;
        doneSending = false;
        imageList = new ArrayList<Bitmap>();
        locationList = new ArrayList<Cord>();
        this.mApplicationContext = mApplicationContext;
        this.mHandler = Handler2;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        }
    }

    public boolean findRaspberryPi()
    {
        Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
        if(bt.size()>0) {
            for (BluetoothDevice device : bt) {
                if (device.getName().equals("raspberrypi")) {
                    btDevice = device;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean connect()
    {
        ClientClass clientClass = new ClientClass(btDevice);
        clientClass.start();
        return true;
    }


    public void writeToServer(byte[]bytes) throws IOException {
        sendReceive.write(bytes);
    }

    private class ClientClass extends Thread
    {
        public ClientClass (BluetoothDevice device1)
        {
            device = device1;
            try {

                socket = device.createRfcommSocketToServiceRecord(MY_UUID);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run()
        {
            try {
                socket.connect();
                sendReceive=new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SendReceive extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket)
        {
            bluetoothSocket=socket;
            InputStream tempIn=null;
            OutputStream tempOut=null;
            try {
                tempIn=bluetoothSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                tempOut=bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream=tempIn;
            outputStream=tempOut;
        }


        public void run() {
            while(true) {
                String value = "";
                try {
                    value = read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (value.equals("done")) {
                    doneDownloading=true;
                    doneSending=false;
                }
                else if(value.equals("ok"))
                {
                    doneSending=true;
                }
                else if(value.equals("listView"))
                {
                     mHandler.obtainMessage(CreateList).sendToTarget();
                }
                else
                {
                    File externalDirectory = mApplicationContext.getExternalFilesDir(null);
                    File [] folderFiles= externalDirectory.listFiles();
                    int index = Integer.parseInt(value);
                    Bitmap mbitmap = BitmapFactory.decodeFile(folderFiles[index].getAbsolutePath());
                    imageList.add(mbitmap);

                    try {
                        pictureExif = new ExifInterface(folderFiles[index].getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    float[] latLong = new float[2];
                    pictureExif.getLatLong(latLong);
                    Cord location = new Cord((double)latLong[0], (double)latLong[0],0,0);
                    locationList.add(location);
                }
            }
        }

        public String read() throws IOException {
            byte[]buffer = new byte[1024];
            int bytes = inputStream.read(buffer);
            String s = new String (buffer,"ASCII");
            s = s.substring(0,bytes);
            return s;
        }


        public void write(byte[]bytes) throws IOException {
            outputStream.write(bytes);
        }
    }
}
