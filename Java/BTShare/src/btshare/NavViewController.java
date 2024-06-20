package btshare;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class NavViewController implements Initializable {        
        @FXML
        private BorderPane mainBP;
        @FXML
        HBox pBarHBox;

        @FXML
        void addDevice(MouseEvent event) {
            Parent root = null;
            FXMLLoader fxmlLoader =
                    new FXMLLoader(NavViewController.class.getResource("ShowAndScanQR.fxml"));
            try {
                root = fxmlLoader.load();
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
           if (root != null) mainBP.setCenter(root); 
           
            else System.out.println("NULL POINTER!!!");
        }
          @FXML
        private ImageView imageView;
      
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image image = new Image("file:D:/app_icon.png");
        imageView.setImage(image);        
        pBarHBox.setVisible(false);
}
        @FXML
        void deviceList(MouseEvent event) {
            
            Parent root = null;
            FXMLLoader fxmlLoader =
                    new FXMLLoader(getClass().getResource("DeviceList.fxml"));
            try {    root = fxmlLoader.load();  mainBP.setCenter(root);  }  catch (Exception e) {  e.printStackTrace(); return;  }     
                           
        } 

        @FXML
        void history(MouseEvent ev) {
            Parent root = null;
            FXMLLoader fxmlLoader =
                    new FXMLLoader(NavViewController.class.getResource("History.fxml"));
            try {
                root = fxmlLoader.load();
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }     
                mainBP.setCenter(root);            
        }
        @FXML
        void openFolder(MouseEvent event) {
        String folderPath = "D:\\BTShare";
        try {
            Runtime.getRuntime().exec("explorer.exe " + folderPath);
        } catch (IOException e) {
            e.printStackTrace();
        }        
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
            @FXML
    void cancelTransferring(MouseEvent event) {
        ProcessorController.cancelTransferring();                  
        System.out.println("CANCEL IS CLICKED.");
        clearSendQR();
    }
            @FXML
    void showSendQR(MouseEvent event) {
     Parent root = null;
            FXMLLoader fxmlLoader =
                    new FXMLLoader(NavViewController.class.getResource("SendQR.fxml"));
            try {
                root = fxmlLoader.load();
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }     
                mainBP.setCenter(root);        
    } 

    }


