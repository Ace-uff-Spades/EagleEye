package com.example.droneapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;

public class BluetoothController extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice btDevice;
    SendReceive sendReceive;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    static final int STATE_LISTENING=1;
    static final int STATE_CONNECTING=2;
    static final int STATE_CONNECTED=3;
    static final int STATE_CONNECTION_FAILED=4;
    static final int STATE_MESSAGE_RECEIVED=5;
    private static final String APP_NAME = "BTChat";
    private static final UUID MY_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    public BluetoothController()
    {
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

    /*
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch(msg.what)
            {
                case STATE_LISTENING:
                    viewer2.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    viewer2.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    viewer2.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    viewer2.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    break;
                case 9:
                    viewer2.setText("Creating Socket");
                    break;
                case 10:
                    viewer2.setText("Socket Created!");
                    break;
                case 11:
                    viewer2.setText("Runable Begins");
                    break;
            }
            return false;
        }
    });
    */


    public void writeToServer(byte[]bytes)
    {
        sendReceive.write(bytes);
    }

    private class ClientClass extends Thread
    {
        public ClientClass (BluetoothDevice device1)
        {
            device = device1;
            try {
                Message message=Message.obtain();
                message.what = 9;
                //handler.sendMessage(message);

                socket = device.createRfcommSocketToServiceRecord(MY_UUID);

                message=Message.obtain();
                message.what = 10;
                //handler.sendMessage(message);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run()
        {
            try {
                socket.connect();
                Message message=Message.obtain();
                message.what = STATE_CONNECTED;
                //handler.sendMessage(message);
                sendReceive=new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
                Message message=Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                //handler.sendMessage(message);
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

        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes;

            while(true) {
                try {
                    bytes = inputStream.read(buffer);
                    //handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[]bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
