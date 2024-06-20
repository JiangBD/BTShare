package btshare;

public class BTTask {


    private String taskID;

    private boolean send;

    private String btAddr;

    private String hfName;
    private String fileName;
    private int size;
    
    private String time;
    public String getTaskID() {return taskID;}
    public boolean isSend() { return send; }

    public void setBtAddr(String btAddr) {
        this.btAddr = btAddr;  }
    public String getBtAddr(){return btAddr;};
    public String getHfName() { return hfName; }
    public String getFileName() { return fileName; }

    public int getSize() {   return size;   }
    
    public String getTime() {
        return time;
    }
    public void setTaskID (String id) { taskID = id; }
    public void setSend(boolean send) {
        this.send = send;
    }

    public void setHfName( String hfName) {
        this.hfName = hfName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public void setSize(int size) {
        this.size = size;
    }

    public void setTime( String time) {
        this.time = time;
    }
}
