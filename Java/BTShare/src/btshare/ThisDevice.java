package btshare;


public class ThisDevice {
    public ThisDevice() {}
    public ThisDevice(String addr) {BtAddr = addr; }
    private String BtAddr;

    public String getBtAddr() { return BtAddr; }

    public void setBtAddr(String addr) {  BtAddr = addr; }

}
