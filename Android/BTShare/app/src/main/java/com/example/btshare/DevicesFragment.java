package com.example.btshare;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DevicesFragment extends Fragment {
    private CountDownLatch mainLatch;
    private BTDevice currentPartner;
    private ActivityResultLauncher<Intent> requestStoragePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        //here we will handle the result of our intent
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            //Android is 11(R) or above
                            if (Environment.isExternalStorageManager()) {
                                //Manage External Storage Permission is granted
                                Toast.makeText(DevicesFragment.this.requireContext(), "Đã được cấp phép truy cấp bộ nhớ"
                                        , Toast.LENGTH_SHORT).show();

                            } else {
                                //Manage External Storage Permission is denied
                                Toast.makeText(DevicesFragment.this.requireContext(),
                                        "Ứng dụng không thể hoạt động\n nếu không được quyền truy cập bộ" +
                                                " nhớ", Toast.LENGTH_SHORT).show();
                                DevicesFragment.this.requireActivity().finish(); // OUT!
                            }
                        }
                    });
    private final ActivityResultLauncher<String> oldAndroidRequestStoragePermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(), result -> {
                        if ( !result) {
                            Toast.makeText(DevicesFragment.this.requireContext(),
                                    "Ứng dụng không thể hoạt động\n nếu không được quyền truy cập bộ" +
                                            " nhớ", Toast.LENGTH_SHORT).show();
                            DevicesFragment.this.requireActivity().finish();
                        }
                    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        atomicFileUri = new AtomicReference<>();
    }
    AtomicReference<Uri> atomicFileUri;
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent rIntent = result.getData();
                    Uri uri = rIntent.getData();
                    if (uri != null) {
                        (new Thread(new MasterSender(DevicesFragment.this.requireContext(),uri,currentPartner)) ).start();

                }
            } });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_devices, container, false);

            RecyclerView rv = view.findViewById(R.id.devicesRecyclerView);
            rv.setLayoutManager(new LinearLayoutManager(DevicesFragment.this.requireContext()));
            rv.setAdapter(new DeviceAdapter());
        return view;
    }
    @Override
    public void onViewCreated(View v, Bundle b) {
        super.onViewCreated(v,b);
        if ( !checkStoragePermission() ) requestStoragePermission();

    }


    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*"); // Set MIME type to any file type
        launcher.launch(intent);


    }
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            ContentResolver contentResolver = DevicesFragment.this.requireContext().getContentResolver();
            try (Cursor cursor = contentResolver.query(
                    uri, null, null, null, null)) {

                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        fileName = cursor.getString(index);
                    }
                }
            }
        }
        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }
    private boolean isPowerOn()
    {
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if (ba == null) return false;

        return ba.isEnabled() ;   }
    private void bluetoothOffToast() {
        Toast.makeText(this.requireContext(),"Bluetooth đang tắt!",Toast.LENGTH_SHORT).show();
    }
    private void busyDeviceToast() {
        Toast.makeText(this.requireContext(),"Thiết bị đang bận,\n vui lòng thử lại sau.",Toast.LENGTH_SHORT).show();
    }

    private static class DeviceHolder extends RecyclerView.ViewHolder {
        private TextView btaddrTV, hfnameTV;
        private BTDevice device;

        public void setDevice(BTDevice d) {
            device = d;
        }

        private DeviceHolder(View v) {
            super(v);
            btaddrTV = v.findViewById(R.id.btaddr);
            hfnameTV = v.findViewById(R.id.hfname);
        }
    }

    private class DeviceAdapter extends RecyclerView.Adapter<DeviceHolder> {
        private List<BTDevice> dl;

        private void loadAllDevices() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<List<BTDevice>> fu = executor.submit(DatabaseWorker::loadAllDevices);
            try {
                dl = fu.get();
            } catch (Exception e) {
                e.getMessage();
            }

        }
        public DeviceAdapter() {
            loadAllDevices();
        }


        @NonNull
        @Override
        public DeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = getLayoutInflater().inflate(R.layout.list_item_device, parent, false);
            return new DeviceHolder(view);
        }

        @Override
        public int getItemCount() {
            return dl.size();
        }


        @Override
        public void onBindViewHolder(DeviceHolder holder, int pos) {
            holder.setDevice(dl.get(pos));
            holder.itemView.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(DevicesFragment.this.requireContext(), holder.itemView);        // Inflating the menu items
                popupMenu.getMenuInflater().inflate(R.menu.floating_menu, popupMenu.getMenu());
                // Setting click listener for menu items
                popupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.send) {
                            if (!ProcessorController.isBusy())
                            {if (isPowerOn()) sendFile(holder.device);
                              else bluetoothOffToast(); }
                            else busyDeviceToast();
                            return true;
                        }
                        if (item.getItemId() == R.id.rename) {
                            showRenameDialog(holder);
                            return true;
                    }
                        if (item.getItemId() == R.id.del) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(DevicesFragment.this.requireContext());
                            builder.setTitle("Xóa thiết bị")
                                    .setMessage("Bạn muốn xóa thiết bị này?")
                                    .setPositiveButton("OK", (dialog,which) -> {
                                        deleteDevice(holder.device);
                                        DeviceAdapter.this.dl.remove(pos);
                                        DeviceAdapter.this.notifyItemRemoved(pos);
                                    })
                                    .setNegativeButton("Cancel", (dialog,which) -> {
                                            dialog.dismiss();

                                    }).setCancelable(false)
                                    .show();
                                    return true;
                    }

                        else return false;
                });
                // Showing the PopupMenu
                popupMenu.setGravity(Gravity.END);
                popupMenu.show();
            });
            holder.btaddrTV.setText(holder.device.getBtAddr());
            holder.hfnameTV.setText(holder.device.getHfName());
        }
    }
    private void sendFile(BTDevice partner) {
        openFileChooser();
        currentPartner = partner;
    }
    private void deleteDevice(BTDevice device) {
        Runnable deleteJob = () -> DatabaseWorker.deleteDevice(device);
        Thread deleteWorker = new Thread(deleteJob);
        deleteWorker.start();

    }
    private void showRenameDialog(DeviceHolder holder) {
        final EditText input = new EditText(DevicesFragment.this.requireContext());
        LinearLayout container = new LinearLayout(DevicesFragment.this.requireContext());
        container.setPadding(16, 16, 16, 16);  // Add some padding
        container.addView(input);
        AlertDialog.Builder builder = new AlertDialog.Builder
                (DevicesFragment.this.requireContext());
        builder.setTitle("Đổi tên")
                .setMessage("Nhập tên mới:")
                .setView(container)
                .setPositiveButton("OK" , (dialog, which) -> {
                    String newName = input.getText().toString();
                    if (checkAndRename(holder.device,newName) )
                          holder.hfnameTV.setText(newName);

                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private boolean isValidName(String name)
    {
        if (name.toLowerCase().contains("delete")) return false;
        if (name.toLowerCase().contains("select")) return false;
        if (name.contains("*") || name.contains("=") || name.contains("-") ) return false;
        return !name.equals((""));
    }

    private boolean checkAndRename(BTDevice device, String newName) {
        if ( newName.equals("") || newName.contains("*")
                || newName.contains("=") ||  newName.contains("-")) return false;
        if (!isValidName(newName)) return false;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Boolean> checkDupJob = () -> {
            return DatabaseWorker.existsHfName(newName);
        };
        Runnable renameJob = () -> {DatabaseWorker.rename(device.getDeviceID(),newName); };
        Boolean isDup = null;
        Future<Boolean> result1 = executor.submit(checkDupJob);
        try { isDup = result1.get(); }
        catch
        (InterruptedException | ExecutionException ie) { isDup = false; }
        if (isDup != null ) { executor.submit(renameJob); isDup = true; }
        executor.shutdown();
        return Boolean.TRUE.equals(isDup);
    }

    private BTDevice getBTDevice(BluetoothDevice rDevice){
        String address = rDevice.getAddress();
        Callable<BTDevice> getJob = () -> DatabaseWorker.getDevice(address);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<BTDevice> result = executor.submit(getJob);
        BTDevice device = null;
        try { device = result.get(); } catch ( Exception e) {}
        return device;
    }
    private boolean isValidSize(Uri uri) { /// EXCEEDS 70000000 bytes!
        int size = getFileSizeFromUri(DevicesFragment.this.requireContext(),uri);
        if (size > 70000000) return false;
        return true;
    }
    private void invalidSizeToast(Context ct) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post( () -> Toast.makeText(ct, "File vượt quá kích cỡ\ncho phép là 70MB!",
                Toast.LENGTH_SHORT).show()  );
    }

    private void fileNameToast(Context ct) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post( () -> Toast.makeText(ct, getFileNameFromUri(atomicFileUri.get()),
                Toast.LENGTH_SHORT).show()  );
    }
    private void errorToast(Context ct) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post( () -> Toast.makeText(ct, "Đã xảy ra lỗi\ngửi file không thành công",
                Toast.LENGTH_SHORT).show()   );
    }
    private int getFileSizeFromUri(Context ct, Uri uri) {
        ContentResolver resolver = ct.getContentResolver();
        int size = -1;
        try {
            InputStream IS = resolver.openInputStream(uri);
            assert IS != null;
            size = IS.available();
            IS.close();
        }
        catch (Exception e) { return -1; }
        return size;
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", this.requireActivity().getPackageName(), null);
            intent.setData(uri);
            requestStoragePermissionLauncher.launch(intent);
        }
        else {
            String[] pm = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};
            oldAndroidRequestStoragePermissionLauncher.launch(pm[0]);
            oldAndroidRequestStoragePermissionLauncher.launch(pm[1]);
        }
    }
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            return (Environment.isExternalStorageManager() );
        else return
                ((ContextCompat.checkSelfPermission(
                        this.requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)
                        && (ContextCompat.checkSelfPermission(
                        this.requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)
                );
    }

}