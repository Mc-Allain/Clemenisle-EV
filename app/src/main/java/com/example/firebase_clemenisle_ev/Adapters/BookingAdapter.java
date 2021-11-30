package com.example.firebase_clemenisle_ev.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.example.firebase_clemenisle_ev.ChatActivity;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.BookingType;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.Route;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.Station;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.example.firebase_clemenisle_ev.MapActivity;
import com.example.firebase_clemenisle_ev.OnTheSpotActivity;
import com.example.firebase_clemenisle_ev.OnlinePaymentActivity;
import com.example.firebase_clemenisle_ev.R;
import com.example.firebase_clemenisle_ev.RouteActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef = firebaseDatabase.getReference("users");
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    List<Booking> bookingList;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    int colorGreen, colorInitial, colorBlue, colorRed;
    ColorStateList cslInitial, cslBlue;

    String userId;
    boolean isLoggedIn = false;

    String startStationText = "Start Station", endStationText = "End Station";
    String destinationSpotText = "Destination Spot", originLocationText = "Origin Location";

    String locateStartStationText = "Locate Start Station",
            locateEndStationText = "Locate End Station";
    String locateOriginLocationText = "Locate Origin Location",
            locateDestinationSpotText = "Locate Destination Spot";

    boolean inDriverModule = false;

    Dialog qrCodeDialog;
    ImageView qrCodeDialogCloseImage, qrCodeImage;

    List<User> users = new ArrayList<>();

    OnActionClickListener onActionClickListener;

    String defaultPassengerText = "Passenger", requestText = "Your Task on Request";

    List<Booking> ongoingTaskList = new ArrayList<>();

    String initiateService = "Initiate Service", dropOffText = "Drop Off";

    String pickUpTimeText = "<b>Pick-up Time</b>: ", dropOffTimeText = "<b>Drop-off Time</b>: ";

    Dialog dialog;
    ImageView dialogCloseImage;
    Button dialogSubmitButton;
    ProgressBar dialogProgressBar;

    EditText etReason;
    TextInputLayout tlReason;
    String reasonValue;

    Dialog dialog2;
    ImageView dialogCloseImage2, star1Image, star2Image, star3Image, star4Image, star5Image;
    Button dialogSubmitButton2;
    ProgressBar dialogProgressBar2;

    EditText etRemarks;
    TextInputLayout tlRemarks;
    String remarksValue;
    int starValue = 0;

    Dialog dialog3;
    ImageView dialogCloseImage3;
    Button dialogSubmitButton3;
    ProgressBar dialogProgressBar3;

    EditText etRemarks2;
    TextInputLayout tlRemarks2;
    String remarksValue2;

    Dialog dialogMessage;
    ImageView dialogMessageCloseImage;
    TextView tvDialogTitle, tvMessage;

    public void setOnLikeClickListener(OnActionClickListener onActionClickListener) {
        this.onActionClickListener = onActionClickListener;
    }

    public interface OnActionClickListener{
        void setProgressBarToVisible(boolean value);
    }

    public void setInDriverMode(boolean inDriverModule) {
        this.inDriverModule = inDriverModule;
        notifyDataSetChanged();
    }

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

    public BookingAdapter(Context context, List<Booking> bookingList) {
        this.bookingList = bookingList;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_booking_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView profileImage = holder.profileImage, driverProfileImage = holder.driverProfileImage,
                thumbnail = holder.thumbnail, moreImage = holder.moreImage,
                openImage = holder.openImage, locateImage = holder.locateImage,
                locateEndImage = holder.locateEndImage, onlinePaymentImage = holder.onlinePaymentImage,
                viewQRImage = holder.viewQRImage, chatImage = holder.chatImage,
                driverImage = holder.driverImage, passImage = holder.passImage, stopImage = holder.stopImage,
                checkImage = holder.checkImage, rateImage = holder.rateImage, remarksImage = holder.remarksImage,
                paidImage = holder.paidImage;
        TextView tvUserFullName = holder.tvUserFullName, tvPassenger = holder.tvPassenger,
                tvDriverFullName = holder.tvDriverFullName, tvPlateNumber = holder.tvPlateNumber,
                tvPickUpTime = holder.tvPickUpTime, tvDropOffTime = holder.tvDropOffTime,
                tvBookingId = holder.tvBookingId, tvSchedule = holder.tvSchedule, tvTypeName = holder.tvTypeName,
                tvPrice = holder.tvPrice, tvStartStation = holder.tvStartStation, tvEndStation = holder.tvEndStation,
                tvStartStation2 = holder.tvStartStation2, tvEndStation2 = holder.tvEndStation2,
                tvOption = holder.tvOption, tvOpen = holder.tvOpen,
                tvLocate = holder.tvLocate, tvLocateEnd = holder.tvLocateEnd,
                tvChat = holder.tvChat, tvOnlinePayment = holder.tvOnlinePayment,
                tvViewQR = holder.tvViewQR, tvDriver = holder.tvDriver, tvPass = holder.tvPass,
                tvStop = holder.tvStop, tvCheck = holder.tvCheck, tvRate = holder.tvRate, tvRemarks = holder.tvRemarks,
                tvViewMessage = holder.tvViewMessage, tvViewRemarks = holder.tvViewRemarks, tvViewReason = holder.tvViewReason;
        ConstraintLayout backgroundLayout = holder.backgroundLayout, buttonLayout = holder.buttonLayout,
                userInfoLayout = holder.userInfoLayout, driverInfoLayout = holder.driverInfoLayout,
                timeInfoLayout = holder.timeInfoLayout;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        colorGreen = myResources.getColor(R.color.green);
        colorInitial = myResources.getColor(R.color.initial);
        colorBlue = myResources.getColor(R.color.blue);
        colorRed = myResources.getColor(R.color.red);

        cslInitial = ColorStateList.valueOf(myResources.getColor(R.color.initial));
        cslBlue = ColorStateList.valueOf(myResources.getColor(R.color.blue));

        initSharedPreferences();
        initQRCodeDialog();
        if(dialog == null) initReasonDialog();
        if(dialog2 == null)  initRateTheDriverDialog();
        if(dialog3 == null)  initRemarksDialog();
        if(dialogMessage == null) initMessageDialog();

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

        Handler optionHandler = new Handler();
        Runnable optionRunnable = () -> closeOption(buttonLayout, backgroundLayout, moreImage, tvOption);

        Booking booking = bookingList.get(position);

        String status = booking.getStatus();

        String bookingId = booking.getId();
        String schedule = booking.getSchedule();

        String pickUpTime = booking.getPickUpTime();
        String dropOffTime = booking.getDropOffTime();

        BookingType bookingType = booking.getBookingType();
        String typeName = bookingType.getName();
        String price = "₱" + bookingType.getPrice();
        if(price.split("\\.")[1].length() == 1) price += 0;

        String message = booking.getMessage();
        String reason = booking.getReason();
        String remarks = booking.getRemarks();
        int rating = booking.getRating();

        String previousDriverUserId = booking.getPreviousDriverUserId();

        tvBookingId.setText(bookingId);
        tvSchedule.setText(schedule);
        tvPrice.setText(price);
        tvTypeName.setText(typeName);

        int color = 0;

        switch (status) {
            case "Pending":
                color = myResources.getColor(R.color.orange);
                break;
            case "Request":
            case "Booked":
                color = myResources.getColor(R.color.green);
                break;
            case "Ongoing":
            case "Completed":
                color = myResources.getColor(R.color.blue);
                break;
            case "Passed":
            case "Cancelled":
            case "Failed":
                color = myResources.getColor(R.color.red);
                break;
        }

        tvBookingId.setBackgroundColor(color);
        tvPrice.setTextColor(color);

        timeInfoLayout.setVisibility(View.GONE);

        if(pickUpTime != null && pickUpTime.length() > 0) {
            timeInfoLayout.setVisibility(View.VISIBLE);
            tvPickUpTime.setText(fromHtml(pickUpTimeText + pickUpTime));
        }

        if(dropOffTime != null && dropOffTime.length() > 0)
            timeInfoLayout.setVisibility(View.VISIBLE);
        else dropOffTime = "Unset";
        tvDropOffTime.setText(fromHtml(dropOffTimeText + dropOffTime));

        tvOnlinePayment.setVisibility(View.GONE);
        onlinePaymentImage.setVisibility(View.GONE);

        if(!bookingType.getId().equals("BT99")) {
            Station startStation = booking.getStartStation();
            Station endStation = booking.getEndStation();

            String img;
            List<Route> routeList = booking.getRouteList();
            if(routeList.size() > 0) {
                img = routeList.get(0).getImg();
                try {
                    Glide.with(myContext).load(img).placeholder(R.drawable.image_loading_placeholder).
                            override(Target.SIZE_ORIGINAL).into(thumbnail);
                }
                catch (Exception ignored) {}
            }

            tvStartStation.setText(startStationText);
            tvEndStation.setText(endStationText);

            tvStartStation2.setText(startStation.getName());
            tvEndStation2.setText(endStation.getName());

            tvLocate.setText(locateStartStationText);
            tvLocateEnd.setText(locateEndStationText);

            boolean isPaid = booking.isPaid();

            if(isPaid) paidImage.setVisibility(View.VISIBLE);
            else paidImage.setVisibility(View.GONE);

            paidImage.getDrawable().setTint(color);

            tvLocate.setOnClickListener(view -> openMap(startStation));
            locateImage.setOnClickListener(view -> openMap(startStation));

            tvLocateEnd.setOnClickListener(view -> openMap(endStation));
            locateEndImage.setOnClickListener(view -> openMap(endStation));

            if(!inDriverModule) {
                tvOnlinePayment.setVisibility(View.VISIBLE);
                onlinePaymentImage.setVisibility(View.VISIBLE);

                tvOnlinePayment.setOnClickListener(view -> openOnlinePayment(bookingId));
                onlinePaymentImage.setOnClickListener(view -> openOnlinePayment(bookingId));
            }

            paidImage.setOnLongClickListener(view -> {
                Toast.makeText(
                        myContext,
                        "Paid",
                        Toast.LENGTH_SHORT
                ).show();
                return false;
            });
        }
        else {
            SimpleTouristSpot destinationSpot = booking.getDestinationSpot();

            String img = destinationSpot.getImg();
            try {
                Glide.with(myContext).load(img).placeholder(R.drawable.image_loading_placeholder).
                        override(Target.SIZE_ORIGINAL).into(thumbnail);
            }
            catch (Exception ignored) {}

            double originLat = booking.getOriginLat(), originLng = booking.getOriginLng();
            String originLocation = "Latitude: " + originLat + "\nLongitude: " + originLng;

            tvStartStation.setText(originLocationText);
            tvEndStation.setText(destinationSpotText);

            tvStartStation2.setText(originLocation);
            tvEndStation2.setText(destinationSpot.getName());

            tvLocate.setText(locateOriginLocationText);
            tvLocateEnd.setText(locateDestinationSpotText);

            tvLocate.setOnClickListener(view -> openMap2(originLat, originLng));
            locateImage.setOnClickListener(view -> openMap2(originLat, originLng));

            tvLocateEnd.setOnClickListener(view -> openMap3(destinationSpot));
            locateEndImage.setOnClickListener(view -> openMap3(destinationSpot));
        }

        tvRate.setVisibility(View.GONE);
        rateImage.setVisibility(View.GONE);
        if(!inDriverModule && status.equals("Completed") && rating == 0) {
            tvRate.setVisibility(View.VISIBLE);
            rateImage.setVisibility(View.VISIBLE);

            tvRate.setOnClickListener(view -> openRateTheDriverDialog(bookingId));
            rateImage.setOnClickListener(view -> openRateTheDriverDialog(bookingId));
        }

        moreImage.setOnClickListener(view -> {
            if(tvOption.getText().equals("false")) {
                openOption(buttonLayout, backgroundLayout, moreImage,
                        optionHandler, optionRunnable, tvOption);
            }
            else {
                closeOption(buttonLayout, backgroundLayout, moreImage, tvOption);
            }
        });

        tvOpen.setOnClickListener(view -> openItem(booking, false));
        openImage.setOnClickListener(view -> openItem(booking, false));

        getUsers(driverInfoLayout, userInfoLayout, message, bookingId, status, tvUserFullName,
                profileImage, tvViewQR, viewQRImage, tvChat, chatImage, tvDriver, driverImage,
                tvPass, passImage, tvStop, stopImage, tvCheck, checkImage, tvRemarks, remarksImage,
                tvDriverFullName, driverProfileImage, tvPassenger, tvPlateNumber,
                previousDriverUserId, reason, remarks, thumbnail, tvViewMessage, tvViewRemarks, tvViewReason, rating);

        int top = dpToPx(4), bottom = dpToPx(4);

        boolean isFirstItem = position + 1 == 1, isLastItem = position + 1 == getItemCount();

        if(isFirstItem) {
            top = dpToPx(8);
        }
        if(isLastItem) {
            if(status.equals("Failed")) {
                bottom = inDriverModule ? dpToPx(8) : dpToPx(88);
            }
            else {
                bottom = dpToPx(8);
            }
        }

        ConstraintLayout.LayoutParams layoutParams = (
                ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, top, layoutParams.rightMargin, bottom);
        backgroundLayout.setLayoutParams(layoutParams);
    }

    private void getThumbnail(ImageView thumbnail, String bookingId) {
        String passengerId = getPassengerUserId(bookingId);

        if(passengerId != null) {
            usersRef.child(passengerId).child("bookingList").child(bookingId).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String img;
                    if(snapshot.exists()) {
                        Booking booking = new Booking(snapshot);
                        List<Route> routeList = booking.getRouteList();
                        if(routeList.size() > 0) {
                            img = routeList.get(0).getImg();
                            try {
                                Glide.with(myContext).load(img).placeholder(R.drawable.image_loading_placeholder).
                                        override(Target.SIZE_ORIGINAL).into(thumbnail);
                            }
                            catch (Exception ignored) {}
                        }
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

    private void setDialogScreenEnabled(boolean value) {
        dialog.setCanceledOnTouchOutside(value);
        dialog.setCancelable(value);
        tlReason.setEnabled(value);
        dialogSubmitButton.setEnabled(value);

        dialog2.setCanceledOnTouchOutside(value);
        dialog2.setCancelable(value);
        tlRemarks.setEnabled(value);
        dialogSubmitButton2.setEnabled(value);

        dialog3.setCanceledOnTouchOutside(value);
        dialog3.setCancelable(value);
        tlRemarks2.setEnabled(value);
        dialogSubmitButton3.setEnabled(value);

        if(value) {
            dialogCloseImage.getDrawable().setTint(colorRed);
            dialogCloseImage2.getDrawable().setTint(colorRed);
            dialogCloseImage3.getDrawable().setTint(colorRed);
        }
        else {
            dialogCloseImage.getDrawable().setTint(colorInitial);
            dialogCloseImage2.getDrawable().setTint(colorInitial);
            dialogCloseImage3.getDrawable().setTint(colorInitial);
        }
    }

    private void openReasonDialog(Booking booking, String reason) {
        etReason.setText(reason);

        tlReason.setErrorEnabled(false);
        tlReason.setError(null);
        tlReason.setStartIconTintList(cslInitial);

        tlReason.clearFocus();
        tlReason.requestFocus();

        dialogSubmitButton.setOnClickListener(view -> submitReason(booking));

        dialog.show();
    }

    private void initReasonDialog() {
        dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_input_pass_task_reason_layout);

        etReason = dialog.findViewById(R.id.etReason);
        tlReason = dialog.findViewById(R.id.tlReason);
        dialogSubmitButton = dialog.findViewById(R.id.submitButton);
        dialogCloseImage = dialog.findViewById(R.id.dialogCloseImage);
        dialogProgressBar = dialog.findViewById(R.id.dialogProgressBar);

        etReason.setOnFocusChangeListener((view1, b) -> {
            if(!tlReason.isErrorEnabled()) {
                if(b) {
                    tlReason.setStartIconTintList(cslBlue);
                }
                else {
                    tlReason.setStartIconTintList(cslInitial);
                }
            }
        });

        etReason.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                reasonValue = etReason.getText().toString().trim();
                dialogSubmitButton.setEnabled(reasonValue.length() > 0);
            }
        });

        dialogCloseImage.setOnClickListener(view -> dialog.dismiss());

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
        dialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void submitReason(Booking booking) {
        dialogProgressBar.setVisibility(View.VISIBLE);
        setDialogScreenEnabled(false);
        tlReason.setStartIconTintList(cslInitial);

        usersRef.child(userId).child("taskList").
                child(booking.getId()).child("reason").setValue(reasonValue)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) passTask(booking);
                    else {
                        Toast.makeText(
                                myContext,
                                "Failed to pass the task",
                                Toast.LENGTH_LONG
                        ).show();
                        dialogProgressBar.setVisibility(View.GONE);
                        setDialogScreenEnabled(true);
                    }
                });
    }

    private void openRateTheDriverDialog(String bookingId) {
        etRemarks.setText(null);
        clickStar(0);

        tlRemarks.setErrorEnabled(false);
        tlRemarks.setError(null);
        tlRemarks.setStartIconTintList(cslInitial);

        tlRemarks.clearFocus();
        tlRemarks.requestFocus();

        dialogSubmitButton2.setOnClickListener(view -> rate(bookingId));

        dialog2.show();
    }

    private void initRateTheDriverDialog() {
        dialog2 = new Dialog(myContext);
        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog2.setContentView(R.layout.dialog_input_rate_layout);

        etRemarks = dialog2.findViewById(R.id.etRemarks);
        tlRemarks = dialog2.findViewById(R.id.tlRemarks);
        dialogSubmitButton2 = dialog2.findViewById(R.id.submitButton);
        dialogCloseImage2 = dialog2.findViewById(R.id.dialogCloseImage);
        dialogProgressBar2 = dialog2.findViewById(R.id.dialogProgressBar);

        star1Image = dialog2.findViewById(R.id.star1Image);
        star2Image = dialog2.findViewById(R.id.star2Image);
        star3Image = dialog2.findViewById(R.id.star3Image);
        star4Image = dialog2.findViewById(R.id.star4Image);
        star5Image = dialog2.findViewById(R.id.star5Image);

        star1Image.setOnClickListener(view -> clickStar(1));
        star2Image.setOnClickListener(view -> clickStar(2));
        star3Image.setOnClickListener(view -> clickStar(3));
        star4Image.setOnClickListener(view -> clickStar(4));
        star5Image.setOnClickListener(view -> clickStar(5));

        etRemarks.setOnFocusChangeListener((view1, b) -> {
            if(!tlRemarks.isErrorEnabled()) {
                if(b) {
                    tlRemarks.setStartIconTintList(cslBlue);
                }
                else {
                    tlRemarks.setStartIconTintList(cslInitial);
                }
            }
        });

        etRemarks.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                remarksValue = etRemarks.getText().toString().trim();
            }
        });

        dialogCloseImage2.setOnClickListener(view -> dialog2.dismiss());

        dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog2.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
        dialog2.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialog2.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void clickStar(int count) {
        starValue = count;

        star1Image.setImageResource(R.drawable.ic_baseline_star_outline_24);
        star2Image.setImageResource(R.drawable.ic_baseline_star_outline_24);
        star3Image.setImageResource(R.drawable.ic_baseline_star_outline_24);
        star4Image.setImageResource(R.drawable.ic_baseline_star_outline_24);
        star5Image.setImageResource(R.drawable.ic_baseline_star_outline_24);

        if(count >= 1) star1Image.setImageResource(R.drawable.ic_baseline_star_24);
        if(count >= 2) star2Image.setImageResource(R.drawable.ic_baseline_star_24);
        if(count >= 3) star3Image.setImageResource(R.drawable.ic_baseline_star_24);
        if(count >= 4) star4Image.setImageResource(R.drawable.ic_baseline_star_24);
        if(count >= 5) star5Image.setImageResource(R.drawable.ic_baseline_star_24);

        dialogSubmitButton2.setEnabled(count != 0);
    }

    private void rate(String bookingId) {
        dialogProgressBar2.setVisibility(View.VISIBLE);
        setDialogScreenEnabled(false);

        usersRef.child(userId).child("bookingList").
                child(bookingId).child("remarks").setValue(remarksValue);
        usersRef.child(userId).child("bookingList").
                child(bookingId).child("rating").setValue(starValue)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(
                                myContext,
                                "Successfully rated the driver",
                                Toast.LENGTH_LONG
                        ).show();
                        dialog2.dismiss();
                    }
                    else {
                        Toast.makeText(
                                myContext,
                                "Failed to rate the driver",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    dialogProgressBar2.setVisibility(View.GONE);
                    setDialogScreenEnabled(true);
                });
    }

    private void openRemarksDialog(String bookingId, String remarksValue2) {
        etRemarks2.setText(remarksValue2);

        tlRemarks2.setErrorEnabled(false);
        tlRemarks2.setError(null);
        tlRemarks2.setStartIconTintList(cslInitial);

        tlRemarks2.clearFocus();
        tlRemarks2.requestFocus();

        dialogSubmitButton3.setOnClickListener(view -> submitRemarks(bookingId));

        dialog3.show();
    }

    private void initRemarksDialog() {
        dialog3 = new Dialog(myContext);
        dialog3.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog3.setContentView(R.layout.dialog_input_remarks_layout);

        etRemarks2 = dialog3.findViewById(R.id.etRemarks);
        tlRemarks2 = dialog3.findViewById(R.id.tlRemarks);
        dialogSubmitButton3 = dialog3.findViewById(R.id.submitButton);
        dialogCloseImage3 = dialog3.findViewById(R.id.dialogCloseImage);
        dialogProgressBar3 = dialog3.findViewById(R.id.dialogProgressBar);

        etRemarks2.setOnFocusChangeListener((view1, b) -> {
            if(!tlRemarks2.isErrorEnabled()) {
                if(b) {
                    tlRemarks2.setStartIconTintList(cslBlue);
                }
                else {
                    tlRemarks2.setStartIconTintList(cslInitial);
                }
            }
        });

        etRemarks2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                remarksValue2 = etRemarks2.getText().toString().trim();
                dialogSubmitButton3.setEnabled(remarksValue2.length() > 0);
            }
        });

        dialogCloseImage3.setOnClickListener(view -> dialog3.dismiss());

        dialog3.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog3.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
        dialog3.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialog3.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void submitRemarks(String bookingId) {
        dialogProgressBar3.setVisibility(View.VISIBLE);
        setDialogScreenEnabled(false);
        tlRemarks2.setStartIconTintList(cslInitial);

        DatabaseReference reference = inDriverModule ?
                usersRef.child(userId).child("taskList") : usersRef.child(userId).child("bookingList");

        reference.child(bookingId).child("remarks").setValue(remarksValue2)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(
                                myContext,
                                "Successfully submitted the remarks",
                                Toast.LENGTH_LONG
                        ).show();
                        dialog3.dismiss();
                    }
                    else {
                        Toast.makeText(
                                myContext,
                                "Failed to submit the remarks",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    dialogProgressBar3.setVisibility(View.GONE);
                    setDialogScreenEnabled(true);
                });
    }

    private void initMessageDialog() {
        dialogMessage = new Dialog(myContext);
        dialogMessage.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogMessage.setContentView(R.layout.dialog_message);

        dialogMessageCloseImage = dialogMessage.findViewById(R.id.dialogCloseImage);
        tvDialogTitle = dialogMessage.findViewById(R.id.tvDialogTitle);
        tvMessage = dialogMessage.findViewById(R.id.tvMessage);

        dialogMessageCloseImage.setOnClickListener(view -> dialogMessage.dismiss());

        dialogMessage.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogMessage.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
        dialogMessage.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialogMessage.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void getUsers(
            ConstraintLayout driverInfoLayout, ConstraintLayout userInfoLayout,
            String message, String bookingId, String status,
            TextView tvUserFullName, ImageView profileImage,
            TextView tvViewQR, ImageView viewQRImage, TextView tvChat, ImageView chatImage,
            TextView tvDriver, ImageView driverImage, TextView tvPass, ImageView passImage,
            TextView tvStop, ImageView stopImage, TextView tvCheck, ImageView checkImage,
            TextView tvRemarks, ImageView remarksImage,
            TextView tvDriverFullName, ImageView driverProfileImage, TextView tvPassenger,
            TextView tvPlateNumber, String previousDriverUserId, String reason, String remarks,
            ImageView thumbnail, TextView tvViewMessage, TextView tvViewRemarks, TextView tvViewReason,
            int rating
    ) {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = new User(dataSnapshot);
                        users.add(user);
                    }
                }

                String taskDriverUserId = getDriverUserId(bookingId);

                tvViewMessage.setVisibility(View.GONE);
                tvViewReason.setVisibility(View.GONE);
                tvViewRemarks.setVisibility(View.GONE);

                if(message.length() > 0) {
                    tvViewMessage.setVisibility(View.VISIBLE);
                    tvViewMessage.setOnClickListener(view -> {
                        tvDialogTitle.setText("Message");
                        tvMessage.setText(message);
                        dialogMessage.show();
                    });
                }

                if((status.equals("Request") || status.equals("Passed") &&
                        reason != null && reason.length() > 0) &&
                        taskDriverUserId != null && taskDriverUserId.equals(userId)) {

                    tvViewReason.setVisibility(View.VISIBLE);
                    tvViewReason.setOnClickListener(view -> {
                        tvDialogTitle.setText("Reason");
                        tvMessage.setText(reason);
                        dialogMessage.show();
                    });
                }

                boolean hasRemarks = (status.equals("Completed") || status.equals("Cancelled") || status.equals("Failed")) &&
                        remarks != null && remarks.length() > 0;

                if(inDriverModule) {
                    driverInfoLayout.setVisibility(View.GONE);
                    userInfoLayout.setVisibility(View.VISIBLE);

                    getThumbnail(thumbnail, bookingId);

                    if(hasRemarks) {
                        tvViewRemarks.setVisibility(View.VISIBLE);
                        tvViewRemarks.setOnClickListener(view -> {
                            StringBuilder star = new StringBuilder();
                            for(int i = 0; i < rating; i ++) star.append("★");

                            tvDialogTitle.setText("Remarks");
                            tvMessage.setText(remarks);
                            dialogMessage.show();
                        });
                    }

                    getUserInfo(bookingId, status, tvUserFullName, profileImage, tvChat, chatImage,
                            tvDriver, driverImage, tvPass, passImage, tvStop, stopImage,
                            tvCheck, checkImage, tvRemarks, remarksImage, tvPassenger, reason, remarks);
                    if(inDriverModule && (status.equals("Passed") ||
                            previousDriverUserId != null && previousDriverUserId.length() > 0 ||
                            status.equals("Request") && taskDriverUserId != null  && !taskDriverUserId.equals(userId)))
                        getDriverInfo(bookingId, tvDriverFullName, tvPlateNumber,
                                driverProfileImage, driverInfoLayout, status, previousDriverUserId);
                }
                else {
                    userInfoLayout.setVisibility(View.GONE);
                    driverInfoLayout.setVisibility(View.GONE);

                    tvViewQR.setVisibility(View.VISIBLE);
                    viewQRImage.setVisibility(View.VISIBLE);

                    tvViewQR.setOnClickListener(view -> viewQRCode(bookingId));
                    viewQRImage.setOnClickListener(view -> viewQRCode(bookingId));

                    tvChat.setVisibility(View.GONE);
                    chatImage.setVisibility(View.GONE);
                    tvRemarks.setVisibility(View.GONE);
                    remarksImage.setVisibility(View.GONE);

                    if(status.equals("Booked")) {
                        tvChat.setVisibility(View.VISIBLE);
                        chatImage.setVisibility(View.VISIBLE);

                        tvChat.setOnClickListener(view ->
                                openChat(false, bookingId, taskDriverUserId));
                        chatImage.setOnClickListener(view ->
                                openChat(false, bookingId, taskDriverUserId));
                    }
                    else if((status.equals("Cancelled") || status.equals("Failed")) &&
                            (remarks == null || remarks.length() == 0)) {
                        tvRemarks.setVisibility(View.VISIBLE);
                        remarksImage.setVisibility(View.VISIBLE);

                        tvRemarks.setOnClickListener(view -> openRemarksDialog(bookingId, remarks));
                        remarksImage.setOnClickListener(view -> openRemarksDialog(bookingId, remarks));
                    }

                    if(hasRemarks) {
                        tvViewRemarks.setVisibility(View.VISIBLE);
                        tvViewRemarks.setOnClickListener(view -> {
                            StringBuilder star = new StringBuilder();
                            for(int i = 0; i < rating; i ++) star.append("★");

                            tvDialogTitle.setText("Remarks");
                            tvMessage.setText(star + " (" + rating + ") " + remarks);
                            dialogMessage.show();
                        });
                    }

                    getDriverInfo(bookingId, tvDriverFullName, tvPlateNumber,
                            driverProfileImage, driverInfoLayout, status, previousDriverUserId);
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

    private void openOnlinePayment(String bookingId) {
        Intent intent = new Intent(myContext, OnlinePaymentActivity.class);
        intent.putExtra("bookingId", bookingId);
        myContext.startActivity(intent);
    }

    private void openChat(boolean inDriverModule, String taskId, String taskDriverUserId) {
        Intent intent = new Intent(myContext, ChatActivity.class);
        intent.putExtra("taskId", taskId);
        intent.putExtra("inDriverModule", inDriverModule);
        if(!inDriverModule) intent.putExtra("driverUserId", taskDriverUserId);
        myContext.startActivity(intent);
    }

    private void getDriverInfo(String bookingId, TextView tvDriverFullName, TextView tvPlateNumber,
                               ImageView driverProfileImage, ConstraintLayout driverInfoLayout, String status,
                               String previousDriverUserId) {
        for(User user : users) {
            if(previousDriverUserId != null && previousDriverUserId.length() > 0 &&
                    user.getId().equals(previousDriverUserId) &&
                    !status.equals("Request") && !status.equals("Passed")) {
                String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
                if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();
                tvDriverFullName.setText(fromHtml(fullName));

                String plateNumber = "Previous Driver";
                tvPlateNumber.setText(plateNumber);

                try {
                    Glide.with(myContext).load(user.getProfileImage())
                            .placeholder(R.drawable.image_loading_placeholder)
                            .into(driverProfileImage);
                }
                catch (Exception ignored) {}

                driverInfoLayout.setVisibility(View.VISIBLE);

                return;
            }
            else if(!status.equals("Booked") || previousDriverUserId == null || previousDriverUserId.length() == 0) {
                List<Booking> taskList = user.getTaskList();
                for(Booking task : taskList) {
                    if(task.getId().equals(bookingId) && !task.getStatus().equals("Passed")) {
                        String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
                        if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();
                        tvDriverFullName.setText(fromHtml(fullName));

                        String plateNumber = "<b>Plate Number</b>: " + user.getPlateNumber();
                        if(status.equals("Request")) plateNumber = "Driver on Request";
                        if(status.equals("Passed")) plateNumber = "Current Driver";
                        tvPlateNumber.setText(fromHtml(plateNumber));

                        try {
                            Glide.with(myContext).load(user.getProfileImage())
                                    .placeholder(R.drawable.image_loading_placeholder)
                                    .into(driverProfileImage);
                        }
                        catch (Exception ignored) {}

                        driverInfoLayout.setVisibility(View.VISIBLE);

                        return;
                    }
                }
            }
        }
    }

    private String getDriverUserId(String bookingId) {
        for(User user : users) {
            List<Booking> taskList = user.getTaskList();
            for(Booking task : taskList) {
                if(task.getId().equals(bookingId) && !task.getStatus().equals("Passed")) {
                    return user.getId();
                }
            }
        }
        return null;
    }

    private void viewQRCode(String bookingId) {
        MultiFormatWriter writer = new MultiFormatWriter();

        try {
            BitMatrix matrix = writer.encode(bookingId, BarcodeFormat.QR_CODE,
                    1024, 1024);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);

            qrCodeImage.setImageBitmap(bitmap);
            qrCodeDialog.show();
        } catch (WriterException e) {
            e.printStackTrace();

            Toast.makeText(
                    myContext,
                    "Failed to view QR Code. Please try again later.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void initQRCodeDialog() {
        qrCodeDialog = new Dialog(myContext);
        qrCodeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        qrCodeDialog.setContentView(R.layout.dialog_qrcode_layout);

        qrCodeDialogCloseImage = qrCodeDialog.findViewById(R.id.dialogCloseImage);
        qrCodeImage = qrCodeDialog.findViewById(R.id.qrCodeImage);

        qrCodeDialogCloseImage.setOnClickListener(view -> qrCodeDialog.dismiss());

        qrCodeDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        qrCodeDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_white_layout));
    }

    private void openMap(Station startStation) {
        Intent intent = new Intent(myContext, MapActivity.class);
        intent.putExtra("id", startStation.getId());
        intent.putExtra("lat", startStation.getLat());
        intent.putExtra("lng", startStation.getLng());
        intent.putExtra("name", startStation.getName());
        intent.putExtra("type", 1);
        myContext.startActivity(intent);
    }

    private void openMap2(double originLat, double originLng) {
        Intent intent = new Intent(myContext, MapActivity.class);
        intent.putExtra("id", 0);
        intent.putExtra("lat", originLat);
        intent.putExtra("lng", originLng);
        intent.putExtra("name", "Origin Location");
        intent.putExtra("type", 2);
        myContext.startActivity(intent);
    }

    private void openMap3(SimpleTouristSpot destinationSpot) {
        Intent intent = new Intent(myContext, MapActivity.class);
        intent.putExtra("id", destinationSpot.getId());
        intent.putExtra("lat", destinationSpot.getLat());
        intent.putExtra("lng", destinationSpot.getLng());
        intent.putExtra("name", destinationSpot.getName());
        intent.putExtra("type", 0);
        myContext.startActivity(intent);
    }

    private void openItem(Booking booking, boolean isScanning) {
        boolean isOnTheSpot = booking.getBookingType().getId().equals("BT99");

        Intent intent;

        if(isOnTheSpot) intent = new Intent(myContext, OnTheSpotActivity.class);
        else intent = new Intent(myContext, RouteActivity.class);

        intent.putExtra("bookingId", booking.getId());
        intent.putExtra("inDriverModule", inDriverModule);
        if(inDriverModule) {
            intent.putExtra("isScanning", isScanning);
            intent.putExtra("status", booking.getStatus());
            intent.putExtra("previousDriverUserId", booking.getPreviousDriverUserId());
            intent.putExtra("userId", getPassengerUserId(booking.getId()));
        }
        else {
            if(!isOnTheSpot) {
                boolean isLatest = bookingList.get(0).getId().equals(booking.getId()) &&
                        booking.getStatus().equals("Completed") &&
                        !booking.getBookingType().getId().equals("BT99");

                intent.putExtra("isLatest", isLatest);
            }
        }
        myContext.startActivity(intent);
    }

    private String getPassengerUserId(String bookingId) {
        for(User user : users) {
            List<Booking> bookingList = user.getBookingList();
            for(Booking booking : bookingList) {
                if(booking.getId().equals(bookingId)) {
                    return user.getId();
                }
            }
        }
        return null;
    }

    private void getUserInfo(String bookingId, String status,
                             TextView tvUserFullName, ImageView profileImage,
                             TextView tvChat, ImageView chatImage,
                             TextView tvDriver, ImageView driverImage,
                             TextView tvPass, ImageView passImage,
                             TextView tvStop, ImageView stopImage,
                             TextView tvCheck, ImageView checkImage,
                             TextView tvRemarks, ImageView remarksImage,
                             TextView tvPassenger, String reason, String remarks) {
        for(User user : users) {
            List<Booking> bookingList = user.getBookingList();
            for(Booking booking : bookingList) {
                if(booking.getId().equals(bookingId)) {
                    String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
                    if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();
                    tvUserFullName.setText(fromHtml(fullName));

                    try {
                        Glide.with(myContext).load(user.getProfileImage())
                                .placeholder(R.drawable.image_loading_placeholder)
                                .into(profileImage);
                    }
                    catch (Exception ignored) {}

                    String taskDriverUserId = getDriverUserId(booking.getId());

                    String takeTask = "Take Task";
                    tvDriver.setText(takeTask);

                    tvDriver.setEnabled(true);
                    driverImage.setEnabled(true);

                    tvDriver.setTextColor(colorBlue);
                    driverImage.getDrawable().setTint(colorBlue);

                    booking.setStatus(status);

                    tvPassenger.setText(defaultPassengerText);
                    tvPassenger.setTextColor(colorInitial);

                    switch (status) {
                        case "Pending":
                            tvChat.setVisibility(View.GONE);
                            chatImage.setVisibility(View.GONE);

                            if (userId.equals(user.getId()) || ongoingTaskList.size() > 0) {
                                if(ongoingTaskList.size() > 0) {
                                    takeTask = "Currently Unavailable";
                                    tvDriver.setText(takeTask);

                                    tvDriver.setVisibility(View.VISIBLE);
                                    driverImage.setVisibility(View.VISIBLE);

                                    tvDriver.setEnabled(false);
                                    driverImage.setEnabled(false);

                                    tvDriver.setTextColor(colorInitial);
                                    driverImage.getDrawable().setTint(colorInitial);
                                }
                                else {
                                    tvDriver.setVisibility(View.GONE);
                                    driverImage.setVisibility(View.GONE);
                                }
                            }
                            else {
                                tvDriver.setVisibility(View.VISIBLE);
                                driverImage.setVisibility(View.VISIBLE);

                                tvDriver.setOnClickListener(view -> takeTask(booking, user.getId(), false));
                                driverImage.setOnClickListener(view -> takeTask(booking, user.getId(), false));
                            }

                            tvPass.setVisibility(View.GONE);
                            passImage.setVisibility(View.GONE);
                            tvStop.setVisibility(View.GONE);
                            stopImage.setVisibility(View.GONE);
                            tvCheck.setVisibility(View.GONE);
                            checkImage.setVisibility(View.GONE);
                            tvRemarks.setVisibility(View.GONE);
                            remarksImage.setVisibility(View.GONE);
                            break;
                        case "Booked":
                            tvChat.setVisibility(View.VISIBLE);
                            chatImage.setVisibility(View.VISIBLE);

                            tvChat.setOnClickListener(view ->
                                    openChat(true, bookingId, taskDriverUserId));
                            chatImage.setOnClickListener(view ->
                                    openChat(true, bookingId, taskDriverUserId));

                            tvDriver.setVisibility(View.GONE);
                            driverImage.setVisibility(View.GONE);
                            tvPass.setVisibility(View.VISIBLE);
                            passImage.setVisibility(View.VISIBLE);

                            tvPass.setOnClickListener(view -> openReasonDialog(booking, reason));
                            passImage.setOnClickListener(view -> openReasonDialog(booking, reason));

                            tvStop.setVisibility(View.GONE);
                            stopImage.setVisibility(View.GONE);
                            tvCheck.setText(initiateService);
                            tvCheck.setVisibility(View.VISIBLE);
                            checkImage.setVisibility(View.VISIBLE);

                            tvCheck.setOnClickListener(view -> openItem(booking, true));
                            checkImage.setOnClickListener(view -> openItem(booking, true));

                            tvRemarks.setVisibility(View.GONE);
                            remarksImage.setVisibility(View.GONE);
                            break;
                        case "Request":
                            if(userId.equals(taskDriverUserId) || ongoingTaskList.size() > 0) {
                                tvPassenger.setText(requestText);
                                tvPassenger.setTextColor(colorGreen);

                                tvChat.setVisibility(View.VISIBLE);
                                chatImage.setVisibility(View.VISIBLE);

                                tvChat.setOnClickListener(view ->
                                        openChat(true, bookingId, taskDriverUserId));
                                chatImage.setOnClickListener(view ->
                                        openChat(true, bookingId, taskDriverUserId));

                                if(ongoingTaskList.size() > 0) {
                                    takeTask = "Currently Unavailable";
                                    tvDriver.setText(takeTask);

                                    tvDriver.setVisibility(View.VISIBLE);
                                    driverImage.setVisibility(View.VISIBLE);

                                    tvDriver.setEnabled(false);
                                    driverImage.setEnabled(false);

                                    tvDriver.setTextColor(colorInitial);
                                    driverImage.getDrawable().setTint(colorInitial);
                                }
                                else {
                                    tvDriver.setVisibility(View.GONE);
                                    driverImage.setVisibility(View.GONE);
                                }

                                tvPass.setVisibility(View.GONE);
                                passImage.setVisibility(View.GONE);
                                tvStop.setVisibility(View.VISIBLE);
                                stopImage.setVisibility(View.VISIBLE);

                                tvStop.setOnClickListener(view -> stopRequest(booking));
                                stopImage.setOnClickListener(view -> stopRequest(booking));

                                tvCheck.setText(initiateService);
                                tvCheck.setVisibility(View.VISIBLE);
                                checkImage.setVisibility(View.VISIBLE);

                                tvCheck.setOnClickListener(view -> openItem(booking, true));
                                checkImage.setOnClickListener(view -> openItem(booking, true));

                            }
                            else {
                                tvChat.setVisibility(View.GONE);
                                chatImage.setVisibility(View.GONE);

                                if(userId.equals(user.getId())) {
                                    tvDriver.setVisibility(View.GONE);
                                    driverImage.setVisibility(View.GONE);
                                }
                                else {
                                    tvDriver.setVisibility(View.VISIBLE);
                                    driverImage.setVisibility(View.VISIBLE);

                                    tvDriver.setOnClickListener(view -> takeTask(booking, user.getId(), true));
                                    driverImage.setOnClickListener(view -> takeTask(booking, user.getId(), true));
                                }

                                tvPass.setVisibility(View.GONE);
                                passImage.setVisibility(View.GONE);
                                tvStop.setVisibility(View.GONE);
                                stopImage.setVisibility(View.GONE);
                                tvCheck.setVisibility(View.GONE);
                                checkImage.setVisibility(View.GONE);
                            }
                            tvRemarks.setVisibility(View.GONE);
                            remarksImage.setVisibility(View.GONE);
                            break;
                        case "Ongoing":
                            tvChat.setVisibility(View.GONE);
                            chatImage.setVisibility(View.GONE);
                            tvDriver.setVisibility(View.GONE);
                            driverImage.setVisibility(View.GONE);
                            tvPass.setVisibility(View.GONE);
                            passImage.setVisibility(View.GONE);
                            tvStop.setVisibility(View.GONE);
                            stopImage.setVisibility(View.GONE);

                            tvCheck.setText(dropOffText);
                            tvCheck.setVisibility(View.VISIBLE);
                            checkImage.setVisibility(View.VISIBLE);

                            tvCheck.setOnClickListener(view -> completeTask(booking));
                            checkImage.setOnClickListener(view -> completeTask(booking));

                            tvRemarks.setVisibility(View.GONE);
                            remarksImage.setVisibility(View.GONE);
                            break;
                        default:
                            tvChat.setVisibility(View.GONE);
                            chatImage.setVisibility(View.GONE);
                            tvDriver.setVisibility(View.GONE);
                            driverImage.setVisibility(View.GONE);
                            tvPass.setVisibility(View.GONE);
                            passImage.setVisibility(View.GONE);
                            tvStop.setVisibility(View.GONE);
                            stopImage.setVisibility(View.GONE);
                            tvCheck.setVisibility(View.GONE);
                            checkImage.setVisibility(View.GONE);

                            if(remarks == null || remarks.length() == 0) {
                                tvRemarks.setVisibility(View.VISIBLE);
                                remarksImage.setVisibility(View.VISIBLE);
                            }

                            tvRemarks.setOnClickListener(view -> openRemarksDialog(bookingId, remarks));
                            remarksImage.setOnClickListener(view -> openRemarksDialog(bookingId, remarks));
                            break;
                    }

                    break;
                }
            }
        }
    }

    private void completeTask(Booking booking) {
        String taskDriverUserId = getDriverUserId(booking.getId());
        String passengerId = getPassengerUserId(booking.getId());

        if(taskDriverUserId != null && passengerId != null) {
            usersRef.child(passengerId).child("bookingList").
                    child(booking.getId()).child("dropOffTime").
                    setValue(new DateTimeToString().getDateAndTime());

            usersRef.child(taskDriverUserId).child("taskList").
                    child(booking.getId()).child("status").setValue("Completed");
            usersRef.child(taskDriverUserId).child("taskList").
                    child(booking.getId()).child("dropOffTime").
                    setValue(new DateTimeToString().getDateAndTime());

            Toast.makeText(
                    myContext,
                    "The Task is now Completed",
                    Toast.LENGTH_SHORT
            ).show();
        }
        else {
            Toast.makeText(
                    myContext,
                    "Failed to complete the task",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void stopRequest(Booking booking) {
        if(onActionClickListener != null)
            onActionClickListener.setProgressBarToVisible(true);
        usersRef.child(userId).child("taskList").
                child(booking.getId()).child("status").setValue("Booked")
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(
                                myContext,
                                "You stopped your task's request",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    else {
                        Toast.makeText(
                                myContext,
                                "Failed to stop the task's request",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    if(onActionClickListener != null)
                        onActionClickListener.setProgressBarToVisible(false);
                });
    }

    private void passTask(Booking booking) {
        usersRef.child(userId).child("taskList").
                child(booking.getId()).child("status").setValue("Request")
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(
                                myContext,
                                "Your task is now on request",
                                Toast.LENGTH_LONG
                        ).show();
                        dialog.dismiss();
                    }
                    else {
                        Toast.makeText(
                                myContext,
                                "Failed to pass the task",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    dialogProgressBar.setVisibility(View.GONE);
                    setDialogScreenEnabled(true);
                });
    }

    private void takeTask(Booking booking, String passengerUserId, boolean fromRequest) {
        if(onActionClickListener != null)
            onActionClickListener.setProgressBarToVisible(true);
        String status = "Booked";
        booking.setTimestamp(new DateTimeToString().getDateAndTime());
        Booking driverTask = new Booking(booking);
        driverTask.setStatus(status);

        String taskDriverUserId = getDriverUserId(booking.getId());

        if(fromRequest) {
            driverTask.setPreviousDriverUserId(taskDriverUserId);
            if(taskDriverUserId == null) return;
            usersRef.child(taskDriverUserId).child("taskList").
                    child(driverTask.getId()).child("status").setValue("Passed");
        }

        DatabaseReference bookingListRef = usersRef.child(passengerUserId).
                child("bookingList").child(driverTask.getId());

        DatabaseReference taskListRef = usersRef.child(userId).child("taskList").
                child(booking.getId());
        taskListRef.setValue(driverTask).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                bookingListRef.child("notified").setValue(false);
                bookingListRef.child("read").setValue(false);
                bookingListRef.child("status").setValue(status).
                        addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()) {
                                Toast.makeText(
                                        myContext,
                                        "Successfully taken the task",
                                        Toast.LENGTH_SHORT
                                ).show();

                                if(onActionClickListener != null)
                                    onActionClickListener.setProgressBarToVisible(false);
                            }
                            else errorTask();
                        });
            }
            else errorTask();
        });
    }

    private void errorTask() {
        Toast.makeText(
                myContext,
                "Failed to take the task. Please try again.",
                Toast.LENGTH_LONG
        ).show();

        if(onActionClickListener != null)
            onActionClickListener.setProgressBarToVisible(false);
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

    private void openOption(ConstraintLayout buttonLayout, ConstraintLayout backgroundLayout,
                            ImageView moreImage, Handler optionHandler, Runnable optionRunnable,
                            TextView tvOption) {
        moreImage.setEnabled(false);

        optionHandler.removeCallbacks(optionRunnable);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(backgroundLayout);

        constraintSet.clear(buttonLayout.getId(), ConstraintSet.TOP);
        constraintSet.connect(buttonLayout.getId(), ConstraintSet.BOTTOM,
                moreImage.getId(), ConstraintSet.BOTTOM);

        setTransition(buttonLayout);
        constraintSet.applyTo(backgroundLayout);

        tvOption.setText("true");
        moreImage.setEnabled(true);
        moreImage.setImageResource(R.drawable.ic_baseline_close_24);
        moreImage.getDrawable().setTint(myContext.getResources().getColor(R.color.red));

        optionHandler.postDelayed(optionRunnable, 3000);
    }

    private void closeOption(ConstraintLayout buttonLayout, ConstraintLayout backgroundLayout,
                             ImageView moreImage, TextView tvOption) {
        moreImage.setEnabled(false);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(backgroundLayout);

        constraintSet.clear(buttonLayout.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(buttonLayout.getId(), ConstraintSet.TOP,
                backgroundLayout.getId(), ConstraintSet.BOTTOM);

        setTransition(buttonLayout);
        constraintSet.applyTo(backgroundLayout);

        tvOption.setText("false");
        moreImage.setEnabled(true);
        moreImage.setImageResource(R.drawable.ic_baseline_more_horiz_24);
        moreImage.getDrawable().setTint(myContext.getResources().getColor(R.color.black));
    }

    private void setTransition(ConstraintLayout constraintLayout) {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);
        TransitionManager.beginDelayedTransition(constraintLayout, transition);
    }

    private int dpToPx(int dp) {
        float px = dp * myContext.getResources().getDisplayMetrics().density;
        return (int) px;
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage, driverProfileImage, thumbnail, moreImage,
                openImage, locateImage, locateEndImage, onlinePaymentImage, viewQRImage, chatImage,
                driverImage, passImage, stopImage, checkImage, rateImage, remarksImage, paidImage;
        TextView tvUserFullName, tvPassenger, tvDriverFullName, tvPlateNumber, tvPickUpTime, tvDropOffTime,
                tvBookingId, tvSchedule, tvTypeName, tvPrice, tvStartStation, tvStartStation2, tvEndStation,
                tvEndStation2, tvOption, tvOpen, tvLocate, tvLocateEnd, tvOnlinePayment, tvViewQR,
                tvChat, tvDriver, tvPass, tvStop, tvCheck, tvRate, tvRemarks, tvViewMessage, tvViewRemarks, tvViewReason;
        ConstraintLayout backgroundLayout, buttonLayout, userInfoLayout, driverInfoLayout, timeInfoLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userInfoLayout = itemView.findViewById(R.id.userInfoLayout);
            tvUserFullName = itemView.findViewById(R.id.tvUserFullName);
            tvPassenger = itemView.findViewById(R.id.tvPassenger);
            profileImage = itemView.findViewById(R.id.profileImage);

            driverInfoLayout = itemView.findViewById(R.id.driverInfoLayout);
            tvDriverFullName = itemView.findViewById(R.id.tvDriverFullName);
            tvPlateNumber = itemView.findViewById(R.id.tvPlateNumber);
            driverProfileImage = itemView.findViewById(R.id.driverProfileImage);

            timeInfoLayout = itemView.findViewById(R.id.timeInfoLayout);
            tvPickUpTime = itemView.findViewById(R.id.tvPickUpTime);
            tvDropOffTime = itemView.findViewById(R.id.tvDropOffTime);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvSchedule = itemView.findViewById(R.id.tvSchedule);
            tvTypeName = itemView.findViewById(R.id.tvTypeName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStartStation = itemView.findViewById(R.id.tvStartStation);
            tvStartStation2 = itemView.findViewById(R.id.tvStartStation2);
            tvEndStation = itemView.findViewById(R.id.tvEndStation);
            tvEndStation2 = itemView.findViewById(R.id.tvEndStation2);

            tvViewMessage = itemView.findViewById(R.id.tvViewMessage);
            tvViewRemarks = itemView.findViewById(R.id.tvViewRemarks);
            tvViewReason = itemView.findViewById(R.id.tvViewReason);

            tvOption = itemView.findViewById(R.id.tvOption);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            buttonLayout = itemView.findViewById(R.id.buttonLayout);
            moreImage = itemView.findViewById(R.id.moreImage);
            paidImage = itemView.findViewById(R.id.paidImage);

            tvOpen = itemView.findViewById(R.id.tvOpen);
            openImage = itemView.findViewById(R.id.openImage);
            tvLocate = itemView.findViewById(R.id.tvLocate);
            locateImage = itemView.findViewById(R.id.locateImage);
            tvLocateEnd = itemView.findViewById(R.id.tvLocateEnd);
            locateEndImage = itemView.findViewById(R.id.locateEndImage);

            tvOnlinePayment = itemView.findViewById(R.id.tvOnlinePayment);
            onlinePaymentImage = itemView.findViewById(R.id.onlinePaymentImage);
            tvViewQR = itemView.findViewById(R.id.tvViewQR);
            viewQRImage = itemView.findViewById(R.id.viewQRImage);
            tvChat = itemView.findViewById(R.id.tvChat);
            chatImage = itemView.findViewById(R.id.chatImage);
            tvDriver = itemView.findViewById(R.id.tvDriver);
            driverImage = itemView.findViewById(R.id.driverImage);
            tvPass = itemView.findViewById(R.id.tvPass);
            passImage = itemView.findViewById(R.id.passImage);
            tvStop = itemView.findViewById(R.id.tvStop);
            stopImage = itemView.findViewById(R.id.stopImage);
            tvCheck = itemView.findViewById(R.id.tvCheck);
            checkImage = itemView.findViewById(R.id.checkImage);
            tvRate = itemView.findViewById(R.id.tvRate);
            rateImage = itemView.findViewById(R.id.rateImage);
            tvRemarks = itemView.findViewById(R.id.tvRemarks);
            remarksImage = itemView.findViewById(R.id.remarksImage);

            setIsRecyclable(false);
        }
    }

    public void setOnGoingTaskList(List<Booking> ongoingTaskList) {
        this.ongoingTaskList.clear();
        this.ongoingTaskList.addAll(ongoingTaskList);
        notifyDataSetChanged();
    }
}