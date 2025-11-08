package com.nikolassievertsen.thedeadcalendar;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar; // Importieren
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period; // Importieren
import java.util.Locale;

public class TimerFragment extends Fragment {

    // --- UI Ansichten ---
    private TextView tvCountdown;
    private TextView tvLifeSoFar; // NEU
    private ProgressBar progressLife; // NEU

    // --- Timer ---
    private CountDownTimer countDownTimer;

    // --- Daten aus dem Bundle ---
    private LocalDate expectedDeathDate;
    private LocalDate birthday; // NEU
    private long totalDays; // NEU
    private long daysPassed; // NEU

    // Erforderlicher leerer Konstruktor
    public TimerFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Daten aus dem Bundle abrufen
        if (getArguments() != null) {
            String dateString = getArguments().getString("EXPECTED_DEATH_DATE_STRING");
            if (dateString != null) {
                expectedDeathDate = LocalDate.parse(dateString);
            }

            // NEU: Daten für Progress Bar und "Life So Far" Text
            String birthdayString = getArguments().getString("BIRTHDAY_STRING");
            if (birthdayString != null) {
                birthday = LocalDate.parse(birthdayString);
            }
            totalDays = getArguments().getLong("TOTAL_DAYS");
            daysPassed = getArguments().getLong("DAYS_PASSED");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        // UI-Elemente zuweisen
        tvCountdown = view.findViewById(R.id.tvCountdown);
        tvLifeSoFar = view.findViewById(R.id.tvLifeSoFar); // NEU
        progressLife = view.findViewById(R.id.progressLife); // NEU

        // --- Live Countdown (Verbleibende Zeit) einrichten ---
        if (expectedDeathDate != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime targetDateTime = expectedDeathDate.atStartOfDay();

            if (now.isAfter(targetDateTime)) {
                if (getContext() != null) {
                    tvCountdown.setText(getString(R.string.timer_countdown_finished));
                }
            } else {
                long millisInFuture = Duration.between(now, targetDateTime).toMillis();
                startCountdown(millisInFuture);
            }
        } else {
            if (getContext() != null) {
                tvCountdown.setText(getString(R.string.timer_countdown_finished));
            }
        }

        // --- Statische Ansicht (Vergangene Zeit) einrichten ---
        setupLifeSoFar();

        return view;
    }

    /**
     * Stellt die Progress Bar und den "Jahre, Monate, Tage" Text ein.
     */
    private void setupLifeSoFar() {
        if (getContext() == null) return; // Sicherstellen, dass Fragment noch verbunden ist

        // 1. Progress Bar aktualisieren
        if (totalDays > 0) {
            // Berechne Prozent als double für Genauigkeit, dann zu int für die Bar
            int progressPercent = (int) (((double) daysPassed / totalDays) * 100);
            progressLife.setProgress(progressPercent);
        }

        // 2. "Life So Far" Text (Jahre, Monate, Tage)
        if (birthday != null) {
            // Period berechnet die Zeit zwischen Geburtsdatum und HEUTE
            Period periodPassed = Period.between(birthday, LocalDate.now());

            int years = periodPassed.getYears();
            int months = periodPassed.getMonths();
            int days = periodPassed.getDays();

            // Erstelle einen formatierten String
            // (Wir verwenden Locale.getDefault() für die richtige Pluralisierung in zukünftigen Versionen,
            // aber für jetzt ist das eine einfache Verknüpfung)
            String lifeSoFarString = String.format(Locale.getDefault(),
                    "%d years, %d months, %d days",
                    years, months, days
            );

            // Passe den String für die deutsche Sprache an
            if (Locale.getDefault().getLanguage().equals("de")) {
                lifeSoFarString = String.format(Locale.getDefault(),
                        "%d Jahre, %d Monate, %d Tage",
                        years, months, days
                );
            }
            // Füge hier "else if" für Spanisch hinzu, falls gewünscht

            tvLifeSoFar.setText(lifeSoFarString);
        }
    }


    /**
     * Startet den Countdown-Timer und aktualisiert die TextView.
     */
    private void startCountdown(long millisInFuture) {
        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Berechne die verbleibenden Einheiten
                long totalSeconds = millisUntilFinished / 1000;
                long days = totalSeconds / (24 * 3600);
                long hours = (totalSeconds % (24 * 3600)) / 3600;
                long minutes = (totalSeconds % 3600) / 60;
                long seconds = totalSeconds % 60;

                String timeString = getString(R.string.timer_countdown_format,
                        days, hours, minutes, seconds
                );

                if (getContext() != null) {
                    tvCountdown.setText(timeString);
                }
            }

            @Override
            public void onFinish() {
                if (getContext() != null) {
                    tvCountdown.setText(getString(R.string.timer_countdown_finished));
                }
            }
        }.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Timer stoppen und Ressourcen freigeben
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}