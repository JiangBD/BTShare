package com.example.btshare;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

public class MasterSender implements Runnable {
    private static final String serviceName = "RFCOMM";
    private BTDevice partner;

    private Context ct;
    private Uri uri;
    public MasterSender(Context c, Uri u, BTDevice p)
    { ct = c; uri = u; partner = p;}

    @Override
    public void run() {
        //BluetoothClient client = new BluetoothClient();
        // client.receive();
        ProcessorController.occupied();
        String thisAddr = DatabaseWorker.getThisBtAddr();
        ArrayList<Thread> myWorker = new ArrayList<>();
        int cores; int this_cores = Runtime.getRuntime().availableProcessors() - 3;
        if ( partner.getCores() < this_cores ) cores = partner.getCores();
        else cores = this_cores;
        UUID[] uuArray = new UUID[cores];
        for (int i = 0; i < cores; i++) uuArray[i] = UUID.randomUUID();
        String sendString = "2::" + thisAddr + "::" + getFileNameFromUri(uri) + "::"
                + getFileSizeFromUri(ct,uri);
        for (int i = 0; i < cores; i++) sendString +="::" + uuArray[i];
        ProcessorController.setSendString(sendString);
        int segSize = getFileSizeFromUri(ct,uri) / cores;

        PBarUpdater updater = new PBarUpdater(getFileSizeFromUri(ct,uri));

        for (int i = 0; i < cores; i++) {
            if ( i < cores - 1 )
                myWorker.add(new Thread( new Sender(ct,uuArray[i],uri
                        ,i * segSize,segSize, updater,partner )  ));
            else
                myWorker.add ( new Thread( new Sender(ct,uuArray[i],uri
                        ,i * segSize,
                        getFileSizeFromUri(ct,uri) - i * segSize, updater, partner )));
            myWorker.get(i).start();
        }
        try {
            for (int i = 0; i < cores; i++) myWorker.get(i).join();
        }
        catch (InterruptedException ie) {
            ie.printStackTrace();
            ProcessorController.raiseErrorFlag();
            errorToast(ct);
            return;
        }
        if( !ProcessorController.hasError()) writeToDB();
        ProcessorController.finishTransferring();

    }

    private void errorToast(Context ct) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post( () -> Toast.makeText(ct, "Đã xảy ra lỗi, kết thúc gửi.",
                Toast.LENGTH_LONG).show()   );
    }
    private void writeToDB(){
        BTTask task = new BTTask();
        int taskNumber = DatabaseWorker.getMaxTasknumber() + 1;
        String taskID = "";
        if (taskNumber < 10) taskID = "000" + taskNumber;
        else if (taskNumber < 100) taskID = "00" + taskNumber;
        else taskID = "0" + taskNumber;

        String btaddr = partner.getBtAddr();
        String hfName = partner.getHfName();
        String fileName = getFileNameFromUri(uri);
        int size = getFileSizeFromUri(ct,uri);
        String time = getDateTimeString();
        task.setTaskID(taskID); task.setSend(true);
         task.setBtAddr(btaddr); task.setHfName(hfName);
        task.setFileName(fileName); task.setSize(size);
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
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            ContentResolver contentResolver = ct.getContentResolver();
            try (Cursor cursor = contentResolver.query(
                    uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        fileName = cursor.getString(index);
                    }
                }
            }
        }
        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }
    private int getFileSizeFromUri(Context ct, Uri uri) {
        ContentResolver resolver = ct.getContentResolver();
        int size = -1;
        try {
            InputStream IS = resolver.openInputStream(uri);
            assert IS != null;
            size = IS.available();
            IS.close();
        }
        catch (Exception e) { return -1; }
        return size;
    }
}
