package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.Classes.DateTimeDifference;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.ReferenceNumber;
import com.example.firebase_clemenisle_ev.R;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ReferenceNumberAdapter extends RecyclerView.Adapter<ReferenceNumberAdapter.ViewHolder> {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);

    List<ReferenceNumber> referenceNumberList;
    String status;
    boolean showAddRN = true;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    OnAddRNListener onAddRNListener;

    long removePressedTime;
    Toast removeToast;

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
                tvValue = holder.tvValue, tvInvalidRN = holder.tvInvalidRN, tvRemoved = holder.tvRemoved;
        ImageView removeImage = holder.removeImage;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        referenceNumberLayout.setVisibility(View.GONE);
        addRNLayout.setVisibility(View.GONE);

        if(status != null && (status.equals("Pending") || status.equals("Booked"))
                && position == 0 && showAddRN) {
            addRNLayout.setVisibility(View.VISIBLE);
            addRNLayout.setOnClickListener(view -> onAddRNListener.addReferenceNumber());
        }
        else if(position != 0) {
            referenceNumberLayout.setVisibility(View.VISIBLE);

            ReferenceNumber referenceNumber = referenceNumberList.get(position-1);
            String id = referenceNumber.getId();
            String referenceNumberValue = "#" + referenceNumber.getReferenceNumber();
            String timestamp = referenceNumber.getTimestamp();
            double value = referenceNumber.getValue();
            boolean isValid = referenceNumber.isValid();

            String userId = referenceNumber.getUserId();
            String bookingId = referenceNumber.getBookingId();

            tvReferenceNumber.setText(referenceNumberValue);

            DateTimeDifference dateTimeDifference = new DateTimeDifference(timestamp);
            timestamp = dateTimeDifference.getResult();
            tvTimestamp.setText(timestamp);

            tvValue.setVisibility(View.GONE);
            removeImage.setVisibility(View.GONE);
            tvInvalidRN.setVisibility(View.GONE);

            if(value == 0 || !isValid) {
                removeImage.setVisibility(View.VISIBLE);
                if(!isValid) tvInvalidRN.setVisibility(View.VISIBLE);
            }
            else {
                String valueText = "₱" + value;
                if(valueText.split("\\.")[1].length() == 1) valueText += 0;

                tvValue.setText(valueText);
                tvValue.setVisibility(View.VISIBLE);
            }

            removeImage.setOnClickListener(view -> {
                if (removePressedTime + 2500 > System.currentTimeMillis() &&
                        tvRemoved.getText().toString().equals("true")) {
                    removeToast.cancel();

                    firebaseDatabase.getReference("users").child(userId).
                            child("bookingList").child(bookingId).
                            child("referenceNumberList").child(id).removeValue();

                    tvRemoved.setText("false");
                } else {
                    removeToast = Toast.makeText(myContext,
                            "Press again to remove", Toast.LENGTH_SHORT);
                    removeToast.show();

                    removePressedTime = System.currentTimeMillis();

                    tvRemoved.setText("true");
                }
            });
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

    public void setOnAddRNListener(OnAddRNListener onAddRNListener) {
        this.onAddRNListener = onAddRNListener;
    }

    public interface OnAddRNListener {
        void addReferenceNumber();
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
        TextView tvReferenceNumber, tvTimestamp, tvValue, tvAddRN, tvInvalidRN, tvRemoved;
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
            tvInvalidRN = itemView.findViewById(R.id.tvInvalidRN);
            removeImage = itemView.findViewById(R.id.removeImage);
            tvRemoved = itemView.findViewById(R.id.tvRemoved);

            setIsRecyclable(false);
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setShowAddRN(boolean showAddRN) {
        this.showAddRN = showAddRN;
    }
}
