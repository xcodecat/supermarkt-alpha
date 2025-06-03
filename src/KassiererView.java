import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * GUI-Klasse für die Kassierer-Oberfläche.
 * Bietet eine moderne Touchscreen-artige Oberfläche für den Kassiervorgang.
 * Unterstützt Warenkorbverwaltung, Rückgeldberechnung und Bon-Parken.
 */
public class KassiererView extends View {
    // Models für Listenanzeigen
    private DefaultListModel<String> produktListModel;
    private DefaultListModel<String> warenkorbListModel;

    // UI-Komponenten
    private JList<String> produktListe;
    private JList<String> warenkorbListe;
    private JTextField anzahlField, geldErhaltenField;
    private JLabel gesamtpreisLabel, rueckgeldLabel;

    // Backend-Anbindung
    private Kassierer kassierer;

    // Interne Daten
    private LinkedList<WarenkorbEintrag> warenkorb;
    private LinkedList<WarenkorbEintrag> geparkterBon = new LinkedList<>();

    /**
     * Konstruktor – erstellt das GUI und initialisiert alle Komponenten.
     *
     * @param datenbank Gemeinsame Produktdatenbank
     * @param graph     Beliebtheitsgraph zur Auswertung (optional)
     */
    public KassiererView(Datenbank datenbank, Beliebtheitsgraph graph) {
        this.kassierer = new Kassierer(datenbank, graph);
        this.warenkorb = new LinkedList<>();

        // Frame-Setup
        frame = new JFrame("Supermarkt Kasse");
        frame.setSize(1100, 650);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        // === Linke Seite: Produktliste ===
        produktListModel = new DefaultListModel<>();
        produktListe = new JList<>(produktListModel);
        produktListe.setFont(new Font("SansSerif", Font.PLAIN, 15));

        // Nur Produkte mit Bestand im Regal anzeigen
        for (Produkt p : kassierer.produkteAusgeben()) {
            if (p.getRegalanzahl() > 0) {
                produktListModel.addElement(p.getName());
            }
        }

        JScrollPane produktScroll = new JScrollPane(produktListe);
        produktScroll.setBorder(BorderFactory.createTitledBorder("Produkte"));
        produktScroll.setPreferredSize(new Dimension(250, 0));

        // === Rechte Seite: Warenkorb/Bon ===
        warenkorbListModel = new DefaultListModel<>();
        warenkorbListe = new JList<>(warenkorbListModel);
        warenkorbListe.setFont(new Font("Monospaced", Font.PLAIN, 15));

        JScrollPane warenkorbScroll = new JScrollPane(warenkorbListe);
        warenkorbScroll.setBorder(BorderFactory.createTitledBorder("Bon"));
        warenkorbScroll.setPreferredSize(new Dimension(300, 0));

        // === Mitte: "Touchscreen"-Panel mit Aktionen ===
        JPanel middlePanel = new JPanel(new GridBagLayout());
        middlePanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Komponenten
        JLabel anzahlLabel = new JLabel("Anzahl:");
        anzahlField = new JTextField("1", 5);
        JLabel erhaltenLabel = new JLabel("Erhalten (€):");
        geldErhaltenField = new JTextField(10);

        gesamtpreisLabel = new JLabel("Gesamt: 0.00 €");
        rueckgeldLabel = new JLabel("Rückgeld: 0.00 €");

        // Buttons mit Farben
        JButton inWarenkorbButton = button("In Warenkorb", new Color(52, 152, 219));
        JButton entfernenButton = button("Entfernen", new Color(149, 165, 166));
        JButton kassierenButton = button("Kassieren", new Color(46, 204, 113));
        JButton stornoButton = button("Storno", new Color(231, 76, 60));
        JButton bonParkenButton = button("Bon parken", new Color(241, 196, 15));
        JButton bonLadenButton = button("Bon laden", new Color(243, 156, 18));

        // Platzieren im Grid
        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; middlePanel.add(anzahlLabel, gbc);
        gbc.gridx = 1; middlePanel.add(anzahlField, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; middlePanel.add(erhaltenLabel, gbc);
        gbc.gridx = 1; middlePanel.add(geldErhaltenField, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; middlePanel.add(gesamtpreisLabel, gbc);
        gbc.gridx = 1; middlePanel.add(rueckgeldLabel, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; middlePanel.add(inWarenkorbButton, gbc);
        gbc.gridx = 1; middlePanel.add(entfernenButton, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; middlePanel.add(kassierenButton, gbc);
        gbc.gridx = 1; middlePanel.add(stornoButton, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; middlePanel.add(bonParkenButton, gbc);
        gbc.gridx = 1; middlePanel.add(bonLadenButton, gbc);

        // GUI zusammensetzen
        frame.add(produktScroll, BorderLayout.WEST);
        frame.add(middlePanel, BorderLayout.CENTER);
        frame.add(warenkorbScroll, BorderLayout.EAST);

        // Event-Handler
        inWarenkorbButton.addActionListener(e -> produktHinzufuegen());
        entfernenButton.addActionListener(e -> produktEntfernen());
        kassierenButton.addActionListener(e -> kassieren());
        stornoButton.addActionListener(e -> storno());
        bonParkenButton.addActionListener(e -> bonParken());
        bonLadenButton.addActionListener(e -> bonLaden());

        frame.setVisible(true);
    }

    /**
     * Erstellt einen standardisierten Button mit Farbe und Stil.
     */
    private JButton button(String text, Color background) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 18));
        b.setBackground(background);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(180, 45));
        b.setForeground(text.contains("Bon") ? Color.BLACK : Color.WHITE);
        return b;
    }

    /**
     * Fügt ein Produkt aus der Liste dem Warenkorb hinzu.
     */
    private void produktHinzufuegen() {
        String name = produktListe.getSelectedValue();
        if (name == null) return;
        int anzahl = parseOrDefault(anzahlField.getText(), 1);
        Produkt p = kassierer.produktSuchen(name);
        if (p != null && p.getRegalanzahl() >= anzahl) {
            kassierer.kassieren(name, anzahl);
            warenkorb.add(new WarenkorbEintrag(name, anzahl));
            warenkorbListModel.addElement(name + " × " + anzahl);
            updateGesamtpreis();
        }
        anzahlField.setText("1");
    }

    /**
     * Entfernt das aktuell ausgewählte Produkt aus dem Warenkorb.
     */
    private void produktEntfernen() {
        int index = warenkorbListe.getSelectedIndex();
        if (index >= 0) {
            warenkorb.remove(index);
            warenkorbListModel.remove(index);
            updateGesamtpreis();
        }
    }

    /**
     * Führt den Bezahlvorgang durch und berechnet Rückgeld.
     */
    private void kassieren() {
        double betrag = berechneGesamtbetrag();
        double erhalten;
        try {
            erhalten = Double.parseDouble(geldErhaltenField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Ungültiger Betrag.");
            return;
        }
        if (erhalten < betrag) {
            JOptionPane.showMessageDialog(frame, "Nicht genug Geld erhalten.");
            return;
        }
        double rueckgeld = Math.round((erhalten - betrag) * 100.0) / 100.0;
        rueckgeldLabel.setText(String.format("Rückgeld: %.2f €", rueckgeld));
        kassierer.warenkorbBeenden();
        bonDrucken(betrag, erhalten, rueckgeld);
        rueckgeldLabel.setText("Rückgeld: 0.00 €");
        warenkorb.clear();
        warenkorbListModel.clear();
        updateGesamtpreis();
        geldErhaltenField.setText("");
        JOptionPane.showMessageDialog(frame, "Zahlung abgeschlossen. Rückgeld: " + rueckgeld + " €");
    }

    /**
     * Storniert einen Eintrag aus dem Warenkorb.
     */
    private void storno() {
        produktEntfernen();
        JOptionPane.showMessageDialog(frame, "Artikel storniert.");
    }

    /**
     * Parkt den aktuellen Bon zwischen.
     */
    private void bonParken() {
        geparkterBon = new LinkedList<>(warenkorb);
        warenkorb.clear();
        warenkorbListModel.clear();
        updateGesamtpreis();
        JOptionPane.showMessageDialog(frame, "Bon wurde geparkt.");
    }

    /**
     * Lädt den geparkten Bon zurück in den Warenkorb.
     */
    private void bonLaden() {
        warenkorb.clear();
        warenkorbListModel.clear();
        warenkorb.addAll(geparkterBon);
        for (WarenkorbEintrag e : geparkterBon) {
            warenkorbListModel.addElement(e.name + " × " + e.anzahl);
        }
        updateGesamtpreis();
        JOptionPane.showMessageDialog(frame, "Geparkter Bon wurde geladen.");
    }

    /**
     * Aktualisiert die Preis-Anzeige.
     */
    private void updateGesamtpreis() {
        double summe = berechneGesamtbetrag();
        gesamtpreisLabel.setText(String.format("Gesamt: %.2f €", summe));
    }

    /**
     * Berechnet den Gesamtpreis des Warenkorbs.
     */
    private double berechneGesamtbetrag() {
        double summe = 0.0;
        for (WarenkorbEintrag e : warenkorb) {
            Produkt p = kassierer.produktSuchen(e.name);
            if (p != null) summe += p.getPreis() * e.anzahl;
        }
        return summe;
    }

    /**
     * Versucht einen int aus Text zu parsen oder nutzt Standardwert.
     */
    private int parseOrDefault(String text, int defaultValue) {
        try {
            return text.trim().isEmpty() ? defaultValue : Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Zeigt einen einfachen digitalen Kassenbon an.
     */
    private void bonDrucken(double betrag, double erhalten, double rueckgeld) {
        StringBuilder sb = new StringBuilder();
        sb.append("       *** Supermarkt-Bon ***\n\n");
        sb.append("Datum: ").append(java.time.LocalDate.now()).append("\n");
        sb.append("Uhrzeit: ").append(java.time.LocalTime.now().withSecond(0).withNano(0)).append("\n\n");
        sb.append("Artikel:\n");
        for (WarenkorbEintrag e : warenkorb) {
            Produkt p = kassierer.produktSuchen(e.name);
            if (p != null) {
                sb.append(String.format("  %-20s x%d   %.2f €\n", e.name, e.anzahl, p.getPreis() * e.anzahl));
            }
        }
        sb.append("\n");
        sb.append(String.format("Gesamt:        %.2f €\n", betrag));
        sb.append(String.format("Erhalten:      %.2f €\n", erhalten));
        sb.append(String.format("Rückgeld:      %.2f €\n", rueckgeld));
        sb.append("\n  Vielen Dank für Ihren Einkauf!");

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(frame, new JScrollPane(textArea), "Bon", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Interne Klasse für einen Warenkorb-Eintrag.
     */
    private static class WarenkorbEintrag {
        String name;
        int anzahl;

        public WarenkorbEintrag(String name, int anzahl) {
            this.name = name;
            this.anzahl = anzahl;
        }
    }
}