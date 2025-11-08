// ... (imports)
import java.time.Period;
import java.util.Locale;

public class TimerFragment extends Fragment {

    // ... (Klassenvariablen)

    // ... (onCreateView bleibt fast gleich)

    private void startCountdown(long millisInFuture) {
        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // ... (Berechnung von seconds, Period, years, months, days bleibt gleich)
                long seconds = (millisUntilFinished / 1000) % 60;
                LocalDate today = LocalDate.now();
                Period period = Period.between(today, expectedDeathDate);
                int years = period.getYears();
                int months = period.getMonths();
                int days = period.getDays();

                // --- TIMER-TEXT AKTUALISIERT ---
                // Verwendet jetzt die String-Ressource für die Formatierung
                String timeString = getString(R.string.timer_countdown_format,
                        (long)years, (long)months, (long)days, seconds
                );

                // (Sicherstellen, dass getString() nicht null ist, bevor es verwendet wird)
                if (getContext() != null) {
                    tvCountdown.setText(timeString);
                }
            }

            @Override
            public void onFinish() {
                // Verwendet jetzt die String-Ressource
                if (getContext() != null) {
                    tvCountdown.setText(getString(R.string.timer_countdown_finished));
                }
            }
        }.start();
    }

    // ... (onDestroy bleibt gleich)
}