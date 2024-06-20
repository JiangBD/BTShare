
package btshare;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Window;
import javax.bluetooth.LocalDevice;

public class DeviceListController implements Initializable {
    @FXML
    ScrollPane devicesSPane;
    private List<BTDevice> deviceList;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        deviceList = loadBTDeviceList();        
        VBox vb =(VBox) devicesSPane.getContent();               
          
                for (int i = 0; i < deviceList.size(); i++) {
                    MyButton b = createDeviceItemMyButton(vb,deviceList.get(i));                            
                    vb.getChildren().add(b);
                }
    }    
    
    
    
    
     private List<BTDevice> loadBTDeviceList() {         
         AtomicReference < List<BTDevice >> atomicRefDeviceList = new AtomicReference<>() ;   
         
         CountDownLatch latch = new CountDownLatch(1);
         Runnable loadJob = () ->  {      
         atomicRefDeviceList.set( DatabaseWorker.loadAllDevices() );    
         latch.countDown();  
     };
         ( new Thread(loadJob) ).start();      
      try { latch.await(); } catch (InterruptedException ie) { return atomicRefDeviceList.get();  }
      
         System.out.println("BTDevice list loaded successfully...");
         return atomicRefDeviceList.get();
     } 
    private MyButton createDeviceItemMyButton(Node vb, BTDevice device) {
            MyButton b = new MyButton(device);
            b.updateDisplay();
            ContextMenu contextMenu = new ContextMenu();
            // Create MenuItems
            MenuItem item1 = new MenuItem("Gửi file");
            MenuItem item2 = new MenuItem("Đổi tên");
            MenuItem item3 = new MenuItem("Xóa thiết bị");
            item1.setOnAction(ev -> {
                if( !LocalDevice.isPowerOn())  {   showBluetoothOffError(vb.getScene().getWindow()) ; return;}
                if ( !ProcessorController.isBusy()) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Chọn file để gửi");
                File selectedFile = fileChooser.showOpenDialog(vb.getScene().getWindow());
                if (selectedFile != null) {   
                    System.out.println("File selected: " + selectedFile.getAbsolutePath());   
                    boolean validsize; if (selectedFile.length() <= 70000000) validsize = true; else validsize = false;
                    if (validsize ) sendFile(device, selectedFile.getAbsolutePath()); else showFileTooBigDialog(vb.getScene().getWindow()  );
                }
                
                } else showBusyDialog(vb.getScene().getWindow());               
                
            });
            item2.setOnAction(ev -> {
                showRenameDialog(vb.getScene().getWindow(), b);
            
            });         
            
            item3.setOnAction( ev ->  {
                Platform.runLater( () ->{
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xóa thiết bị?");
            alert.setHeaderText("Bạn muốn xóa thiết bị này?");            
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            alert.initModality(Modality.WINDOW_MODAL);

            alert.initOwner(vb.getScene().getWindow() );
            ButtonType acceptButton = new ButtonType("Xóa");
            ButtonType declineButton = new ButtonType("Hủy");
            alert.getButtonTypes().setAll(acceptButton, declineButton);   
                alert.showAndWait().ifPresent(response -> {
                if (response == acceptButton) {   ( new Thread( () -> DatabaseWorker.deleteDevice(b.getBTDevice().getDeviceID())     )  ).start();
              VBox parent =(VBox) b.getParent(); parent.getChildren().remove(b);     
                }                 
            }); 

            }) ;             
            });
            
            
            contextMenu.getItems().addAll(item1, item2, item3);
            b.setContextMenu(contextMenu);
                    b.setOnMouseClicked(ev -> {
                        b.getContextMenu().show(b,ev.getScreenX(),ev.getScreenY() );
                    });
                    b.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-border-style: solid; -fx-font-weight: bold;");
                    b.setAlignment(Pos.CENTER_LEFT);
                    b.setMaxWidth(Double.MAX_VALUE);
                    b.setMinWidth(550.0);
                    b.setMaxHeight(Double.MAX_VALUE);
                    b.setPadding(new Insets(5,0,0,0));
                    
            return b;
        }
    private boolean isValidName(String name) {
        if (name.toLowerCase().contains("delete")) return false;
        if (name.toLowerCase().contains("select")) return false;
        if (name.contains("*") || name.contains("=") || name.contains("-") ) return false;
        if(name.equals((""))) return false;
        return true;
        }
        private void showRenameDialog(Window owner, MyButton button) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Đổi tên thiết bị");
        dialog.setHeaderText("Nhập tên mới:");
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);

        dialog.showAndWait().ifPresent(result -> {
            System.out.println("Entered name: " + result);
            if (isValidName(result)) {
            button.getBTDevice().setHfName(result);  button.updateDisplay();
            ( new Thread( () -> DatabaseWorker.rename(button.getBTDevice().getDeviceID(), result)  ) ).start();           
            }  });
        }
        
    private void sendFile(BTDevice partner, String filePath) {
        
            if(LocalDevice.isPowerOn())     ( new Thread( new MasterSender(partner, filePath) ) ).start();        
            else showBluetoothOffError(devicesSPane.getScene().getWindow());
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
    
    private void showBusyDialog (Window window) {
         Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Không thể gửi!");
        alert.setHeaderText("Thiết bị đang bận!");
        // Set modality and owner
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(window);
        // Show the dialog
        alert.showAndWait();        
        }
    private void showFileTooBigDialog (Window window) {
         Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("File không hợp lệ!");
        alert.setHeaderText("Kích cỡ file đã chọn quá lớn (vượt 70 MB).");
        // Set modality and owner
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(window);
        // Show the dialog
        alert.showAndWait();        
        }
    
}
