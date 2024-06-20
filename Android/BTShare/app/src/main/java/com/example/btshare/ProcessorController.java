package com.example.btshare;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;

public class ProcessorController
{
    private static ProcessorController instance;
    private ProcessorController(){}
    private boolean errorFlag;
    private boolean isBusy;
    private boolean wantsToTransfer;
    private String sendString;
    private ArrayList<BluetoothServerSocket> ssList;
    private ArrayList<BluetoothSocket> sList;
    synchronized public static String getSendString() {
            return instance.sendString;
 }
    public static void initialize() {
        if (instance == null)
        {
            instance = new ProcessorController();
            instance.isBusy = false;
            instance.wantsToTransfer = false;
            instance.errorFlag = false;
            instance.sendString = null;
            instance.ssList = new ArrayList<>();
            instance.sList = new ArrayList<>();
        }
    }
    synchronized public static void addSocket(BluetoothSocket scn) { instance.sList.add(scn); }
    synchronized public static void addServerSocket(BluetoothServerSocket scn) { instance.ssList.add(scn); }
    public static void setSendString(String newString) { instance.sendString = newString;  }
    synchronized public static boolean isBusy() { return instance.isBusy; }
    synchronized public static void occupied()
    {   revealProgressBar();
        instance.isBusy = true;
        instance.wantsToTransfer = true;
        instance.errorFlag = false;

    }
    synchronized public static boolean continueTransferring()
    {
        return instance.wantsToTransfer;
    }
    synchronized public static void finishTransferring()
    {   instance.isBusy = false;
        instance.wantsToTransfer = false;
        instance.sendString = null;
        instance.sList.clear(); instance.ssList.clear();
        refreshProgressBar();
    }
    synchronized public static void cancelTransferring() { //BY THIS USER, OR SOME THREAD EXCEPTIONS
        instance.wantsToTransfer = false;

        try { if (!instance.sList.isEmpty()) for(int i = 0; i < instance.sList.size(); i++) instance.sList.get(i).close();
            if (!instance.ssList.isEmpty()) for(int i = 0; i < instance.ssList.size(); i++) instance.ssList.get(i).close();
        } catch (Exception e)
        {
            Log.e("ERROR","error while controller closing sockets......");  }
        instance.errorFlag = true;
        instance.sendString = null;

    }
    synchronized private static void refreshProgressBar() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post( () -> {
            MyApplication.getMainActivity().getPBar().setProgress(0);
            MyApplication.getMainActivity().hidePBarLayout();
        });
    }
    synchronized private static void revealProgressBar() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post( () ->
        MyApplication.getMainActivity().revealPBarLayout() );
    }

    synchronized  public static boolean hasError() {return instance.errorFlag;}
    synchronized public static void raiseErrorFlag() {  instance.errorFlag = true; }

}



