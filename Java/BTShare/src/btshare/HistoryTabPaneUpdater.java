
package btshare;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import java.text.Normalizer;
import javafx.application.Platform;
public class HistoryTabPaneUpdater {
    public static void checkHistoryPane(String tabName) {
    BorderPane bp = (BorderPane)  BluetoothJavaFXApplication.getPrimaryStage().getScene().getRoot();
    Node center = bp.getCenter();
    if (center instanceof TabPane )
    {   TabPane tp = (TabPane) center;
        ObservableList<Tab> tabs = tp.getTabs();
        boolean isShowing = false;
        
        for (Tab t : tabs) if ( Normalizer.normalize( t.getText(),Normalizer.Form.NFD)
                .equals(Normalizer.normalize(tabName, Normalizer.Form.NFD) ) ) { isShowing = true;  break;}
        
        if (!isShowing) return;
        
        Platform.runLater( HistoryTabPaneUpdater:: updateHistoryTabPane );
    }   
    
    }
    private static void updateHistoryTabPane() {
         BorderPane mainBP = (BorderPane)  BluetoothJavaFXApplication.getPrimaryStage().getScene().getRoot() ;
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
      
    
    
    private static List<BTTask> getReceiveList() {
            
         AtomicReference < List<BTTask >> atomicRefTaskList = new AtomicReference<>() ;   
        
         Runnable loadJob = () ->  {      
         atomicRefTaskList.set( DatabaseWorker.getReceiveList() );    
         
     };
        Thread thr = ( new Thread(loadJob) ); thr.start();      
      try { thr.join(); } catch (InterruptedException ie) {System.out.println("ERROR"); return atomicRefTaskList.get();  }
               
         return atomicRefTaskList.get();    
    }
    private static List<BTTask> getSendList() {            
         AtomicReference < List<BTTask >> atomicRefTaskList = new AtomicReference<>() ;                     
         Runnable loadJob = () ->  {      
         atomicRefTaskList.set( DatabaseWorker.getSendList() );              
     };
         Thread thr = ( new Thread(loadJob) ); thr.start();      
      try { thr.join(); } catch (InterruptedException ie) {System.out.println("ERROR"); return atomicRefTaskList.get();  }        
         return atomicRefTaskList.get();    
    }
      private static void updateSendTab (Tab sendTab) {
        List<BTTask> sendList = getSendList();
     if (sendList != null) {
        if (!sendList.isEmpty())
        {VBox sendVBox =(VBox) sendTab.getContent();
        sendVBox.getChildren().clear();
        for( BTTask task : sendList  ) {
            String showString = task.getHfName() +"\n"+ task.getBtAddr() + "\n" + task.getFileName();
            String sizeStr = ""; int size = task.getSize();
            if (size < 1000) sizeStr = size + " bytes"; else if (size < 1000000) sizeStr = (size/1000) + " KB";
            else sizeStr = (size/1000000) + " MB";
            showString += "\n" + sizeStr + "\n" +task.getTime();        
            
            Label label = new Label( showString );
            label.setStyle("-fx-border-color: black; -fx-border-width: 2px; -fx-border-style: solid; -fx-font-weight: bold;");
            label.setAlignment(Pos.CENTER_LEFT);
                    label.setMaxWidth(Double.MAX_VALUE);
                    label.setMinWidth(550.0);
                    label.setMaxHeight(Double.MAX_VALUE);
                    label.setPadding(new Insets(5,0,0,0)); 
            sendVBox.getChildren().add(label);        
        }
        }
        }
    
    
    }
      private static void updateReceiveTab (Tab receiveTab) {
        List<BTTask> receiveList = getReceiveList();
     if (receiveList != null) {
        if (!receiveList.isEmpty())
        {VBox receiveVBox =(VBox) receiveTab.getContent();
        receiveVBox.getChildren().clear();
        for( BTTask task : receiveList  ) {
            String showString = task.getHfName() +"\n"+ task.getBtAddr() + "\n" + task.getFileName();
            String sizeStr = ""; int size = task.getSize();
            if (size < 1000) sizeStr = size + " bytes"; else if (size < 1000000) sizeStr = (size/1000) + " KB";
            else sizeStr = (size/1000000) + " MB";
            showString += "\n" + sizeStr + "\n" +task.getTime();        
            
            Label label = new Label( showString );
            label.setStyle("-fx-border-color: black; -fx-border-width: 2px; -fx-border-style: solid; -fx-font-weight: bold;");
            label.setAlignment(Pos.CENTER_LEFT);
                    label.setMaxWidth(Double.MAX_VALUE);
                    label.setMinWidth(550.0);
                    label.setMaxHeight(Double.MAX_VALUE);
                    label.setPadding(new Insets(5,0,0,0)); 
            receiveVBox.getChildren().add(label);        
        }
        }
        }
    
    
    }
      
      
      
      
}
