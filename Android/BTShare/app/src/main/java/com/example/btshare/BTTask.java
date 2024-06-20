package com.example.btshare;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history")
public class BTTask {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "TASKID")
    private String taskID;
    @NonNull @ColumnInfo (name = "SEND")
    private boolean send;
    @NonNull @ColumnInfo (name = "BTADDR")
    private String btAddr;
    @NonNull @ColumnInfo (name = "HFNAME")
    private String hfName;
    @NonNull @ColumnInfo (name = "FILENAME")
    private String fileName;
    @NonNull @ColumnInfo (name = "SIZE")
    private int size;
    @NonNull @ColumnInfo (name = "TIME")
    private String time;
    public String getTaskID() {return taskID;}
    public boolean isSend() { return send; }
    public void setBtAddr(@NonNull String btAddr) {
        this.btAddr = btAddr;  }
    public String getBtAddr(){return btAddr;};
    public String getHfName() { return hfName; }
    public String getFileName() { return fileName; }

    public int getSize() {   return size;   }
    @NonNull
    public String getTime() {
        return time;
    }
    public void setTaskID (String id) { taskID = id; }
    public void setSend(boolean send) {
        this.send = send;
    }
    public void setHfName(@NonNull String hfName) {
        this.hfName = hfName;
    }
    public void setFileName(@NonNull String fileName) {
        this.fileName = fileName;
    }
    public void setSize(int size) {
        this.size = size;
    }

    public void setTime(@NonNull String time) {
        this.time = time;
    }
}
