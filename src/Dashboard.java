import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;

public class Dashboard {
    public Dashboard() {
        // Erstellen einer Instanz der Datenbank
        Datenbank datenbank = new Datenbank();

        Beliebtheitsgraph beliebtheitsgraph = new Beliebtheitsgraph(datenbank);
        // Erstellen einer Instanz des Geschäftsführers mit der Datenbank
        Geschaeftsfuehrer geschaeftsfuehrer = new Geschaeftsfuehrer(datenbank);

        // Erstellen des Hauptfensters
        JFrame frame = new JFrame("Rollenwahl – Supermarkt-System");

        // Setzen der Standard-Schließoperation (Schließen des Fensters beendet das Programm)
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Festlegen der Größe des Fensters
        frame.setSize(500, 300);

        // Setzen des Layouts des Fensters (2 Zeilen, 3 Spalten, 10 Pixel Abstand)
        frame.setLayout(new GridLayout(2, 3, 10, 10));

        //Position in die Mitte des Bildschirms setzen
        frame.setLocationRelativeTo(null);

        // Erstellen der Buttons für die verschiedenen Rollen und Funktionen
        JButton btnKassierer = new JButton("Kassierer");
        JButton btnLager = new JButton("Lagermitarbeiter");
        JButton btnKunde = new JButton("Kunde");
        JButton btnGeschaeftsfuehrer = new JButton("Geschäftsführer");

        // Hinzufügen von ActionListenern zu den Buttons, um die entsprechenden Views zu öffnen
        btnKassierer.addActionListener(e -> new KassiererView(datenbank, beliebtheitsgraph));
        btnLager.addActionListener(e -> new LagermitarbeiterView(datenbank, beliebtheitsgraph));
        btnKunde.addActionListener(e -> new KundeView(datenbank));
        btnGeschaeftsfuehrer.addActionListener(e -> new GeschaeftsfuehrerView(geschaeftsfuehrer));

        // Hinzufügen der Buttons zum Fenster
        frame.add(btnKassierer);
        frame.add(btnLager);
        frame.add(btnKunde);
        frame.add(btnGeschaeftsfuehrer);

        // Fenster sichtbar machen
        frame.setVisible(true);
    }
}
