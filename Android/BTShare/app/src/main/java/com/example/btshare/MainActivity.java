package com.example.btshare;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> requestStoragePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                //here we will handle the result of our intent
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    //Android is 11(R) or above
                    if (Environment.isExternalStorageManager()) {
                        //Manage External Storage Permission is granted
                        Toast.makeText(MainActivity.this, "Đã được cấp phép truy cấp bộ nhớ"
                                , Toast.LENGTH_SHORT).show();

                    } else {
                        //Manage External Storage Permission is denied
                        Toast.makeText(MainActivity.this,
                                "Ứng dụng không thể hoạt động\n nếu không được quyền truy cập bộ" +
                                        " nhớ", Toast.LENGTH_SHORT).show();
                        MainActivity.this.finish(); // OUT!
                    }
                }
            });//// END LAMBDA);
    private final ActivityResultLauncher<String> oldAndroidRequestStoragePermissionLauncher =
            registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if ( !result) {
                    Toast.makeText(MainActivity.this,
                            "Ứng dụng không thể hoạt động\n nếu không được quyền truy cập bộ" +
                                    " nhớ", Toast.LENGTH_SHORT).show();
                    MainActivity.this.finish();
                }
            });
    private DrawerLayout drawer;
    private NavigationView navView;
    private ActionBarDrawerToggle toggle;
    private ProgressBar pBar;

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!checkBluetoothPermission())requestBluetoothPermission();


        MyApplication.setMainActivity(MainActivity.this);

        drawer = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        // Setup drawer toggle
        toggle = new ActionBarDrawerToggle(MainActivity.this, drawer,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        pBar = findViewById(R.id.progress_bar);

    }
    @Override public void onStart() {
        super.onStart();

        Fragment _currentFragment =
                MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.main_content);
        if ( _currentFragment == null ) {
            AddDeviceFragment hf = new AddDeviceFragment();
            MainActivity.this.getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_content,hf)
                    .commitNow(); }
        initializeMainUI();
        if ( !checkStoragePermission() ) requestStoragePermission();
    }
    private void clearSendQR(){
        ProcessorController.setSendString(null);
        Fragment frag = MyApplication.getMainActivity().getSupportFragmentManager()
                .findFragmentById(R.id.main_content);
        if (frag instanceof SendQRFragment)  {
            SendQRFragment qrfrag = (SendQRFragment) frag;
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(qrfrag::clearSendQR);
        }
    }
    private void initializeMainUI () {
        findViewById(R.id.stopTransferringButton).setOnClickListener(
                view -> { ProcessorController.cancelTransferring();
                          clearSendQR();
    });

        navView.setNavigationItemSelectedListener( (item) -> {
            int id = item.getItemId();
            if (id == R.id.nav_add) {
                MainActivity.this.setTitle("Show & quét QR");
                Fragment currentFragment =
                        MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.main_content);
                if ( currentFragment == null ) {
                    AddDeviceFragment hf =new AddDeviceFragment();
                    MainActivity.this.getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.main_content,hf)
                            .commitNow();

                }
                else {
                    AddDeviceFragment hf =new AddDeviceFragment();
                    MainActivity.this.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.main_content,hf)
                            .commitNow();

                }
            } else if (id == R.id.nav_devices) {
                MainActivity.this.setTitle("Danh sách thiết bị");
                // Handle the messages action
                Fragment currentFragment =
                        MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.main_content);
                if ( currentFragment == null ) {
                    DevicesFragment hf = new DevicesFragment();
                    MainActivity.this.getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.main_content,hf)
                            .commitNow();

                }
                else {
                    DevicesFragment hf = new DevicesFragment();
                    MainActivity.this.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.main_content,hf)
                            .commitNow();

                }
            } else if (id == R.id.nav_lichsu) {
                MainActivity.this.setTitle("Lịch sử");
                Fragment currentFragment =
                        MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.main_content);
                if ( currentFragment == null ) {
                    HistoryFragment hf = new HistoryFragment();
                    MainActivity.this.getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.main_content,hf)
                            .commitNow();

                }
                else {
                    HistoryFragment hf = new HistoryFragment();
                    MainActivity.this.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.main_content,hf)
                            .commitNow();

                }
            }
            else if (id == R.id.nav_sendqr) {
                MainActivity.this.setTitle("Đang gửi");
                Fragment currentFragment =
                        MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.main_content);
                if ( currentFragment == null ) {
                    SendQRFragment hf = new SendQRFragment();
                    MainActivity.this.getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.main_content,hf)
                            .commitNow();
                }
                else {
                    SendQRFragment hf = new SendQRFragment();
                    MainActivity.this.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.main_content,hf)
                            .commitNow();

                }
            }
            else if (id == R.id.nav_openfolder) {
                MainActivity.this.setTitle("Thư mục");
                Fragment currentFragment =
                        MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.main_content);
                if ( currentFragment == null ) {
                    FolderPathFragment hf = new FolderPathFragment();
                    MainActivity.this.getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.main_content,hf)
                            .commitNow();
                }
                else {
                    FolderPathFragment hf = new FolderPathFragment();
                    MainActivity.this.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.main_content,hf)
                            .commitNow();
                }
            }
            drawer.closeDrawers();//   Close the drawer after handling the click
            return true;
        });
        toggle.syncState();
        // Important to get the ActionBar's Up button to work correctly
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            return (Environment.isExternalStorageManager() );
        else return
                ((ContextCompat.checkSelfPermission(
                        this.getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)
                        && (ContextCompat.checkSelfPermission(
                        this.getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)
                );
    }

    private void checkExistsFolder ()  {
        if (checkStoragePermission()) {
                File folder = new File(Environment.getExternalStorageDirectory() + "/BTShare");
                if ( !folder.exists()) folder.mkdir();
        }
 }


    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle
        if (toggle.onOptionsItemSelected(item)) {
            return true;        }
        return super.onOptionsItemSelected(item);
    }

private void showRegisterDialog() {
        if ( BluetoothAdapter.getDefaultAdapter() != null) {
            RegisterDialogFragment rdf = RegisterDialogFragment.newInstance();
            rdf.show(MainActivity.this.getSupportFragmentManager(), "RegisterDialogFragment");
        }
        else {
            Toast.makeText(MainActivity.this,
                "Bluetooth đang tắt,\nvui lòng bật bluetooth và thử lại", Toast.LENGTH_LONG).show();
            MainActivity.this.finish();
        }
}
private boolean checkBluetoothPermission() {
    boolean step1 = true;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
    { step1 = (ActivityCompat.checkSelfPermission(MainActivity.this,
            Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
            ;
    }
    boolean step2 = (ActivityCompat.checkSelfPermission(MainActivity.this,
            Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED);
    return (step1 && step2);
}
    private void requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) // ONLY FOR NEWER VERSIONS
        {
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT
                    },
                    112);
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_ADMIN
                    },
                    120);
        }
    }
    }
    public ProgressBar getPBar() { return pBar;  }
    public void hidePBarLayout() {
        findViewById(R.id.progress_bar_layout).setVisibility(View.INVISIBLE);
    }
    public void revealPBarLayout() {
        findViewById(R.id.progress_bar_layout).setVisibility(View.VISIBLE);
    }

}