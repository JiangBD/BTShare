package com.example.btshare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HistoryFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        TabLayout tabLayout = view.findViewById(R.id.historyTabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.historyViewPager);

        // Create and set up the PagerAdapter
        HistoryFragmentStateAdapter fragStateAdapter = new HistoryFragmentStateAdapter(
                HistoryFragment.this.getChildFragmentManager(), HistoryFragment.this.getLifecycle());
        viewPager.setAdapter(fragStateAdapter);

        // Connect the TabLayout with the ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position)  {
                        case 0:
                            tab.setText("Gửi");
                            break;
                        case 1:
                            tab.setText("Nhận");
                        default: break;

                    }
                }).attach();
        return view;
    }
}