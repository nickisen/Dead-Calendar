package com.nikolassievertsen.thedeadcalendar;

// ... (alle imports bleiben gleich)
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    // ... (alle Klassenvariablen bleiben gleich)

    // --- SharedPreferences Konstanten ---
    // ... (bleibt alles gleich, aber wir fügen KEY_GENDER_POSITION hinzu)
    public static final String KEY_GENDER_POSITION = "gender_position"; // NEU
    // ...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedBirthday = prefs.getString(KEY_BIRTHDAY, null);

        if (savedBirthday != null) {
            // --- DATENLADEN AKTUALISIERT ---
            int savedGenderPosition = prefs.getInt(KEY_GENDER_POSITION, 0); // Lade Position
            String savedCountry = prefs.getString(KEY_COUNTRY, "World (Average)");
            boolean isSmoker = prefs.getBoolean(KEY_SMOKER, false);
            boolean isDrinker = prefs.getBoolean(KEY_DRINKER, false);
            boolean isOverweight = prefs.getBoolean(KEY_OVERWEIGHT, false);

            LocalDate birthday = LocalDate.parse(savedBirthday);

            // Starte Dashboard mit der POSITION
            launchDashboardAndFinish(birthday, savedGenderPosition, savedCountry, isSmoker, isDrinker, isOverweight);

            return;
        }

        // --- Standard onCreate (bleibt gleich) ---
        setContentView(R.layout.activity_main);
        datePickerBirthday = findViewById(R.id.datePickerBirthday);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerCountry = findViewById(R.id.spinnerCountry);
        btnCalculate = findViewById(R.id.btnCalculate);
        switchSmoker = findViewById(R.id.switchSmoker);
        switchDrinker = findViewById(R.id.switchDrinker);
        switchOverweight = findViewById(R.id.switchOverweight);

        setupSpinners();

        btnCalculate.setOnClickListener(v -> readAndSaveData());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupSpinners() {
        // 1. Geschlecht-Spinner (lädt jetzt aus der übersetzbaren strings.xml)
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // 2. Länder-Spinner (lädt jetzt die ENGLISCHEN Schlüssel, sortiert)
        ArrayList<String> countryList = new ArrayList<>(LifeExpectancyData.getAvailableCountries());
        Collections.sort(countryList);

        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, countryList);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(countryAdapter);

        // Standard auf "World (Average)" setzen
        int defaultPosition = countryList.indexOf("World (Average)"); // Englischer Schlüssel
        if (defaultPosition >= 0) {
            spinnerCountry.setSelection(defaultPosition);
        }
    }

    private void readAndSaveData() {
        // ... (Geburtstag auslesen bleibt gleich)
        LocalDate birthday = LocalDate.of(datePickerBirthday.getYear(), datePickerBirthday.getMonth() + 1, datePickerBirthday.getDayOfMonth());

        // --- HIER IST DER WICHTIGE FIX ---
        // Speichere die *Position* (0 oder 1), nicht den Text
        int genderPosition = spinnerGender.getSelectedItemPosition();
        String country = spinnerCountry.getSelectedItem().toString(); // (Land bleibt Text-Schlüssel)

        boolean isSmoker = switchSmoker.isChecked();
        boolean isDrinker = switchDrinker.isChecked();
        boolean isOverweight = switchOverweight.isChecked();

        if (birthday.isAfter(LocalDate.now())) {
            // Toast verwendet jetzt String-Ressource
            Toast.makeText(this, getString(R.string.input_toast_birthday_future), Toast.LENGTH_LONG).show();
            return;
        }

        // --- DATEN SPEICHERN (AKTUALISIERT) ---
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_BIRTHDAY, birthday.toString());
        editor.putInt(KEY_GENDER_POSITION, genderPosition); // Speichere Position
        editor.putString(KEY_COUNTRY, country);
        editor.putBoolean(KEY_SMOKER, isSmoker);
        editor.putBoolean(KEY_DRINKER, isDrinker);
        editor.putBoolean(KEY_OVERWEIGHT, isOverweight);
        editor.apply();

        // --- BERECHNEN UND DASHBOARD STARTEN ---
        launchDashboardAndFinish(birthday, genderPosition, country, isSmoker, isDrinker, isOverweight);
    }

    /**
     * Nimmt ALLE Eingabedaten, berechnet die Werte und startet die DashboardActivity.
     * AKZEPTIERT JETZT 'genderPosition' (int) statt 'genderString' (String)
     */
    private void launchDashboardAndFinish(LocalDate birthday, int genderPosition, String country,
                                          boolean isSmoker, boolean isDrinker, boolean isOverweight) {

        // 1. Geschlecht parsen (basierend auf Position)
        LifeExpectancyData.Gender gender = (genderPosition == 0) ?
                LifeExpectancyData.Gender.MALE : LifeExpectancyData.Gender.FEMALE;

        // 2. Basis-Lebenserwartung abrufen (mit englischem Schlüssel)
        double baseLifeExpectancy = LifeExpectancyData.getLifeExpectancy(country, gender);

        // ... (Rest der Berechnung bleibt exakt gleich)
        double lifestylePenaltyInYears = LifeExpectancyData.getLifestylePenalty(isSmoker, isDrinker, isOverweight);
        double finalLifeExpectancyInYears = baseLifeExpectancy - lifestylePenaltyInYears;
        LocalDate today = LocalDate.now();
        long totalLifeSpanInDays = (long) (finalLifeExpectancyInYears * 365.25);
        LocalDate expectedDeathDate = birthday.plusDays(totalLifeSpanInDays);
        long daysPassed = ChronoUnit.DAYS.between(birthday, today);
        long daysRemaining = ChronoUnit.DAYS.between(today, expectedDeathDate);

        // ... (Intent erstellen bleibt exakt gleich)
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        intent.putExtra("TOTAL_DAYS", totalLifeSpanInDays);
        intent.putExtra("DAYS_PASSED", daysPassed);
        intent.putExtra("DAYS_REMAINING", daysRemaining);
        intent.putExtra("EXPECTED_DEATH_DATE_STRING", expectedDeathDate.toString());

        startActivity(intent);
        finish();
    }
}