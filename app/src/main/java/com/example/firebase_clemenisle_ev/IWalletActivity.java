package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.Adapters.IWalletTransactionAdapter;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.IWalletTransaction;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class IWalletActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef = firebaseDatabase.getReference("users");
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    TextView tvIWallet;
    RecyclerView transactionView;

    ImageView reloadImage;
    TextView  tvLog;
    ProgressBar progressBar;

    Context myContext;
    Resources myResources;

    List<IWalletTransaction> transactionList = new ArrayList<>();
    IWalletTransactionAdapter iWalletTransactionAdapter;

    String userId;
    User user;

    boolean isLoggedIn = false;

    String defaultLogText = "No Record";

    ColorStateList cslInitial, cslBlue;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private void sendLoginPreferences() {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences(
                "login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isLoggedIn", false);
        editor.putBoolean("isRemembered", false);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iwallet);

        tvIWallet = findViewById(R.id.tvIWallet);
        transactionView = findViewById(R.id.transactionView);

        reloadImage = findViewById(R.id.reloadImage);
        tvLog = findViewById(R.id.tvLog);
        progressBar = findViewById(R.id.progressBar);

        myContext = IWalletActivity.this;
        myResources = getResources();

        initSharedPreferences();

        firebaseAuth = FirebaseAuth.getInstance();
        if(isLoggedIn) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null) firebaseUser.reload();
            if(firebaseUser == null) {
                firebaseAuth.signOut();
                sendLoginPreferences();

                Toast.makeText(
                        myContext,
                        "Failed to get the current user",
                        Toast.LENGTH_LONG
                ).show();
            }
            else userId = firebaseUser.getUid();
        }

        LinearLayoutManager linearLayout =
                new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        transactionView.setLayoutManager(linearLayout);
        iWalletTransactionAdapter = new IWalletTransactionAdapter(myContext, transactionList);
        transactionView.setAdapter(iWalletTransactionAdapter);

        getTransactionList();
    }

    private void getTransactionList() {
        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        transactionView.setVisibility(View.INVISIBLE);

        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                if(snapshot.exists()) {
                    user = new User(snapshot);
                    transactionList.addAll(user.getTransactionList());
                }

                if(transactionList.size() > 0) finishLoading();
                else errorLoading(defaultLogText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                errorLoading(error.toString());
            }
        });
    }

    private void finishLoading() {
        iWalletTransactionAdapter.notifyDataSetChanged();

        String iWallet = "₱" + user.getIWallet();
        if(iWallet.split("\\.")[1].length() == 1) iWallet += 0;

        tvIWallet.setText(iWallet);

        progressBar.setVisibility(View.GONE);
        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        transactionView.setVisibility(View.VISIBLE);
    }

    private void errorLoading(String error) {
        transactionList.clear();
        iWalletTransactionAdapter.notifyDataSetChanged();

        tvLog.setText(error);
        tvLog.setVisibility(View.VISIBLE);
        reloadImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        transactionView.setVisibility(View.INVISIBLE);
    }
}