package com.example.btshare;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AddDeviceFragment extends Fragment {



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_device, container, false);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        // Create and set up the PagerAdapter
        AddDeviceFragmentStateAdapter fragStateAdapter = new AddDeviceFragmentStateAdapter(
                AddDeviceFragment.this.getChildFragmentManager(), AddDeviceFragment.this.getLifecycle());
        viewPager.setAdapter(fragStateAdapter);

        // Connect the TabLayout with the ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                      switch (position)  {
                          case 0:
                              tab.setText("QR của tôi");
                              break;
                          case 1:
                              tab.setText("Quét QR");
                          default: break;

                      }
        }).attach();
        return view;
    }
}