package com.example.btshare;

import android.content.Context;
import android.os.Environment;

import androidx.core.app.NotificationManagerCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

public class MasterReceiver implements Runnable {

   private Context c;
    private String remoteAddress;
    private String filename;
    private int receivedSize; //// fileSize
    private String[] uuids;
    private MasterReceiver () {};
    public static MasterReceiver newInstance(Context ct, String rA, String[] uu, String fN, int fS  ){
        MasterReceiver masterReceiver = new MasterReceiver();
        try {
        masterReceiver.c = ct; masterReceiver.remoteAddress = rA; masterReceiver.filename = fN;
            masterReceiver.receivedSize = fS;
            masterReceiver.uuids = uu;
        } catch (NumberFormatException nfe) { return null; }
        return masterReceiver;
    }
@Override
public void run() {
    byte[] array = new byte[receivedSize];
    int cores = uuids.length;
    ProcessorController.occupied();

    ArrayList <Thread> myWorker = new ArrayList<Thread>();
    int segSize = receivedSize / cores;
    PBarUpdater updater = new PBarUpdater(receivedSize);


    for (int i = 0; i < cores; i++) {
        Thread mW;
        if ( i < cores - 1 )
            mW = new Thread( new Receiver(c,remoteAddress, UUID.fromString(uuids[i]),
            i*segSize, segSize, array, updater ));
        else
            mW = new Thread( new Receiver(c,remoteAddress, UUID.fromString(uuids[i]),
            i*segSize, receivedSize - i * segSize, array, updater ));
        mW.start(); myWorker.add(mW);
    }

    try {
        for (int i = 0; i < cores; i++) myWorker.get(i).join();

    } catch (InterruptedException ie) {
        ProcessorController.raiseErrorFlag();
        return; }
    /////  WAIT UNTIL THOSE SINGLE RECEIVERS FINISH THEIR JOBS
    if ( !ProcessorController.hasError()) {
        File file = checkDuplicateFile();
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(array, 0, receivedSize);
            fos.flush();
            fos.close();
        } catch (IOException ie) {
            ie.getMessage();
        }

        writeToDB();
    }
    ProcessorController.finishTransferring();
}


private File checkDuplicateFile() {
    File myfolder = new File (Environment.getExternalStorageDirectory()
            + "/BTShare");
    if ( !myfolder.exists()) myfolder.mkdir();

    File file = new File(Environment.getExternalStorageDirectory()
            + "/BTShare/" + filename);
    while (file.exists()) {
        filename =Environment.getExternalStorageDirectory()
                + "/BTShare/" + "(1)" + filename;
        file = new File(filename);
    }
    return file;
}
    private void writeToDB(){
        BTDevice partner = DatabaseWorker.getDevice(remoteAddress);
        BTTask task = new BTTask();
        int taskNumber = DatabaseWorker.getMaxTasknumber() + 1;
        String taskID = "";
        if (taskNumber < 10) taskID = "000" + taskNumber;
        else if (taskNumber < 100) taskID = "00" + taskNumber;
        else taskID = "0" + taskNumber;
        String partnerID = partner.getDeviceID();

        String btaddr = partner.getBtAddr();
        String hfName = partner.getHfName();
        int size = receivedSize;
        String time = getDateTimeString();
        task.setTaskID(taskID); task.setSend(false);
        task.setBtAddr(btaddr); task.setHfName(hfName);
        task.setFileName(filename); task.setSize(size);
        assert time != null;
        task.setTime(time);
        DatabaseWorker.insertNewTask(task);

    }
    private String getDateTimeString() {
        LocalDateTime now = null;
        DateTimeFormatter formatter = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            now = LocalDateTime.now();
            formatter = DateTimeFormatter.ofPattern("yyyy MM dd HH:mm");
            return now.format(formatter);
        }
        return null;
    }
}//END CLASS
