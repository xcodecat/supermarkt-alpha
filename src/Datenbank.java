import java.io.*;
import java.util.Comparator;
import java.util.LinkedList;
import java.time.LocalDate;
import java.nio.file.*;

/**
 * Zentrale Datenhaltung für Produkte und Finanzen des Supermarkts
 */
public class Datenbank {
    /**
     * Liste aller Produkte des Supermarkts
     */
    private LinkedList<Produkt> produkte;
    /**
     * gesamter Umsatz
     */
    private double umsatz;
    /**
     * Liste aller Tagesumsätze
     */
    private LinkedList<Double> tage;
    /**
     * Pfad zur Datei, in der das Datum des letzten Tagesabschlusses gespeichert wird
     */
    private static final String STATUS_DATEI = "tagstatus.txt";

    /**
     * erzeugt eine neue Datenbank, lädt Produkte und Finanzen aus Dateien
     */
    public Datenbank() {
        this.produkte = produkteLaden();
        this.umsatz = 0.0;
        this.tage = finanzenLaden();
    }

    public LinkedList<Produkt> produkteAusgeben() {
        return produkte;
    }

    /**
     * gibt die Liste aller Produkte als Array aus
     *
     * @return Produkt[]
     */
    public Produkt[] arrayAusgeben() {
        Produkt[] ptemp = new Produkt[produkte.size()];
        for (int i = 0; i < produkte.size(); i++) {
            ptemp[i] = produkte.get(i);
        }
        return ptemp;
    }

