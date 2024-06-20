package com.example.btshare;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class EnterHFNameDialogFragment extends DialogFragment {
    private String[] mInfo;

    public static EnterHFNameDialogFragment newInstance(String[] a) {
        EnterHFNameDialogFragment frag = new EnterHFNameDialogFragment();
        frag.mInfo = a;
          return frag;
    }

    // Setter method for the listener
   // public void setOnInputListener(OnInputListener listener) {
   //     this.listener = listener;
   // }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_enterhfname, null);
        builder.setTitle("Nhập tên gợi nhớ");
        EditText ed = view.findViewById(R.id.etInputHFName);
        String suggesttedName = suggestName();

        ed.setText(suggesttedName);
        builder.setView(view)
                .setPositiveButton("OK", (dialog, id) -> {
                    if (!isValidName(ed.getText().toString())) ed.setText(suggesttedName);
                    insertToDB(ed.getText().toString());
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    //listener.onHFNameReceived(suggestName());
                  //  insertToDB(suggestName());
                    dismiss();
                }).setCancelable(false);

        return builder.create();
    }
    private boolean isValidName(String name)
    {
        if (name.toLowerCase().contains("delete")) return false;
        if (name.toLowerCase().contains("select")) return false;
        if (name.contains("*") || name.contains("=") || name.contains("-") ) return false;
        if(name.equals((""))) return false;
        return true;
    }
    private String suggestName() {
        AtomicInteger aInt = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(1);
        new Thread ( () -> {
            aInt.set(DatabaseWorker.getMaxDeviceNumber());
            latch.countDown();}).start();
        try {latch.wait(); } catch (Exception e) {
            Log.e("TAG",e.getMessage()); }

        return "Thiết bị " + ( aInt.get() + 1) ;
    }
    private void insertToDB(String temphfname) {
        BTDevice device = new BTDevice();
        String id = ""; AtomicInteger aInt = new AtomicInteger(); CountDownLatch latch = new CountDownLatch(1);
        new Thread ( () -> {
        aInt.set(DatabaseWorker.getMaxDeviceNumber()); latch.countDown();}).start();
        try { latch.wait(); } catch (Exception e) {}

        int devicenumber = 0 ;
        devicenumber = aInt.get() + 1;
        if (devicenumber < 10) id = id + "000" + devicenumber;
        else if (devicenumber < 100) id = id + "00" + devicenumber;
        else if (devicenumber < 1000) id = id + "0" + devicenumber;
        device.setDeviceID(id);
        device.setBtAddr(mInfo[1]);
        try {
            device.setCores(Integer.parseInt(mInfo[2]));
            device.setHfName(temphfname);
        }
        catch (NumberFormatException nfn) {
            return;
        }
        new Thread ( () -> DatabaseWorker.insertNewDevice(device) ).start();

    }


    @Override
    public void onStart() {
        super.onStart();
       getDialog().setCanceledOnTouchOutside(false);
       getDialog().setCancelable(false);

    }
}
