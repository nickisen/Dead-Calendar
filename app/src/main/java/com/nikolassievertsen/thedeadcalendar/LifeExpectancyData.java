package com.nikolassievertsen.thedeadcalendar;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Eine Hilfsklasse, die statische Daten zur Lebenserwartung bereitstellt.
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

    // --- DATENBANK FÜR LEBENSERWARTUNG (STARK ERWEITERT) ---
    private static final Map<String, Expectancy> data = new HashMap<>();

    static {
        // Datenquelle: Worldometer / UN-Schätzungen 2024/2025
        data.put("Welt (Durchschnitt)", new Expectancy(70.9, 76.2));
        data.put("Hong Kong", new Expectancy(83.1, 88.39));
        data.put("Japan", new Expectancy(81.99, 88.03));
        data.put("Schweiz", new Expectancy(82.34, 86.06));
        data.put("Südkorea", new Expectancy(81.44, 87.4));
        data.put("Australien", new Expectancy(82.43, 85.97));
        data.put("Italien", new Expectancy(81.94, 86.01));
        data.put("Singapur", new Expectancy(81.53, 86.48));
        data.put("Spanien", new Expectancy(81.27, 86.59));
        data.put("Norwegen", new Expectancy(82.11, 85.09));
        data.put("Schweden", new Expectancy(81.84, 85.34));
        data.put("Frankreich", new Expectancy(80.73, 86.31));
        data.put("Kanada", new Expectancy(80.74, 85.03));
        data.put("Österreich", new Expectancy(79.97, 84.57));
        data.put("Niederlande", new Expectancy(80.89, 83.98));
        data.put("Deutschland", new Expectancy(79.42, 84.01));
        data.put("UK", new Expectancy(79.72, 83.45));
        data.put("USA", new Expectancy(77.22, 82.11));
        data.put("China", new Expectancy(75.65, 81.25));
        data.put("Türkei", new Expectancy(74.94, 80.82));
        data.put("Russland", new Expectancy(67.6, 78.2)); // (Stark abweichend je nach Quelle, Schätzung)
        data.put("Brasilien", new Expectancy(72.7, 79.4));
        data.put("Indien", new Expectancy(69.9, 72.9));
        data.put("Südafrika", new Expectancy(62.3, 68.3));
        data.put("Nigeria", new Expectancy(53.8, 55.7));
        // Füge hier nach Bedarf weitere Länder hinzu...
    }

    /**
     * Gibt ein Set mit allen verfügbaren Ländernamen zurück.
     * @return Ein String-Set mit Ländernamen.
     */
    public static Set<String> getAvailableCountries() {
        return data.keySet();
    }

    /**
     * Ruft die durchschnittliche Lebenserwartung ab.
     * @param country Das Land.
     * @param gender Das Geschlecht.
     * @return Die Lebenserwartung in Jahren.
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

    /**
     * Berechnet den Abzug durch Lifestyle-Faktoren.
     * @param isSmoker Ob die Person raucht.
     * @param isDrinker Ob die Person stark trinkt.
     * @param isOverweight Ob die Person stark übergewichtig ist.
     * @return Die Anzahl der Jahre, die abgezogen werden müssen.
     */
    public static double getLifestylePenalty(boolean isSmoker, boolean isDrinker, boolean isOverweight) {
        double penalty = 0.0;

        // Basierend auf Recherche-Daten (Quellen 1.2, 2.1, 3.4, 4.1)
        if (isSmoker) {
            penalty += 9.0; // Durchschnittlicher Verlust für starke Raucher
        }
        if (isDrinker) {
            penalty += 4.0; // Verlust für starken Alkoholkonsum
        }
        if (isOverweight) {
            penalty += 5.0; // Verlust für Adipositas (BMI > 30)
        }

        return penalty;
    }
}