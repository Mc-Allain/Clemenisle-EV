package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class HelpActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ConstraintLayout questionInputLayout, loginQuestionLayout, userQuestionLayout;
    TextView tvUserQuestion2;
    EditText etQuestion;
    ImageView editImage;
    Button submitButton, loginButton;
    ProgressBar progressBar;

    Context myContext;
    Resources myResources;

    String userId;

    boolean isLoggedIn = false;

    String question, questionValue;

    DatabaseReference usersRef;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private void sendLoginPreferences() {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences(
                "login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isLoggedIn", false);
        editor.putBoolean("remember", false);
        editor.putString("emailAddress", null);
        editor.putString("password", null);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        questionInputLayout = findViewById(R.id.questionInputLayout);
        loginQuestionLayout = findViewById(R.id.loginQuestionLayout);
        userQuestionLayout = findViewById(R.id.userQuestionLayout);
        tvUserQuestion2 = findViewById(R.id.tvUserQuestion2);
        etQuestion = findViewById(R.id.etQuestion);
        editImage = findViewById(R.id.editImage);
        submitButton = findViewById(R.id.submitButton);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);

        myContext = HelpActivity.this;
        myResources = myContext.getResources();

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
            else {
                userId = firebaseUser.getUid();
            }
        }

        if(userId != null) {
            loginQuestionLayout.setVisibility(View.GONE);
            questionInputLayout.setVisibility(View.VISIBLE);
            userQuestionLayout.setVisibility(View.VISIBLE);

            usersRef = firebaseDatabase.getReference("users").child(userId);
            getSubmittedQuestion();
        }
        else {
            loginQuestionLayout.setVisibility(View.VISIBLE);
            questionInputLayout.setVisibility(View.GONE);
            userQuestionLayout.setVisibility(View.GONE);
        }

        etQuestion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                question = etQuestion.getText().toString();

                if(question.length() > 0) submitButton.setEnabled(true);
                else submitButton.setEnabled(false);
            }
        });

        submitButton.setOnClickListener(view -> submitQuestion());

        loginButton.setOnClickListener(view -> {
            Intent newIntent = new Intent(myContext, LoginActivity.class);
            startActivity(newIntent);
        });

        editImage.setOnClickListener(view -> {
            questionInputLayout.setVisibility(View.VISIBLE);
            userQuestionLayout.setVisibility(View.GONE);

            etQuestion.setText(questionValue);

            if(question.length() > 0) submitButton.setEnabled(true);
            else submitButton.setEnabled(false);
        });
    }

    private void getSubmittedQuestion() {
        progressBar.setVisibility(View.VISIBLE);

        usersRef.child("question").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    questionInputLayout.setVisibility(View.GONE);
                    userQuestionLayout.setVisibility(View.VISIBLE);

                    questionValue = snapshot.getValue().toString();
                    tvUserQuestion2.setText(questionValue);
                }
                else {
                    questionInputLayout.setVisibility(View.VISIBLE);
                    userQuestionLayout.setVisibility(View.GONE);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void submitQuestion() {
        progressBar.setVisibility(View.VISIBLE);

        usersRef.child("question").setValue(question).addOnCompleteListener(task -> {
            if(!task.isSuccessful()) {
                if(task.getException() != null) {

                    String error = task.getException().toString();
                    Toast.makeText(
                            myContext,
                            error,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
            progressBar.setVisibility(View.GONE);
        });

        questionInputLayout.setVisibility(View.GONE);
        userQuestionLayout.setVisibility(View.VISIBLE);
    }
}