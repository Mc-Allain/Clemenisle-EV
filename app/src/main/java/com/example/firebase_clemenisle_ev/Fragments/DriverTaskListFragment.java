package com.example.firebase_clemenisle_ev.Fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.firebase_clemenisle_ev.R;

import androidx.fragment.app.Fragment;

public class DriverTaskListFragment extends Fragment {

    Context myContext;
    Resources myResources;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_driver_task_list, container, false);

        myContext = getContext();
        myResources = getResources();

        return view;
    }
}