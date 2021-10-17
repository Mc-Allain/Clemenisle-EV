package com.example.firebase_clemenisle_ev.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.firebase_clemenisle_ev.R;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class BookingListFragment extends Fragment {

    Context myContext;

    boolean isLoggedIn = false;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private void sendSharedPreferences() {
        SharedPreferences sharedPreferences =
                myContext.getSharedPreferences("nav", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("mode", 1);
        editor.apply();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_booking_list, container, false);

        myContext = inflater.getContext();

        initSharedPreferences();
        sendSharedPreferences();

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction;

        if(isLoggedIn) {
            fragmentTransaction = fragmentManager.beginTransaction().
                    replace(R.id.bookingListFragmentContainer, new LoggedInBookingListFragment(), null);
        }
        else {
            fragmentTransaction = fragmentManager.beginTransaction().
                    replace(R.id.bookingListFragmentContainer, new LoginPromptFragment(), null);
        }
        fragmentTransaction.commit();

        return view;
    }
}