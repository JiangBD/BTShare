package btshare;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.util.HashMap;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.stage.Modality;
import javafx.stage.Window;
import javax.bluetooth.LocalDevice;

public class ShowAndScanQRController implements  Initializable{
    @FXML
    private ImageView QRImageView;
    @FXML
    private Label dNameLabel;
    
    @FXML
    private Tab myQRTab;
    @FXML
    private Tab scanQRTab;
  
        @FXML
    private Pane myQRPane;    
        @FXML
     private TabPane qrTabPane;   

    @FXML
    void handleTabChange() { 
    if (myQRTab != null && scanQRTab != null)  if (  myQRTab.isSelected()  ) showMyQR();
    if (myQRTab != null && scanQRTab != null) if (scanQRTab.isSelected()) scanQR();  
    
    }  
    @Override 
    public void initialize(URL url , ResourceBundle rb) {
        dNameLabel.setText("");
        QRImageView.setImage(null);
     qrTabPane.getSelectionModel().select(myQRTab);
    }
    private String getBtAddr() {
        String b = "";       
        try {
            LocalDevice localDevice = LocalDevice.getLocalDevice();
        b = localDevice.getBluetoothAddress(); 
        }
        catch (Exception e) { e.printStackTrace();  return b; }
        String standardAddr = "" + b.charAt(0) + b.charAt(1) + ":" + b.charAt(2) + b.charAt(3) + ":"
                + b.charAt(4) + b.charAt(5) + ":" + b.charAt(6) + b.charAt(7) + ":" + b.charAt(8) + b.charAt(9) + ":"
                + b.charAt(10) + b.charAt(11);
         return standardAddr;       
    }
    private String getDName() {    
     String b = "";       
        try {
            LocalDevice localDevice = LocalDevice.getLocalDevice();
        b = localDevice.getFriendlyName();
        }
        catch (Exception e) { e.printStackTrace(); return b; }
         return b;   
    }    

    void showMyQR() {   
        QRImageView.setImage(null);        
        if ( LocalDevice.isPowerOn() )
        {
        String addr = getBtAddr();
        int cores = Runtime.getRuntime().availableProcessors() - 3;
        if (cores < 1) cores = 1;
        String dName = getDName();
        
          try {  QRImageView.setImage(generateQRCodeImage("1  " + addr + "  " + cores  ));   } catch (Exception e) { e.printStackTrace(); }
          dNameLabel.setText( dName + "\n" + addr );                      
       
       
        }
        else{             
            dNameLabel.setText(""); QRImageView.setImage(null);
            showBluetoothOffError(myQRPane.getScene().getWindow());         
        }
  }
    private void showBluetoothOffError (Window owner) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Bluetooth đang tắt!");
        alert.setHeaderText("Vui lòng bật bluetooth rồi thử lại!");
        // Set modality and owner
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(owner);
        // Show the dialog
        alert.showAndWait();        
    
    }
    
        void scanQR() {   
        if (LocalDevice.isPowerOn() )  QRScanner.runScanner(myQRPane.getScene().getWindow()    );
        else showBluetoothOffError(myQRPane.getScene().getWindow());
    }   
    private WritableImage generateQRCodeImage(String data) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        
        BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 300, 300, hints);
        return SwingFXUtils.toFXImage(MatrixToImageWriter.toBufferedImage(bitMatrix), null);
    }

}

