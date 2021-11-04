package com.example.firebase_clemenisle_ev.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.ChatActivity;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.MapCoordinates;
import com.example.firebase_clemenisle_ev.Classes.Route;
import com.example.firebase_clemenisle_ev.Classes.Setting;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.Station;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.example.firebase_clemenisle_ev.MapActivity;
import com.example.firebase_clemenisle_ev.OnTheSpotActivity;
import com.example.firebase_clemenisle_ev.R;
import com.example.firebase_clemenisle_ev.RouteActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class BookingActionAdapter extends RecyclerView.Adapter<BookingActionAdapter.ViewHolder> {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef = firebaseDatabase.getReference("users");
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    List<Setting> actionList;
    boolean isOnTheSpot = false, isInList = true, inDriverModule = false;
    String bookingId, status, taskDriverUserId, userId;
    Station startStation, endStation;
    SimpleTouristSpot destinationSpot;
    double originLat = new MapCoordinates().getInitialLatLng().latitude,
            originLng = new MapCoordinates().getInitialLatLng().longitude;
    User user;
    Booking booking;
    List<User> users;
    List<Booking> bookingList;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    Dialog qrCodeDialog;
    ImageView qrCodeDialogCloseImage, qrCodeImage;

    OnActionListListener onActionListListener;

    boolean isLoggedIn = false;

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

    public BookingActionAdapter(Context context, List<Setting> actionList, List<User> users,
                                List<Booking> bookingList) {
        this.actionList = actionList;
        this.users = users;
        this.bookingList = bookingList;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_booking_action_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        TextView tvAction = holder.tvAction;
        ImageView actionImage = holder.actionImage;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

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
            else userId = firebaseUser.getUid();
        }

        int actionIcon = actionList.get(position).getSettingIcon();
        String actionName = actionList.get(position).getSettingName();

        actionImage.setImageResource(actionIcon);
        tvAction.setText(actionName);

        actionImage.setVisibility(View.GONE);
        tvAction.setVisibility(View.GONE);

        switch (actionName) {
            case "Open":
                if(isInList) {
                    actionImage.setVisibility(View.VISIBLE);
                    tvAction.setVisibility(View.VISIBLE);
                }
                break;
            case "Location Start Station":
            case "Location End Station":
                if(!isOnTheSpot) {
                    actionImage.setVisibility(View.VISIBLE);
                    tvAction.setVisibility(View.VISIBLE);

                    switch (actionName) {
                        case "Location Start Station":
                            actionImage.setOnClickListener(view -> openMap(startStation));
                            tvAction.setOnClickListener(view -> openMap(startStation));
                            break;
                        case "Location End Station":
                            actionImage.setOnClickListener(view -> openMap(endStation));
                            tvAction.setOnClickListener(view -> openMap(endStation));
                            break;
                    }
                }
                break;
            case "Location Origin Location":
            case "Location Destination Spot":
                if(isOnTheSpot) {
                    actionImage.setVisibility(View.VISIBLE);
                    tvAction.setVisibility(View.VISIBLE);

                    switch (actionName) {
                        case "Location Origin Location":
                            actionImage.setOnClickListener(view -> openMap2(originLat, originLng));
                            tvAction.setOnClickListener(view -> openMap2(originLat, originLng));
                            break;
                        case "Location Destination Spot":
                            actionImage.setOnClickListener(view -> openMap3(destinationSpot));
                            tvAction.setOnClickListener(view -> openMap3(destinationSpot));
                            break;
                    }
                }
                break;
            case "View QR Code":
                if(!inDriverModule) {
                    actionImage.setVisibility(View.VISIBLE);
                    tvAction.setVisibility(View.VISIBLE);

                    actionImage.setOnClickListener(view -> viewQRCode(bookingId));
                    tvAction.setOnClickListener(view -> viewQRCode(bookingId));
                }
                break;
            case "Chat":
                if(!inDriverModule && status.equals("Booked")) {
                    actionImage.setVisibility(View.VISIBLE);
                    tvAction.setVisibility(View.VISIBLE);

                    actionImage.setOnClickListener(view ->
                            openChat(false, bookingId, taskDriverUserId));
                    tvAction.setOnClickListener(view ->
                            openChat(false, bookingId, taskDriverUserId));
                }
        }

        if(inDriverModule) {
            switch (status) {
                case "Pending":
                    if (actionName.equals("Take Task") && !userId.equals(user.getId())) {
                        actionImage.setVisibility(View.VISIBLE);
                        tvAction.setVisibility(View.VISIBLE);

                        actionImage.setOnClickListener(view -> takeTask(booking, user.getId(), false));
                        tvAction.setOnClickListener(view -> takeTask(booking, user.getId(), false));
                    }
                    break;
                case "Booked":
                    switch (actionName) {
                        case "Chat":
                            actionImage.setVisibility(View.VISIBLE);
                            tvAction.setVisibility(View.VISIBLE);

                            actionImage.setOnClickListener(view ->
                                    openChat(true, bookingId, taskDriverUserId));
                            tvAction.setOnClickListener(view ->
                                    openChat(true, bookingId, taskDriverUserId));
                            break;
                        case "Pass Task":
                            actionImage.setVisibility(View.VISIBLE);
                            tvAction.setVisibility(View.VISIBLE);

                            actionImage.setOnClickListener(view -> passTask(booking));
                            tvAction.setOnClickListener(view -> passTask(booking));
                            break;
                        case "Mark as Completed":
                            actionImage.setVisibility(View.VISIBLE);
                            tvAction.setVisibility(View.VISIBLE);

                            actionImage.setOnClickListener(view -> openItem(booking, true));
                            tvAction.setOnClickListener(view -> openItem(booking, true));
                            break;
                    }
                    break;
                case "Request":
                    if(userId.equals(taskDriverUserId)) {
                        if(onActionListListener != null) onActionListListener.setPassengerTextRequest();

                        switch (actionName) {
                            case "Chat":
                                actionImage.setVisibility(View.VISIBLE);
                                tvAction.setVisibility(View.VISIBLE);

                                actionImage.setOnClickListener(view ->
                                        openChat(true, bookingId, taskDriverUserId));
                                tvAction.setOnClickListener(view ->
                                        openChat(true, bookingId, taskDriverUserId));
                                break;
                            case "Stop Request":
                                actionImage.setVisibility(View.VISIBLE);
                                tvAction.setVisibility(View.VISIBLE);

                                actionImage.setOnClickListener(view -> stopRequest(booking));
                                tvAction.setOnClickListener(view -> stopRequest(booking));
                                break;
                            case "Mark as Completed":
                                actionImage.setVisibility(View.VISIBLE);
                                tvAction.setVisibility(View.VISIBLE);

                                actionImage.setOnClickListener(view -> openItem(booking, true));
                                tvAction.setOnClickListener(view -> openItem(booking, true));
                                break;
                        }
                    }
                    else {
                        if(onActionListListener != null) onActionListListener.setPassengerTextDefault();

                        if(actionName.equals("Take Task") && !userId.equals(user.getId())) {
                            actionImage.setVisibility(View.VISIBLE);
                            tvAction.setVisibility(View.VISIBLE);

                            actionImage.setOnClickListener(view -> takeTask(booking, user.getId(), true));
                            tvAction.setOnClickListener(view -> takeTask(booking, user.getId(), true));
                        }
                    }
                    break;
            }
        }

        int top = dpToPx(1), bottom = dpToPx(1);

        boolean isFirstItem = position + 1 == 1, isLastItem = position + 1 == getItemCount();

        if(isFirstItem) {
            top = dpToPx(8);
        }
        if(isLastItem) {
            bottom = dpToPx(8);
        }

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, top, layoutParams.rightMargin, bottom);
        backgroundLayout.setLayoutParams(layoutParams);
    }

    public void setOnActionListListener(OnActionListListener onActionListListener) {
        this.onActionListListener = onActionListListener;
    }

    public interface OnActionListListener {
        void setPassengerTextDefault();
        void setPassengerTextRequest();
        void setProgressBarToVisible(boolean value);
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

    private void takeTask(Booking booking, String passengerUserId, boolean fromRequest) {
        if(onActionListListener != null)
            onActionListListener.setProgressBarToVisible(true);
        String status = "Booked";
        List<Route> bookingRouteList = booking.getRouteList();
        booking.setTimestamp(new DateTimeToString().getDateAndTime());
        Booking driverTask = new Booking(booking);
        driverTask.setStatus(status);

        String taskDriverUserId = getDriverUserId(booking.getId());

        if(fromRequest) {
            driverTask.setPreviousDriverUserId(taskDriverUserId);
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
                            if(task1.isSuccessful())
                                addBookingRoute(bookingRouteList, taskListRef);
                            else errorTask();
                        });
            }
            else errorTask();
        });
    }

    private void passTask(Booking booking) {
        if(onActionListListener != null)
            onActionListListener.setProgressBarToVisible(true);
        usersRef.child(userId).child("taskList").
                child(booking.getId()).child("status").setValue("Request")
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(
                                myContext,
                                "Your task is now on request",
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
                    if(onActionListListener != null)
                        onActionListListener.setProgressBarToVisible(false);
                });
    }

    private void stopRequest(Booking booking) {
        if(onActionListListener != null)
            onActionListListener.setProgressBarToVisible(true);
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
                    if(onActionListListener != null)
                        onActionListListener.setProgressBarToVisible(false);
                });
    }

    private void errorTask() {
        Toast.makeText(
                myContext,
                "Failed to take the task. Please try again.",
                Toast.LENGTH_LONG
        ).show();

        if(onActionListListener != null)
            onActionListListener.setProgressBarToVisible(false);
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

                        if(onActionListListener != null)
                            onActionListListener.setProgressBarToVisible(false);
                    }
                }
            });
            index++;
        }
    }

    private void openChat(boolean inDriverModule, String taskId, String taskDriverUserId) {
        Intent intent = new Intent(myContext, ChatActivity.class);
        intent.putExtra("taskId", taskId);
        intent.putExtra("inDriverModule", inDriverModule);
        if(!inDriverModule) intent.putExtra("driverUserId", taskDriverUserId);
        myContext.startActivity(intent);
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

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    @Override
    public int getItemCount() {
        return actionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout;
        TextView tvAction;
        ImageView actionImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvAction = itemView.findViewById(R.id.tvAction);
            actionImage = itemView.findViewById(R.id.actionImage);

            setIsRecyclable(false);
        }
    }

    public void setAllValue(boolean onTheSpot, boolean inList, boolean inDriverModule,
                               String bookingId, String status, String taskDriverUserId, String userId,
                               Station startStation, Station endStation, SimpleTouristSpot destinationSpot,
                               double originLat, double originLng, User user, Booking booking) {
        isOnTheSpot = onTheSpot;
        isInList = inList;
        this.inDriverModule = inDriverModule;
        this.bookingId = bookingId;
        this.status = status;
        this.taskDriverUserId = taskDriverUserId;
        this.userId = userId;
        this.startStation = startStation;
        this.endStation = endStation;
        this.destinationSpot = destinationSpot;
        this.originLat = originLat;
        this.originLng = originLng;
        this.user = user;
        this.booking = booking;

        notifyDataSetChanged();
    }

    public void setOnTheSpot(boolean onTheSpot) {
        isOnTheSpot = onTheSpot;
        notifyDataSetChanged();
    }

    public void setInList(boolean inList) {
        isInList = inList;
        notifyDataSetChanged();
    }

    public void setInDriverModule(boolean inDriverModule) {
        this.inDriverModule = inDriverModule;
        notifyDataSetChanged();
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
        notifyDataSetChanged();
    }

    public void setStatus(String status) {
        this.status = status;
        notifyDataSetChanged();
    }

    public void setTaskDriverUserId(String taskDriverUserId) {
        this.taskDriverUserId = taskDriverUserId;
        notifyDataSetChanged();
    }

    public void setUserId(String userId) {
        this.userId = userId;
        notifyDataSetChanged();
    }

    public void setStartStation(Station startStation) {
        this.startStation = startStation;
        notifyDataSetChanged();
    }

    public void setEndStation(Station endStation) {
        this.endStation = endStation;
        notifyDataSetChanged();
    }

    public void setDestinationSpot(SimpleTouristSpot destinationSpot) {
        this.destinationSpot = destinationSpot;
        notifyDataSetChanged();
    }

    public void setOriginLat(double originLat) {
        this.originLat = originLat;
        notifyDataSetChanged();
    }

    public void setOriginLng(double originLng) {
        this.originLng = originLng;
        notifyDataSetChanged();
    }

    public void setUser(User user) {
        this.user = user;
        notifyDataSetChanged();
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
        notifyDataSetChanged();
    }
}
