package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.IncomeFromTaskActivity;
import com.example.firebase_clemenisle_ev.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class IncomeMonthAdapter extends RecyclerView.Adapter<IncomeMonthAdapter.ViewHolder> {

    List<String> monthList;
    String itemYear;
    List<Booking> taskList;
    String userId;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    public IncomeMonthAdapter(Context context, List<String> monthList, String itemYear,
                              List<Booking> taskList, String userId) {
        this.monthList = monthList;
        this.itemYear = itemYear;
        this.taskList = taskList;
        this.userId = userId;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_income_data_month_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncomeMonthAdapter.ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        TextView tvMonth = holder.tvMonth, tvMonthIncome = holder.tvMonthIncome;
        ImageView viewTaskHistory = holder.viewTaskHistory;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        String itemMonth = monthList.get(position);

        tvMonth.setText(itemMonth);

        getMonthIncome(tvMonthIncome, itemMonth);

        int start = dpToPx(1), end = dpToPx(1);

        boolean isFirstRow = position + 1 <= 3, isLastRow = position + 1 > getItemCount() - 3;

        if(isFirstRow) {
            start = dpToPx(0);
        }
        if(isLastRow) {
            end = dpToPx(0);
        }

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
                layoutParams.rightMargin, layoutParams.bottomMargin);
        layoutParams.setMarginStart(start);
        layoutParams.setMarginEnd(end);
        backgroundLayout.setLayoutParams(layoutParams);

        viewTaskHistory.setOnClickListener(view -> {
            Intent intent = new Intent(myContext, IncomeFromTaskActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("monthYear", itemMonth + " " + itemYear);
            myContext.startActivity(intent);
        });
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    private void getMonthIncome(TextView tvMonthIncome, String itemMonth) {
        double monthIncome = 0;
        for(Booking task : taskList) {
            if(task.getSchedule().contains(itemMonth + " " + itemYear) &&
                    (task.getStatus().equals("Completed") || task.getStatus().equals("Ongoing")))
                monthIncome += task.getBookingType().getPrice();
        }

        String monthIncomeText = "₱" + monthIncome;
        if(monthIncomeText.split("\\.")[1].length() == 1) monthIncomeText += 0;
        tvMonthIncome.setText(monthIncomeText);
    }

    @Override
    public int getItemCount() {
        return monthList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout;
        TextView tvMonth, tvMonthIncome;
        ImageView viewTaskHistory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            tvMonthIncome = itemView.findViewById(R.id.tvMonthIncome);
            viewTaskHistory = itemView.findViewById(R.id.viewTaskHistory);

            setIsRecyclable(false);
        }
    }
}
