package com.nikolassievertsen.thedeadcalendar;

import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI-Elemente initialisieren
        datePickerBirthday = findViewById(R.id.datePickerBirthday);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerCountry = findViewById(R.id.spinnerCountry);
        btnCalculate = findViewById(R.id.btnCalculate);

        // ----- SPINNER SETUP -----

        // 1. Geschlecht-Spinner füllen (aus strings.xml)
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // 2. Länder-Spinner füllen (aus unserer Datenklasse)
        String[] countries = LifeExpectancyData.getAvailableCountries();
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, countries);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(countryAdapter);

        // ----- BUTTON CLICK LISTENER -----
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateAndStartDashboard();
            }
        });

        // Den Standard-Insets-Listener anpassen
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Wird aufgerufen, wenn der Button geklickt wird.
     * Liest die Daten aus, berechnet die Lebensdaten und startet die DashboardActivity.
     */
    /**
     * Wird aufgerufen, wenn der Button geklickt wird.
     * Liest die Daten aus, berechnet die Lebensdaten und startet die DashboardActivity.
     */
    private void calculateAndStartDashboard() {
        // 1. Geburtstag auslesen
        int day = datePickerBirthday.getDayOfMonth();
        int month = datePickerBirthday.getMonth() + 1; // Monate sind 0-basiert
        int year = datePickerBirthday.getYear();
        LocalDate birthday = LocalDate.of(year, month, day);

        // 2. Geschlecht auslesen
        String genderString = spinnerGender.getSelectedItem().toString();
        LifeExpectancyData.Gender gender = genderString.equals("Männlich") ?
                LifeExpectancyData.Gender.MALE : LifeExpectancyData.Gender.FEMALE;

        // 3. Land auslesen
        String country = spinnerCountry.getSelectedItem().toString();

        // 4. Lebenserwartung abrufen
        double lifeExpectancyInYears = LifeExpectancyData.getLifeExpectancy(country, gender);

        // 5. Lebensdaten berechnen
        LocalDate today = LocalDate.now();

        // Prüfen, ob der Geburtstag in der Zukunft liegt
        if (birthday.isAfter(today)) {
            Toast.makeText(this, "Bitte wähle ein Geburtsdatum in der Vergangenheit.", Toast.LENGTH_LONG).show();
            return;
        }

        // Gesamte Lebensspanne in Tagen berechnen
        long totalLifeSpanInDays = (long) (lifeExpectancyInYears * 365.25);

        // Erwartetes Todesdatum berechnen
        LocalDate expectedDeathDate = birthday.plusDays(totalLifeSpanInDays);

        // Vergangene Tage berechnen
        long daysPassed = ChronoUnit.DAYS.between(birthday, today);

        // Verbleibende Tage berechnen
        long daysRemaining = ChronoUnit.DAYS.between(today, expectedDeathDate);

        // ----- NÄCHSTE ACTIVITY STARTEN -----

        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);

        // Wir übergeben die berechneten Daten an die neue Activity
        intent.putExtra("TOTAL_DAYS", totalLifeSpanInDays);
        intent.putExtra("DAYS_PASSED", daysPassed);
        intent.putExtra("DAYS_REMAINING", daysRemaining);

        // NEU: Das genaue Zieldatum für den Timer übergeben
        intent.putExtra("EXPECTED_DEATH_DATE_STRING", expectedDeathDate.toString());

        startActivity(intent);
    }
}