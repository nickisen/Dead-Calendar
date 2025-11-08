package com.nikolassievertsen.thedeadcalendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CalendarFragment extends Fragment {

    private RecyclerView recyclerViewCalendar;
    private LifeCalendarAdapter adapter;

    private long totalLifeSpanInDays;
    private long daysPassed;

    // Erforderlicher leerer Konstruktor
    public CalendarFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Daten aus den Fragment-Argumenten abrufen
        if (getArguments() != null) {
            totalLifeSpanInDays = getArguments().getLong("TOTAL_DAYS");
            daysPassed = getArguments().getLong("DAYS_PASSED");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        recyclerViewCalendar = view.findViewById(R.id.recyclerViewCalendar);

        // Daten umrechnen in Wochen
        int totalWeeks = (int) (totalLifeSpanInDays / 7);
        int weeksPassed = (int) (daysPassed / 7);

        // Anzahl der Spalten (52 Wochen pro Jahr)
        int spanCount = 52;

        // Adapter und LayoutManager setzen
        adapter = new LifeCalendarAdapter(getContext(), totalWeeks, weeksPassed);
        recyclerViewCalendar.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        recyclerViewCalendar.setAdapter(adapter);

        // Kleine Optimierung
        recyclerViewCalendar.setHasFixedSize(true);

        return view;
    }
}