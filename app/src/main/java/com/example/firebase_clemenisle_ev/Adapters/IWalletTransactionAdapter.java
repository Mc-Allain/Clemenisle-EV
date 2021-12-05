package com.example.firebase_clemenisle_ev.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.firebase_clemenisle_ev.Classes.DateTimeDifference;
import com.example.firebase_clemenisle_ev.Classes.IWalletTransaction;
import com.example.firebase_clemenisle_ev.OnlinePaymentActivity;
import com.example.firebase_clemenisle_ev.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class IWalletTransactionAdapter extends RecyclerView.Adapter<IWalletTransactionAdapter.ViewHolder> {

    List<IWalletTransaction> transactionList;
    String selectedBookingId;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    int colorRed, colorBlue, colorGreen, colorBlack, colorOrange;

    String defaultInvalidMNText = "Invalid Mobile Number", pendingText = "Pending";

    public IWalletTransactionAdapter(Context context, List<IWalletTransaction> transactionList,
                                     String selectedBookingId) {
        this.transactionList = transactionList;
        this.selectedBookingId = selectedBookingId;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_iwallet_transaction_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        TextView tvTransactionId = holder.tvTransactionId, tvCategory = holder.tvCategory,
                tvTimestamp = holder.tvTimestamp, tvPendingReferenceNumber = holder.tvPendingReferenceNumber,
                tvValueType = holder.tvValueType, tvValue = holder.tvValue, tvTransactionStatus = holder.tvTransactionStatus;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        colorGreen = myResources.getColor(R.color.green);
        colorBlue = myResources.getColor(R.color.blue);
        colorRed = myResources.getColor(R.color.red);
        colorBlack = myResources.getColor(R.color.black);
        colorOrange = myResources.getColor(R.color.orange);

        IWalletTransaction transaction = transactionList.get(position);
        String transactionId = transaction.getId();
        String category = transaction.getCategory();
        String timestamp = transaction.getTimestamp();
        String referenceNumber = transaction.getReferenceNumber();
        String mobileNumber = transaction.getMobileNumber();
        double value = transaction.getValue();
        boolean valid = transaction.isValid();

        String bookingId = transaction.getBookingId();

        tvTransactionId.setText(transactionId);
        tvCategory.setText(category);

        DateTimeDifference dateTimeDifference = new DateTimeDifference(timestamp);
        timestamp = dateTimeDifference.getResult();
        tvTimestamp.setText(timestamp);

        String valueText = "₱" + value;
        if(valueText.split("\\.")[1].length() == 1) valueText += 0;

        tvValue.setText(valueText);

        tvPendingReferenceNumber.setVisibility(View.GONE);
        tvTransactionStatus.setVisibility(View.GONE);

        if(category.equals("Top-up")) {
            tvValueType.setText("+");
            tvValueType.setTextColor(colorGreen);

            if(referenceNumber != null) {
                referenceNumber = "#" + referenceNumber;

                tvTransactionStatus.setVisibility(View.VISIBLE);
                if(value == 0) {
                    tvTransactionStatus.setTextColor(colorOrange);
                    tvTransactionStatus.setText(pendingText);
                    tvTransactionStatus.setTypeface(tvTransactionStatus.getTypeface(), Typeface.ITALIC);

                    tvPendingReferenceNumber.setVisibility(View.VISIBLE);
                    tvPendingReferenceNumber.setText(referenceNumber);
                }
                else {
                    tvTransactionStatus.setTextColor(colorBlue);
                    tvTransactionStatus.setText(referenceNumber);
                    tvTransactionStatus.setTypeface(tvTransactionStatus.getTypeface(), Typeface.NORMAL);
                }
            }
        }

        if(category.equals("Transfer")) {
            tvValueType.setText("-");
            tvValueType.setTextColor(colorRed);

            if(mobileNumber != null) {
                tvPendingReferenceNumber.setVisibility(View.VISIBLE);
                tvPendingReferenceNumber.setText(mobileNumber);
            }

            tvTransactionStatus.setVisibility(View.VISIBLE);
            if(!valid) {
                tvTransactionStatus.setTextColor(colorRed);
                tvTransactionStatus.setText(defaultInvalidMNText);
                tvTransactionStatus.setTypeface(tvTransactionStatus.getTypeface(), Typeface.ITALIC);
            }
            else {
                if(referenceNumber == null) {
                    tvTransactionStatus.setTextColor(colorOrange);
                    tvTransactionStatus.setText(pendingText);
                    tvTransactionStatus.setTypeface(tvTransactionStatus.getTypeface(), Typeface.ITALIC);
                }
                else {
                    referenceNumber = "#" + referenceNumber;
                    tvTransactionStatus.setTextColor(colorBlue);
                    tvTransactionStatus.setText(referenceNumber);
                    tvTransactionStatus.setTypeface(tvTransactionStatus.getTypeface(), Typeface.NORMAL);
                }
            }
        }

        if(category.equals("Refund") || category.equals("Payment")) {
            if (category.equals("Refund")) {
                tvValueType.setText("+");
                tvValueType.setTextColor(colorGreen);
            }
            else if (category.equals("Payment")) {
                tvValueType.setText("-");
                tvValueType.setTextColor(colorRed);
            }

            if(bookingId != null) {
                tvTransactionStatus.setVisibility(View.VISIBLE);
                tvTransactionStatus.setTextColor(colorBlue);
                tvTransactionStatus.setText(bookingId);

                backgroundLayout.setOnClickListener(view -> {
                    if(bookingId.equals(selectedBookingId)) ((Activity) myContext).onBackPressed();
                    else openOnlinePayment(bookingId);
                });
            }
        }

        int top = dpToPx(2), bottom = dpToPx(2);

        boolean isFirstItem = position + 1 == 1, isLastItem = position + 1 == getItemCount();

        if(isFirstItem) {
            top = dpToPx(0);
        }
        if(isLastItem) {
            bottom = dpToPx(8);
        }

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, top, layoutParams.rightMargin, bottom);
        backgroundLayout.setLayoutParams(layoutParams);
    }

    private void openOnlinePayment(String bookingId) {
        Intent intent1 = new Intent(myContext, OnlinePaymentActivity.class);
        intent1.putExtra("bookingId", bookingId);
        intent1.putExtra("fromIWallet", true);
        myContext.startActivity(intent1);
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout;
        TextView tvTransactionId, tvCategory, tvTimestamp, tvPendingReferenceNumber,
                tvValueType, tvValue, tvTransactionStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvTransactionId = itemView.findViewById(R.id.tvTransactionId);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvPendingReferenceNumber = itemView.findViewById(R.id.tvPendingReferenceNumber);
            tvValueType = itemView.findViewById(R.id.tvValueType);
            tvValue = itemView.findViewById(R.id.tvValue);
            tvTransactionStatus = itemView.findViewById(R.id.tvTransactionStatus);

            setIsRecyclable(false);
        }
    }
}
