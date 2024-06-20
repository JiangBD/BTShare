package com.example.btshare;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class AddDeviceDialogFragment extends DialogFragment {
    private String[] mInfo;
    public static AddDeviceDialogFragment newInstance(String[] info) {
        AddDeviceDialogFragment frag = new AddDeviceDialogFragment();
        frag.mInfo = info;
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
        View view = inflater.inflate(R.layout.dialog_adddevice, null);
        TextView tv = view.findViewById(R.id.agreeToAdd);
        tv.setText("Bạn đồng ý thêm thiết bị " + mInfo[1] + " ?" );
        builder.setTitle("Thêm thiết bị mới")
               .setView(view)
               .setPositiveButton("OK", (dialog, id) -> {
                    EnterHFNameDialogFragment hfNameFrag = EnterHFNameDialogFragment.newInstance(mInfo);
                    hfNameFrag.show(AddDeviceDialogFragment.this.getParentFragmentManager(),"HFNameFrag");
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                  dismiss(); // NOT ADDING ANYTHING
                }).setCancelable(false);

        return builder.create();
    }
    @Override
    public void onStart() {
        super.onStart();
        getDialog().setCanceledOnTouchOutside(false);

    }


}
