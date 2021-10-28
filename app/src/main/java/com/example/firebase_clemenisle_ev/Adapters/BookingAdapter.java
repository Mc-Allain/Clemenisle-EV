package com.example.firebase_clemenisle_ev.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
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
import com.example.firebase_clemenisle_ev.R;
import com.example.firebase_clemenisle_ev.RouteActivity;
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
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
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

    String userId;
    boolean isLoggedIn = false;

    String startStationText = "Start Station", endStationText = "End Station";
    String destinationSpotText = "Destination Spot", originLocationText = "Origin Location";

    String locateStartStationText = "Locate Start Station",
            locateEndStationText = "Locate End Station";
    String locateOriginLocationText = "Locate Origin Location",
            locateDestinationSpotText = "Locate Destination Spot";

    boolean inDriverMode = false;

    Dialog qrCodeDialog;
    ImageView qrCodeDialogCloseImage, qrCodeImage;

    List<User> users = new ArrayList<>();

    String taskDriverUserId;

    public void setInDriverMode(boolean inDriverMode) {
        this.inDriverMode = inDriverMode;
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
        editor.putBoolean("remember", false);
        editor.putString("emailAddress", null);
        editor.putString("password", null);
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
                locateEndImage = holder.locateEndImage, viewQRImage = holder.viewQRImage,
                chatImage = holder.chatImage,
                driverImage = holder.driverImage, passImage = holder.passImage,
                checkImage = holder.checkImage, paidImage = holder.paidImage;
        TextView tvUserFullName = holder.tvUserFullName, tvPassTaskNote = holder.tvPassTaskNote,
                tvDriverFullName = holder.tvDriverFullName, tvBookingId = holder.tvBookingId,
                tvSchedule = holder.tvSchedule, tvTypeName = holder.tvTypeName, tvPrice = holder.tvPrice,
                tvStartStation = holder.tvStartStation, tvEndStation = holder.tvEndStation,
                tvStartStation2 = holder.tvStartStation2, tvEndStation2 = holder.tvEndStation2,
                tvOption = holder.tvOption, tvOpen = holder.tvOpen,
                tvLocate = holder.tvLocate, tvLocateEnd = holder.tvLocateEnd,
                tvChat = holder.tvChat,
                tvViewQR = holder.tvViewQR, tvDriver = holder.tvDriver,
                tvPass = holder.tvPass, tvCheck = holder.tvCheck;
        ExpandableTextView extvMessage = holder.extvMessage;
        ConstraintLayout backgroundLayout = holder.backgroundLayout, buttonLayout = holder.buttonLayout,
                userInfoLayout = holder.userInfoLayout, driverInfoLayout = holder.driverInfoLayout;

        myContext = inflater.getContext();

        initSharedPreferences();
        initQRCodeDialog();

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

        Handler optionHandler = new Handler();
        Runnable optionRunnable = () -> closeOption(buttonLayout, backgroundLayout, moreImage, tvOption);

        Booking booking = bookingList.get(position);

        String status = booking.getStatus();

        String bookingId = booking.getId();
        String schedule = booking.getSchedule();

        BookingType bookingType = booking.getBookingType();
        String typeName = bookingType.getName();
        String price = "₱" + bookingType.getPrice();
        if(price.split("\\.")[1].length() == 1) price += 0;

        String message = booking.getMessage();

        tvBookingId.setText(bookingId);
        tvSchedule.setText(schedule);
        tvPrice.setText(price);
        tvTypeName.setText(typeName);

        Resources resources = myContext.getResources();
        int color = 0;
        Drawable backgroundDrawable = resources.getDrawable(R.color.blue);

        switch (status) {
            case "Processing":
                color = resources.getColor(R.color.orange);
                backgroundDrawable = resources.getDrawable(R.color.orange);
                break;
            case "Request":
            case "Booked":
                color = resources.getColor(R.color.green);
                backgroundDrawable = resources.getDrawable(R.color.green);
                break;
            case "Completed":
                color = resources.getColor(R.color.blue);
                backgroundDrawable = resources.getDrawable(R.color.blue);
                break;
            case "Cancelled":
            case "Failed":
                color = resources.getColor(R.color.red);
                backgroundDrawable = resources.getDrawable(R.color.red);
                break;
        }

        tvBookingId.setBackground(backgroundDrawable);
        tvPrice.setTextColor(color);

        if(!bookingType.getId().equals("BT99")) {
            Station startStation = booking.getStartStation();
            Station endStation = booking.getEndStation();

            String img;
            List<Route> routeList = bookingList.get(position).getRouteList();
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

        getUsers(driverInfoLayout, userInfoLayout, extvMessage, message, bookingId, status, tvUserFullName,
                profileImage, tvChat, chatImage, tvDriver, driverImage, tvPass, passImage, tvCheck, checkImage,
                tvViewQR, viewQRImage, tvDriverFullName, driverProfileImage, tvPassTaskNote);

        int top = dpToPx(4), bottom = dpToPx(4);

        boolean isFirstItem = position + 1 == 1, isLastItem = position + 1 == getItemCount();

        if(isFirstItem) {
            top = dpToPx(8);
        }
        if(isLastItem) {
            if(status.equals("Failed")) {
                bottom = dpToPx(88);
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

    private void getUsers(
            ConstraintLayout driverInfoLayout, ConstraintLayout userInfoLayout,
            ExpandableTextView extvMessage, String message, String bookingId, String status,
            TextView tvUserFullName, ImageView profileImage, TextView tvChat, ImageView chatImage,
            TextView tvDriver, ImageView driverImage, TextView tvPass, ImageView passImage,
            TextView tvCheck, ImageView checkImage, TextView tvViewQR, ImageView viewQRImage,
            TextView tvDriverFullName, ImageView driverProfileImage, TextView tvPassTaskNote
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

                if(inDriverMode) {
                    driverInfoLayout.setVisibility(View.GONE);
                    userInfoLayout.setVisibility(View.VISIBLE);

                    extvMessage.setText(message);
                    getUserInfo(bookingId, status, tvUserFullName, profileImage, tvChat, chatImage,
                            tvDriver, driverImage, tvPass, passImage, tvCheck, checkImage, tvPassTaskNote);
                }
                else {
                    userInfoLayout.setVisibility(View.GONE);
                    driverInfoLayout.setVisibility(View.GONE);

                    tvViewQR.setVisibility(View.VISIBLE);
                    viewQRImage.setVisibility(View.VISIBLE);

                    tvViewQR.setOnClickListener(view -> viewQRCode(bookingId));
                    viewQRImage.setOnClickListener(view -> viewQRCode(bookingId));

                    if(status.equals("Booked")) {
                        tvChat.setVisibility(View.VISIBLE);
                        chatImage.setVisibility(View.VISIBLE);

                        tvChat.setOnClickListener(view -> openChat(false, bookingId));
                        chatImage.setOnClickListener(view -> openChat(false, bookingId));
                    }
                    else {
                        tvChat.setVisibility(View.GONE);
                        chatImage.setVisibility(View.GONE);
                    }

                    extvMessage.setText(null);
                    getDriverInfo(bookingId, tvDriverFullName, driverProfileImage, driverInfoLayout);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void openChat(boolean inDriverMode, String bookingId) {
        Intent intent = new Intent(myContext, ChatActivity.class);
        intent.putExtra("bookingId", bookingId);
        intent.putExtra("inDriverMode", inDriverMode);
        myContext.startActivity(intent);
    }

    private void getDriverInfo(String bookingId, TextView tvDriverFullName,
                               ImageView driverProfileImage, ConstraintLayout driverInfoLayout) {
        for(User user : users) {
            List<Booking> taskList = user.getTaskList();
            for(Booking booking : taskList) {
                if(booking.getId().equals(bookingId)) {
                    String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
                    if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();
                    tvDriverFullName.setText(fromHtml(fullName));

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

    private void getDriverUserId(String bookingId) {
        for(User user : users) {
            List<Booking> taskList = user.getTaskList();
            for(Booking booking : taskList) {
                if(booking.getId().equals(bookingId)) {
                    taskDriverUserId = user.getId();
                    return;
                }
            }
        }
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
        qrCodeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
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
        intent.putExtra("name", "Your Location");
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

        if(isOnTheSpot)
            intent = new Intent(myContext, OnTheSpotActivity.class);
        else
            intent = new Intent(myContext, RouteActivity.class);

        intent.putExtra("bookingId", booking.getId());
        intent.putExtra("inDriverMode", inDriverMode);
        if(inDriverMode) {
            intent.putExtra("isScanning", isScanning);
            intent.putExtra("status", booking.getStatus());
            getPassengerUserId(booking.getId(), intent);
        }
        else {
            if(!isOnTheSpot) {
                boolean isLatest = bookingList.get(0).getId().equals(booking.getId()) &&
                        booking.getStatus().equals("Completed") &&
                        !booking.getBookingType().getId().equals("BT99");

                intent.putExtra("isLatest", isLatest);
                intent.putExtra("status", booking.getStatus());
            }
            myContext.startActivity(intent);
        }
    }

    private void getPassengerUserId(String bookingId, Intent intent) {
        for(User user : users) {
            List<Booking> bookingList = user.getBookingList();
            for(Booking booking : bookingList) {
                if(booking.getId().equals(bookingId)) {
                    intent.putExtra("userId", user.getId());
                    myContext.startActivity(intent);
                    return;
                }
            }
        }
    }

    private void getUserInfo(String bookingId, String status,
                             TextView tvUserFullName, ImageView profileImage,
                             TextView tvChat, ImageView chatImage,
                             TextView tvDriver, ImageView driverImage,
                             TextView tvPass, ImageView passImage,
                             TextView tvCheck, ImageView checkImage,
                             TextView tvPassTaskNote) {
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

                    switch (status) {
                        case "Processing":
                            tvChat.setVisibility(View.GONE);
                            chatImage.setVisibility(View.GONE);

                            if (userId.equals(user.getId())) {
                                tvDriver.setVisibility(View.GONE);
                                driverImage.setVisibility(View.GONE);
                            }
                            else {
                                tvDriver.setVisibility(View.VISIBLE);
                                driverImage.setVisibility(View.VISIBLE);

                                tvDriver.setOnClickListener(view -> takeTask(booking, user.getId(), false));
                                driverImage.setOnClickListener(view -> takeTask(booking, user.getId(), false));
                            }

                            tvPass.setVisibility(View.GONE);
                            passImage.setVisibility(View.GONE);
                            tvCheck.setVisibility(View.GONE);
                            checkImage.setVisibility(View.GONE);
                            break;
                        case "Booked":
                            tvChat.setVisibility(View.VISIBLE);
                            chatImage.setVisibility(View.VISIBLE);

                            tvChat.setOnClickListener(view -> openChat(true, bookingId));
                            chatImage.setOnClickListener(view -> openChat(true, bookingId));

                            tvDriver.setVisibility(View.GONE);
                            driverImage.setVisibility(View.GONE);
                            tvPass.setVisibility(View.VISIBLE);
                            passImage.setVisibility(View.VISIBLE);

                            tvPass.setOnClickListener(view -> passTask(booking));
                            passImage.setOnClickListener(view -> passTask(booking));

                            tvCheck.setVisibility(View.VISIBLE);
                            checkImage.setVisibility(View.VISIBLE);

                            tvCheck.setOnClickListener(view -> openItem(booking, true));
                            checkImage.setOnClickListener(view -> openItem(booking, true));
                            break;
                        case "Request":
                            getDriverUserId(bookingId);

                            tvPassTaskNote.setVisibility(View.GONE);

                            if(userId.equals(taskDriverUserId)) {
                                tvPassTaskNote.setVisibility(View.VISIBLE);

                                tvChat.setVisibility(View.VISIBLE);
                                chatImage.setVisibility(View.VISIBLE);

                                tvChat.setOnClickListener(view -> openChat(true, bookingId));
                                chatImage.setOnClickListener(view -> openChat(true, bookingId));

                                tvDriver.setVisibility(View.GONE);
                                driverImage.setVisibility(View.GONE);
                                tvPass.setVisibility(View.GONE);
                                passImage.setVisibility(View.GONE);
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
                                tvCheck.setVisibility(View.GONE);
                                checkImage.setVisibility(View.GONE);
                            }
                            break;
                        default:
                            tvChat.setVisibility(View.GONE);
                            chatImage.setVisibility(View.GONE);
                            tvDriver.setVisibility(View.GONE);
                            driverImage.setVisibility(View.GONE);
                            tvPass.setVisibility(View.GONE);
                            passImage.setVisibility(View.GONE);
                            tvCheck.setVisibility(View.GONE);
                            checkImage.setVisibility(View.GONE);
                            break;
                    }

                    break;
                }
            }
        }
    }

    private void passTask(Booking booking) {
        usersRef.child(userId).child("taskList").
                child(booking.getId()).child("status").setValue("Request")
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(
                                myContext,
                                "Your Task is now on request",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    else {
                        Toast.makeText(
                                myContext,
                                "Failed to pass the task",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void takeTask(Booking booking, String passengerUserId, boolean fromRequest) {
        String status = "Booked";
        List<Route> bookingRouteList = booking.getRouteList();
        booking.setTimestamp(new DateTimeToString().getDateAndTime());
        booking.setStatus(status);
        Booking driverTask = new Booking(booking);

        if(fromRequest) {
            usersRef.child(taskDriverUserId).child("taskList").
                    child(driverTask.getId()).removeValue();
        }

        DatabaseReference bookingListRef = usersRef.child(passengerUserId).
                child("bookingList").child(driverTask.getId());

        DatabaseReference taskListRef = usersRef.child(userId).child("taskList").
                child(booking.getId());
        taskListRef.setValue(driverTask).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                bookingListRef.child("notify").setValue(true);
                bookingListRef.child("notificationTimestamp").
                        setValue(new DateTimeToString().getDateAndTime());

                if(fromRequest) {
                    bookingListRef.child("chats").removeValue().
                            addOnCompleteListener(task2 -> {
                                if(task2.isSuccessful())
                                    addBookingRoute(bookingRouteList, taskListRef);
                                else errorTask();
                            });
                }
                else {
                    bookingListRef.child("status").setValue(status).
                            addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful())
                                    addBookingRoute(bookingRouteList, taskListRef);
                                else errorTask();
                            });
                }
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
    }

    private void addBookingRoute(List<Route> bookingRouteList,
                                 DatabaseReference taskListRef) {
        int index = 1;
        for(Route route : bookingRouteList) {
            boolean isLastItem;
            isLastItem = index == bookingRouteList.size();

            DatabaseReference routeSpotsRef =
                    taskListRef.child("routeSpots").child(route.getRouteId());
            routeSpotsRef.setValue(route).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    if(isLastItem) {
                        Toast.makeText(
                                myContext,
                                "Successfully taken the task",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
            });
            index++;
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
                openImage, locateImage, locateEndImage, viewQRImage, chatImage,
                driverImage, passImage, checkImage, paidImage;
        TextView tvUserFullName, tvPassTaskNote, tvDriverFullName, tvBookingId, tvSchedule, tvTypeName, tvPrice,
                tvStartStation, tvStartStation2, tvEndStation, tvEndStation2,
                tvOption, tvOpen, tvLocate, tvLocateEnd, tvViewQR, tvChat, tvDriver, tvPass, tvCheck;
        ExpandableTextView extvMessage;
        ConstraintLayout backgroundLayout, buttonLayout, userInfoLayout, driverInfoLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userInfoLayout = itemView.findViewById(R.id.userInfoLayout);
            tvUserFullName = itemView.findViewById(R.id.tvUserFullName);
            tvPassTaskNote = itemView.findViewById(R.id.tvPassTaskNote);
            profileImage = itemView.findViewById(R.id.profileImage);

            driverInfoLayout = itemView.findViewById(R.id.driverInfoLayout);
            tvDriverFullName = itemView.findViewById(R.id.tvDriverFullName);
            driverProfileImage = itemView.findViewById(R.id.driverProfileImage);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvSchedule = itemView.findViewById(R.id.tvSchedule);
            tvTypeName = itemView.findViewById(R.id.tvTypeName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStartStation = itemView.findViewById(R.id.tvStartStation);
            tvStartStation2 = itemView.findViewById(R.id.tvStartStation2);
            tvEndStation = itemView.findViewById(R.id.tvEndStation);
            tvEndStation2 = itemView.findViewById(R.id.tvEndStation2);
            extvMessage = itemView.findViewById(R.id.extvMessage);

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

            tvViewQR = itemView.findViewById(R.id.tvViewQR);
            viewQRImage = itemView.findViewById(R.id.viewQRImage);
            tvChat = itemView.findViewById(R.id.tvChat);
            chatImage = itemView.findViewById(R.id.chatImage);
            tvDriver = itemView.findViewById(R.id.tvDriver);
            driverImage = itemView.findViewById(R.id.driverImage);
            tvPass = itemView.findViewById(R.id.tvPass);
            passImage = itemView.findViewById(R.id.passImage);
            tvCheck = itemView.findViewById(R.id.tvCheck);
            checkImage = itemView.findViewById(R.id.checkImage);
        }
    }
}
