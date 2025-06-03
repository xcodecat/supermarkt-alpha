import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

//Emojis weggemacht
/**
 * GUI-Oberfläche zur Verwaltung der Finanzen des Supermarkts.
 * Zeigt den Gesamtumsatz, den Umsatz des Vortages, den durchschnittlichen Umsatz,
 * die Anzahl der abgeschlossenen Tage und den Tagesverlauf der Finanzen an.
 */
public class FinanzenView extends View {


    /** Labels zur Anzeige der Finanzdaten */
    private JLabel gesamtUmsatzLabel, gesternLabel, durchschnittLabel, tageLabel;

    /** Textbereich zur Anzeige des Tagesverlaufs der Finanzen */
    private JTextArea verlaufTextArea;

    /** Button zum Beenden des Tages */
    private JButton tagBeendenButton;

    /** Instanz des Geschäftsführers */
    private Geschaeftsfuehrer geschaeftsfuehrer;

    /**
     * Konstruktor: Erstellt die grafische Oberfläche und initialisiert alle Komponenten.
     * @param geschaeftsfuehrer Instanz des Geschäftsführers
     */
    public FinanzenView(Geschaeftsfuehrer geschaeftsfuehrer) {
        this.geschaeftsfuehrer = geschaeftsfuehrer;

        // Erstellen des Hauptfensters
        frame = new JFrame("Finanzen");
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        // Oben: Zusammenfassung der Finanzdaten
        JPanel summaryPanel = new JPanel(new GridLayout(4, 1));
        gesamtUmsatzLabel = new JLabel();
        gesternLabel = new JLabel();
        durchschnittLabel = new JLabel();
        tageLabel = new JLabel();

        // Hinzufügen der Labels zum Zusammenfassungspanel
        summaryPanel.add(gesamtUmsatzLabel);
        summaryPanel.add(gesternLabel);
        summaryPanel.add(durchschnittLabel);
        summaryPanel.add(tageLabel);

        // Zusammenfassungspanel zum Fenster hinzufügen (oben)
        frame.add(summaryPanel, BorderLayout.NORTH);

        // Mitte: Tagesverlauf der Finanzen
        verlaufTextArea = new JTextArea();
        verlaufTextArea.setEditable(false); // Textbereich nicht editierbar
        verlaufTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Schriftart festlegen

        // ScrollPane für den Textbereich, damit lange Texte scrollbar sind
        JScrollPane scrollPane = new JScrollPane(verlaufTextArea);
        frame.add(scrollPane, BorderLayout.CENTER); // Textbereich zum Fenster hinzufügen (Mitte)

        // Unten: Button zum Beenden des Tages
        tagBeendenButton = new JButton("Tag beenden");
        frame.add(tagBeendenButton, BorderLayout.SOUTH); // Button zum Fenster hinzufügen (unten)

        // Aktion für den "Tag beenden" Button
        tagBeendenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double umsatzHeute = geschaeftsfuehrer.tagBeenden();
                if (umsatzHeute == -1) {
                    JOptionPane.showMessageDialog(frame, "Der Tag wurde bereits beendet.", "Fehler", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(frame, "Umsatz für heute: " + String.format("%.2f €", umsatzHeute), "Tag abgeschlossen", JOptionPane.INFORMATION_MESSAGE);
                tagBeendenButton.setEnabled(false);
                updateView();
            }
        });

        updateView(); // Initiales Aktualisieren der Ansicht
        frame.setVisible(true); // Fenster sichtbar machen
    }

    /**
     * Methode zum Aktualisieren der Ansicht mit den neuesten Finanzdaten.
     */
    private void updateView() {
        double gesamt = geschaeftsfuehrer.gesamtUmsatz();
        double gestern = geschaeftsfuehrer.finanzenGestern();
        double schnitt = geschaeftsfuehrer.durchschnittsUmsatz();
        int tage = geschaeftsfuehrer.anzahlTage();
        LinkedList<Double> verlauf = geschaeftsfuehrer.finanzenAusgeben();

        // Aktualisieren der Labels mit den neuesten Werten
        gesamtUmsatzLabel.setText("Gesamtumsatz: " + String.format("%.2f €", gesamt));
        gesternLabel.setText("Umsatz gestern: " + String.format("%.2f €", gestern));
        durchschnittLabel.setText("Durchschnitt: " + String.format("%.2f €", schnitt));
        tageLabel.setText("Anzahl abgeschlossener Tage: " + tage);

        // Textbereich mit dem Tagesverlauf befüllen
        verlaufTextArea.setText("");
        for (int i = 0; i < verlauf.size(); i++) {
            verlaufTextArea.append("Tag " + (i + 1) + ": " + String.format("%.2f €", verlauf.get(i)) + "\n");
        }

        tagBeendenButton.setEnabled(!geschaeftsfuehrer.istTagBeendet());
    }
}