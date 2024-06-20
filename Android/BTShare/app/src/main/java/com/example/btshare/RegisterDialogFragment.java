package com.example.btshare;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class RegisterDialogFragment extends DialogFragment {

   // public interface OnInputListener {
   //     void onInputReceived(String input);
   // }
    //private OnInputListener listener;

    public static RegisterDialogFragment newInstance() {
        return new RegisterDialogFragment();
    }

    // Setter method for the listener
   // public void setOnInputListener(OnInputListener listener) {
   //     this.listener = listener;
   // }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_register, null);
        EditText editText = view.findViewById(R.id.etInputThisDevice);


        builder.setTitle("Nhập địa chỉ Bluetooth")
                .setView(view)
                .setPositiveButton("Ok", (dialog, id) -> {
                    String inputText = editText.getText().toString();
                    if (inputText.equals("")) {
                        Toast.makeText(requireContext(), "Cần nhập địa chỉ Bluetooth của thiết bị này\n" +
                                "mới có thể sử dụng!", Toast.LENGTH_LONG).show();
                        requireActivity().finish();
                        return;
                    }
                    ThisDevice thisDevice = new ThisDevice();
                    thisDevice.setBtAddr(toStandardBtAddr(inputText.toUpperCase()));
                    if (inputText.length() == 12 ) { insertToDB(thisDevice); updateMyQRFragment();  }
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    Toast.makeText(requireContext(), "Cần nhập địa chỉ Bluetooth của thiết bị này\n" +
                            "mới có thể sử dụng!", Toast.LENGTH_LONG).show();
                    requireActivity().finish();
                }).setCancelable(false);

        return builder.create();
    }
    private void updateMyQRFragment() {
        Fragment frag = MyApplication.getMainActivity()
                .getSupportFragmentManager().findFragmentById(R.id.main_content);
        if (frag instanceof  AddDeviceFragment ) {
            AddDeviceFragment hf =new AddDeviceFragment();
            MyApplication.getMainActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_content,hf)
                    .commitNow();
        }
    }
    private String toStandardBtAddr(String input){
        if (input.length() == 12) {
            Toast.makeText(this.requireContext(), input, Toast.LENGTH_LONG).show();
            return  ("" + input.charAt(0) + input.charAt(1) + ":" +
                    input.charAt(2) + input.charAt(3) +  ":"
                    + input.charAt(4) + input.charAt(5) +  ":"
                    + input.charAt(6) + input.charAt(7) +  ":"
                    + input.charAt(8) + input.charAt(9) +  ":"
                    + input.charAt(10) + input.charAt(11)
            );
        }
        else return input;// FALSE FORMAT!!!!!!!!!!
    }
    private void insertToDB(ThisDevice device) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit( () -> {
                DatabaseWorker.registerUser(device);
    });
        executor.shutdown();
    }
    @Override
    public void onStart() {
        super.onStart();
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setCancelable(false);
        /* getDialog().setOnDismissListener( dialogInterface -> {
            if (!hasRegistered()) this.requireActivity().finish();
        }); */

    }
    private boolean hasRegistered() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> f = executor.submit(DatabaseWorker::hasRegistered);
        AtomicBoolean hasRegistered = new AtomicBoolean();
        executor.shutdown();
        try{
            hasRegistered.set(f.get());
        } catch (Exception e) {e.getMessage();}
        return hasRegistered.get();
    }
}
