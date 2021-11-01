package com.example.firebase_clemenisle_ev.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.firebase_clemenisle_ev.ChatListActivity;
import com.example.firebase_clemenisle_ev.R;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class BookingListFragment extends Fragment {

    ImageView chatImage;

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
        chatImage = view.findViewById(R.id.chatImage);

        myContext = inflater.getContext();

        initSharedPreferences();
        sendSharedPreferences();

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction;

        if(isLoggedIn) {
            fragmentTransaction = fragmentManager.beginTransaction().
                    replace(R.id.bookingListFragmentContainer, new LoggedInBookingListFragment(), null);
            chatImage.setVisibility(View.VISIBLE);
        }
        else {
            fragmentTransaction = fragmentManager.beginTransaction().
                    replace(R.id.bookingListFragmentContainer, new LoginPromptFragment(), null);
            chatImage.setVisibility(View.GONE);
        }
        fragmentTransaction.commit();

        chatImage.setOnClickListener(view1 -> {
            Intent intent = new Intent(myContext, ChatListActivity.class);
            startActivity(intent);
        });

        return view;
    }
}