/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package btshare;

import java.net.URL;
import java.text.DecimalFormat;
import javafx.fxml.Initializable;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class HistoryController implements Initializable {
    private List<BTTask> sendList = getSendList();
    private List<BTTask> receiveList = getReceiveList();
    @FXML
    private Tab sendTab;
    @FXML
    private Tab receiveTab;
    @FXML
    private VBox  sendVBox;
    @FXML
    private VBox  receiveVBox;

    @FXML
    void handleTabChange() { 
    if (sendTab != null && receiveTab != null) if (  sendTab.isSelected()  ) showSendTasks();
    if (sendTab != null && receiveTab != null) if (receiveTab.isSelected()) showReceiveTasks();  
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       sendList = getSendList()  ; 
       sendList.sort( (task1,task2) ->{ return -(Integer.compare(Integer.parseInt(task1.getTaskID()), Integer.parseInt(task2.getTaskID()))) ;    
               });
       
       receiveList = getReceiveList();
       receiveList.sort( (task1,task2) ->{ return -(Integer.compare(Integer.parseInt(task1.getTaskID()), Integer.parseInt(task2.getTaskID()))) ;    
               });
    }    
    private List<BTTask> getSendList() {            
         AtomicReference < List<BTTask >> atomicRefTaskList = new AtomicReference<>() ;            
         
         Runnable loadJob = () ->  {      
         atomicRefTaskList.set( DatabaseWorker.getSendList() );              
     };
         Thread thr = ( new Thread(loadJob) ); thr.start();      
      try { thr.join(); } catch (InterruptedException ie) {System.out.println("ERROR"); return atomicRefTaskList.get();  }
      
         
         return atomicRefTaskList.get();    
    }
    private List<BTTask> getReceiveList() {
            
         AtomicReference < List<BTTask >> atomicRefTaskList = new AtomicReference<>() ;   
        
         Runnable loadJob = () ->  {      
         atomicRefTaskList.set( DatabaseWorker.getReceiveList() );    
         
     };
        Thread thr = ( new Thread(loadJob) ); thr.start();      
      try { thr.join(); } catch (InterruptedException ie) {System.out.println("ERROR"); return atomicRefTaskList.get();  }
               
         return atomicRefTaskList.get();    
    }
    private void showSendTasks() {
        if (sendList != null) {
        if (!sendList.isEmpty())
        {
        sendVBox.getChildren().clear();
        for( BTTask task : sendList  ) {
            String showString = task.getHfName() +"\n"+ task.getBtAddr() + "\n" + task.getFileName();
            String sizeStr = ""; int size = task.getSize();
           double fs = (double) size;
             DecimalFormat df = new DecimalFormat("#.##");
            
            if (size < 1000) sizeStr = size + " bytes"; else if (size < 1000000) sizeStr = df.format(fs/1000) + " KB";
            else sizeStr = df.format(fs/1000000) + " MB";
            showString += "\n" + sizeStr + "\n" +task.getTime();        
            
            Label label = new Label( showString );
            label.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-border-style: solid; -fx-font-weight: bold;");
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
    private void showReceiveTasks() {
        if (receiveList != null) {
        if (!receiveList.isEmpty())
        {
            receiveVBox.getChildren().clear();
    for( BTTask task : receiveList  ) {
            String showString = task.getHfName() +"\n"+ task.getBtAddr() + "\n" + task.getFileName();
            String sizeStr = ""; int size = task.getSize();
           double fs = (double) size;
             DecimalFormat df = new DecimalFormat("#.##");
            
            if (size < 1000) sizeStr = size + " bytes"; else if (size < 1000000) sizeStr = df.format(fs/1000) + " KB";
            else sizeStr = df.format(fs/1000000) + " MB";
            showString += "\n" + sizeStr + "\n" +task.getTime();        
            
            Label label = new Label( showString );
            label.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-border-style: solid; -fx-font-weight: bold;");
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
