package com.nikolassievertsen.thedeadcalendar;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class TimerFragment extends Fragment {

    private TextView tvCountdown;
    private ProgressBar progressBarLife;
    private TextView tvProgressPercentage;
    private CountDownTimer countDownTimer;
    private LocalDate expectedDeathDate;

    // Daten aus der Activity
    private long totalLifeSpanInDays;
    private long daysPassed;
    private long daysRemaining;
    private String expectedDeathDateString;

    // Erforderlicher leerer Konstruktor
    public TimerFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Daten aus den Fragment-Argumenten abrufen
        if (getArguments() != null) {
            totalLifeSpanInDays = getArguments().getLong("TOTAL_DAYS");
            daysPassed = getArguments().getLong("DAYS_PASSED");
            daysRemaining = getArguments().getLong("DAYS_REMAINING");
            expectedDeathDateString = getArguments().getString("EXPECTED_DEATH_DATE_STRING");
            if (expectedDeathDateString != null) {
                expectedDeathDate = LocalDate.parse(expectedDeathDateString);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Layout für dieses Fragment laden
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        tvCountdown = view.findViewById(R.id.tvCountdown);
        progressBarLife = view.findViewById(R.id.progressBarLife);
        tvProgressPercentage = view.findViewById(R.id.tvProgressPercentage);

        // ----- Fortschrittsleiste setzen -----
        if (totalLifeSpanInDays > 0) {
            double percentage = ((double) daysPassed / totalLifeSpanInDays) * 100.0;
            progressBarLife.setProgress((int) percentage);
            tvProgressPercentage.setText(String.format(Locale.GERMAN, "%.2f%%", percentage));
        }

        // ----- Countdown-Timer starten -----
        if (expectedDeathDate != null) {
            // Millisekunden bis zum Zieldatum
            long remainingMillis = daysRemaining * 24 * 60 * 60 * 1000;
            startCountdown(remainingMillis);
        }

        return view;
    }

    /**
     * Startet den Countdown-Timer mit der NEUEN Logik (Jahre, Monate, Tage, Sekunden)
     */
    private void startCountdown(long millisInFuture) {
        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Berechne die verbleibenden Sekunden (für die Anzeige)
                long seconds = (millisUntilFinished / 1000) % 60;

                // Berechne die genaue Periode zwischen Jetzt und dem Zieldatum
                // Wir tun dies bei jedem Tick, um Schaltjahre usw. korrekt zu behandeln
                LocalDate today = LocalDate.now();
                Period period = Period.between(today, expectedDeathDate);

                int years = period.getYears();
                int months = period.getMonths();
                int days = period.getDays();

                // Den TextView aktualisieren
                String timeString = String.format(Locale.getDefault(),
                        "%d Jahre, %d Monate, %d Tage, %02d Sek",
                        years, months, days, seconds
                );
                tvCountdown.setText(timeString);
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("Zeit abgelaufen");
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}