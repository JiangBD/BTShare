package btshare;

import java.io.IOException;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javax.microedition.io.StreamConnectionNotifier;
import javax.microedition.io.StreamConnection;
import java.util.*;
public class ProcessorController
{
    private static ProcessorController instance;
    private ProcessorController(){}
    private boolean errorFlag;
    private boolean isBusy;
    private boolean wantsToTransfer;
    private String sendString;
    private ArrayList<StreamConnectionNotifier> scnList;
    private ArrayList<StreamConnection> scList;
   
    
    public static void initialize() {
        if (instance == null)
        {
        instance = new ProcessorController();
        instance.isBusy = false;
        instance.wantsToTransfer = false;
        instance.errorFlag = false;
        instance.sendString = null;
        instance.scnList = new ArrayList<>();
        instance.scList = new ArrayList<>();
        }
    }
    synchronized public static void addStreamConn(StreamConnection scn) { instance.scList.add(scn); }
    synchronized public static void addStreamConnNotifier(StreamConnectionNotifier scn) { instance.scnList.add(scn); }
    public static void setSendString(String newString) { instance.sendString = newString;  }
    synchronized public static String getSendString() {        return instance.sendString;  }
    synchronized public static boolean isBusy() { return instance.isBusy; }
    synchronized public static void occupied()
    {   revealProgressBar();
        instance.isBusy = true;
        instance.wantsToTransfer = true;
        instance.errorFlag = false;      
        
    }
    synchronized public static boolean continueTransferring()
    {
        return instance.wantsToTransfer;
    }
    synchronized public static void finishTransferring()
    {   instance.isBusy = false;
        instance.wantsToTransfer = false;     
        instance.sendString = null;
        instance.scList.clear(); instance.scnList.clear();
        refreshProgressBar();
    }
    synchronized public static void cancelTransferring() { //BY THIS USER ONLY
        instance.wantsToTransfer = false;
        
        try { for(int i = 0; i < instance.scnList.size(); i++) instance.scnList.get(i).close();       
            for(int i = 0; i < instance.scList.size(); i++) instance.scList.get(i).close();
        
        } catch (Exception e)
        { System.err.println("SCNs CLOSED BY ProcessorController. ");  }
        
                
        instance.errorFlag = true;
        instance.sendString = null;
        
        refreshProgressBar();
    }
   synchronized private static void refreshProgressBar() {
        BorderPane bp =(BorderPane) BluetoothJavaFXApplication.getPrimaryStage().getScene().getRoot();
        HBox hb =(HBox) bp.getBottom(); ObservableList<Node> list = hb.getChildren();
        ProgressBar pBar = null;
        for ( Node node : list  ) if ( node instanceof ProgressBar) pBar =(ProgressBar)  node;       
        if (pBar != null)pBar.setProgress(0.0); pBar.getParent().setVisible(false);
    }
   synchronized private static void revealProgressBar() {
        BorderPane bp =(BorderPane) BluetoothJavaFXApplication.getPrimaryStage().getScene().getRoot();
        HBox hb =(HBox) bp.getBottom(); ObservableList<Node> list = hb.getChildren();
        ProgressBar pBar = null;
        for ( Node node : list  ) if ( node instanceof ProgressBar) pBar =(ProgressBar)  node;       
        if (pBar != null)pBar.setProgress(0.0); pBar.getParent().setVisible(true);
    }
   
    
    
    synchronized  public static boolean hasError() {return instance.errorFlag;}
    synchronized public static void raiseErrorFlag() {  instance.errorFlag = true; }
}