    /**
     * verändert den Ort eines Produkts
     * @param ort  Regalort des Produkts im Format BuchstabeZahl
     * @param name Name des veränderten Produkts
     * @return int, der Art eines möglichen Fehlers angibt
     * 0: kein Fehler
     * 1: Ort nicht vorhanden oder schon 4 Produkte an diesem Ort (--> voll)
     * 2: Produkt nicht vorhanden (oder z.B. Name falsch geschrieben)
     */
    public int produktortVeraendern(String ort, String name) {
        if (ortChecken(ort)) {
            Produkt p = produktSuchen(name);
            if (p == null) return 2;
            p.setOrt(ort);
            produkteSpeichern();
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * überprüft, ob ein Regalort bereits besetzt ist (4 Produkte --> besetzt)
     *
     * @param ort Ort, der gecheckt werden soll (Format BuchstabeZahl)
     * @return true, wenn frei; false, wenn besetzt
     */
    public boolean ortChecken(String ort) {
        int z = 0;
        for (Produkt p : produkte) {
            if (p.getOrt().equals(ort)) {
                z++;
            }
        }
        return z <= 3;
    }

    /**
     * verändert die Anzahl der Produkte in Lager oder Regal(gesteuert durch boolean)
     * entfernen durch eine negative Anzahl
     * bei Hinzufügen zu Regal wird dieselbe Anzahl aus Lager entfernt
     *
     * @param anzahl, gibt Anzahl der hinzugefügten (anzahl > 0) oder der entfernten (anzahl < 0) Produkte an
     * @param ort     als boolean: true, wenn in Lager; false, wenn in Regal
     * @param name    des betreffenden Produkts
     * @return int, der einen möglichen Fehler angibt
     * 0: kein Fehler
     * 1: Name nicht vorhanden (oder falsch geschrieben)
     * 2: mehr aus Lager entfernt als vorhanden ist
     * 3: mehr aus Regal entfernt als vorhanden ist
     * 4: kein Regalort zugewiesen
     * 5: mehr zu Regal hinzugefügt, als im Lager vorhanden ist
     */
    public int produktanzahlVeraendern(int anzahl, boolean ort, String name) {
        Produkt p = produktSuchen(name);
        if (p == null) return 1;

        if (ort) { // Lager
            if (p.getLageranzahl() + anzahl < 0) return 2;
            p.setLageranzahl(p.getLageranzahl() + anzahl);
        } else { // Regal
            if (p.getOrt().equals("Lager")) return 4;
            if (anzahl > 0 && p.getLageranzahl() - anzahl < 0) return 5;
            if (p.getRegalanzahl() + anzahl < 0) return 3;
            if (anzahl > 0) p.setLageranzahl(p.getLageranzahl() - anzahl);
            p.setRegalanzahl(p.getRegalanzahl() + anzahl);
        }

        produkteSpeichern();
        return 0;
    }

    /**
     * Spezielle Methode für Verkäufer
     * entfernt das gekaufte Produkt aus dem Regal und rechnet den Verkaufspreis auf den Umsatz auf
     *
     * @param anzahl des verkauften Produkts
     * @param name   des verkauften Produkts
     * @return mögliche Fehlermeldungen
     * 0: kein Fehler
     * 1: Name nicht vorhanden (oder falsch geschrieben)
     */
    public int produktanzahlVeraendernK(int anzahl, String name) {
        Produkt p = produktSuchen(name);
        if (p != null) {
            p.setRegalanzahl(p.getRegalanzahl() - anzahl);
            produkteSpeichern();
            umsatz += p.getPreis() * anzahl;
            return 0;
        }
        return 1;
    }

    public void produktEinfuegen(Produkt produkt) {
        produkte.add(produkt);
        produkteSpeichern();
    }

    public boolean produktEntfernen(String name) {
        Produkt p = produktSuchen(name);
        if (p != null) {
            produkte.remove(p);
            produkteSpeichern();
            return true;
        }
        return false;
    }

    public Produkt produktSuchen(String name) {
    	
        for (Produkt p : produkte) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        
        return null;
    }

    /**
     * erhöht die im Produkt gespeicherten Einkaufszahlen
     *
     * @param anzahl, der verkauften Stücke
     * @param name,   des betreffenden Produkts
     */
    public void einkaufszahlenErhoehen(int anzahl, String name) {
        Produkt p = produktSuchen(name);
        if (p != null) {
            p.setEinkaufszahlen(p.getEinkaufszahlen() + anzahl);
            produkteSpeichern();
        }
    }

    public double getUmsatz() {
        return umsatz;
    }

    public void umsatzErhoehen(double betrag) {
        this.umsatz += betrag;
    }

    public LinkedList<Double> finanzenAusgeben() {
        return tage;
    }

    public void tagAbschliessen() {
        tage.add(umsatz);
        finanzenSpeichern();
    }


    /**
     * gibt den Ort des Produktes aus
     *
     * @param name Name des Produktes
     * @return Ort des Produktes
     */
    public String getProduktort(String name) {

        Produkt temp = produktSuchen(name);
        return temp.getOrt();

    }

    /**
     * Speichert alle Produktdaten in die Datei "produkte.csv".
     * Jedes Produkt wird als eine Zeile mit Semikolon-getrennten Werten geschrieben.
     */
    public void produkteSpeichern() {
        produkte.sort(Comparator.comparing(Produkt::getName, String.CASE_INSENSITIVE_ORDER));
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("produkte.csv"))) {

            // Iteriere über alle Produkte in der Liste
            for (Produkt p : produkte) {
                // Schreibe alle Eigenschaften des Produkts in einer Zeile, getrennt durch Semikolon
                // Format: Name;Ort;Lageranzahl;Regalanzahl;Preis;Verkaufszahlen;Einkaufspreis;Einkaufszahlen
                bw.write(p.getName() + ";" +
                        p.getOrt() + ";" +
                        p.getLageranzahl() + ";" +
                        p.getRegalanzahl() + ";" +
                        p.getPreis() + ";" +
                        p.getVerkaufszahlen() + ";" +
                        p.getEinkaufspreis() + ";" +
                        p.getEinkaufszahlen());
                bw.newLine(); // neue Zeile für das nächste Produkt
            }

        } catch (IOException e) {
            // Fehler beim Schreiben der Datei
            e.printStackTrace();
        }
    }

