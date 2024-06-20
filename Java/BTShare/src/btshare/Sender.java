package btshare;
import btshare.ProcessorController;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.OutputConnection;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.*;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
public class Sender implements Runnable {
private String uuidstring;
private String serviceName;
private String filename;
private int startIndex;
private int segSize;
private PBarUpdater updater;
StreamConnectionNotifier streamConnNotifier; 
private BTDevice partner;
public Sender (String uu, String sN ,String file_name, int s, int len, PBarUpdater up, BTDevice p   )  {
uuidstring = uu;
serviceName = sN;
filename = file_name;
startIndex = s;
segSize = len;
updater = up;
partner = p;
}

@Override
public void run() {
    try {
        File file = new File(filename);
        byte[] array = new byte[segSize];
        FileInputStream fis = new FileInputStream(file);
        int ss = startIndex;
        fis.skip(ss );
        fis.read(array,0,segSize);

        String connectionString = "btspp://localhost:" + uuidstring + ";name=" + serviceName
                + ";authenticate=false;encrypt=false";
        streamConnNotifier = (StreamConnectionNotifier) Connector.open(connectionString);
        ProcessorController.addStreamConnNotifier(streamConnNotifier);
        
        System.out.println("Server Started. Waiting for clients to connect...");      
        
        
        StreamConnection connection = streamConnNotifier.acceptAndOpen();
        if ( !RemoteDevice.getRemoteDevice(connection).getBluetoothAddress().equals(partner.getBtAddr().replace(":", ""))  )
        {      connection.close();  ProcessorController.raiseErrorFlag();  return;   }
        
        
        ProcessorController.addStreamConn(connection);
        System.out.println("CLIENT "+
                RemoteDevice.getRemoteDevice(connection).getBluetoothAddress()+" HAS CONNECTED..."
                );
        clearSendQR();
        
        OutputStream OS = connection.openOutputStream();
        InputStream IS = connection.openInputStream();
        /////////////////
        
        int beginIndex = 0;
        int packetSize = 1000;
        while ( beginIndex < array.length && ProcessorController.continueTransferring() ) {
            if ( (beginIndex + packetSize) <= array.length   )
                OS.write(array,beginIndex,packetSize);
            else OS.write(array,beginIndex, array.length-beginIndex);
            OS.flush();
            beginIndex = beginIndex + packetSize; updater.onDataChunkTransferred(packetSize);
          
        }
        
        System.out.println("DONE SENDING CHUNK...");
        if ( ProcessorController.continueTransferring() ) {
        int tt = 19;
        while ( (tt = IS.read()) != 75  ) {}
        }

        fis.close();
        IS.close();
        OS.close();
        System.out.println("DONE!");
    }
    catch (Exception e) {
        ProcessorController.raiseErrorFlag();
        System.out.println("send has been cancelled!");
        e.printStackTrace();   }

}
private void clearSendQR() {
if (ProcessorController.getSendString() != null) ProcessorController.setSendString(null);
BorderPane bp = (BorderPane)  BluetoothJavaFXApplication.getPrimaryStage().getScene().getRoot();

if (bp.getCenter()  instanceof Pane   ) {  //  IT'S THE SENDQR FRAGMENT
    Pane p = (Pane) bp.getCenter();
    ImageView tv = (ImageView) p.getChildren().get(0);
    tv.setImage(null);
}


}


}
