package com.example.btshare;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import java.util.List;

public class DatabaseWorker {
    private static DatabaseWorker instance;
    private  BTShareDAO DAO;
    private DatabaseWorker(){};
    public static void initialize(Context ct){
        if (instance == null) {
            instance = new DatabaseWorker();
            instance.DAO = Room.databaseBuilder(ct, BTShareDatabase.class
                            , "btshare")
                    .createFromAsset("database/btshare.db").build().BTShareDAO();

        }
    }
    synchronized public static List<BTDevice> loadAllDevices() {
        return instance.DAO.loadAllDevices();
    }
    synchronized public static void insertNewDevice(BTDevice device){
        if (!instance.DAO.existsDevice(device.getBtAddr())) instance.DAO.insert(device);

    }
    synchronized public static void insertNewTask(BTTask task){
        instance.DAO.insert(task);
    }

    public static boolean hasRegistered() {
        return instance.DAO.hasRegistered();
    }
    public static String getThisBtAddr() {
        return instance.DAO.getThisBtAddr().get(0);
    }
    public static void registerUser(ThisDevice device) {
        if (device.getBtAddr().length() == 17)
            instance.DAO.insert(device);  }
    public static int countDevices() {
        return instance.DAO.countDevices();
    }
    public static int countTasks() { return instance.DAO.countTasks(); }
    public static boolean existsDevice(String btAddr) {
        return instance.DAO.existsDevice(btAddr);
    }
    public static boolean existsHfName(String hfname){
        return instance.DAO.existsHfName(hfname);
    }
    public static int getMaxDeviceNumber() {
        if (instance.DAO.countDevices() == 0) return 0;

        List<String> idList =  instance.DAO.getAllDeviceID();
        int max = Integer.parseInt(idList.get(0) );
        for (String str : idList   ) {
            int thisValue = Integer.parseInt(str);
            if (max < thisValue) max = thisValue;
        }
        return max;
    }
    public static int getMaxTasknumber() {
        if (instance.DAO.countTasks() == 0) return 0;

        List<String> idList =  instance.DAO.getAllTaskID();
        int max = Integer.parseInt(idList.get(0) );
        for (String str : idList   ) {
            int thisValue = Integer.parseInt(str);
            if (max < thisValue) max = thisValue;
        }
        return max;
    }
    public static BTDevice getDevice(String btAddr) {
       return instance.DAO.getDevice(btAddr).get(0);
    }
    public static void rename (String deviceID, String newName) {
        instance.DAO.rename(deviceID,newName);
    }

    public static void deleteDevice (BTDevice device) {
        instance.DAO.deleteDevice(device.getDeviceID());
    }
    public static LiveData<List<BTTask>> getSendList() {return instance.DAO.getSendList();}
    public static LiveData<List<BTTask>> getReceiveList() {return instance.DAO.getReceiveList(); }

}
