package com.example.btshare;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import org.intellij.lang.annotations.JdkConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class Receiver implements Runnable {
    private Context c;
    private String remoteAddress;
    private UUID serviceUUID;
    private int startIndex;
    private int segSize;
    private byte[] array;
    private BluetoothAdapter bluetoothAdapter;
    private PBarUpdater updater;


    public Receiver(Context ct, String rA, UUID serUU, int sI,
                    int sS, byte[] ar, PBarUpdater up  ) {
        c = ct; remoteAddress = rA; serviceUUID = serUU;
        startIndex = sI; segSize = sS; array = ar;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        updater = up;
    }
    @Override
    public void run() {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(remoteAddress);
            // Create a socket
            if (ActivityCompat.checkSelfPermission(c,
                    android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                BluetoothSocket bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(serviceUUID );
                // Connect to the server
                ProcessorController.addSocket(bluetoothSocket);
                bluetoothSocket.connect();
                // Receive data
                InputStream inputStream = bluetoothSocket.getInputStream();
                OutputStream outputStream = bluetoothSocket.getOutputStream();
                int writeIndex = startIndex;
                int bytesRead = -77;

                bytesRead = inputStream.read(array,writeIndex,startIndex + segSize - writeIndex);
                while ( ( bytesRead != -1 && bytesRead != 0))
                  { Log.e("bytesRead","bytesRead = " + bytesRead +"  segSize = " + segSize);
                    writeIndex = writeIndex + bytesRead;
                 updater.onDataChunkTransferred(bytesRead);
                      bytesRead = inputStream.read(array,writeIndex,startIndex + segSize - writeIndex);
                }
                Log.d("TAG","DONE RECEIVING CHUNK...");
                 if (ProcessorController.continueTransferring())
                 { outputStream.write(75); outputStream.flush(); }

                outputStream.close();
                inputStream.close();
                bluetoothSocket.close();
            } catch(Exception e){
                ProcessorController.raiseErrorFlag();
                Log.e("TAG", "Error: " + e.getMessage());
            }


        } else ProcessorController.raiseErrorFlag();
}

}
