
package btshare;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import javafx.stage.Stage;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Window;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import java.util.ArrayList;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.control.TextInputDialog;

public class QRScanner {
    public static void runScanner(Window primaryWindow) {
       Webcam webcam = Webcam.getDefault();
     if (webcam == null) {
            System.out.println("No webcam detected");
           return;
        }
    
       webcam.setViewSize(WebcamResolution.VGA.getSize()); 
      WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
      panel.setDisplayDebugInfo(true);
       panel.setImageSizeDisplayed(true);
      panel.setMirrored(true); 

      ImageView imageView = new ImageView();
        
        imageView.setFitWidth(450.0);imageView.setFitHeight(400.0);      
      
      Stage stage = new Stage();
        stage.initOwner(primaryWindow);
        stage.initModality(Modality.WINDOW_MODAL);
        
        StackPane root = new StackPane();
        root.setPrefWidth(600.0);root.setPrefHeight(400.0);
        root.getChildren().add(imageView);
        Scene scene = new Scene(root, 640, 480);
        
        stage.setTitle("Quét QR");
        stage.setScene(scene);
              
        Thread webcamThread = new Thread ( () ->  {  
            Reader reader = new QRCodeReader();
            webcam.open();
            while (true) {
            BufferedImage image = webcam.getImage();  // returns awt Image
            if (image != null) {
            Image fxImage = SwingFXUtils.toFXImage(image, null); // converts to FXImage
            Platform.runLater( () -> imageView.setImage(fxImage) ) ;   // displays the FXImage on screen            
            try {
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));                  
                    Result result = reader.decode(bitmap);                 
                    if (result   != null) {                        
                       System.out.println("QR Code text: " + result.getText()); ////PRINT TO CONSOLE
                       webcam.close(); 
                        handleRequest(result.getText(),stage);
                     //   Platform.runLater( () -> stage.close()  ); 
                        break;
                    } 
            }   
             catch (Exception e) { 
                 e.printStackTrace();   
                if (!stage.isShowing()) {    webcam.close(); break;}          
                continue;
             }
                 if (!stage.isShowing()) {  if(webcam.isOpen())  webcam.close(); break;}
        }         
        }  } );      
        
       Platform.runLater( () -> stage.show() );
        webcamThread.start();    
    }
    private static void showInvalidQRDialog(Stage stage){        
        // Show the dialog
        Platform.runLater( () ->{
            Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi!");
        alert.setHeaderText("QR vừa quét không hợp lệ.");
        // Set modality and owner
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(stage);     
          alert.showAndWait() ;
          stage.close();
         });    
    }
    private static void showUnbondedDeviceDialog(Stage stage){
        
        // Show the dialog
        Platform.runLater( () ->{ 
            Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Chưa ghép đôi");
        alert.setHeaderText(" Thiết bị này chưa được ghép đôi. ");
        // Set modality and owner
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(stage);
            alert.showAndWait(); 
            stage.close();
                });    
    }
    private static void showBusyDeviceDialog(Stage stage){
        
        // Show the dialog
        Platform.runLater( () ->{ 
            Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Không thể nhận!");
        alert.setHeaderText(" Thiết bị đang bận, không thể nhận file! ");
        // Set modality and owner
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(stage);
            alert.showAndWait(); 
            stage.close();
                });    
    }
    
    private static void handleRequest(String decodedQR, Stage stage) {
    if (decodedQR.equals("")) showInvalidQRDialog(stage); 
    
    if (decodedQR.charAt(0) != '1'  && decodedQR.charAt(0) != '2'     )
        showInvalidQRDialog(stage);
    
    if (decodedQR.charAt(0) == '1') handleAddRequest(decodedQR, stage);
    if (decodedQR.charAt(0) == '2') handleFileSendRequest(decodedQR, stage);   
    }
      private static void showNotAddedDialog(Stage stage){        
        // Show the dialog
        Platform.runLater( () ->{ 
            Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Không thể nhận!");
        alert.setHeaderText(" Thiết bị chưa được thêm vào danh sách! ");
        // Set modality and owner
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(stage);
            alert.showAndWait(); 
            stage.close();
                });    
    }
    private static void handleFileSendRequest(String request,Stage stage) {  // REQUESTCODE = 2   "2  AB:78:99:1F:66:0E  programming_java7e.pdf  7890000  UUIDs
        String[] requestArray = request.split("::"); 
        if ( requestArray.length <  5) {  showInvalidQRDialog(stage); return;    }  /// NOT ENOUGH INFO TO LOAD THE FILE
        if ( !requestArray[2].contains(".")  ) {showInvalidQRDialog(stage); return;}     ////  UNKNOWN FILE TYPE, NO DOT FOUND
        int fileSize = 0;
        try { fileSize = Integer.parseInt(requestArray[3]); } catch (NumberFormatException nfe) {  showInvalidQRDialog(stage); return;  }
                                                                                                /// INVALID FILESIZE
        if (fileSize <= 0) {  showInvalidQRDialog(stage); return;  } /// MUST BE POSITIVE
        if ( !isAdded(requestArray[1])) { showNotAddedDialog(stage);    return; }
        if (!ProcessorController.isBusy() ) showAcceptFileDialog(requestArray,stage);
        else showBusyDeviceDialog(stage);
    }
    private static void receiveFile(String[] requestArray ) {   
        ArrayList<String> temp = new ArrayList<>();
        for (int i = 0; i < (requestArray.length - 4) ; i++ ) temp.add(requestArray[i+4]);
        String[] uu = new String[temp.size()];
        for (int i = 0; i < uu.length; i++) uu[i] = temp.get(i);
        
        MasterReceiver mMasterReceiver = new MasterReceiver(requestArray[1],
                uu,requestArray[2],Integer.parseInt(requestArray[3]));
        ( new Thread(mMasterReceiver)  ).start();        
    }
    private static boolean isAdded(String address) {
    AtomicBoolean hasBonded = new AtomicBoolean();
        CountDownLatch latch = new CountDownLatch(1);
        Runnable checkJob = () -> {
            hasBonded.set( DatabaseWorker.existsDevice(address) );
            latch.countDown();
        };
        ( new Thread(checkJob) ).start();    try { latch.await(); } catch (InterruptedException ie) { ie.printStackTrace(); }
        return hasBonded.get();
    
    }
    
    private static void showAcceptFileDialog(String[] requestArray, Stage stage){           
        
Platform.runLater( () -> {
                   Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(" Nhận file ");
            int fileSize = Integer.parseInt(requestArray[3]); String fst;
            double fs = (double) fileSize;
             DecimalFormat df = new DecimalFormat("#.##");
          
            
            if (fileSize < 1000) fst =", kích thước " + fileSize + " bytes,"; else if (fileSize < 1000000) fst =", kích thước " + df.format(fs/1000) + " KB,";
            else fst =", kích thước " + df.format(fs/1000000) + " MB,";
            
            
            alert.setHeaderText("Bạn đồng ý nhận file " + requestArray[2] + fst +" từ thiết bị "+requestArray[1] +" ?");            
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            alert.initModality(Modality.WINDOW_MODAL);

            alert.initOwner(stage);
            ButtonType acceptButton = new ButtonType("Đồng ý");
            ButtonType declineButton = new ButtonType("Hủy");
            alert.getButtonTypes().setAll(acceptButton, declineButton); 
                   
                   alert.showAndWait().ifPresent(response -> {
                if (response == acceptButton) {
                    receiveFile(requestArray);
                    stage.close();
                } 
            });  })   ;                  
   
    }
     
    private static void showAddDeviceDialog(String[] requestArray, Stage stage) {         
            if (LocalDevice.isPowerOn()) {       
            Platform.runLater( () ->{
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(" Thêm thiết bị này?");
            alert.setHeaderText("Bạn đồng ý thêm thiết bị " + requestArray[1] + " ?");            
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            alert.initModality(Modality.WINDOW_MODAL);

            alert.initOwner(stage);
            ButtonType acceptButton = new ButtonType("Đồng ý");
            ButtonType declineButton = new ButtonType("Hủy");
            alert.getButtonTypes().setAll(acceptButton, declineButton);   
                alert.showAndWait().ifPresent(response -> {
                if (response == acceptButton) {  showEnterHfNameDialog(requestArray,stage);       }                 
            }); 

            }) ;          }
            else showBluetoothOffError(stage);
    }
    private static void showBluetoothOffError (Window owner) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Bluetooth đang tắt!");
        alert.setHeaderText("Vui lòng bật bluetooth rồi thử lại!");
        // Set modality and owner
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(owner);
        // Show the dialog
        alert.showAndWait();        
    
    }
    private static boolean isValidString(String name) {
        if (name.toLowerCase().contains("delete")) return false;
        if (name.toLowerCase().contains("select")) return false;
        if (name.contains("*") || name.contains("=") || name.contains("-") ) return false;
        if(name.equals((""))) return false;
        return true;
        }
    private static void showEnterHfNameDialog(String[] requestArray, Stage owner) {     
        Platform.runLater( () -> {             
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nhập tên gợi nhớ");
        dialog.setHeaderText("Nhập tên gợi nhớ của thiết bị mới: ");        
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);            
            
            dialog.showAndWait().ifPresent(result -> {
            System.out.println("Entered name: " + result);
            Runnable addJob = () -> {
            
                BTDevice newBTDevice = new BTDevice();
                int deviceNum = DatabaseWorker.getMaxDeviceNumber() + 1;
                System.out.println("MAX DEVICE NUMBER = " + deviceNum);
                String deviceID ;
                if (deviceNum < 10) deviceID = "000" + deviceNum;
                else if (deviceNum < 100) deviceID = "00" + deviceNum;
                else deviceID = "0" + deviceNum;
                newBTDevice.setDeviceID(deviceID);
                newBTDevice.setBtAddr(requestArray[1]);
                String suggestedName = "Thiết bị " + deviceNum;     
                if (isValidString(result))
                newBTDevice.setHfName(result); else newBTDevice.setHfName(suggestedName); 
                int cores = Integer.parseInt(requestArray[2]);  
                newBTDevice.setCores(cores);               
                
                DatabaseWorker.insert(newBTDevice);
            
        };
            
            (new Thread(addJob)).start();
        });
        owner.close();
});    
    }
    
    private static boolean isDeviceBonded(String targetAddress) {
        LocalDevice localDevice;
        try {
            localDevice = LocalDevice.getLocalDevice();
            RemoteDevice[] bondedDevices = localDevice.getDiscoveryAgent().retrieveDevices(DiscoveryAgent.PREKNOWN);
            if (bondedDevices != null)
            for (RemoteDevice device : bondedDevices) {
                System.out.println(device.getBluetoothAddress());
                if (device.getBluetoothAddress().equals(targetAddress)) {   return true;   }
            }
            else return false;
        } catch ( IOException e) {   e.printStackTrace();  }
        return false;
    }
    private static void handleAddRequest(String request, Stage stage) {// REQUESTCODE = 1    "1  AB:78:99:1F:66:0E  4"
      String[] requestArray = request.split("  ");
        if ( requestArray.length <  3) {  showInvalidQRDialog(stage); return;  }
        if (!isDeviceBonded(requestArray[1].replace(":", "")))  { showUnbondedDeviceDialog(stage) ;
       // try { BluetoothBonding.discoverAndBondNewDevice(requestArray[1].replace(":", "")); } catch (Exception e) { e.printStackTrace(); }        
        return;     }
        if(!isValidString(requestArray[1]))  { showInvalidQRDialog(stage)  ;  return; }
        
        int cores = 0;
        try { cores = Integer.parseInt(requestArray[2]); } catch (NumberFormatException nfe) {  showInvalidQRDialog(stage); return;  }
        if (cores <= 0) {  showInvalidQRDialog(stage); return;  } /// MUST BE POSITIVE
        if ( !DatabaseWorker.existsDevice(requestArray[1])   )  showAddDeviceDialog(requestArray,stage);        
        
    }
    
}
