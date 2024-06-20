package com.example.btshare;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class Sender implements Runnable {
    private Context ct;
    private UUID uuid;
    private Uri fileUri;
    private int startIndex;
    private int segSize;
    PBarUpdater updater;
    BTDevice partner;

    public Sender(Context c, UUID uu, Uri file_Uri, int s, int len, PBarUpdater up, BTDevice pa) {
        ct = c;
        uuid = uu;
        fileUri = file_Uri;
        startIndex = s;
        segSize = len;
        updater = up;
        partner = pa;
    }

    private byte[] getByteArrayFromUri(Uri uri) {
        ContentResolver resolver = ct.getContentResolver();
        byte[] array = null;
        try {
            InputStream IS = resolver.openInputStream(uri);
            array = new byte[segSize];
            int ss = startIndex;
            IS.skip(ss);
            IS.read(array, 0, segSize);
            IS.close();
        } catch (Exception e) {
            return null;
        }

        return array;
    }

    @Override
    public void run() {
        byte[] array = getByteArrayFromUri(fileUri);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        BluetoothServerSocket serverSocket = null;
        try {
            if (ActivityCompat.checkSelfPermission(ct, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ProcessorController.raiseErrorFlag();
                return;   }
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("MyBluetoothService", uuid);

        ProcessorController.addServerSocket(serverSocket);
        BluetoothSocket socket = serverSocket.accept();
        if ( !socket.getRemoteDevice().getAddress().equals(partner.getBtAddr())) { //   HACKER!!!
            socket.close();
            ProcessorController.raiseErrorFlag();
            return;
        }
        // This call blocks until a connection is accepted
        // Connection accepted, you can now use socket.getInputStream() and socket.getOutputStream() for communication
        ProcessorController.addSocket(socket);
        if (ProcessorController.getSendString() != null) clearSendQR();

        OutputStream OS = socket.getOutputStream();
        InputStream IS = socket.getInputStream();
        /////////////////
        int beginIndex = 0;
        int packetSize = 1000;
        while ( beginIndex < array.length && ProcessorController.continueTransferring() ) {
            if ( (beginIndex + packetSize) <= array.length   )
            {  OS.write(array,beginIndex,packetSize); updater.onDataChunkTransferred(packetSize);         }
            else{ OS.write(array,beginIndex, array.length-beginIndex);updater.onDataChunkTransferred(packetSize);  }
            OS.flush();
            beginIndex = beginIndex + packetSize;

        }
        Log.d("TAG","DONE RECEIVING CHUNK....");
        if (   ProcessorController.continueTransferring()) {
            int tt = 19;
            while ((tt = IS.read()) != 75) {
            }///SPINNING...
        }
        IS.close();
        OS.close();
        socket.close();
        serverSocket.close();
    }
    catch (Exception e) {
        e.printStackTrace();
        ProcessorController.raiseErrorFlag();
    }
}
private void clearSendQR() {
    if (ProcessorController.getSendString() != null) {
        ProcessorController.setSendString(null);
        Fragment frag = MyApplication.getMainActivity().getSupportFragmentManager()
                .findFragmentById(R.id.main_content);
        if (frag instanceof SendQRFragment) {
            SendQRFragment qrfrag = (SendQRFragment) frag;
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(qrfrag::clearSendQR);
        }
    }
}
}
