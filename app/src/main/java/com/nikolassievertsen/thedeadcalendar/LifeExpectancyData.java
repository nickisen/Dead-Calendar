package com.nikolassievertsen.thedeadcalendar;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Eine Hilfsklasse, die statische Daten zur Lebenserwartung bereitstellt.
 * In einer echten App würde man dies aus einer Datenbank oder einer API laden.
 */
public class LifeExpectancyData {

    // Definiert die Geschlechter-Optionen
    public enum Gender {
        MALE,
        FEMALE
    }

    // Speichert die Lebenserwartung (in Jahren) für ein Land und Geschlecht
    private static class Expectancy {
        double male;
        double female;

        Expectancy(double male, double female) {
            this.male = male;
            this.female = female;
        }
    }

    // Unsere "Datenbank" für die Statistiken
    private static final Map<String, Expectancy> data = new HashMap<>();

    static {
        // Datenquelle: Worldometer / UN-Schätzungen 2025
        data.put("Deutschland", new Expectancy(79.42, 84.01));
        data.put("USA", new Expectancy(77.22, 82.11));
        data.put("Schweiz", new Expectancy(82.34, 86.06));
        data.put("Japan", new Expectancy(81.99, 88.03));
        data.put("Österreich", new Expectancy(79.97, 84.57));
        data.put("Welt (Durchschnitt)", new Expectancy(70.9, 76.2));
        // TODO: Füge hier weitere Länder hinzu, die du unterstützen möchtest
    }

    /**
     * Gibt ein Array mit allen verfügbaren Ländernamen zurück.
     * @return Ein String-Array mit Ländernamen.
     */
    public static String[] getAvailableCountries() {
        return data.keySet().toArray(new String[0]);
    }

    /**
     * Ruft die durchschnittliche Lebenserwartung ab.
     * @param country Das Land.
     * @param gender Das Geschlecht.
     * @return Die Lebenserwartung in Jahren oder -1, wenn keine Daten gefunden wurden.
     */
    public static double getLifeExpectancy(String country, Gender gender) {
        Expectancy expectancy = data.get(country);
        if (expectancy == null) {
            // Fallback, wenn das Land nicht in unserer Liste ist
            expectancy = data.get("Welt (Durchschnitt)");
        }

        if (gender == Gender.MALE) {
            return Objects.requireNonNull(expectancy).male;
        } else {
            return Objects.requireNonNull(expectancy).female;
        }
    }
}