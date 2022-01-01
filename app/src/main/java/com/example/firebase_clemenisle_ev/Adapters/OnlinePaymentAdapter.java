package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.firebase_clemenisle_ev.Classes.DateTimeDifference;
import com.example.firebase_clemenisle_ev.Classes.OnlinePayment;
import com.example.firebase_clemenisle_ev.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class OnlinePaymentAdapter extends RecyclerView.Adapter<OnlinePaymentAdapter.ViewHolder> {

    List<OnlinePayment> onlinePaymentList;
    String status;
    boolean isCompletePayment = false, inDriverModule;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    OnInitiatePaymentListener onInitiatePaymentListener;

    public OnlinePaymentAdapter(Context context, List<OnlinePayment> onlinePaymentList, boolean inDriverModule) {
        this.onlinePaymentList = onlinePaymentList;
        this.inDriverModule = inDriverModule;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_reference_number_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout,
                referenceNumberLayout = holder.referenceNumberLayout,
                addRNLayout = holder.addRNLayout, iWalletLayout = holder.iWalletLayout;
        TextView tvReferenceNumber = holder.tvReferenceNumber, tvTimestamp = holder.tvTimestamp,
                tvValue = holder.tvValue, tvInvalidRN = holder.tvInvalidRN;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        referenceNumberLayout.setVisibility(View.GONE);
        addRNLayout.setVisibility(View.GONE);
        iWalletLayout.setVisibility(View.GONE);

        if(status != null && (status.equals("Pending") || status.equals("Booked")) &&
                !isCompletePayment && (position == 0 || position == 1) && !inDriverModule) {
            if(position == 0) {
                iWalletLayout.setVisibility(View.VISIBLE);
                iWalletLayout.setOnClickListener(view -> onInitiatePaymentListener.useIWallet());
            }
            else {
                addRNLayout.setVisibility(View.VISIBLE);
                addRNLayout.setOnClickListener(view -> onInitiatePaymentListener.addReferenceNumber());
            }
        }
        else if(position >= 2) {
            referenceNumberLayout.setVisibility(View.VISIBLE);

            OnlinePayment onlinePayment = onlinePaymentList.get(position-2);
            String timestamp = onlinePayment.getTimestamp();
            double value = onlinePayment.getValue();
            boolean isValid = onlinePayment.isValid();

            String referenceNumberValue = "#" + onlinePayment.getReferenceNumber();
            boolean isIWalletUsed = onlinePayment.isiWalletUsed();
            if(isIWalletUsed) referenceNumberValue = "iWallet";

            tvReferenceNumber.setText(referenceNumberValue);

            DateTimeDifference dateTimeDifference = new DateTimeDifference(timestamp);
            timestamp = dateTimeDifference.getResult();
            tvTimestamp.setText(timestamp);

            tvInvalidRN.setVisibility(View.GONE);

            if(value > 0) {
                String valueText = "₱" + value;
                if(valueText.split("\\.")[1].length() == 1) valueText += 0;

                tvValue.setText(valueText);
                tvValue.setVisibility(View.VISIBLE);
            }
            else if(!isValid) tvInvalidRN.setVisibility(View.VISIBLE);
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

    public void setInitiatePaymentListener(OnInitiatePaymentListener onInitiatePaymentListener) {
        this.onInitiatePaymentListener = onInitiatePaymentListener;
    }

    public interface OnInitiatePaymentListener {
        void addReferenceNumber();
        void useIWallet();
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    @Override
    public int getItemCount() {
        return onlinePaymentList.size() + 2;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout, referenceNumberLayout, addRNLayout, iWalletLayout;
        TextView tvReferenceNumber, tvTimestamp, tvValue, tvInvalidRN, tvAddRN, tvIWallet;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            referenceNumberLayout = itemView.findViewById(R.id.referenceNumberLayout);
            tvReferenceNumber = itemView.findViewById(R.id.tvReferenceNumber);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvValue = itemView.findViewById(R.id.tvValue);
            tvInvalidRN = itemView.findViewById(R.id.tvInvalidRN);

            addRNLayout = itemView.findViewById(R.id.addRNLayout);
            tvAddRN = itemView.findViewById(R.id.tvAddRN);

            iWalletLayout = itemView.findViewById(R.id.iWalletLayout);
            tvIWallet = itemView.findViewById(R.id.tvIWallet);

            setIsRecyclable(false);
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCompletePayment(boolean isCompletePayment) {
        this.isCompletePayment = isCompletePayment;
    }
}
