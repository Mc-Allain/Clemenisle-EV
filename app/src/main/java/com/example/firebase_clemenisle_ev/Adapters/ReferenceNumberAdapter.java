package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.firebase_clemenisle_ev.Classes.ReferenceNumber;
import com.example.firebase_clemenisle_ev.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ReferenceNumberAdapter extends RecyclerView.Adapter<ReferenceNumberAdapter.ViewHolder> {

    List<ReferenceNumber> referenceNumberList;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    public ReferenceNumberAdapter(Context context, List<ReferenceNumber> referenceNumberList) {
        this.referenceNumberList = referenceNumberList;
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
                addRNLayout = holder.addRNLayout;
        TextView tvReferenceNumber = holder.tvReferenceNumber, tvTimestamp = holder.tvTimestamp,
                tvValue = holder.tvValue, tvAddRN = holder.tvValue;
        ImageView removeImage = holder.removeImage;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        referenceNumberLayout.setVisibility(View.GONE);
        addRNLayout.setVisibility(View.GONE);

        if(position == 0) {
            addRNLayout.setVisibility(View.VISIBLE);
            addRNLayout.setOnClickListener(view -> addRN());
        }
        else {
            referenceNumberLayout.setVisibility(View.VISIBLE);

            ReferenceNumber referenceNumber = referenceNumberList.get(position);
            String referenceNumberValue = referenceNumber.getReferenceNumber();
            String timestamp = referenceNumber.getTimestamp();
            double value = referenceNumber.getValue();

            tvReferenceNumber.setText(referenceNumberValue);
            tvTimestamp.setText(timestamp);

            if(value == 0) tvValue.setVisibility(View.GONE);
            else {
                tvValue.setText(String.valueOf(value));
                tvValue.setVisibility(View.VISIBLE);
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

    private void addRN() {

    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    @Override
    public int getItemCount() {
        return referenceNumberList.size() + 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout, referenceNumberLayout, addRNLayout;
        TextView tvReferenceNumber, tvTimestamp, tvValue, tvAddRN;
        ImageView removeImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            referenceNumberLayout = itemView.findViewById(R.id.referenceNumberLayout);
            addRNLayout = itemView.findViewById(R.id.addRNLayout);
            tvReferenceNumber = itemView.findViewById(R.id.tvReferenceNumber);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvValue = itemView.findViewById(R.id.tvValue);
            tvAddRN = itemView.findViewById(R.id.tvAddRN);
            removeImage = itemView.findViewById(R.id.removeImage);

            setIsRecyclable(false);
        }
    }
}
