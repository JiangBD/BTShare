package com.example.btshare;



import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AddDeviceFragmentStateAdapter extends FragmentStateAdapter {


    public AddDeviceFragmentStateAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return the fragment for the corresponding position
        switch (position) {
            case 0:
                return new MyQRFragment();
            case 1:
                return new ScanQRFragment();
            default: return new Fragment();

        }
    }

    @Override
    public int getItemCount() {
        // Return the total number of tabs
        return 2;
    }
}