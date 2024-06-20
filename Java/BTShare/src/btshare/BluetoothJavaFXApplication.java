package btshare;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import java.lang.ref.WeakReference;
import java.io.File;

public class BluetoothJavaFXApplication extends Application {
    private static WeakReference<Stage> primaryStageRef;    
    
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader =
                new FXMLLoader(BluetoothJavaFXApplication.class.getResource("NavView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        initializeSingletons();
        checkExistsFolder(); 
        
        stage.setTitle("BTShare");
        stage.setScene(scene); 
        if ( primaryStageRef == null ) primaryStageRef = new WeakReference<>(stage);
        stage.show();       
    }
    private void checkExistsFolder() {
    File folder = new File("D:/BTShare");
    if ( !folder.exists()) folder.mkdir();    
    }
    
    
    private void initializeSingletons() {
    DatabaseWorker.initialize();
    ProcessorController.initialize();
    }

    public static void main(String[] args) {
        launch();        
    }
    public static Stage getPrimaryStage() { return primaryStageRef.get(); }
    
    
}