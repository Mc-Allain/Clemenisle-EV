package com.example.firebase_clemenisle_ev.Adapters;

import android.os.Bundle;

import com.example.firebase_clemenisle_ev.Fragments.MapSettingsFragment;
import com.example.firebase_clemenisle_ev.Fragments.MapStationFragment;
import com.example.firebase_clemenisle_ev.Fragments.MapTouristSpotFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MapTabFragmentAdapter extends FragmentStateAdapter {

    Fragment mapTouristSpotFragment = new MapTouristSpotFragment();
    Fragment mapStationFragment = new MapStationFragment();
    Fragment mapSettingsFragment = new MapSettingsFragment();

    public MapTabFragmentAdapter(@NonNull FragmentManager fragmentManager,
                                 @NonNull Lifecycle lifecycle, String id, int type, String name) {
        super(fragmentManager, lifecycle);

        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putString("name", name);

        if(type == 0) mapTouristSpotFragment.setArguments(bundle);
        else if(type == 1) mapStationFragment.setArguments(bundle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return mapStationFragment;
            case 2:
                return mapSettingsFragment;
        }

        return mapTouristSpotFragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
