package com.example.btshare;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScanQRFragment extends Fragment {
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(ScanQRFragment.this.requireContext(),
                            "Cần cấp phép camera để quét QR", Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                }
            });

    private ActivityResultLauncher<ScanOptions> QRScanLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() == null)
                    Toast.makeText(ScanQRFragment.this.requireContext(),
                            "Đã hủy quét", Toast.LENGTH_SHORT).show();
                else {/////////////
                    handleRequest(result);
                }
            });

    private void invalidQRToast() {
        Toast.makeText(ScanQRFragment.this.requireContext(),
                "QR không hợp lệ!",
                Toast.LENGTH_LONG).show();
    }

    private void unbondedDeviceToast() {
        Toast.makeText(ScanQRFragment.this.requireContext(),
                "Thiết bị này chưa được ghép đôi.",
                Toast.LENGTH_LONG).show();
    }
    private void busyDeviceToast() {
        Toast.makeText(ScanQRFragment.this.requireContext(),
                "Thiết bị đang bận, không thể nhận file!",
                Toast.LENGTH_LONG).show();
    }

    private void notAddedDeviceToast() {
        Toast.makeText(ScanQRFragment.this.requireContext(),
                "Thiết bị này chưa được thêm vào danh sách\n quét QR để thêm thiết bị.",
                Toast.LENGTH_LONG).show();
    }

    private void handleRequest(ScanIntentResult result) {
        String text = result.getContents();
        if (text.equals("")) invalidQRToast();
        if (text.charAt(0) != '1' && text.charAt(0) != '2') invalidQRToast();
        if (text.charAt(0) == '1') handleAddRequest(text);
        if (text.charAt(0) == '2') handleFileSendRequest(text);
    }

    private void handleAddRequest(String request) {
        String[] info = request.split("  ");
        String[] requestArray = request.split("  ");
        if (requestArray.length < 3) {
            invalidQRToast();
            return;
        }
        if (!isBonded(requestArray[1])) {
            unbondedDeviceToast();
            tryToBond(requestArray[1]);
            return;
        }

        int cores = 0;
        try {
            cores = Integer.parseInt(requestArray[2]);
        } catch (NumberFormatException nfe) {
            invalidQRToast();
            return;
        }
        if (cores <= 0) {
            invalidQRToast();
            return;
        } /// MUST BE POSITIVE
        if (isAdded(requestArray[1])) { return; }
        showAddDeviceDialogFragment(requestArray);
    }

    private void handleFileSendRequest(String text) {
        String[] requestArray = text.split("::");
        if (!isBonded(requestArray[1])) {
            unbondedDeviceToast();
            return;
        }
        if (!isAdded(requestArray[1])) {
            notAddedDeviceToast();
            return;
        }
        if (requestArray.length < 5) {
            invalidQRToast();
            return;
        }  /// NOT ENOUGH INFO TO LOAD THE FILE
        if (!requestArray[2].contains(".")) {
            invalidQRToast();
            return;
        }     ////  UNKNOWN FILE TYPE, NO DOT FOUND
        int fileSize = 0;
        try {
            fileSize = Integer.parseInt(requestArray[3]);
        } catch (NumberFormatException nfe) {
            invalidQRToast();
            return;
        } /// INVALID FILESIZE
        if (fileSize <= 0) {
            invalidQRToast();
            return;
        } /// MUST BE POSITIVE
        if (!isPowerOn()) { bluetoothOffToast(); return; }
        if( !ProcessorController.isBusy()) showAcceptFileDialog(requestArray);
        else busyDeviceToast();
    }
    private void bluetoothOffToast() {
        Toast.makeText(this.requireContext(),"Bluetooth đang tắt!",Toast.LENGTH_SHORT).show();
    }
    private boolean isPowerOn()
    {
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if (ba == null) return false;

        return ba.isEnabled() ;   }
    private void showAddDeviceDialogFragment(String[] requestArray) {
        AddDeviceDialogFragment addf = AddDeviceDialogFragment.newInstance(requestArray);
        addf.show(ScanQRFragment.this.getParentFragmentManager(), "AddDevice");
    }

    private void showAcceptFileDialog(String[] requestArray) {
         AlertDialog.Builder builder = new AlertDialog.Builder(ScanQRFragment.this.requireContext());
        builder.setTitle("Nhận file");
        int fileSize = -9;
        String fst = "";
        try {
            fileSize = Integer.parseInt(requestArray[3]);
        } catch (NumberFormatException nfe) {
            invalidQRToast();
            ;
            return;
        }
        double fs = (double) fileSize;
        DecimalFormat df = new DecimalFormat("#.##");

        if (fileSize < 1000 ) fst = fileSize + " bytes";
        else if ( fileSize < 1000000) fst = df.format(fs/1000) + " KB";
        else fst = df.format(fs/1000000) + " MB";

        ArrayList<String> temp = new ArrayList<>();
        for (int i = 0; i < (requestArray.length - 4); i++)
            temp.add(requestArray[i + 4]);
        String[] uu = new String[temp.size()];
        for (int i = 0; i < temp.size(); i++) uu[i] = temp.get(i);

        builder.setMessage("Bạn có muốn nhận file " + requestArray[2] + "\n, kích thước " +
                fst + "?");
        // Set the positive button and its click listener
        int finalFileSize = fileSize;
        builder.setPositiveButton("OK", (dialog, which) -> {
                    (new Thread(MasterReceiver.newInstance(ScanQRFragment.this.requireContext()
                            , requestArray[1], uu, requestArray[2], finalFileSize))).start();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });

        builder.setCancelable(false);
        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean checkCameraPermission() {
        return (ContextCompat.checkSelfPermission(ScanQRFragment.this.requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean isBonded(String remoteAddress) {
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(remoteAddress);
        if (device == null) return false;
        try {
            if (device.getBondState() == BluetoothDevice.BOND_BONDED)
                return true;

        } catch (SecurityException se) {
            return false;
        }
        return false;
    }

    private void tryToBond(String address) {
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        if (device != null) {
            if (ActivityCompat.checkSelfPermission(this.requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {  return;
            }
            device.createBond();
        }
    }
    private boolean isAdded(String remoteAddress) {
        AtomicBoolean ab = new AtomicBoolean();
        CountDownLatch latch = new CountDownLatch(1);
        new Thread( () -> {
            ab.set(DatabaseWorker.existsDevice(remoteAddress));
            latch.countDown();
        }).start();
        try { latch.await(); } catch (Exception e) {}
        return ab.get();
    }



    private void requestCameraPermission( ) {
        if ( shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ) {
            Toast.makeText(ScanQRFragment.this.requireContext(),
                    "Cần cấp phép camera để quét QR", Toast.LENGTH_SHORT).show();
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
        else requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }
    private void runScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Scan QR code");
        options.setCameraId(0);
        options.setBeepEnabled(false);
        options.setBarcodeImageEnabled(true);
        options.setOrientationLocked(false);
        QRScanLauncher.launch(options);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_scanqr, container, false);
        RelativeLayout masterQRLayout = view.findViewById(R.id.masterqrlayout);
        if (!checkCameraPermission()) requestCameraPermission();

        masterQRLayout.setOnClickListener( v -> {
            if (!checkCameraPermission()) requestCameraPermission();
            else if (!isPowerOn()) { bluetoothOffToast(); }
               else runScanner();
    });
        return view;
    }

}
