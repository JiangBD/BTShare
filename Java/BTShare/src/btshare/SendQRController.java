
package btshare;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import javafx.fxml.FXML;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

public class SendQRController implements Initializable {
    
    @FXML
    private ImageView sendQRim;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String sendString = ProcessorController.getSendString();
        if (sendString != null)  {  
                try {    sendQRim.setImage(generateQRCodeImage(sendString));     } catch (WriterException we) { we.printStackTrace(); }          
        
        }
        else System.err.println("sendString null!!!!!");
       
    }    
        private WritableImage generateQRCodeImage(String data) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        
        BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 400, 400, hints);
        return SwingFXUtils.toFXImage(MatrixToImageWriter.toBufferedImage(bitMatrix), null);
    }
    
}
