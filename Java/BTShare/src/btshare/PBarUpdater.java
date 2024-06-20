/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package btshare;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
public class PBarUpdater {
    double currentPerc;
    int received, totalSize;
    ProgressBar pBar;
    public PBarUpdater(int fileSize) {
    currentPerc = 0.0; received = 0; totalSize = fileSize;
        BorderPane bp =(BorderPane) BluetoothJavaFXApplication.getPrimaryStage().getScene().getRoot();
        HBox hb =(HBox) bp.getBottom(); ObservableList<Node> list = hb.getChildren();
        for ( Node node : list  ) if ( node instanceof ProgressBar) pBar =(ProgressBar)  node;       
        
    }     
    
    synchronized public void onDataChunkTransferred(int newChunk) {
            received += newChunk;
            if (received >= totalSize) pBar.setProgress(100.0);
            else {
            double tempPerc = received / totalSize;
            if (tempPerc - currentPerc >= 0.03)  {  currentPerc = tempPerc ; Platform.runLater( () -> pBar.setProgress(tempPerc) ); }           
            }
            }
    
    
}
