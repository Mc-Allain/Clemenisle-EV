package com.example.firebase_clemenisle_ev.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.firebase_clemenisle_ev.LoginActivity;
import com.example.firebase_clemenisle_ev.R;

import androidx.fragment.app.Fragment;

public class LoginPromptFragment extends Fragment {

    TextView fragmentName;
    ImageView fragmentIcon;
    Button loginButton;

    Context myContext;

    int navMode;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences =
                myContext.getSharedPreferences("nav", Context.MODE_PRIVATE);
        navMode = sharedPreferences.getInt("mode", 1);

        String fragmentNameText = "";
        if(navMode == 1) {
            fragmentNameText = "Booking List";
            fragmentIcon.setImageResource(R.drawable.booking_list_rounded_art);
        }
        else if(navMode == 2) {
            fragmentNameText = "User Profile";
            fragmentIcon.setImageResource(R.drawable.user_profile_rounded__art);
        }
        fragmentName.setText(fragmentNameText);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login_prompt, container, false);

        fragmentName = view.findViewById(R.id.tvFragmentName);
        fragmentIcon = view.findViewById(R.id.fragmentIconImage);
        loginButton = view.findViewById(R.id.loginButton);

        myContext = getContext();

        initSharedPreferences();

        loginButton.setOnClickListener(view1 -> {
            Intent intent = new Intent(myContext, LoginActivity.class);
            startActivity(intent);
        });

        return view;
    }
}