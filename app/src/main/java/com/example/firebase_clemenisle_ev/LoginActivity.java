package com.example.firebase_clemenisle_ev;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.example.firebase_clemenisle_ev.Fragments.LoginFragment;
import com.example.firebase_clemenisle_ev.Adapters.LoginTabFragmentAdapter;
import com.example.firebase_clemenisle_ev.Fragments.RegisterFragment;

public class LoginActivity extends AppCompatActivity implements LoginFragment.TabPosInterface {

    ViewPager2 viewPager;
    LoginTabFragmentAdapter tabAdapter;

    LoginFragment loginFragment = new LoginFragment();
    RegisterFragment registerFragment = new RegisterFragment();

    int tabPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewPager = findViewById(R.id.viewPager);

        FragmentManager fragmentManager = getSupportFragmentManager();
        tabAdapter = new LoginTabFragmentAdapter(fragmentManager, getLifecycle(), loginFragment, registerFragment);
        viewPager.setAdapter(tabAdapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                tabPos = position;
            }
        });
    }

    @Override
    public void sendTabPos(int pos) {
        tabPos = pos;
        viewPager.setCurrentItem(tabPos);
    }

    @Override
    public void onBackPressed() {
        if(tabPos == 1) {
            if(registerFragment.currentStep > 1) {
                registerFragment.backPressed();
            }
            else {
                tabPos = 0;
                viewPager.setCurrentItem(tabPos);
            }
        }
        else {
            super.onBackPressed();
        }
    }
}