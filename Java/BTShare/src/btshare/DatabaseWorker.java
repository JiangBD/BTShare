
package btshare;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;

public class DatabaseWorker {
    private final static String url = "jdbc:sqlite:D:/btshare.db";
    private static Connection conn;
    public static  void initialize() {
    if (conn == null) 
    {
        try { conn = DriverManager.getConnection(url);       } catch (SQLException sqle) {   sqle.printStackTrace();}
        
    }   
    }
    public static List<BTDevice>  loadAllDevices(){              
        
        ArrayList<BTDevice> deviceList = new ArrayList<>();
        
        
       String selectSQL = "SELECT * FROM device";
       try {
              
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL);            
             while (rs.next()) {
                 BTDevice device = new BTDevice();
                    device.setDeviceID(rs.getString(1));
                    device.setBtAddr(  rs.getString(2) );
                    device.setHfName(rs.getString(3));
                    device.setCores(rs.getInt(4));
                    deviceList.add(device);
                }       
       }
       catch (SQLException sqle) { System.out.println(sqle.getMessage());}        
       return deviceList;
    }
    public static int getMaxDeviceNumber() {
    if ( countDevices() == 0  ) return 0;
    List<BTDevice> list = loadAllDevices();
    int maxNum = Integer.parseInt( list.get(0).getDeviceID() );
    for ( BTDevice device : list ) {
        if ( Integer.parseInt(device.getDeviceID()) > maxNum   ) maxNum =  Integer.parseInt(device.getDeviceID());
    }
    return maxNum;  
   }
     public static int getMaxTaskNumber() {
    if ( countTasks()== 0  ) return 0;
    List<String> list = getAllTaskID();
    int maxNum = Integer.parseInt( list.get(0) );
    for ( String id : list ) {
        if ( Integer.parseInt( id) > maxNum   ) maxNum =  Integer.parseInt( id );
    }
    return maxNum;  
   }

    public static int countDevices() {
        String selectSQL = "SELECT COUNT(*) FROM device ";
        int number = -1;
               try {
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL);
              rs.next();
             number =rs.getInt(1);                    
       }
       catch (SQLException sqle) { System.out.println(sqle.getMessage()); System.out.println("countDevices error");}
               
    return number;
    }
    
    public static void insert(BTDevice newBTDevice) {
         String insertSQL  = "INSERT INTO device (DEVICEID, BTADDR, HFNAME, CORES ) VALUES ( ?, ?, ?, ?)";
         try {
            PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setString(1, newBTDevice.getDeviceID());
                pstmt.setString(2, newBTDevice.getBtAddr());
                
                pstmt.setString(3, newBTDevice.getHfName());
                pstmt.setInt(4, newBTDevice.getCores());
                pstmt.executeUpdate();
         }
         catch (SQLException sqle) { System.out.println(sqle.getMessage());
                                                        sqle.printStackTrace();
         }    
    }

    public static void deleteDevice(String deviceID) {
    try  {           
            String sql = "DELETE FROM device WHERE DEVICEID = ?"; 
            PreparedStatement statement = conn.prepareStatement(sql); statement.setString(1, deviceID);
            statement.executeUpdate();  
    
    } catch (SQLException sqle) { System.out.println(sqle.getMessage());}    
  }

   public static boolean existsDevice(String btaddr) {
       String selectSQL = "SELECT EXISTS (SELECT * FROM device WHERE BTADDR = ?)";       
       boolean x = false;
    try {
            PreparedStatement stmt = conn.prepareStatement(selectSQL); stmt.setString(1, btaddr);
            ResultSet rs = stmt.executeQuery();
            rs.next(); x = rs.getBoolean(1);            
         }
         catch (SQLException sqle) { System.out.println(sqle.getMessage());   }        
    return x;
    }
        

    //   "SELECT EXISTS (SELECT * FROM thisdevice)  ")
