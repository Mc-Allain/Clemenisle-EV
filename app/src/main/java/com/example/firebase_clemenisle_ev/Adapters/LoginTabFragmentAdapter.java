package com.example.firebase_clemenisle_ev.Adapters;

import com.example.firebase_clemenisle_ev.Fragments.LoginFragment;
import com.example.firebase_clemenisle_ev.Fragments.RegisterFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class LoginTabFragmentAdapter extends FragmentStateAdapter {

    LoginFragment loginFragment;
    RegisterFragment registerFragment;

    public LoginTabFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle,
                                   LoginFragment loginFragment, RegisterFragment registerFragment) {
        super(fragmentManager, lifecycle);
        this.loginFragment = loginFragment;
        this.registerFragment = registerFragment;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return registerFragment;
        }

        return loginFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
