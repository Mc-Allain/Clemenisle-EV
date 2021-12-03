package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.R;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class IncomeYearAdapter extends RecyclerView.Adapter<IncomeYearAdapter.ViewHolder> {

    int startingYear = 2021, currentYear;
    List<Booking> taskList;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    List<String> monthList = Arrays.asList("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December");

    int columnCount = 3;

    IncomeMonthAdapter incomeMonthAdapter;

    public IncomeYearAdapter(Context context, int currentYear, List<Booking> taskList) {
        this.currentYear = currentYear;
        this.taskList = taskList;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_income_data_year_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        TextView tvYear = holder.tvYear, tvYearIncome = holder.tvYearIncome;
        RecyclerView monthIncomeView = holder.monthIncomeView;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        String itemYear = String.valueOf(currentYear - position);

        tvYear.setText(itemYear);

        getYearIncome(itemYear, tvYearIncome);

        int top = dpToPx(2), bottom = dpToPx(2);

        boolean isFirstItem = position + 1 == 1, isLastItem = position + 1 == getItemCount();

        if(isFirstItem) {
            top = dpToPx(0);
        }
        if(isLastItem) {
            bottom = dpToPx(0);
        }

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, top, layoutParams.rightMargin, bottom);
        backgroundLayout.setLayoutParams(layoutParams);

        GridLayoutManager gridLayoutManager =
                new GridLayoutManager(myContext, columnCount, GridLayoutManager.HORIZONTAL, false);
        monthIncomeView.setLayoutManager(gridLayoutManager);
        incomeMonthAdapter = new IncomeMonthAdapter(myContext, monthList, itemYear, taskList);
        monthIncomeView.setAdapter(incomeMonthAdapter);
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    private void getYearIncome(String itemYear, TextView tvYearIncome) {
        double yearIncome = 0;
        for(Booking task : taskList) {
            if(task.getSchedule().contains(itemYear) && task.getStatus().equals("Completed"))
                yearIncome += task.getBookingType().getPrice();
        }

        String yearIncomeText = "₱" + yearIncome;
        if(yearIncomeText.split("\\.")[1].length() == 1) yearIncomeText += 0;
        tvYearIncome.setText(yearIncomeText);
    }

    @Override
    public int getItemCount() {
        return currentYear - (startingYear - 1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout;
        TextView tvYear, tvYearIncome;
        RecyclerView monthIncomeView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvYear = itemView.findViewById(R.id.tvYear);
            tvYearIncome = itemView.findViewById(R.id.tvYearIncome);
            monthIncomeView = itemView.findViewById(R.id.monthIncomeView);

            setIsRecyclable(false);
        }
    }
}
