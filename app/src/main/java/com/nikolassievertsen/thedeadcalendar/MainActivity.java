package com.nikolassievertsen.thedeadcalendar;

import android.content.Intent;
import android.content.SharedPreferences; // Importieren
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class MainActivity extends AppCompatActivity {

    private DatePicker datePickerBirthday;
    private Spinner spinnerGender;
    private Spinner spinnerCountry;
    private Button btnCalculate;

    // --- SharedPreferences Konstanten ---
    // Name unserer Speicher-Datei
    public static final String PREFS_NAME = "DeadCalendarPrefs";
    // Schlüssel für die einzelnen Werte
    public static final String KEY_BIRTHDAY = "birthday_iso_string";
    public static final String KEY_GENDER = "gender_string";
    public static final String KEY_COUNTRY = "country_string";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- 1. AUF GESPEICHERTE DATEN PRÜFEN ---
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedBirthday = prefs.getString(KEY_BIRTHDAY, null);

        if (savedBirthday != null) {
            // DATEN GEFUNDEN: Dashboard direkt starten
            String savedGender = prefs.getString(KEY_GENDER, "Männlich"); // Standard-Fallback
            String savedCountry = prefs.getString(KEY_COUNTRY, "Welt (Durchschnitt)"); // Standard-Fallback

            // Daten parsen (String -> LocalDate)
            LocalDate birthday = LocalDate.parse(savedBirthday);

            // Berechnung und Start des Dashboards
            launchDashboardAndFinish(birthday, savedGender, savedCountry);

            // WICHTIG: return, damit der Rest von onCreate (das Anzeigen der Eingabemaske)
            // nicht ausgeführt wird.
            return;
        }

        // --- 2. KEINE DATEN GEFUNDEN: Eingabemaske anzeigen ---
        // (Dieser Code wird nur erreicht, wenn savedBirthday == null ist)
        setContentView(R.layout.activity_main);

        // UI-Elemente initialisieren
        datePickerBirthday = findViewById(R.id.datePickerBirthday);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerCountry = findViewById(R.id.spinnerCountry);
        btnCalculate = findViewById(R.id.btnCalculate);

        // ----- SPINNER SETUP -----
        setupSpinners();

        // ----- BUTTON CLICK LISTENER -----
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Daten aus UI auslesen, speichern und Dashboard starten
                readAndSaveData();
            }
        });

        // Insets-Listener
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Richtet die Adapter für die Spinner ein.
     */
    private void setupSpinners() {
        // 1. Geschlecht-Spinner
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // 2. Länder-Spinner
        String[] countries = LifeExpectancyData.getAvailableCountries();
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, countries);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(countryAdapter);
    }

    /**
     * Liest Daten aus der UI, speichert sie in SharedPreferences und startet das Dashboard.
     */
    private void readAndSaveData() {
        // 1. Geburtstag auslesen
        int day = datePickerBirthday.getDayOfMonth();
        int month = datePickerBirthday.getMonth() + 1; // Monate sind 0-basiert
        int year = datePickerBirthday.getYear();
        LocalDate birthday = LocalDate.of(year, month, day);

        // 2. Geschlecht auslesen
        String genderString = spinnerGender.getSelectedItem().toString();

        // 3. Land auslesen
        String country = spinnerCountry.getSelectedItem().toString();

        // Prüfen, ob der Geburtstag in der Zukunft liegt
        if (birthday.isAfter(LocalDate.now())) {
            Toast.makeText(this, "Bitte wähle ein Geburtsdatum in der Vergangenheit.", Toast.LENGTH_LONG).show();
            return;
        }

        // --- 4. DATEN SPEICHERN ---
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Wir speichern das Geburtsdatum als ISO-String (z.B. "1990-05-20")
        editor.putString(KEY_BIRTHDAY, birthday.toString());
        editor.putString(KEY_GENDER, genderString);
        editor.putString(KEY_COUNTRY, country);
        editor.apply(); // Speichern (asynchron)

        // --- 5. BERECHNEN UND DASHBOARD STARTEN ---
        launchDashboardAndFinish(birthday, genderString, country);
    }

    /**
     * Nimmt die Eingabedaten, berechnet die Werte, startet die DashboardActivity
     * und beendet die MainActivity.
     */
    private void launchDashboardAndFinish(LocalDate birthday, String genderString, String country) {
        // 1. Geschlecht parsen
        LifeExpectancyData.Gender gender = genderString.equals("Männlich") ?
                LifeExpectancyData.Gender.MALE : LifeExpectancyData.Gender.FEMALE;

        // 2. Lebenserwartung abrufen
        double lifeExpectancyInYears = LifeExpectancyData.getLifeExpectancy(country, gender);

        // 3. Lebensdaten berechnen (wird JEDES MAL neu berechnet)
        LocalDate today = LocalDate.now();
        long totalLifeSpanInDays = (long) (lifeExpectancyInYears * 365.25);
        LocalDate expectedDeathDate = birthday.plusDays(totalLifeSpanInDays);
        long daysPassed = ChronoUnit.DAYS.between(birthday, today);
        long daysRemaining = ChronoUnit.DAYS.between(today, expectedDeathDate);

        // 4. Intent erstellen und Daten übergeben
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        intent.putExtra("TOTAL_DAYS", totalLifeSpanInDays);
        intent.putExtra("DAYS_PASSED", daysPassed);
        intent.putExtra("DAYS_REMAINING", daysRemaining);
        intent.putExtra("EXPECTED_DEATH_DATE_STRING", expectedDeathDate.toString());

        startActivity(intent);

        // 5. Diese Activity (MainActivity) beenden
        finish();
    }
}