package com.example.droneapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class BluetoothController extends AppCompatActivity{

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice btDevice;
    SendReceive sendReceive;
    Handler mHandler;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private final int MessageRecieved = 1;
    private final int DoneMediaCreation = 2;
    public boolean doneDownloading;
    public boolean doneSending;
    private static final UUID MY_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    public BluetoothController(Handler Handler2)
    {
        doneDownloading = false;
        doneSending = false;
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

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch(message.what){
                case(2):
                    mHandler.obtainMessage(2).sendToTarget();
                    Log.e("Chicken","Small Chicken");
                    break;
            }
            return false;
        }
    });

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
            /*byte[] buffer = new byte[1024];
            int bytes = 0;
            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                } catch (IOException e) {
                    break;
                }
            }
            mHandler.obtainMessage(2).sendToTarget();
            Log.e("received","received");
             */
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
                if(value.equals("ok"))
                {
                    doneSending=true;
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
