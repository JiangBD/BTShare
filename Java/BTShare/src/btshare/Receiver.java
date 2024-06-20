package btshare;

import btshare.PBarUpdater;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;


public class Receiver implements Runnable {
    private String remoteAddress;
    
    private int startIndex;
    private int segSize;
    private byte[] array;
    private PBarUpdater updater;
    private UUID serviceUUID;
    
    
    
    
    
    public Receiver(String rA, UUID serUU, int sI,
                    int sS, byte[] ar , PBarUpdater up ) {
        remoteAddress = rA;
        startIndex = sI; segSize = sS; array = ar; updater = up;     
        serviceUUID = serUU;

    }
    @Override
    public void run() {
        
            
                try {
                    BluetoothServiceChannelFinder finder = new BluetoothServiceChannelFinder();
                    System.out.println("CHANNEL NUMBER =  " + finder.findServiceChannel(serviceUUID, remoteAddress));
                    
                    String url = "btspp://" + remoteAddress.replace(":","") +  ":"   + finder.findServiceChannel(serviceUUID, remoteAddress) +
                ";authenticate=false;encrypt=false";
                StreamConnection streamConnection = (StreamConnection) Connector.open(url);
                    ProcessorController.addStreamConn(streamConnection);
                     InputStream inputStream = streamConnection.openInputStream();                   
                     OutputStream outputStream = streamConnection.openOutputStream();

                int writeIndex = startIndex;
                int bytesRead = inputStream.read(array,writeIndex,startIndex + segSize - writeIndex) ;
                while ( bytesRead  != -1
                        && bytesRead  != 0
                       && ProcessorController.continueTransferring() )
                  {
                    writeIndex = writeIndex + bytesRead;                    
                    updater.onDataChunkTransferred(bytesRead);
                    bytesRead = inputStream.read(array,writeIndex,startIndex + segSize - writeIndex) ;
                }
                    outputStream.write(75); outputStream.flush();
                outputStream.close();
                inputStream.close();
                streamConnection.close();
            } catch(Exception e){
                e.printStackTrace();
                ProcessorController.raiseErrorFlag();
            }
        }
    
     
}
   

