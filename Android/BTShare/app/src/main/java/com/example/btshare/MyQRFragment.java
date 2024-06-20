package com.example.btshare;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MyQRFragment extends Fragment {
    private ImageView im;
    private TextView tv;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    private boolean hasRegistered() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean ab = new AtomicBoolean();
        new Thread ( () -> {
            ab.set(DatabaseWorker.hasRegistered());
            latch.countDown();
        }).start(); try { latch.await(); } catch (Exception e) { }
        return ab.get();
    }
    private String getThisBtAddr() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> addrRef = new AtomicReference<>();
        new Thread ( () -> {
            addrRef.set(DatabaseWorker.getThisBtAddr());
            latch.countDown();
        }).start(); try { latch.await(); } catch (Exception e) { }
        return addrRef.get();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_myqr, container, false);
        im = view.findViewById(R.id.QRimage);
        if (hasRegistered() ) {
            String btAddr = getThisBtAddr();
            int cores = Runtime.getRuntime().availableProcessors() - 3;
            if (cores < 1) cores = 1;
            String QRText ="1  " + btAddr + "  " + cores;
            setQRBitmap(QRText);
        }
        tv =view.findViewById(R.id.dName);
        return view;
    }
    @Override
    public void onViewCreated(View v,Bundle b ) {
        super.onViewCreated(v,b);
        if (!hasRegistered())showRegisterDialog();
        String name = getDiscoverableName();
        if (name != null) tv.setText(name); else tv.setText("");

    }

    private void showRegisterDialog() {
        if ( BluetoothAdapter.getDefaultAdapter() != null) {
            RegisterDialogFragment rdf = RegisterDialogFragment.newInstance();
            rdf.show(this.getParentFragmentManager(), "RegisterDialogFragment");
        }
        else {
            Toast.makeText(this.requireContext(),
                    "Bluetooth đang tắt,\nvui lòng bật bluetooth và thử lại", Toast.LENGTH_LONG).show();
            this.requireActivity().finish();
        }
    }
    private String getDiscoverableName() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (ActivityCompat.checkSelfPermission(this.requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        if (bluetoothAdapter == null) return null;
        String deviceName = bluetoothAdapter.getName();
        return deviceName;
    }

    private void setQRBitmap(String content) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            BitMatrix bitMatrix = barcodeEncoder.encode(content, BarcodeFormat.QR_CODE, 800 , 800);
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            im.setImageBitmap(bitmap);
            // Save the QR code as PNG file
    /*        File file = new File(requireContext().getExternalFilesDir(null), "qr.png");
            if (!file.exists()) file.createNewFile();
            else { file.delete(); file.createNewFile();  }
            FileOutputStream outputStream = new FileOutputStream(file,true);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close(); */
                    } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
