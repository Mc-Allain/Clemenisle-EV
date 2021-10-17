package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
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
import android.view.View;
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
import com.example.firebase_clemenisle_ev.Classes.DetailedTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
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

    ImageView thumbnail, likeImage, visitImage, bookImage, moreImage, i360Image, locateImage, homeImage;
    TextView tvName, tvStation, tvLikes, tvVisits, tvBooks, tvNearSpot, tvLiked, tvOption,
            tv360Image, tvLocate;
    ExpandableTextView extvDescription;
    ConstraintLayout  backgroundLayout, buttonLayout, connectingLayout;
    ScrollView scrollView;
    RecyclerView nearSpotView;
    ProgressBar progressBar;

    ConstraintLayout commentTitleLayout, commentLayout,
            commentInputLayout, userCommentLayout, commentBackgroundLayout;
    EditText etComment;
    ImageView sendImage, commentArrowImage;

    TextView tvUserFullName, tvCommentStatus;
    ExpandableTextView extvComment;
    ImageView profileImage, editImage, appealImage, deactivateImage;

    RecyclerView commentView;
    ProgressBar commentProgressBar;

    Context myContext;
    Resources myResources;

    int colorBlue, colorInitial, colorRed;

    String userId;

    boolean loggedIn = false;

    String id, name, description, img;
    int likes, visits, books;
    double lat, lng;
    List<SimpleTouristSpot> nearSpots = new ArrayList<>();
    List<Station> nearStations;
    boolean deactivated;
    StringBuilder stations;

    NearSpotAdapter nearSpotAdapter;

    User user;
    List<SimpleTouristSpot> likedSpots = new ArrayList<>();
    SimpleTouristSpot selectedSpot;

    boolean onScreen = false;
    String liked;

    DatabaseReference usersRef, likedSpotsRef, commentsRef,
            upVotedCommentsRef, downVotedCommentsRef, reportedCommentsRef;

    boolean isCommentShown = false, commentShowingAnimation = false;

    CommentAdapter commentAdapter;
    List<User> users = new ArrayList<>(), commentedUsers = new ArrayList<>(),
            foulCommentedUsers = new ArrayList<>();

    String deactivateText = "Deactivate Comment";
    String activateText = "Activate Comment";
    String currentDeactivateText = deactivateText;

    Handler optionHandler = new Handler();
    Runnable optionRunnable;

    String inputComment, commentValue;
    boolean isUserCommentExist = true;

    String defaultStatusText = "Foul comment", appealedtext = "(Appealed)",
            notActiveText = "This comment is not active";

    long deactivatePressedTime;
    Toast deactivateToast;
    boolean isDeactivateClicked = false;

    long reportPressedTime;
    Toast reportToast;
    boolean isReportClicked = false;

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
        tvLikes = findViewById(R.id.tvLikes);
        tvVisits = findViewById(R.id.tvVisits);
        tvBooks = findViewById(R.id.tvBooks);
        moreImage = findViewById(R.id.moreImage);
        backgroundLayout = findViewById(R.id.backgroundLayout);
        buttonLayout = findViewById(R.id.buttonLayout);
        tvNearSpot = findViewById(R.id.tvNearSpot);
        nearSpotView = findViewById(R.id.nearSpotView);
        tvLiked = findViewById(R.id.tvLiked);
        tvOption = findViewById(R.id.tvOption);
        i360Image = findViewById(R.id.i360Image);
        locateImage = findViewById(R.id.locateImage);
        tv360Image = findViewById(R.id.tv360Image);
        tvLocate = findViewById(R.id.tvLocate);
        connectingLayout = findViewById(R.id.connectingLayout);
        homeImage = findViewById(R.id.homeImage);
        progressBar = findViewById(R.id.progressBar);

        commentTitleLayout = findViewById(R.id.commentTitleLayout);
        commentLayout = findViewById(R.id.commentLayout);
        commentInputLayout = findViewById(R.id.commentInputLayout);
        etComment = findViewById(R.id.etComment);
        sendImage = findViewById(R.id.sendImage);
        commentArrowImage = findViewById(R.id.commentArrowImage);

        userCommentLayout = findViewById(R.id.userCommentLayout);
        commentBackgroundLayout = findViewById(R.id.commentBackgroundLayout);
        tvUserFullName = findViewById(R.id.tvUserFullName);
        tvCommentStatus = findViewById(R.id.tvCommentStatus);
        extvComment = findViewById(R.id.extvComment);
        profileImage = findViewById(R.id.profileImage);
        editImage = findViewById(R.id.editImage);
        appealImage = findViewById(R.id.appealImage);
        deactivateImage = findViewById(R.id.deactivateImage);
        commentView = findViewById(R.id.commentView);
        commentProgressBar = findViewById(R.id.commentProgressBar);

        myContext = SelectedSpotActivity.this;
        myResources = myContext.getResources();

        colorBlue = myResources.getColor(R.color.blue);
        colorInitial = myResources.getColor(R.color.initial);
        colorRed = myResources.getColor(R.color.red);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        loggedIn = intent.getBooleanExtra("loggedIn", false);

        onScreen = true;

        sendImage.setEnabled(false);
        sendImage.setColorFilter(colorInitial);

        firebaseAuth = FirebaseAuth.getInstance();
        if(loggedIn) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null) {
                firebaseUser.reload();
                userId = firebaseUser.getUid();
            }
        }

        usersRef = firebaseDatabase.getReference("users");
        likedSpotsRef = usersRef.child(userId).child("likedSpots");
        commentsRef = usersRef.child(userId).child("comments");
        upVotedCommentsRef = usersRef.child(userId).child("upVotedComments");
        downVotedCommentsRef = usersRef.child(userId).child("downVotedComments");
        reportedCommentsRef = usersRef.child(userId).child("reportedComments");

        LinearLayoutManager linearLayout =
                new LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false);
        nearSpotView.setLayoutManager(linearLayout);
        nearSpotAdapter = new NearSpotAdapter(myContext, nearSpots, loggedIn);
        nearSpotView.setAdapter(nearSpotAdapter);

        LinearLayoutManager linearLayout2 =
                new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        commentView.setLayoutManager(linearLayout2);
        commentAdapter = new CommentAdapter(myContext, commentedUsers, id, userId);
        commentView.setAdapter(commentAdapter);
        commentAdapter.setOnActionButtonClickedListener(this);

        getTouristSpots();
        getLikedSpots();
        getUsers();
        checkCurrentUserComment();

        likeImage.setOnClickListener(view -> {
            if(loggedIn) {
                if(firebaseUser != null) {
                    likeImage.setEnabled(false);
                    if(liked.equals("false")) {
                        likeSpot(selectedSpot);
                    }
                    else{
                        unlikeSpot();
                    }
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

        moreImage.setOnClickListener(view -> {
            if(tvOption.getText().equals("false")) {
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
                inputComment = etComment.getText().toString();

                if(inputComment.length() > 0) {
                    sendImage.setEnabled(true);
                    sendImage.setColorFilter(colorBlue);
                }
                else {
                    sendImage.setEnabled(false);
                    sendImage.setColorFilter(colorInitial);
                }
            }
        });

        commentTitleLayout.setOnClickListener(view -> showCommentLayout());

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

    @Override
    public void appealImageOnClick() {
        commentsRef.child(id).child("appealed").setValue(true).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
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
            }
            commentProgressBar.setVisibility(View.GONE);
        });
    }

    @Override
    public void deactivateImageOnClick() {
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

    @Override
    public void reportImageOnClick(String spotId, String senderUserId, Comment comment) {
        if(reportPressedTime + 2500 > System.currentTimeMillis() && isReportClicked) {
            reportToast.cancel();

            setReportedComment(spotId, senderUserId, comment);

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

    @Override
    public void upVoteImageOnClick(String spotId, String senderUserId, Comment comment,
                                   boolean upVoted, boolean downVoted) {
        commentProgressBar.setVisibility(View.VISIBLE);
        comment.setUserId(senderUserId);

        if(upVoted) upVotedCommentsRef.child(spotId).child(senderUserId).setValue(null);
        else if(downVoted) downVotedCommentsRef.child(spotId).child(senderUserId).setValue(null);

        if(!upVoted) upVotedCommentsRef.child(spotId).child(senderUserId).setValue(comment)
                .addOnCompleteListener(task -> commentProgressBar.setVisibility(View.GONE));
    }

    @Override
    public void downVoteImageOnClick(String spotId, String senderUserId, Comment comment,
                                     boolean upVoted, boolean downVoted) {
        commentProgressBar.setVisibility(View.VISIBLE);
        comment.setUserId(senderUserId);

        if(upVoted) upVotedCommentsRef.child(spotId).child(senderUserId).setValue(null);
        if(downVoted) downVotedCommentsRef.child(spotId).child(senderUserId).setValue(null);

        if(!downVoted) downVotedCommentsRef.child(spotId).child(senderUserId).setValue(comment)
                .addOnCompleteListener(task -> commentProgressBar.setVisibility(View.GONE));
    }

    private void setReportedComment(String spotId, String senderUserId, Comment comment) {
        commentProgressBar.setVisibility(View.VISIBLE);
        comment.setUserId(senderUserId);

        reportedCommentsRef.child(spotId).child(senderUserId).setValue(comment).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        String toastMessage = "The comment is now reported";

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
                    }
            commentProgressBar.setVisibility(View.GONE);
        });
    }

    private void setDeactivatedComment() {
        commentProgressBar.setVisibility(View.VISIBLE);
        boolean value = false;
        if(currentDeactivateText.equals(deactivateText)) value = true;

        commentsRef.child(id).child("deactivated").setValue(value)
        .addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
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
            }
            commentProgressBar.setVisibility(View.GONE);
        });
    }

    private void updateComment() {
        commentProgressBar.setVisibility(View.VISIBLE);
        commentsRef.child(id).child("value").setValue(inputComment)
                .addOnCompleteListener(task -> commentProgressBar.setVisibility(View.GONE));

        commentInputLayout.setVisibility(View.GONE);
        userCommentLayout.setVisibility(View.VISIBLE);
    }

    private void setComment() {
        commentProgressBar.setVisibility(View.VISIBLE);
        Comment comment = new Comment(id, inputComment);
        commentsRef.child(id).setValue(comment)
                .addOnCompleteListener(task -> commentProgressBar.setVisibility(View.GONE));
    }

    private void checkCurrentUserComment() {
        commentsRef.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Comment comment = snapshot.getValue(Comment.class);

                    isUserCommentExist = true;
                    commentInputLayout.setVisibility(View.GONE);
                    userCommentLayout.setVisibility(View.VISIBLE);

                    String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
                    if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();
                    tvUserFullName.setText(fromHtml(fullName));

                    commentValue = comment.getValue();
                    extvComment.setText(commentValue);

                    if(comment.isFouled()) {
                        tvCommentStatus.setVisibility(View.VISIBLE);

                        appealImage.setEnabled(true);
                        appealImage.setVisibility(View.VISIBLE);
                        deactivateImage.setVisibility(View.GONE);

                        String status = defaultStatusText;
                        if(comment.isAppealed()) {
                            appealImage.setEnabled(false);
                            appealImage.setColorFilter(colorInitial);
                            status = defaultStatusText + " " + appealedtext;
                        }
                        else appealImage.setColorFilter(colorBlue);

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

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        if(html == null){
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
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                commentedUsers.clear();
                foulCommentedUsers.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User thisUser = new User(dataSnapshot);
                        users.add(thisUser);

                        List<Comment> comments = thisUser.getComments();
                        for(Comment comment : comments) {
                            if(comment.getId().equals(id)) {
                                if(comment.isFouled()) foulCommentedUsers.add(thisUser);
                                else commentedUsers.add(thisUser);
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
                    commentProgressBar.setVisibility(View.GONE);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                commentProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private int getVotes(User user) {
        int upVotes = 0, downVotes = 0;
        for(User user1 : users) {
            List<Comment> upVotedComments = user1.getUpVotedComments();
            for (Comment comment : upVotedComments) {
                if (comment.getId().equals(id) && comment.getUserId().equals(user.getId())) {
                    upVotes++;
                }
            }

            List<Comment> downVotedComments = user1.getDownVotedComments();
            for (Comment comment : downVotedComments) {
                if (comment.getId().equals(id) && comment.getUserId().equals(user.getId())) {
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
        likedSpotsRef.child(id).setValue(null);
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
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
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

        tvOption.setText("true");
        moreImage.setEnabled(true);
        moreImage.setImageResource(R.drawable.ic_baseline_close_24);
        moreImage.setColorFilter(myContext.getResources().getColor(R.color.red));

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

        tvOption.setText("false");
        moreImage.setEnabled(true);
        moreImage.setImageResource(R.drawable.ic_baseline_more_horiz_24);
        moreImage.setColorFilter(myContext.getResources().getColor(R.color.black));
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
                        Toast.LENGTH_SHORT
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
                int books = 0, likes = 0, visits = 0;

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User thisUser = new User(dataSnapshot);

                        List<SimpleTouristSpot> userLikedSpots = thisUser.getLikedSpots();
                        for(SimpleTouristSpot likedSpot : userLikedSpots) {
                            if(likedSpot.getId().equals(id)) {
                                likes++;
                            }
                        }

                        List<Booking> userBookingList = thisUser.getBookingList();
                        for(Booking booking : userBookingList) {
                            List<Route> routeList = booking.getRouteList();
                            for(Route route : routeList) {
                                if(route.getId().equals(id)) {
                                    books++;
                                    if(route.isVisited()) {
                                        visits++;
                                    }
                                }
                            }
                        }
                    }
                }
                touristSpot.setBooks(books);
                touristSpot.setLikes(likes);
                touristSpot.setVisits(visits);
                setInfo(touristSpot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
                ).show();

                touristSpot.setBooks(0);
                touristSpot.setLikes(0);
                touristSpot.setVisits(0);
                setInfo(touristSpot);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onScreen = false;
    }

    private void setInfo(DetailedTouristSpot touristSpot) {
        name = touristSpot.getName();
        description = touristSpot.getDescription();
        img = touristSpot.getImg();
        likes = touristSpot.getLikes();
        visits = touristSpot.getVisits();
        books = touristSpot.getBooks();
        lat = touristSpot.getLat();
        lng = touristSpot.getLng();
        nearStations = touristSpot.getNearStations();
        deactivated = touristSpot.isDeactivated();
        stations = new StringBuilder();

        selectedSpot = new SimpleTouristSpot(deactivated, id, img, name);

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

        if(onScreen) updateInfo();
    }

    private void updateInfo() {
        progressBar.setVisibility(View.GONE);

        Glide.with(myContext).load(img).placeholder(R.drawable.image_loading_placeholder).
                override(Target.SIZE_ORIGINAL).into(thumbnail);
        tvName.setText(name);
        extvDescription.setText(description);
        tvStation.setText(stations);
        tvLikes.setText(String.valueOf(likes));
        tvVisits.setText(String.valueOf(visits));
        tvBooks.setText(String.valueOf(books));

        tvLiked.setText(String.valueOf(
                isInLikedSpots(selectedSpot)
        ));
        liked = tvLiked.getText().toString();

        int color;
        if(liked.equals("false")) {
            color = myContext.getResources().getColor(R.color.black);
        }
        else {
            color = myContext.getResources().getColor(R.color.blue);
        }
        likeImage.setColorFilter(color);

        likeImage.setEnabled(true);
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
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}