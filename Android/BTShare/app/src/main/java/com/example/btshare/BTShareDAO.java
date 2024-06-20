package com.example.btshare;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BTShareDAO {
    @Query("SELECT * FROM device")
    List<BTDevice> loadAllDevices();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(BTDevice newBTDevice);

    @Query("DELETE FROM device WHERE DEVICEID = :deviceID")
    void deleteDevice(String deviceID);

    @Query("SELECT EXISTS (SELECT * FROM device WHERE BTADDR = :btaddr )")
    boolean existsDevice(String btaddr);
    @Query("SELECT EXISTS (SELECT * FROM device WHERE HFNAME = :hfname )")
    boolean existsHfName(String hfname);

    @Query("SELECT EXISTS (SELECT * FROM thisdevice)  ")
    boolean hasRegistered();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ThisDevice thisDevice);
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(BTTask task);

    @Query(" SELECT BTADDR FROM thisdevice ")
    List<String> getThisBtAddr();
    @Query ( "SELECT COUNT(*) FROM device " )
    int countDevices();
    @Query ( "SELECT COUNT(*) FROM history " )
    int countTasks();
    @Query ("SELECT DEVICEID FROM device")
    List<String> getAllDeviceID();
    @Query ("SELECT TASKID FROM history")
    List<String> getAllTaskID();

    @Query ("SELECT * FROM device WHERE BTADDR = :btAddr ")
    List<BTDevice> getDevice(String btAddr);
    @Query("UPDATE device SET HFNAME = :newName WHERE DEVICEID = :deviceID")
    void rename(String deviceID, String newName);

    @Query ("SELECT * FROM history WHERE SEND = 1 ")
    LiveData<List<BTTask>> getSendList();
    @Query ("SELECT * FROM history WHERE SEND = 0 ")
    LiveData<List<BTTask>> getReceiveList();



}