package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.firebase_clemenisle_ev.Classes.ScheduleTime;
import com.example.firebase_clemenisle_ev.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ScheduleTimeAdapter extends RecyclerView.Adapter<ScheduleTimeAdapter.ViewHolder> {

    List<ScheduleTime> scheduleTimeList;
    int columnCount;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    int colorBlue, colorWhite, colorBlack, colorInitial;

    String scheduleTimeId = null;

    boolean isZeroCountDisable = true;

    OnItemClickListener onItemClickListener;

    public ScheduleTimeAdapter(Context context, List<ScheduleTime> scheduleTimeList, int columnCount) {
        this.scheduleTimeList = scheduleTimeList;
        this.columnCount = columnCount;
        this.inflater = LayoutInflater.from(context);
    }

    public void setScheduleTimeId(String scheduleTimeId) {
        this.scheduleTimeId = scheduleTimeId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_schedule_time_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        TextView tvScheduleTime = holder.tvScheduleTime;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        colorBlue = myResources.getColor(R.color.blue);
        colorWhite = myResources.getColor(R.color.white);
        colorBlack = myResources.getColor(R.color.black);
        colorInitial = myResources.getColor(R.color.initial);

        ScheduleTime scheduleTime = scheduleTimeList.get(position);

        String id = scheduleTimeList.get(position).getId();
        String time = scheduleTimeList.get(position).getTime();
        boolean deactivated = scheduleTimeList.get(position).isDeactivated();

        tvScheduleTime.setText(time);

        boolean isSelected = false;
        if(scheduleTimeId != null)
            isSelected = scheduleTimeId.equals(id);

        if(!deactivated || !isZeroCountDisable) {
            if(isSelected) {
                backgroundLayout.setBackgroundColor(colorBlue);
                tvScheduleTime.setTextColor(colorWhite);
            }
            else {
                backgroundLayout.setBackgroundColor(colorWhite);
                tvScheduleTime.setTextColor(colorBlack);
            }
        }
        else {
            backgroundLayout.setBackgroundColor(colorWhite);
            tvScheduleTime.setTextColor(colorInitial);
        }

        int start = dpToPx(4), top = dpToPx(4), end = dpToPx(4), bottom = dpToPx(4);

        boolean isFirstRow = position + 1 <= columnCount, isLastRow = position >= getItemCount() - columnCount;
        boolean isLeftSide = (position + 1) % columnCount == 1, isRightSide = (position + 1) % columnCount == 0;

        if(isFirstRow) {
            top = dpToPx(8);
        }
        if(isLastRow) {
            bottom = dpToPx(8);
        }
        if(isLeftSide) {
            start = dpToPx(8);
        }
        if(isRightSide) {
            end = dpToPx(8);
        }

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, top, layoutParams.rightMargin, bottom);
        layoutParams.setMarginStart(start);
        layoutParams.setMarginEnd(end);
        backgroundLayout.setLayoutParams(layoutParams);

        backgroundLayout.setOnClickListener(view -> {
            if(!deactivated || !isZeroCountDisable) {
                scheduleTimeId = id;
                onItemClickListener.sendScheduleTime(scheduleTime);
                notifyDataSetChanged();
            }
        });
    }

    public interface OnItemClickListener {
        void sendScheduleTime(ScheduleTime scheduleTime);
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
        return scheduleTimeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout;
        TextView tvScheduleTime, tvPrice, tvRouteCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvScheduleTime = itemView.findViewById(R.id.tvScheduleTime);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvRouteCount = itemView.findViewById(R.id.tvRouteCount);
        }
    }
}
