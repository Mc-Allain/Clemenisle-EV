package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.firebase_clemenisle_ev.Classes.BookingType;
import com.example.firebase_clemenisle_ev.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class BookingTypeAdapter extends RecyclerView.Adapter<BookingTypeAdapter.ViewHolder> {

    List<BookingType> bookingTypeList;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    int colorBlue, colorWhite, colorBlack, colorInitial;

    String defaultRouteCountText = "Recommended Route(s): ";

    String bookingTypeId = null;

    boolean isZeroCountDisable = false;

    OnItemClickListener onItemClickListener;

    public BookingTypeAdapter(Context context, List<BookingType> bookingTypeList) {
        this.bookingTypeList = bookingTypeList;
        this.inflater = LayoutInflater.from(context);
    }

    public void setBookingTypeId(String bookingTypeId) {
        this.bookingTypeId = bookingTypeId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_booking_type_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        TextView tvTypeName = holder.tvTypeName, tvPrice = holder.tvPrice,
                tvRouteCount = holder.tvRouteCount;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        colorBlue = myResources.getColor(R.color.blue);
        colorWhite = myResources.getColor(R.color.white);
        colorBlack = myResources.getColor(R.color.black);
        colorInitial = myResources.getColor(R.color.initial);

        BookingType bookingType = bookingTypeList.get(position);

        String id = bookingTypeList.get(position).getId();
        String typeName = bookingTypeList.get(position).getName();
        String price = "₱" + bookingTypeList.get(position).getPrice();
        if(price.split("\\.")[1].length() == 1) price += 0;
        int routeCount = bookingTypeList.get(position).getRouteList().size();
        String routeCountText = defaultRouteCountText + routeCount;

        tvTypeName.setText(typeName);
        tvPrice.setText(price);
        tvRouteCount.setText(routeCountText);

        boolean isSelected = false;
        if(bookingTypeId != null)
            isSelected = bookingTypeId.equals(id);

        if(routeCount > 0 || !isZeroCountDisable) {
            if(isSelected) {
                backgroundLayout.setBackgroundColor(colorBlue);
                tvTypeName.setTextColor(colorWhite);
                tvPrice.setTextColor(colorWhite);
                tvRouteCount.setTextColor(colorWhite);
            }
            else {
                backgroundLayout.setBackgroundColor(colorWhite);
                tvTypeName.setTextColor(colorBlack);
                tvPrice.setTextColor(colorBlue);
                tvRouteCount.setTextColor(colorBlack);
            }
        }
        else {
            backgroundLayout.setBackgroundColor(colorWhite);
            tvTypeName.setTextColor(colorInitial);
            tvPrice.setTextColor(colorInitial);
            tvRouteCount.setTextColor(colorInitial);
        }

        int top = dpToPx(0), bottom = dpToPx(0);

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

        backgroundLayout.setOnClickListener(view -> {
            if(routeCount > 0 || !isZeroCountDisable) {
                bookingTypeId = id;
                onItemClickListener.sendBookingType(bookingType);
                notifyDataSetChanged();
            }
        });
    }

    public interface OnItemClickListener {
        void sendBookingType(BookingType bookingType);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    @Override
    public int getItemCount() {
        return bookingTypeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout;
        TextView tvTypeName, tvPrice, tvRouteCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvTypeName = itemView.findViewById(R.id.tvTypeName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvRouteCount = itemView.findViewById(R.id.tvRouteCount);
        }
    }
}
