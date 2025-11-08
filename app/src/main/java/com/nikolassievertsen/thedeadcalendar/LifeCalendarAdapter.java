package com.nikolassievertsen.thedeadcalendar;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class LifeCalendarAdapter extends RecyclerView.Adapter<LifeCalendarAdapter.WeekViewHolder> {

    private final int totalWeeks;
    private final int weeksPassed;
    private final Context context;

    public LifeCalendarAdapter(Context context, int totalWeeks, int weeksPassed) {
        this.context = context;
        this.totalWeeks = totalWeeks;
        this.weeksPassed = weeksPassed;
    }

    @NonNull
    @Override
    public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_week_cell, parent, false);
        return new WeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekViewHolder holder, int position) {
        // 'position' ist die aktuelle Woche (von 0 bis totalWeeks-1)
        if (position < weeksPassed) {
            // Vergangene Woche (rot)
            holder.weekCell.setBackgroundColor(ContextCompat.getColor(context, R.color.day_passed));
        } else {
            // Zukünftige Woche (grün)
            holder.weekCell.setBackgroundColor(ContextCompat.getColor(context, R.color.day_remaining));
        }
    }

    @Override
    public int getItemCount() {
        return totalWeeks;
    }

    // Der ViewHolder hält die Referenz auf das einzelne 'View' (Kästchen)
    public static class WeekViewHolder extends RecyclerView.ViewHolder {
        View weekCell;
        public WeekViewHolder(@NonNull View itemView) {
            super(itemView);
            weekCell = itemView.findViewById(R.id.weekCell);
        }
    }
}