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

    int colorRed, colorBlue, colorGreen;

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
        TextView tvCategory = holder.tvCategory, tvTimestamp = holder.tvTimestamp,
                tvReferenceNumber = holder.tvReferenceNumber, tvValue = holder.tvValue,
                tvInvalidMN = holder.tvInvalidMN;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        colorGreen = myResources.getColor(R.color.green);
        colorBlue = myResources.getColor(R.color.blue);
        colorRed = myResources.getColor(R.color.red);

        IWalletTransaction transaction = transactionList.get(position);
        String category = transaction.getCategory();
        String timestamp = transaction.getTimestamp();
        String referenceNumber = transaction.getReferenceNumber();
        String mobileNumber = transaction.getMobileNumber();
        double value = transaction.getValue();
        boolean valid = transaction.isValid();

        String bookingId = transaction.getBookingId();

        tvCategory.setText(category);

        DateTimeDifference dateTimeDifference = new DateTimeDifference(timestamp);
        timestamp = dateTimeDifference.getResult();
        tvTimestamp.setText(timestamp);

        String valueText = "₱" + value;
        if(valueText.split("\\.")[1].length() == 1) valueText += 0;

        tvValue.setText(valueText);

        tvReferenceNumber.setVisibility(View.GONE);
        tvInvalidMN.setVisibility(View.GONE);

        if(category.equals("Transfer")) {
            if(referenceNumber != null) {
                tvReferenceNumber.setVisibility(View.VISIBLE);
                referenceNumber = "#" + referenceNumber;
                tvReferenceNumber.setText(referenceNumber);
            }

            tvInvalidMN.setVisibility(View.VISIBLE);
            if(!valid) {
                tvInvalidMN.setTextColor(colorRed);
                tvInvalidMN.setText(defaultInvalidMNText);
                tvInvalidMN.setTypeface(tvInvalidMN.getTypeface(), Typeface.ITALIC);
            }
            else {
                if(referenceNumber == null && mobileNumber != null) {
                    tvInvalidMN.setTextColor(colorGreen);
                    tvInvalidMN.setText(pendingText);
                    tvInvalidMN.setTypeface(tvInvalidMN.getTypeface(), Typeface.ITALIC);
                }
                else if(mobileNumber != null) {
                    tvInvalidMN.setTypeface(tvInvalidMN.getTypeface(), Typeface.NORMAL);
                    tvInvalidMN.setTextColor(colorBlue);
                    tvInvalidMN.setText(mobileNumber);
                }
            }
        }

        if(category.equals("Refund")) {
            if(bookingId != null) {
                tvReferenceNumber.setVisibility(View.VISIBLE);
                tvReferenceNumber.setText(bookingId);

                backgroundLayout.setOnClickListener(view -> {
                    if(bookingId.equals(selectedBookingId)) ((Activity) myContext).onBackPressed();
                    else {
                        Intent intent1 = new Intent(myContext, OnlinePaymentActivity.class);
                        intent1.putExtra("bookingId", bookingId);
                        intent1.putExtra("fromIWallet", true);
                        myContext.startActivity(intent1);
                    }
                });
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
        TextView tvCategory, tvTimestamp, tvReferenceNumber, tvValue, tvInvalidMN;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvReferenceNumber = itemView.findViewById(R.id.tvReferenceNumber);
            tvValue = itemView.findViewById(R.id.tvValue);
            tvInvalidMN = itemView.findViewById(R.id.tvInvalidMN);

            setIsRecyclable(false);
        }
    }
}