    /**
     * Lädt die Produktdaten aus der Datei "produkte.csv".
     * Erwartet keine Kopfzeile.
     *
     * @return Liste von Produkten aus der Datei
     */
    private LinkedList<Produkt> produkteLaden() {
        LinkedList<Produkt> list = new LinkedList<>();
        File file = new File("produkte.csv");

        // Wenn Datei nicht existiert, gib leere Liste zurück
        if (!file.exists()) {return list;}

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String zeile;

            while ((zeile = br.readLine()) != null) {
                // Spalten anhand des Trennzeichens (;) aufteilen
                String[] teile = zeile.split(";");

                // Prüfen, ob genau 8 Spalten vorhanden sind
                if (teile.length == 8) {
                    String name = teile[0];
                    String ort = teile[1];
                    int lager = Integer.parseInt(teile[2]);
                    int regal = Integer.parseInt(teile[3]);

                    // Preise können Kommas statt Punkten enthalten → umwandeln
                    double preis = Double.parseDouble(teile[4].replace(",", "."));
                    int verkauf = Integer.parseInt(teile[5]);
                    double ek = Double.parseDouble(teile[6].replace(",", "."));
                    int ekZahl = Integer.parseInt(teile[7]);

                    // Produkt zur Liste hinzufügen
                    list.add(new Produkt(name, ort, lager, regal, preis, verkauf, ek, ekZahl));
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Fehlerausgabe bei Leseproblemen
        }

        return list; // Rückgabe der geladenen Produkte
    }

    /**
     * Lädt die Liste aller Tagesumsätze aus der Datei "finanzen.csv".
     * Erwartet eine Zahl pro Zeile (ein Umsatzwert).
     *
     * @return LinkedList mit den bisherigen Tagesumsätzen
     */
    private LinkedList<Double> finanzenLaden() {
        LinkedList<Double> liste = new LinkedList<>();
        File file = new File("finanzen.csv");

        // Wenn die Datei nicht existiert, gib leere Liste zurück
        if (!file.exists()) return liste;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String zeile;

            // Zeile für Zeile einlesen
            while ((zeile = br.readLine()) != null) {
                // Komma in Dezimalzahlen (z. B. 12,99) durch Punkt ersetzen
                zeile = zeile.replace(",", ".");

                // In double parsen und zur Liste hinzufügen
                liste.add(Double.parseDouble(zeile));
            }

        } catch (IOException e) {
            e.printStackTrace(); // Fehler bei Dateioperationen ausgeben
        }

        return liste; // Rückgabe der Umsatzliste
    }

    /**
     * Speichert die Liste aller Tagesumsätze in die Datei "finanzen.csv".
     * Jeder Umsatzwert wird in eine eigene Zeile geschrieben.
     */
    public void finanzenSpeichern() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("finanzen.csv"))) {

            // Für jeden Tagesumsatz ...
            for (Double betrag : tage) {
                // Schreibe den Betrag in die Datei (z.B. 12.99)
                bw.write(String.valueOf(betrag));
                bw.newLine(); // neue Zeile für nächsten Wert
            }

        } catch (IOException e) {
            // Bei einem Fehler während des Schreibens in die Datei
            e.printStackTrace();
        }
    }

    /**
     * Speichert das Datum des letzten abgeschlossenen Tages in die Datei "tagstatus.txt"
     * @param datum Datum des Tagesabschlusses
     */
    public void speichereLetztesDatum(LocalDate datum) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(STATUS_DATEI))) {
            writer.write(datum.toString()); // Format: yyyy-MM-dd
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern des Tagesabschluss-Datums: " + e.getMessage());
        }
    }

    /**
     * Lädt das Datum des letzten abgeschlossenen Tages aus der Datei "tagstatus.txt"
     * @return LocalDate des letzten Abschlusses oder null, wenn keine Datei vorhanden ist
     */
    public LocalDate ladeLetztesDatum() {
        try {
            String zeile = Files.readString(Paths.get(STATUS_DATEI)).trim();
            if (!zeile.isEmpty()) {
                return LocalDate.parse(zeile); // erwartet Format yyyy-MM-dd
            }
        } catch (IOException e) {
            // Datei existiert nicht oder konnte nicht gelesen werden
        }
        return null;
    }
}
