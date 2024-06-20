package btshare;


public class BTDevice {

    private String deviceID;

    private String BtAddr;


    private String hfName;

    private int cores;


public String getDeviceID() { return deviceID; }
public String getBtAddr() { return BtAddr; }

public String getHfName() {return hfName; }

public int getCores() {      return cores;    }


public void setDeviceID( String id) { deviceID = id; }
public void setBtAddr(String addr) {  BtAddr = addr; }

public void setHfName( String name) { hfName = name; }
public void setCores( int c) { cores = c; }

}
