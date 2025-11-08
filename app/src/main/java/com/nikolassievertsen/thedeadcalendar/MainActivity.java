package com.nikolassievertsen.thedeadcalendar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat; // WICHTIG: Import für SwitchCompat
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList; // Import für ArrayList
import java.util.Collections; // Import für Collections

public class MainActivity extends AppCompatActivity {

    private DatePicker datePickerBirthday;
    private Spinner spinnerGender;
    private Spinner spinnerCountry;
    private Button btnCalculate;
    // --- NEUE UI ELEMENTE ---
    private SwitchCompat switchSmoker;
    private SwitchCompat switchDrinker;
    private SwitchCompat switchOverweight;

    // --- SharedPreferences Konstanten ---
    public static final String PREFS_NAME = "DeadCalendarPrefs";
    public static final String KEY_BIRTHDAY = "birthday_iso_string";
    public static final String KEY_GENDER = "gender_string";
    public static final String KEY_COUNTRY = "country_string";
    // --- NEUE SPEICHER-SCHLÜSSEL ---
    public static final String KEY_SMOKER = "is_smoker";
    public static final String KEY_DRINKER = "is_drinker";
    public static final String KEY_OVERWEIGHT = "is_overweight";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- 1. AUF GESPEICHERTE DATEN PRÜFEN ---
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedBirthday = prefs.getString(KEY_BIRTHDAY, null);

        if (savedBirthday != null) {
            // DATEN GEFUNDEN: Dashboard direkt starten
            String savedGender = prefs.getString(KEY_GENDER, "Männlich");
            String savedCountry = prefs.getString(KEY_COUNTRY, "Welt (Durchschnitt)");
            // Lifestyle-Daten laden
            boolean isSmoker = prefs.getBoolean(KEY_SMOKER, false);
            boolean isDrinker = prefs.getBoolean(KEY_DRINKER, false);
            boolean isOverweight = prefs.getBoolean(KEY_OVERWEIGHT, false);

            LocalDate birthday = LocalDate.parse(savedBirthday);

            // Berechnung und Start des Dashboards (mit Lifestyle-Daten)
            launchDashboardAndFinish(birthday, savedGender, savedCountry, isSmoker, isDrinker, isOverweight);

            return;
        }

        // --- 2. KEINE DATEN GEFUNDEN: Eingabemaske anzeigen ---
        setContentView(R.layout.activity_main);

        // UI-Elemente initialisieren
        datePickerBirthday = findViewById(R.id.datePickerBirthday);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerCountry = findViewById(R.id.spinnerCountry);
        btnCalculate = findViewById(R.id.btnCalculate);
        // Neue UI-Elemente
        switchSmoker = findViewById(R.id.switchSmoker);
        switchDrinker = findViewById(R.id.switchDrinker);
        switchOverweight = findViewById(R.id.switchOverweight);

        // Spinner Setup
        setupSpinners();

        // Button Click Listener
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
     * Richtet die Adapter für die Spinner ein (jetzt sortiert).
     */
    private void setupSpinners() {
        // 1. Geschlecht-Spinner
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // 2. Länder-Spinner (jetzt sortiert)
        // Holen der Länder als Set und Umwandeln in eine sortierte Liste
        ArrayList<String> countryList = new ArrayList<>(LifeExpectancyData.getAvailableCountries());
        Collections.sort(countryList); // Sortiert die Liste alphabetisch

        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, countryList);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(countryAdapter);

        // "Welt (Durchschnitt)" als Standard auswählen, falls vorhanden
        int defaultPosition = countryList.indexOf("Welt (Durchschnitt)");
        if (defaultPosition >= 0) {
            spinnerCountry.setSelection(defaultPosition);
        }
    }

    /**
     * Liest Daten aus der UI, speichert sie in SharedPreferences und startet das Dashboard.
     */
    private void readAndSaveData() {
        // 1. Basis-Daten auslesen
        int day = datePickerBirthday.getDayOfMonth();
        int month = datePickerBirthday.getMonth() + 1;
        int year = datePickerBirthday.getYear();
        LocalDate birthday = LocalDate.of(year, month, day);

        String genderString = spinnerGender.getSelectedItem().toString();
        String country = spinnerCountry.getSelectedItem().toString();

        // 2. NEUE Lifestyle-Daten auslesen
        boolean isSmoker = switchSmoker.isChecked();
        boolean isDrinker = switchDrinker.isChecked();
        boolean isOverweight = switchOverweight.isChecked();

        // Prüfen, ob der Geburtstag in der Zukunft liegt
        if (birthday.isAfter(LocalDate.now())) {
            Toast.makeText(this, "Bitte wähle ein Geburtsdatum in der Vergangenheit.", Toast.LENGTH_LONG).show();
            return;
        }

        // --- 3. DATEN SPEICHERN ---
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_BIRTHDAY, birthday.toString());
        editor.putString(KEY_GENDER, genderString);
        editor.putString(KEY_COUNTRY, country);
        // Neue Daten speichern
        editor.putBoolean(KEY_SMOKER, isSmoker);
        editor.putBoolean(KEY_DRINKER, isDrinker);
        editor.putBoolean(KEY_OVERWEIGHT, isOverweight);
        editor.apply();

        // --- 4. BERECHNEN UND DASHBOARD STARTEN ---
        launchDashboardAndFinish(birthday, genderString, country, isSmoker, isDrinker, isOverweight);
    }

    /**
     * Nimmt ALLE Eingabedaten, berechnet die Werte und startet die DashboardActivity.
     */
    private void launchDashboardAndFinish(LocalDate birthday, String genderString, String country,
                                          boolean isSmoker, boolean isDrinker, boolean isOverweight) {

        // 1. Geschlecht parsen
        LifeExpectancyData.Gender gender = genderString.equals("Männlich") ?
                LifeExpectancyData.Gender.MALE : LifeExpectancyData.Gender.FEMALE;

        // 2. Basis-Lebenserwartung abrufen
        double baseLifeExpectancy = LifeExpectancyData.getLifeExpectancy(country, gender);

        // --- 3. NEUE BERECHNUNG: Lifestyle-Abzüge holen ---
        double lifestylePenaltyInYears = LifeExpectancyData.getLifestylePenalty(isSmoker, isDrinker, isOverweight);

        // Endgültige Lebenserwartung berechnen
        double finalLifeExpectancyInYears = baseLifeExpectancy - lifestylePenaltyInYears;

        // 4. Lebensdaten berechnen
        LocalDate today = LocalDate.now();
        long totalLifeSpanInDays = (long) (finalLifeExpectancyInYears * 365.25);
        LocalDate expectedDeathDate = birthday.plusDays(totalLifeSpanInDays);
        long daysPassed = ChronoUnit.DAYS.between(birthday, today);
        long daysRemaining = ChronoUnit.DAYS.between(today, expectedDeathDate);

        // 5. Intent erstellen und Daten übergeben
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        intent.putExtra("TOTAL_DAYS", totalLifeSpanInDays);
        intent.putExtra("DAYS_PASSED", daysPassed);
        intent.putExtra("DAYS_REMAINING", daysRemaining);
        intent.putExtra("EXPECTED_DEATH_DATE_STRING", expectedDeathDate.toString());

        startActivity(intent);

        // 6. Diese Activity (MainActivity) beenden
        finish();
    }
}