package com.example.firebase_clemenisle_ev;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.example.firebase_clemenisle_ev.Adapters.CommentAdapter;
import com.example.firebase_clemenisle_ev.Adapters.NearSpotAdapter;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.Comment;
import com.example.firebase_clemenisle_ev.Classes.DateTimeDifference;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
import com.example.firebase_clemenisle_ev.Classes.DetailedTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.OtherComment;
import com.example.firebase_clemenisle_ev.Classes.Route;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.Station;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SelectedSpotActivity extends AppCompatActivity implements CommentAdapter.OnActionButtonClicked {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ConstraintLayout constraintLayout, headerLayout;

    ImageView thumbnail, likeImage, visitImage, bookImage, commentImage,
            moreImage, i360Image, locateImage, homeImage, reloadImage;
    TextView tvName, tvStation, tvLikes, tvVisits, tvBooks, tvComments,
            tvNearSpot, tv360Image, tvLocate, tvTimestamp, tvLog;
    ExpandableTextView extvDescription;
    ConstraintLayout  backgroundLayout, buttonLayout, connectingLayout;
    ScrollView scrollView;
    RecyclerView nearSpotView;
    ProgressBar progressBar;

    ConstraintLayout commentTitleLayout, commentLayout, commentBackgroundLayout;
    TextView tvCommentSpot;

    ConstraintLayout loginCommentLayout;
    Button loginButton;

    ConstraintLayout commentInputLayout;
    EditText etComment;
    ImageView sendImage, commentArrowImage;

    ConstraintLayout userCommentLayout, badgeLayout;
    TextView tvUserFullName, tvCommentStatus;
    ExpandableTextView extvComment;
    ImageView profileImage, editImage, appealImage, deactivateImage;
    ImageView developerImage, adminImage, driverImage, likerImage, ownerImage;

    RecyclerView commentView;
    ProgressBar commentProgressBar;

    Context myContext;
    Resources myResources;

    int colorBlue, colorInitial, colorRed;

    String userId;

    boolean isLoggedIn = false, toComment = false;

    String id, name, description, img;
    int likes, visits, books, comments;
    double lat, lng;
    List<SimpleTouristSpot> nearSpots = new ArrayList<>();
    List<Station> nearStations;
    boolean deactivated;
    StringBuilder stations;

    NearSpotAdapter nearSpotAdapter;

    User user;
    List<SimpleTouristSpot> likedSpots = new ArrayList<>();
    SimpleTouristSpot selectedSpot;

    boolean isOnScreen = false;
    boolean isLiked, isOptionShown;

    DatabaseReference usersRef, likedSpotsRef, commentsRef,
            upVotedCommentsRef, downVotedCommentsRef, reportedCommentsRef;

    boolean isCommentShown = false, commentShowingAnimation, defaultValueForAnimation = false;

    CommentAdapter commentAdapter;
    List<User> users = new ArrayList<>(), commentedUsers = new ArrayList<>(),
            foulCommentedUsers = new ArrayList<>();

    String deactivateText = "Deactivate Comment";
    String activateText = "Activate Comment";
    String currentDeactivateText = deactivateText;

    Handler optionHandler = new Handler();
    Runnable optionRunnable;

    String defaultLogText = "No Comment\nPlease make yours the first one";

    Comment currentUserComment;

    String inputComment, commentValue;
    boolean isUserCommentExist = true;

    String defaultStatusText = "Foul comment", appealedText = "(Appealed)",
            notActiveText = "This comment is not active";

    long deactivatePressedTime;
    Toast deactivateToast;
    boolean isDeactivateClicked = false;

    long reportPressedTime;
    Toast reportToast;
    boolean isReportClicked = false;

    boolean isUpdatingComments = false;

    Toast errorToast;

    Dialog confirmationDialog;
    ImageView confirmationDialogCloseImage;
    TextView tvDialogTitleConfirmation, tvDialogCaptionConfirmation;
    Button confirmationDialogConfirmButton, confirmationDialogCancelButton;
    ProgressBar confirmationDialogProgressBar;

    boolean isConfirmationDialogEnabled;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        isConfirmationDialogEnabled = sharedPreferences.getBoolean("isConfirmationDialogEnabled", true);
    }

    private void sendLoginPreferences() {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences(
                "login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isLoggedIn", false);
        editor.putBoolean("isRemembered", false);
        editor.apply();

        NotificationManager notificationManager = (NotificationManager) myContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_spot);

        constraintLayout = findViewById(R.id.constraintLayout);
        headerLayout = findViewById(R.id.headerLayout);

        scrollView = findViewById(R.id.scrollView);
        thumbnail = findViewById(R.id.thumbnail);
        tvName = findViewById(R.id.tvName);
        tvStation = findViewById(R.id.tvStartStation2);
        extvDescription = findViewById(R.id.extvDescription);
        likeImage = findViewById(R.id.likeImage);
        visitImage = findViewById(R.id.visitImage);
        bookImage = findViewById(R.id.bookImage);
        commentImage = findViewById(R.id.commentImage);
        tvLikes = findViewById(R.id.tvLikes);
        tvVisits = findViewById(R.id.tvVisits);
        tvBooks = findViewById(R.id.tvBooks);
        tvComments = findViewById(R.id.tvComments);

        moreImage = findViewById(R.id.moreImage);
        backgroundLayout = findViewById(R.id.backgroundLayout);
        buttonLayout = findViewById(R.id.buttonLayout);
        tvNearSpot = findViewById(R.id.tvNearSpot);
        nearSpotView = findViewById(R.id.nearSpotView);
        i360Image = findViewById(R.id.i360Image);
        locateImage = findViewById(R.id.locateImage);
        tv360Image = findViewById(R.id.tv360Image);
        tvLocate = findViewById(R.id.tvLocate);
        connectingLayout = findViewById(R.id.connectingLayout);
        homeImage = findViewById(R.id.homeImage);
        progressBar = findViewById(R.id.progressBar);

        commentTitleLayout = findViewById(R.id.commentTitleLayout);
        commentBackgroundLayout = findViewById(R.id.commentBackgroundLayout);
        commentLayout = findViewById(R.id.commentLayout);
        commentArrowImage = findViewById(R.id.commentArrowImage);
        tvCommentSpot = findViewById(R.id.tvCommentSpot);

        loginCommentLayout = findViewById(R.id.loginCommentLayout);
        loginButton = findViewById(R.id.loginButton);

        commentInputLayout = findViewById(R.id.commentInputLayout);
        etComment = findViewById(R.id.etComment);
        sendImage = findViewById(R.id.sendImage);

        userCommentLayout = findViewById(R.id.userCommentLayout);
        tvUserFullName = findViewById(R.id.tvUserFullName);
        tvTimestamp = findViewById(R.id.tvTimestamp);
        tvCommentStatus = findViewById(R.id.tvCommentStatus);
        extvComment = findViewById(R.id.extvComment);
        profileImage = findViewById(R.id.profileImage);
        editImage = findViewById(R.id.editImage);
        appealImage = findViewById(R.id.appealImage);
        deactivateImage = findViewById(R.id.deactivateImage);

        badgeLayout = findViewById(R.id.badgeLayout);
        ownerImage = findViewById(R.id.ownerImage);
        developerImage = findViewById(R.id.developerImage);
        adminImage = findViewById(R.id.adminImage);
        driverImage = findViewById(R.id.driverImage);
        likerImage = findViewById(R.id.likerImage);

        commentView = findViewById(R.id.commentView);
        commentProgressBar = findViewById(R.id.commentProgressBar);

        tvLog = findViewById(R.id.tvLog);
        reloadImage = findViewById(R.id.reloadImage);

        myContext = SelectedSpotActivity.this;
        myResources = myContext.getResources();

        initSharedPreferences();
        if(isConfirmationDialogEnabled) initConfirmationDialog();

        colorBlue = myResources.getColor(R.color.blue);
        colorInitial = myResources.getColor(R.color.initial);
        colorRed = myResources.getColor(R.color.red);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        isLoggedIn = intent.getBooleanExtra("isLoggedIn", false);
        toComment = intent.getBooleanExtra("toComment", false);

        isOnScreen = true;
        commentShowingAnimation = defaultValueForAnimation;

        sendImage.setEnabled(false);
        sendImage.getDrawable().setTint(colorInitial);

        firebaseAuth = FirebaseAuth.getInstance();
        if(isLoggedIn) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null) firebaseUser.reload();
            if(firebaseUser == null) {
                firebaseAuth.signOut();
                sendLoginPreferences();

                Toast.makeText(
                        myContext,
                        "Failed to get the current user. Account logged out.",
                        Toast.LENGTH_LONG
                ).show();
            }
            else userId = firebaseUser.getUid();
        }

        try {
            Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage);
        }
        catch (Exception ignored) {}

        usersRef = firebaseDatabase.getReference("users");
        if(userId != null) {
            likedSpotsRef = usersRef.child(userId).child("likedSpots");
            commentsRef = usersRef.child(userId).child("comments");
            upVotedCommentsRef = usersRef.child(userId).child("upVotedComments");
            downVotedCommentsRef = usersRef.child(userId).child("downVotedComments");
            reportedCommentsRef = usersRef.child(userId).child("reportedComments");
        }

        LinearLayoutManager linearLayout =
                new LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false);
        nearSpotView.setLayoutManager(linearLayout);
        nearSpotAdapter = new NearSpotAdapter(myContext, nearSpots, isLoggedIn);
        nearSpotView.setAdapter(nearSpotAdapter);

        LinearLayoutManager linearLayout2 =
                new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        commentView.setLayoutManager(linearLayout2);
        commentAdapter = new CommentAdapter(myContext, users, commentedUsers, id, userId);
        commentView.setAdapter(commentAdapter);
        commentAdapter.setOnActionButtonClickedListener(this);

        if(toComment) backgroundLayout.setVisibility(View.GONE);

        getTouristSpots();
        getUsers();

        if(userId != null) {
            getLikedSpots();
            loginCommentLayout.setVisibility(View.GONE);
        }
        else {
            loginCommentLayout.setVisibility(View.VISIBLE);
            commentInputLayout.setVisibility(View.GONE);
            userCommentLayout.setVisibility(View.GONE);
        }

        likeImage.setOnClickListener(view -> {
            if(isLoggedIn) {
                if(firebaseUser != null) {
                    likeImage.setEnabled(false);
                    if(!isLiked) likeSpot(selectedSpot);
                    else unlikeSpot();
                }
            }
            else {
                Intent newIntent = new Intent(myContext, LoginActivity.class);
                myContext.startActivity(newIntent);
            }
        });

        likeImage.setOnLongClickListener(view -> {
            Toast.makeText(myContext,
                    "Likes: " + tvLikes.getText(),
                    Toast.LENGTH_SHORT).show();
            return false;
        });

        visitImage.setOnLongClickListener(view -> {
            Toast.makeText(myContext,
                    "Visits: " + tvVisits.getText(),
                    Toast.LENGTH_SHORT).show();
            return false;
        });

        bookImage.setOnLongClickListener(view -> {
            Toast.makeText(myContext,
                    "Books: " + tvBooks.getText(),
                    Toast.LENGTH_SHORT).show();
            return false;
        });

        commentImage.setOnClickListener(view -> showCommentLayout());

        commentImage.setOnLongClickListener(view -> {
            Toast.makeText(myContext,
                    "Comments: " + tvComments.getText(),
                    Toast.LENGTH_SHORT).show();
            return false;
        });

        moreImage.setOnClickListener(view -> {
            if(!isOptionShown) {
                openOption();
            }
            else {
                closeOption();
            }
        });

        homeImage.setOnClickListener(view -> backToHome());

        thumbnail.setOnClickListener(view -> openStreetView(id));

        tv360Image.setOnClickListener(view -> openStreetView(id));

        i360Image.setOnClickListener(view -> openStreetView(id));

        tvLocate.setOnClickListener(view -> openMap(id, lat, lng, name, 0));

        locateImage.setOnClickListener(view -> openMap(id, lat, lng, name, 0));

        etComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                inputComment = etComment.getText().toString().trim();

                if(inputComment.length() > 0) {
                    sendImage.setEnabled(true);
                    sendImage.getDrawable().setTint(colorBlue);
                }
                else {
                    sendImage.setEnabled(false);
                    sendImage.getDrawable().setTint(colorInitial);
                }
            }
        });

        commentTitleLayout.setOnClickListener(view -> showCommentLayout());

        loginButton.setOnClickListener(view -> {
            Intent newIntent = new Intent(myContext, LoginActivity.class);
            startActivity(newIntent);
        });

        sendImage.setOnClickListener(view -> {
            if(isUserCommentExist) updateComment();
            else setComment();
        });

        editImage.setOnLongClickListener(view -> {
            editImageOnLongClick();
            return false;
        });

        appealImage.setOnLongClickListener(view -> {
            appealImageOnLongClick();
            return false;
        });

        deactivateImage.setOnLongClickListener(view -> {
            deactivateImageOnLongClick();
            return false;
        });

        editImage.setOnClickListener(view -> editImageOnClick());

        appealImage.setOnClickListener(view -> appealImageOnClick());

        deactivateImage.setOnClickListener(view -> deactivateImageOnClick());
    }

    private void openConfirmationDialog(String title, String caption) {
        tvDialogTitleConfirmation.setText(title);
        tvDialogCaptionConfirmation.setText(caption);
        confirmationDialog.show();
    }

    private void initConfirmationDialog() {
        confirmationDialog = new Dialog(myContext);
        confirmationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        confirmationDialog.setContentView(R.layout.dialog_confirmation_layout);

        confirmationDialogCloseImage = confirmationDialog.findViewById(R.id.dialogCloseImage);
        tvDialogTitleConfirmation = confirmationDialog.findViewById(R.id.tvDialogTitle);
        tvDialogCaptionConfirmation = confirmationDialog.findViewById(R.id.tvDialogCaption);
        confirmationDialogConfirmButton = confirmationDialog.findViewById(R.id.confirmButton);
        confirmationDialogCancelButton = confirmationDialog.findViewById(R.id.cancelButton);
        confirmationDialogProgressBar = confirmationDialog.findViewById(R.id.dialogProgressBar);

        confirmationDialogCloseImage.setOnClickListener(view -> confirmationDialog.dismiss());

        confirmationDialogCancelButton.setOnClickListener(view -> confirmationDialog.dismiss());

        confirmationDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        confirmationDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
        confirmationDialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        confirmationDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void setConfirmationDialogScreenEnabled(boolean value) {
        confirmationDialog.setCanceledOnTouchOutside(value);
        confirmationDialog.setCancelable(value);
        confirmationDialogConfirmButton.setEnabled(value);
        confirmationDialogCancelButton.setEnabled(value);

        if(value) confirmationDialogCloseImage.getDrawable().setTint(colorRed);
        else confirmationDialogCloseImage.getDrawable().setTint(colorInitial);
    }

    private void showCommentLayout() {
        commentTitleLayout.setEnabled(false);

        if(!isCommentShown) {
            showCommentLayout2();
            commentArrowImage.setImageResource(R.drawable.ic_baseline_expand_more_24);
        }
        else {
            showCommentLayout4();
            commentArrowImage.setImageResource(R.drawable.ic_baseline_expand_less_24);
        }
    }

    private void showCommentLayout2() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        if(!isCommentShown) {
            constraintSet.clear(scrollView.getId(), ConstraintSet.TOP);
            constraintSet.clear(scrollView.getId(), ConstraintSet.BOTTOM);
            constraintSet.connect(scrollView.getId(), ConstraintSet.BOTTOM,
                    headerLayout.getId(), ConstraintSet.BOTTOM);
        }
        else {
            constraintSet.clear(scrollView.getId(), ConstraintSet.BOTTOM);
            constraintSet.connect(scrollView.getId(), ConstraintSet.TOP,
                    headerLayout.getId(), ConstraintSet.BOTTOM);
            constraintSet.connect(scrollView.getId(), ConstraintSet.BOTTOM,
                    commentTitleLayout.getId(), ConstraintSet.TOP);
        }

        if(commentShowingAnimation) setTransition1();
        constraintSet.applyTo(constraintLayout);

        if(!commentShowingAnimation) {
            if(!isCommentShown) showCommentLayout3();
            else {
                commentTitleLayout.setEnabled(true);
                isCommentShown = !isCommentShown;
            }
        }
    }

    private void showCommentLayout3() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        if(!isCommentShown) {
            constraintSet.clear(commentLayout.getId(), ConstraintSet.TOP);
            constraintSet.connect(commentLayout.getId(), ConstraintSet.TOP,
                    constraintLayout.getId(), ConstraintSet.BOTTOM);

            constraintSet.clear(commentTitleLayout.getId(), ConstraintSet.BOTTOM);
            constraintSet.connect(commentTitleLayout.getId(), ConstraintSet.TOP,
                    headerLayout.getId(), ConstraintSet.BOTTOM);
        }
        else {
            constraintSet.clear(commentTitleLayout.getId(), ConstraintSet.TOP);
            constraintSet.connect(commentTitleLayout.getId(), ConstraintSet.BOTTOM,
                    constraintLayout.getId(), ConstraintSet.BOTTOM);

            constraintSet.clear(commentLayout.getId(), ConstraintSet.TOP);
            constraintSet.connect(commentLayout.getId(), ConstraintSet.TOP,
                    commentTitleLayout.getId(), ConstraintSet.BOTTOM);
        }

        if(commentShowingAnimation) setTransition2();
        constraintSet.applyTo(constraintLayout);

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) commentTitleLayout.getLayoutParams();
        ConstraintLayout.LayoutParams layoutParams2 =
                (ConstraintLayout.LayoutParams) commentLayout.getLayoutParams();

        if(!isCommentShown) {
            layoutParams.setMargins(layoutParams.leftMargin, dpToPx(8),
                    layoutParams.rightMargin, layoutParams.bottomMargin);
        }
        else {
            layoutParams2.setMargins(layoutParams.leftMargin, dpToPx(8),
                    layoutParams.rightMargin, layoutParams.bottomMargin);
        }

        commentTitleLayout.setLayoutParams(layoutParams);
        commentLayout.setLayoutParams(layoutParams2);

        if(!commentShowingAnimation) {
            if(!isCommentShown) showCommentLayout4();
            else showCommentLayout2();
        }
    }

    private void showCommentLayout4() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        if(!isCommentShown) {
            constraintSet.connect(commentLayout.getId(), ConstraintSet.TOP,
                    commentTitleLayout.getId(), ConstraintSet.BOTTOM);
            constraintSet.connect(commentLayout.getId(), ConstraintSet.BOTTOM,
                    constraintLayout.getId(), ConstraintSet.BOTTOM);
        }
        else {
            constraintSet.clear(commentLayout.getId(), ConstraintSet.TOP);
            constraintSet.clear(commentLayout.getId(), ConstraintSet.BOTTOM);
            constraintSet.connect(commentLayout.getId(), ConstraintSet.TOP,
                    constraintLayout.getId(), ConstraintSet.BOTTOM);
        }

        if(commentShowingAnimation) setTransition3();
        constraintSet.applyTo(constraintLayout);

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) commentLayout.getLayoutParams();

        if(!isCommentShown) {
            layoutParams.setMargins(layoutParams.leftMargin, dpToPx(8),
                    layoutParams.rightMargin, dpToPx(8));
        }

        commentLayout.setLayoutParams(layoutParams);

        if(!commentShowingAnimation) {
            if(!isCommentShown) {
                commentTitleLayout.setEnabled(true);
                isCommentShown = !isCommentShown;

                if(toComment) {
                    toComment = false;
                    backgroundLayout.setVisibility(View.VISIBLE);
                }
            }
            else showCommentLayout3();
        }
    }

    private void setTransition1() {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);

        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                if(!isCommentShown) showCommentLayout3();
                else {
                    commentTitleLayout.setEnabled(true);
                    etComment.clearFocus();
                    isCommentShown = !isCommentShown;
                }
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });

        TransitionManager.beginDelayedTransition(scrollView, transition);
    }

    private void setTransition2() {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);

        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                if(!isCommentShown) showCommentLayout4();
                else showCommentLayout2();
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });

        TransitionManager.beginDelayedTransition(commentTitleLayout, transition);
    }

    private void setTransition3() {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);

        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                if(!isCommentShown) {
                    commentTitleLayout.setEnabled(true);
                    etComment.clearFocus();
                    isCommentShown = !isCommentShown;
                }
                else showCommentLayout3();
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });

        TransitionManager.beginDelayedTransition(commentLayout, transition);
    }

    private int dpToPx(int dp) {
        float px = dp * myContext.getResources().getDisplayMetrics().density;
        return (int) px;
    }

    @Override
    public void editImageOnLongClick() {
        Toast.makeText(myContext,
                "Edit Comment",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void appealImageOnLongClick() {
        Toast.makeText(myContext,
                "Appeal Comment",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void upVoteImageOnLongClick() {
        Toast.makeText(myContext,
                "Up Vote Comment",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void downVoteImageOnLongClick() {
        Toast.makeText(myContext,
                "Down Vote Comment",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void deactivateImageOnLongClick() {
        Toast.makeText(myContext,
                currentDeactivateText,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void reportImageOnLongClick() {
        Toast.makeText(myContext,
                "Report Comment",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void editImageOnClick() {
        etComment.setText(commentValue);
        etComment.requestFocus();

        commentInputLayout.setVisibility(View.VISIBLE);
        userCommentLayout.setVisibility(View.GONE);
    }

    private void setCommentOnScreenEnabled(boolean value) {
        deactivateImage.setEnabled(value);
        appealImage.setEnabled(value);
        editImage.setEnabled(value);

        if(!value) {
            deactivateImage.getDrawable().setTint(colorInitial);
            appealImage.getDrawable().setTint(colorInitial);
            editImage.getDrawable().setTint(colorInitial);
        }
    }

    @Override
    public void appealImageOnClick() {
        if(!isUpdatingComments) {
            isUpdatingComments = true;
            setCommentOnScreenEnabled(false);
            commentProgressBar.setVisibility(View.VISIBLE);

            currentUserComment.setAppealed(true);

            commentsRef.child(id).child("appealed").setValue(true)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            isUpdatingComments = false;
                            commentAdapter.notifyDataSetChanged();

                            String toastMessage = "Your comment is now in appeal";

                            Toast.makeText(
                                    myContext,
                                    toastMessage,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                        else {
                            if(task.getException() != null) {
                                String error = task.getException().toString();
                                Toast.makeText(
                                        myContext,
                                        error,
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                            isUpdatingComments = false;
                            updateCommentUI(currentUserComment);
                        }

                        if(isConfirmationDialogEnabled) {
                            confirmationDialogProgressBar.setVisibility(View.GONE);
                            setConfirmationDialogScreenEnabled(true);
                            if(task.isSuccessful()) confirmationDialog.dismiss();
                        }
                        else commentProgressBar.setVisibility(View.GONE);
                    });
        }
    }

    @Override
    public void deactivateImageOnClick() {
        if(isConfirmationDialogEnabled) {
            String activationMessage = "Deactivate";
            if(currentDeactivateText.equals(activateText)) activationMessage = "Activate";

            openConfirmationDialog(activationMessage + " Comment", "Do you want to " + activationMessage.toLowerCase() + " your comment?");
            confirmationDialogConfirmButton.setOnClickListener(view -> setDeactivatedComment());
        }
        else {
            if(deactivatePressedTime + 2500 > System.currentTimeMillis() && isDeactivateClicked) {
                deactivateToast.cancel();

                setDeactivatedComment();

                isDeactivateClicked = false;
            }
            else {
                String toastMessage = "Press again to ";
                if(currentDeactivateText.equals(deactivateText)) toastMessage += "deactivate";
                else if(currentDeactivateText.equals(activateText)) toastMessage += "activate";
                toastMessage += " your comment";

                deactivateToast = Toast.makeText(myContext, toastMessage, Toast.LENGTH_SHORT);
                deactivateToast.show();

                isDeactivateClicked = true;
            }
            deactivatePressedTime = System.currentTimeMillis();
        }
    }

    private void setDeactivatedComment() {
        if(!isUpdatingComments) {
            if(isConfirmationDialogEnabled) {
                confirmationDialogProgressBar.setVisibility(View.VISIBLE);
                setConfirmationDialogScreenEnabled(false);
            }
            else commentProgressBar.setVisibility(View.VISIBLE);

            isUpdatingComments = true;
            setCommentOnScreenEnabled(false);

            boolean value = false;
            if(currentDeactivateText.equals(deactivateText)) value = true;
            currentUserComment.setDeactivated(value);

            commentsRef.child(id).child("deactivated").setValue(value)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            isUpdatingComments = false;
                            commentAdapter.notifyDataSetChanged();

                            String toastMessage = "Your comment is now ";
                            if(currentDeactivateText.equals(deactivateText)) toastMessage += "activated";
                            else if(currentDeactivateText.equals(activateText)) toastMessage += "deactivated";

                            Toast.makeText(
                                    myContext,
                                    toastMessage,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                        else {
                            if(task.getException() != null) {
                                String error = task.getException().toString();
                                Toast.makeText(
                                        myContext,
                                        error,
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                            isUpdatingComments = false;
                            updateCommentUI(currentUserComment);
                        }

                        if(isConfirmationDialogEnabled) {
                            confirmationDialogProgressBar.setVisibility(View.GONE);
                            setConfirmationDialogScreenEnabled(true);
                            if(task.isSuccessful()) confirmationDialog.dismiss();
                        }
                        else commentProgressBar.setVisibility(View.GONE);
                    });
        }
    }

    @Override
    public void reportImageOnClick(String spotId, String senderUserId, String timestamp) {
        if(isConfirmationDialogEnabled) {
            openConfirmationDialog("Report Comment", "Do you want to report the comment?");
            confirmationDialogConfirmButton.setOnClickListener(view -> setReportedComment(spotId, senderUserId, timestamp));
        }
        else {
            if(reportPressedTime + 2500 > System.currentTimeMillis() && isReportClicked) {
                reportToast.cancel();

                setReportedComment(spotId, senderUserId, timestamp);

                isReportClicked = false;
            }
            else {
                String toastMessage = "Press again to report the comment";

                reportToast = Toast.makeText(myContext, toastMessage, Toast.LENGTH_SHORT);
                reportToast.show();

                isReportClicked = true;
            }
            reportPressedTime = System.currentTimeMillis();
        }
    }

    private void setReportedComment(String spotId, String senderUserId, String timestamp) {
        if(!isUpdatingComments) {
            if(isConfirmationDialogEnabled) {
                confirmationDialogProgressBar.setVisibility(View.VISIBLE);
                setConfirmationDialogScreenEnabled(false);
            }
            else commentProgressBar.setVisibility(View.VISIBLE);

            setCommentOnScreenEnabled(false);
            OtherComment otherComment = new OtherComment(senderUserId, spotId);

            upVotedCommentsRef.child(spotId).child(senderUserId).removeValue();
            downVotedCommentsRef.child(spotId).child(senderUserId).setValue(otherComment);
            reportedCommentsRef.child(spotId).child(senderUserId).setValue(otherComment).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    String toastMessage = "The comment is now reported";

                    Toast.makeText(
                            myContext,
                            toastMessage,
                            Toast.LENGTH_SHORT
                    ).show();

                    confirmationDialog.dismiss();
                }
                else {
                    if(task.getException() != null) {
                        String error = task.getException().toString();
                        Toast.makeText(
                                myContext,
                                error,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
                updateCommentUI(currentUserComment);

                if(isConfirmationDialogEnabled) {
                    confirmationDialogProgressBar.setVisibility(View.GONE);
                    setConfirmationDialogScreenEnabled(true);
                }
                else commentProgressBar.setVisibility(View.GONE);
            });
        }
    }

    @Override
    public void upVoteImageOnClick(String spotId, String senderUserId, String timestamp,
                                   boolean isUpVoted, boolean isDownVoted) {
        setCommentOnScreenEnabled(false);
        commentProgressBar.setVisibility(View.VISIBLE);
        OtherComment otherComment = new OtherComment(senderUserId, spotId);

        if(isUpVoted) upVotedCommentsRef.child(spotId).child(senderUserId).removeValue()
                .addOnCompleteListener(task -> {
                    updateCommentUI(currentUserComment);
                    commentProgressBar.setVisibility(View.GONE);
                });
        else if(isDownVoted) downVotedCommentsRef.child(spotId).child(senderUserId).removeValue();
        if(!isUpVoted) upVotedCommentsRef.child(spotId).child(senderUserId).setValue(otherComment)
                .addOnCompleteListener(task -> {
                    updateCommentUI(currentUserComment);
                    commentProgressBar.setVisibility(View.GONE);
                });
    }

    @Override
    public void downVoteImageOnClick(String spotId, String senderUserId, String timestamp,
                                     boolean isUpVoted, boolean isDownVoted) {
        setCommentOnScreenEnabled(false);
        commentProgressBar.setVisibility(View.VISIBLE);
        OtherComment otherComment = new OtherComment(senderUserId, spotId);

        if(isUpVoted) upVotedCommentsRef.child(spotId).child(senderUserId).removeValue();
        if(isDownVoted) downVotedCommentsRef.child(spotId).child(senderUserId).removeValue()
                .addOnCompleteListener(task -> {
                    updateCommentUI(currentUserComment);
                    commentProgressBar.setVisibility(View.GONE);
                });
        if(!isDownVoted) downVotedCommentsRef.child(spotId).child(senderUserId).setValue(otherComment)
                .addOnCompleteListener(task -> {
                    updateCommentUI(currentUserComment);
                    commentProgressBar.setVisibility(View.GONE);
                });
    }

    private void updateComment() {
        if(!isUpdatingComments) {
            isUpdatingComments = true;
            setCommentOnScreenEnabled(false);
            commentProgressBar.setVisibility(View.VISIBLE);

            currentUserComment.setValue(inputComment);

            commentsRef.child(id).child("value").setValue(inputComment)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            isUpdatingComments = false;
                            commentAdapter.notifyDataSetChanged();
                            commentProgressBar.setVisibility(View.GONE);
                        }
                        else {
                            if(task.getException() != null) {
                                String error = task.getException().toString();
                                Toast.makeText(
                                        myContext,
                                        error,
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                            isUpdatingComments = false;
                            updateCommentUI(currentUserComment);
                            commentProgressBar.setVisibility(View.GONE);
                        }
                    });

            updateCommentUI(currentUserComment);
            commentInputLayout.setVisibility(View.GONE);
            userCommentLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setComment() {
        commentProgressBar.setVisibility(View.VISIBLE);
        Comment comment = new Comment(id, inputComment, new DateTimeToString().getDateAndTime());
        commentsRef.child(id).setValue(comment)
                .addOnCompleteListener(task -> commentProgressBar.setVisibility(View.GONE));
    }

    private void checkCurrentUserComment() {
        setCommentOnScreenEnabled(false);
        commentsRef.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    currentUserComment = comment;

                    isUserCommentExist = true;
                    commentInputLayout.setVisibility(View.GONE);
                    userCommentLayout.setVisibility(View.VISIBLE);

                    updateCommentUI(comment);
                }
                else {
                    isUserCommentExist = false;
                    commentInputLayout.setVisibility(View.VISIBLE);
                    userCommentLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void updateCommentUI(Comment comment) {
        if(comment != null) {
            setCommentOnScreenEnabled(true);
            editImage.getDrawable().setTint(colorBlue);

            if(isOnScreen) {
                try {
                    Glide.with(myContext).load(user.getProfileImage())
                            .placeholder(R.drawable.image_loading_placeholder)
                            .into(profileImage);
                }
                catch (Exception ignored) {}
            }

            String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
            if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();
            tvUserFullName.setText(fromHtml(fullName));

            badgeLayout.setVisibility(View.GONE);
            ownerImage.setVisibility(View.GONE);
            developerImage.setVisibility(View.GONE);
            adminImage.setVisibility(View.GONE);
            driverImage.setVisibility(View.GONE);

            if(user.getRole().isOwner()) {
                badgeLayout.setVisibility(View.VISIBLE);
                ownerImage.setVisibility(View.VISIBLE);
                ownerImage.setOnLongClickListener(view -> {
                    Toast.makeText(
                            myContext,
                            "Owner",
                            Toast.LENGTH_SHORT
                    ).show();
                    return false;
                });
            }
            if(user.getRole().isDeveloper()) {
                badgeLayout.setVisibility(View.VISIBLE);
                developerImage.setVisibility(View.VISIBLE);
                developerImage.setOnLongClickListener(view -> {
                    Toast.makeText(
                            myContext,
                            "Developer",
                            Toast.LENGTH_SHORT
                    ).show();
                    return false;
                });
            }
            if(user.getRole().isAdmin()) {
                badgeLayout.setVisibility(View.VISIBLE);
                adminImage.setVisibility(View.VISIBLE);
                adminImage.setOnLongClickListener(view -> {
                    Toast.makeText(
                            myContext,
                            "Admin",
                            Toast.LENGTH_SHORT
                    ).show();
                    return false;
                });
            }
            if(user.getRole().isDriver()) {
                badgeLayout.setVisibility(View.VISIBLE);
                driverImage.setVisibility(View.VISIBLE);
                driverImage.setOnLongClickListener(view -> {
                    Toast.makeText(
                            myContext,
                            "Driver",
                            Toast.LENGTH_SHORT
                    ).show();
                    return false;
                });
            }
            if(!isLiked) likerImage.setVisibility(View.GONE);
            else {
                badgeLayout.setVisibility(View.VISIBLE);
                likerImage.setVisibility(View.VISIBLE);
                likerImage.setOnLongClickListener(view -> {
                    Toast.makeText(
                            myContext,
                            "Liker",
                            Toast.LENGTH_SHORT
                    ).show();
                    return false;
                });
            }

            commentValue = comment.getValue();
            extvComment.setText(commentValue);

            String timestamp = comment.getTimestamp();
            DateTimeDifference dateTimeDifference = new DateTimeDifference(timestamp);
            timestamp = dateTimeDifference.getResult();
            tvTimestamp.setText(timestamp);

            if(comment.isFouled()) {
                tvCommentStatus.setVisibility(View.VISIBLE);

                appealImage.setEnabled(true);
                appealImage.setVisibility(View.VISIBLE);
                deactivateImage.setVisibility(View.GONE);

                String status = defaultStatusText;
                if(comment.isAppealed()) {
                    appealImage.setEnabled(false);
                    appealImage.getDrawable().setTint(colorInitial);
                    status = defaultStatusText + " " + appealedText;
                }
                else appealImage.getDrawable().setTint(colorBlue);

                tvCommentStatus.setText(status);
            }
            else {
                tvCommentStatus.setVisibility(View.GONE);

                appealImage.setVisibility(View.GONE);
                deactivateImage.setVisibility(View.VISIBLE);

                if(comment.isDeactivated()) {
                    tvCommentStatus.setVisibility(View.VISIBLE);
                    tvCommentStatus.setText(notActiveText);

                    deactivateImage.setImageResource(R.drawable.ic_baseline_comment_24);
                    deactivateImage.getDrawable().setTint(colorBlue);
                    currentDeactivateText = activateText;
                }
                else {
                    tvCommentStatus.setVisibility(View.GONE);
                    tvCommentStatus.setText(defaultStatusText);

                    deactivateImage.setImageResource(R.drawable.ic_baseline_comments_disabled_24);
                    deactivateImage.getDrawable().setTint(colorRed);
                    currentDeactivateText = deactivateText;
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        if(html == null) {
            return new SpannableString("");
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        }
        else {
            return Html.fromHtml(html);
        }
    }

    private void getUsers() {
        commentProgressBar.setVisibility(View.VISIBLE);
        commentLayout.setVisibility(View.GONE);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                commentedUsers.clear();
                foulCommentedUsers.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = new User(dataSnapshot);
                        users.add(user);

                        List<Comment> comments = user.getComments();
                        for(Comment comment : comments) {
                            if(comment.getId().equals(id)) {
                                if(comment.isFouled()) foulCommentedUsers.add(user);
                                else commentedUsers.add(user);
                            }
                        }
                    }

                    Collections.sort(foulCommentedUsers, new Comparator<User>() {
                        @Override
                        public int compare(User user, User t1) {
                            return getVotes(user) - getVotes(t1);
                        }
                    });

                    Collections.reverse(foulCommentedUsers);

                    Collections.sort(commentedUsers, new Comparator<User>() {
                        @Override
                        public int compare(User user, User t1) {
                            return getVotes(user) - getVotes(t1);
                        }
                    });

                    Collections.reverse(commentedUsers);

                    commentedUsers.addAll(foulCommentedUsers);
                }

                if(!isUpdatingComments) {
                    commentLayout.setVisibility(View.VISIBLE);
                    if(users.size() > 0) finishLoading();
                    else errorLoading(defaultLogText);
                }
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
        commentAdapter.notifyDataSetChanged();

        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        commentProgressBar.setVisibility(View.GONE);
    }

    private void errorLoading(String error) {
        foulCommentedUsers.clear();
        commentedUsers.clear();
        commentAdapter.notifyDataSetChanged();

        tvLog.setText(error);
        tvLog.setVisibility(View.VISIBLE);
        reloadImage.setVisibility(View.VISIBLE);
        commentProgressBar.setVisibility(View.GONE);
    }

    private int getVotes(User user) {
        int upVotes = 0, downVotes = 0;
        for(User user1 : users) {
            List<OtherComment> upVotedComments = user1.getUpVotedComments();
            for (OtherComment otherComment : upVotedComments) {
                if (otherComment.getSpotId().equals(id) && otherComment.getSenderUserId().equals(user.getId())) {
                    upVotes++;
                }
            }

            List<OtherComment> downVotedComments = user1.getDownVotedComments();
            for (OtherComment otherComment : downVotedComments) {
                if (otherComment.getSpotId().equals(id) && otherComment.getSenderUserId().equals(user.getId())) {
                    downVotes++;
                }
            }
        }
        return upVotes - downVotes;
    }

    private void likeSpot(SimpleTouristSpot touristSpot) {
        likedSpotsRef.child(id).setValue(touristSpot);
    }

    private void unlikeSpot() {
        likedSpotsRef.child(id).removeValue();
    }

    private boolean isInLikedSpots(SimpleTouristSpot targetSpot) {
        for(SimpleTouristSpot likedSpot : likedSpots) {
            if(likedSpot.getId().equals(targetSpot.getId())) {
                return true;
            }
        }
        return false;
    }

    private void backToHome() {
        Intent newIntent = new Intent(myContext, MainActivity.class);
        startActivity(newIntent);
        finishAffinity();
    }

    private void openMap(String id, double lat, double lng, String name, int type) {
        Intent intent = new Intent(myContext, MapActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("lat", lat);
        intent.putExtra("lng", lng);
        intent.putExtra("name", name);
        intent.putExtra("type", type);
        myContext.startActivity(intent);
    }

    private void openStreetView(String id) {
        DatabaseReference vriRef = firebaseDatabase.getReference("touristSpots")
                .child(id).child("vri");
        vriRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Intent intent = new Intent(myContext, StreetWebView.class);
                    intent.putExtra("id", id);
                    myContext.startActivity(intent);
                }
                else {
                    Toast.makeText(
                            myContext,
                            "No Street View Record",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void openOption() {
        moreImage.setEnabled(false);

        optionHandler.removeCallbacks(optionRunnable);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(connectingLayout);

        constraintSet.clear(buttonLayout.getId(), ConstraintSet.TOP);
        constraintSet.connect(buttonLayout.getId(), ConstraintSet.TOP,
                moreImage.getId(), ConstraintSet.TOP);
        constraintSet.connect(buttonLayout.getId(), ConstraintSet.BOTTOM,
                moreImage.getId(), ConstraintSet.BOTTOM);

        setTransition(buttonLayout);
        constraintSet.applyTo(connectingLayout);

        isOptionShown = true;
        moreImage.setEnabled(true);
        moreImage.setImageResource(R.drawable.ic_baseline_close_24);
        moreImage.getDrawable().setTint(myContext.getResources().getColor(R.color.red));

        optionRunnable = () -> closeOption();

        optionHandler.postDelayed(optionRunnable, 3000);
    }

    private void closeOption() {
        moreImage.setEnabled(false);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(connectingLayout);

        constraintSet.clear(buttonLayout.getId(), ConstraintSet.TOP);
        constraintSet.clear(buttonLayout.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(buttonLayout.getId(), ConstraintSet.TOP,
                connectingLayout.getId(), ConstraintSet.BOTTOM);

        setTransition(buttonLayout);
        constraintSet.applyTo(connectingLayout);

        isOptionShown = false;
        moreImage.setEnabled(true);
        moreImage.setImageResource(R.drawable.ic_baseline_more_horiz_24);
        moreImage.getDrawable().setTint(myContext.getResources().getColor(R.color.black));
    }

    private void setTransition(ConstraintLayout constraintLayout) {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);
        TransitionManager.beginDelayedTransition(constraintLayout, transition);
    }

    private void getTouristSpots() {
        progressBar.setVisibility(View.VISIBLE);

        DatabaseReference touristSpotsRef = firebaseDatabase.getReference("touristSpots")
                .child(id);
        touristSpotsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nearSpots.clear();
                if(snapshot.exists()) {
                    DetailedTouristSpot touristSpot = new DetailedTouristSpot(snapshot);
                    getStats(touristSpot);
                    updateNearSpots(touristSpot);
                }
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

    private void updateNearSpots(DetailedTouristSpot touristSpot) {
        nearSpots.addAll(touristSpot.getNearSpots());

        if(nearSpots.size() == 0) {
            tvNearSpot.setVisibility(View.GONE);
            nearSpotView.setVisibility(View.GONE);
        }
        else {
            tvNearSpot.setVisibility(View.VISIBLE);
            nearSpotView.setVisibility(View.VISIBLE);
        }

        nearSpotAdapter.notifyDataSetChanged();
    }


    private void getStats(DetailedTouristSpot touristSpot) {
        DatabaseReference usersRef = firebaseDatabase.getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int books = 0, likes = 0, visits = 0, comments = 0;

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if(dataSnapshot.hasChildren()) {
                            User user = new User(dataSnapshot);

                            List<SimpleTouristSpot> userLikedSpots = user.getLikedSpots();
                            for(SimpleTouristSpot likedSpot : userLikedSpots) {
                                if(likedSpot.getId().equals(id)) likes++;
                            }

                            List<Booking> userBookingList = user.getBookingList();
                            for(Booking booking : userBookingList) {
                                List<Route> routeList = booking.getRouteList();
                                for(Route route : routeList) {
                                    if(route.getId().equals(id)) {
                                        books++;
                                        if(route.isVisited()) visits++;
                                    }
                                }

                                if(booking.getDestinationSpot() != null) {
                                    if(booking.getDestinationSpot().getId().equals(id)) {
                                        books++;
                                        if(booking.getStatus().equals("Completed")) visits++;
                                    }
                                }
                            }

                            List<Comment> userComments = user.getComments();
                            for(Comment comment : userComments) {
                                if(comment.getId().equals(id)) comments++;
                            }
                        }
                    }
                }
                touristSpot.setBooks(books);
                touristSpot.setLikes(likes);
                touristSpot.setVisits(visits);
                touristSpot.setComments(comments);
                setInfo(touristSpot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                touristSpot.setBooks(0);
                touristSpot.setLikes(0);
                touristSpot.setVisits(0);
                touristSpot.setComments(0);
                setInfo(touristSpot);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isOnScreen = false;
    }

    private void setInfo(DetailedTouristSpot touristSpot) {
        name = touristSpot.getName();
        description = touristSpot.getDescription();
        img = touristSpot.getImg();
        likes = touristSpot.getLikes();
        visits = touristSpot.getVisits();
        books = touristSpot.getBooks();
        comments = touristSpot.getComments();
        lat = touristSpot.getLat();
        lng = touristSpot.getLng();
        nearStations = touristSpot.getNearStations();
        deactivated = touristSpot.isDeactivated();
        stations = new StringBuilder();

        selectedSpot = new SimpleTouristSpot(touristSpot);

        boolean isFirst = true;
        for(Station nearStation : nearStations) {
            if(isFirst) {
                stations.append(nearStation.getName());
                isFirst = false;
            }
            else {
                stations.append(", ").append(nearStation.getName());
            }
        }

        if(isOnScreen) updateInfo();
    }

    private void updateInfo() {
        progressBar.setVisibility(View.GONE);

        try {
            Glide.with(myContext).load(img).
                    placeholder(R.drawable.image_loading_placeholder).
                    override(Target.SIZE_ORIGINAL).into(thumbnail);
        }
        catch (Exception ignored) {}

        tvName.setText(name);
        extvDescription.setText(description);
        tvStation.setText(stations);
        tvLikes.setText(String.valueOf(likes));
        tvVisits.setText(String.valueOf(visits));
        tvBooks.setText(String.valueOf(books));
        tvComments.setText(String.valueOf(comments));

        isLiked = isInLikedSpots(selectedSpot);
        if(userId != null) checkCurrentUserComment();

        int color;
        if(!isLiked) color = myResources.getColor(R.color.black);
        else color = myResources.getColor(R.color.blue);
        likeImage.getDrawable().setTint(color);

        likeImage.setEnabled(true);

        if(toComment && !isCommentShown) {
            commentShowingAnimation  = false;
            showCommentLayout();
            commentShowingAnimation  = defaultValueForAnimation;
        }

        tvCommentSpot.setText(name);
    }

    private void getLikedSpots() {
        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likedSpots.clear();
                if(snapshot.exists()) {
                    user = new User(snapshot);
                    likedSpots.addAll(user.getLikedSpots());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}