/*    public static boolean hasRegistered() {
    String selectSQL = "SELECT EXISTS (SELECT * FROM thisdevice)" ;     
       boolean x = false;
    try {
            Connection conn = DriverManager.getConnection(url); 
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(selectSQL);
            rs.next(); x = rs.getBoolean(1);            
         }
         catch (SQLException sqle) { System.out.println(sqle.getMessage());}        
    return x;    
    }

    public static void registerThisDevice() {
    if ( !hasRegistered() )  {
        
        try {
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            String bluetoothAddress = localDevice.getBluetoothAddress();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 12; i++) builder.append(bluetoothAddress.charAt(i));
            String insertSQL  = "INSERT INTO thisdevice ( BTADDR ) VALUES ( " + builder.toString() + " )";   
            Connection conn = DriverManager.getConnection(url); 
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(insertSQL);       
            
        } catch (Exception e) {
            e.printStackTrace();
        }  
      } } */

    public static void insert(BTTask task) {  
        String insertSQL  = "INSERT INTO history ( TASKID, SEND, BTADDR, HFNAME, FILENAME, SIZE, TIME ) VALUES ( ?, ?, ?, ?, ?, ?, ? )";
         try {
            PreparedStatement pstmt = conn.prepareStatement(insertSQL);
            int send; if ( task.isSend() ) send = 1; else send = 0;
            pstmt.setString(1, task.getTaskID());
            pstmt.setInt(2, send); pstmt.setString(3, task.getBtAddr());
            pstmt.setString(4, task.getHfName()); pstmt.setString(5, task.getFileName());
            pstmt.setInt(6, task.getSize()); pstmt.setString(7, task.getTime());
            
                pstmt.executeUpdate();
         }
         catch (SQLException sqle) { System.out.println(sqle.getMessage());}    
         
    }

    /// " SELECT BTADDR FROM thisdevice ")
  /*  public static String getThisBtAddr() {
         String thisAddr = null;
       String selectSQL = "SELECT * FROM thisdevice";
       try {
              Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL);              
             while (rs.next()) {
                    thisAddr = rs.getString(1);
                }       
       }
       catch (SQLException sqle) { System.out.println(sqle.getMessage());} 
    return thisAddr;
    } */
    
    ////   )
    public  static int countTasks() {
          String selectSQL = "SELECT COUNT(*) FROM history " ;
        int number = -1;
               try {
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL);
              rs.next();
             number =rs.getInt(1);                    
       }
       catch (SQLException sqle) { System.out.println(sqle.getMessage());}
               return number;
    }
    /*
    @Query ("SELECT DEVICEID FROM device")
    List<String> getAllDeviceID();
    
*/
    /// ()
    public static List<String> getAllTaskID() {
    ArrayList<String> list = new ArrayList<>();
       String selectSQL = "SELECT TASKID FROM history" ;
       try {
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL);   
             while (rs.next()) list.add( rs.getString(1) );
                       
           }
       catch (Exception sqle) { System.out.println(sqle.getMessage());} 
       return list;
       }
    
    
    public static List<BTDevice> getDevice(String btAddr) {
         ArrayList<BTDevice> list = new ArrayList<>();
       String selectSQL = "SELECT * FROM device WHERE BTADDR = ?";
       try {
              
             PreparedStatement stmt = conn.prepareStatement(selectSQL);
             stmt.setString(1, btAddr);
             ResultSet rs = stmt.executeQuery();              
             while (rs.next()) {
                 BTDevice device = new BTDevice();
                 int number = getMaxDeviceNumber() + 1;
                 
                 device.setDeviceID(rs.getString(1));
                 device.setBtAddr(rs.getString(2)); device.setHfName(rs.getString(3));
                 device.setCores(rs.getInt(4));              
                 list.add(device);
                }           
       }
       catch (SQLException sqle) { System.out.println(sqle.getMessage());} 
    return list;
    
    }
    
     //    "UPDATE device SET HFNAME = :newName WHERE DEVICEID = :deviceID")
    public static void rename(String deviceID, String newName) {
        try {           
            String sql = "UPDATE device SET HFNAME = ? WHERE DEVICEID = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, newName); statement.setString(2, deviceID);
            
            statement.executeUpdate();  
    
    } catch (SQLException sqle) { System.out.println(sqle.getMessage());}    
    
    }

    public static List<BTTask> getSendList() {
    String selectSQL = "SELECT * FROM history WHERE SEND = 1 " ;
    ArrayList<BTTask> list = new ArrayList<>();
    try {
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL);           
             while (rs.next()) {
                 BTTask task = new BTTask();
                
                 task.setTaskID(rs.getString(1));
                  task.setSend(true);
                 task.setBtAddr(rs.getString(3)); task.setHfName(rs.getString(4)); 
                 task.setHfName(rs.getString(4)); task.setFileName(rs.getString(5));
                 task.setSize(rs.getInt(6)); task.setTime(rs.getString(7));                 
             
                 list.add(task);
             }
             
       }
       catch (SQLException sqle) { System.out.println(sqle.getMessage());}  
    return list;
    
    }
    
    public static List<BTTask> getReceiveList()
    {
        String selectSQL = "SELECT * FROM history WHERE SEND = 0 " ;
    ArrayList<BTTask> list = new ArrayList<>();
    try {
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL);           
             while (rs.next()) {
                 BTTask task = new BTTask();
                 
                 task.setTaskID(rs.getString(1));
                 task.setSend(false);
                 task.setBtAddr(rs.getString(3)); task.setHfName(rs.getString(4)); 
                 task.setHfName(rs.getString(4)); task.setFileName(rs.getString(5));
                 task.setSize(rs.getInt(6)); task.setTime(rs.getString(7));                 
             
                 list.add(task);
             }
             
       }
       catch (SQLException sqle) { System.out.println(sqle.getMessage());}  
        return list;
        }


}