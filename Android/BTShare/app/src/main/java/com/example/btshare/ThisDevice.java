package com.example.btshare;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "thisdevice")
public class ThisDevice {
    public ThisDevice() {
    }

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "BTADDR")
    private String BtAddr;

    public String getBtAddr() {
        return BtAddr;
    }
    public void setBtAddr(@NonNull String addr) {
        BtAddr = addr;
    }
}
