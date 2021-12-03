package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
import com.example.firebase_clemenisle_ev.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class IncomeDayAdapter extends RecyclerView.Adapter<IncomeDayAdapter.ViewHolder> {

    int itemCount;
    String monthYear;
    List<Booking> taskList;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    int colorBlue, colorWhite, colorBlack, colorRed;

    int selectedDay = 1;

    public IncomeDayAdapter(Context context, int itemCount, String monthYear, List<Booking> taskList) {
        this.itemCount = itemCount;
        this.monthYear = monthYear;
        this.taskList = taskList;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_income_data_day_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        TextView tvDay = holder.tvDay, tvDay2 = holder.tvDay2, tvDayIncome = holder.tvDayIncome;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        colorBlue = myResources.getColor(R.color.blue);
        colorWhite = myResources.getColor(R.color.white);
        colorBlack = myResources.getColor(R.color.black);
        colorRed = myResources.getColor(R.color.red);

        int day = position + 1;
        DateTimeToString dateTimeToString = new DateTimeToString();
        dateTimeToString.setFormattedSchedule(day + " " + monthYear + " | 12:00 AM");
        String dayOfWeek = dateTimeToString.getDayOfWeek();

        tvDay.setText(String.valueOf(day));
        tvDay2.setText(dayOfWeek);

        if(day == selectedDay) {
            backgroundLayout.setBackgroundColor(colorBlue);
            tvDay.setTextColor(colorWhite);
            tvDay2.setTextColor(colorWhite);
            tvDayIncome.setTextColor(colorWhite);
        }
        else {
            backgroundLayout.setBackgroundColor(colorWhite);

            if(dayOfWeek.equals("Sun")) {
                tvDay.setTextColor(colorRed);
                tvDay2.setTextColor(colorRed);
            }
            else {
                tvDay.setTextColor(colorBlack);
                tvDay2.setTextColor(colorBlack);
            }

            tvDayIncome.setTextColor(colorBlue);
        }

        getDayIncome(tvDayIncome, day + " " + monthYear);

        int start = dpToPx(1), end = dpToPx(1);

        boolean isFirstItem = position + 1 == 1, isLastItem = position + 1 == getItemCount();

        if(isFirstItem) {
            start = dpToPx(0);
        }
        if(isLastItem) {
            end = dpToPx(0);
        }

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
                layoutParams.rightMargin, layoutParams.bottomMargin);
        layoutParams.setMarginStart(start);
        layoutParams.setMarginEnd(end);
        backgroundLayout.setLayoutParams(layoutParams);

        backgroundLayout.setOnClickListener(view -> {
            selectedDay = day;
            notifyDataSetChanged();
        });
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    private void getDayIncome(TextView tvDayIncome, String query) {
        double dayIncome = 0;
        for(Booking task : taskList) {
            String scheduleDate = task.getSchedule().split("\\|")[0].trim();
            if(scheduleDate.equals(query) && task.getStatus().equals("Completed"))
                dayIncome += task.getBookingType().getPrice();
        }

        String dayIncomeText = "₱" + dayIncome;
        if(dayIncomeText.split("\\.")[1].length() == 1) dayIncomeText += 0;
        tvDayIncome.setText(dayIncomeText);
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout;
        TextView tvDay, tvDay2, tvDayIncome;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvDay2 = itemView.findViewById(R.id.tvDay2);
            tvDayIncome = itemView.findViewById(R.id.tvDayIncome);

            setIsRecyclable(false);
        }
    }
}
