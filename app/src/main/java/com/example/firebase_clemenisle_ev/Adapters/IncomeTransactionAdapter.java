package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.firebase_clemenisle_ev.Classes.IncomeTransaction;
import com.example.firebase_clemenisle_ev.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class IncomeTransactionAdapter extends RecyclerView.Adapter<IncomeTransactionAdapter.ViewHolder> {

    List<IncomeTransaction> transactionList;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    public IncomeTransactionAdapter(Context context, List<IncomeTransaction> transactionList) {
        this.transactionList = transactionList;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_income_transaction_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        TextView tvTransactionId = holder.tvTransactionId, tvTimestamp = holder.tvTimestamp, tvValue = holder.tvValue;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        IncomeTransaction incomeTransaction = transactionList.get(position);
        String transactionId = incomeTransaction.getId();
        String timestamp = incomeTransaction.getTimestamp();
        double value = incomeTransaction.getValue();

        tvTransactionId.setText(transactionId);
        tvTimestamp.setText(timestamp);

        String valueText = "₱" + value;
        if(valueText.split("\\.")[1].length() == 1) valueText += 0;

        tvValue.setText(valueText);

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
        TextView tvTransactionId, tvTimestamp, tvValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvTransactionId = itemView.findViewById(R.id.tvTransactionId);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvValue = itemView.findViewById(R.id.tvValue);

            setIsRecyclable(false);
        }
    }
}
