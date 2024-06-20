package com.example.btshare;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "device")
public class BTDevice {
    @PrimaryKey
    @NonNull
    @ColumnInfo (name = "DEVICEID")
    private String deviceID;
    @NonNull @ColumnInfo (name = "BTADDR")
    private String BtAddr;
    @NonNull
    @ColumnInfo (name = "HFNAME")
    private String hfName;
    @NonNull
    @ColumnInfo (name = "CORES")
    private int cores;


public String getDeviceID() { return deviceID; }
public String getBtAddr() { return BtAddr; }

public String getHfName() {return hfName; }

public int getCores() {      return cores;    }

public void setDeviceID(@NonNull String id) { deviceID = id; }
public void setBtAddr(@NonNull String addr) {  BtAddr = addr; }

public void setHfName(@NonNull String name) { hfName = name; }
public void setCores(@NonNull int c) { cores = c; }

}
