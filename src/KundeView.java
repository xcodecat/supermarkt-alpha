/**
 * Eingabefenster zum Suchen von Produkten und deren Atributen
 */


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class KundeView extends View {
    private JTextField suchenField;
    private JButton suchenButton;
    private JList<String> produktListe;
    private DefaultListModel<String> produktListModel;
    private Kunde kunde;

    public KundeView(Datenbank datenbank) {
        this.kunde = new Kunde(datenbank);
        /**
         * Initialisierung des Frames inclusive Suchfeld, Suchknopf und Produktliste
         */
        frame = new JFrame("Kunde - Produktauswahl");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(700, 600);
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        JPanel eingabePanel = new JPanel(new GridLayout(2, 2, 5, 5));
        eingabePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        suchenButton = new JButton("Suchen");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(suchenButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        
        suchenField = new JTextField();

        eingabePanel.add(new JLabel("Suchen:")); eingabePanel.add(suchenField);

        frame.add(eingabePanel, BorderLayout.NORTH);

        produktListModel = new DefaultListModel<>();
        produktListe = new JList<>(produktListModel);
        JScrollPane scrollPane = new JScrollPane(produktListe);
        scrollPane.setPreferredSize(new Dimension(500, 250));
        frame.add(scrollPane, BorderLayout.CENTER);

        for (Produkt p : kunde.produkteAusgeben()) {
            produktListModel.addElement(p.getName());
        }

        /**
         * Wenn auf ein Produkt in der Produktliste mit Rechtsklick geklickt wurde, 
         * erscheint ein Popup, in welchem Name, Ort, Preis und Anzahl des Produkts steht
         */
        produktListe.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                int index = produktListe.locationToIndex(evt.getPoint());
                if (index >= 0) {
                    produktListe.setSelectedIndex(index);
                    String name = produktListe.getSelectedValue();
                    Produkt p = kunde.produktSuchen(name);
                    if (p == null) return;

                    // Popup nur bei Rechtsklick
                    if (SwingUtilities.isRightMouseButton(evt)) {
                        String info = "Produkt: " + p.getName()
                                + "\nOrt: " + p.getOrt()
                                + "\nPreis: " + p.getPreis() + " €"
                				+ "\nRegal: " + p.getRegalanzahl();
                        JOptionPane.showMessageDialog(frame, info, "Produktinfo", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
        /**
         * Wenn auf auf den Suchenknopf gedrückt wurde, erscheint ein Popup, 
         * in welchem Name, Ort, Preis und Anzahl des Produkts steht, welches zufor in das Suchfeld eingegeben wurde
         */
        suchenButton.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		String name = suchenField.getText();
        		Produkt p = kunde.produktSuchen(name);
        		if(p == null) return;
        		else {
        			String info = "Produkt: " + p.getName()
        					+ "\nOrt: " + p.getOrt()
        					+ "\nPreis: " + p.getPreis() + " €"
        					+ "\nRegal: " + p.getRegalanzahl();
        				JOptionPane.showMessageDialog(frame, info, "Produktinfo", JOptionPane.INFORMATION_MESSAGE);
        		}
        	}
        });
        /**
         * Wenn ein Produkt in das Suchfeld eingegeben wurde und enter gedrückt wurde, 
         * erscheint ein Popup, in welchem Name, Ort, Preis und Anzahl des Produkts steht
         */
        suchenField.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		String name = suchenField.getText();
        		Produkt p = kunde.produktSuchen(name);
        		if(p == null) return;
        		else {
        			String info = "Produkt: " + p.getName()
        					+ "\nOrt: " + p.getOrt()
        					+ "\nPreis: " + p.getPreis() + " €"
        					+ "\nRegal: " + p.getRegalanzahl();
        				JOptionPane.showMessageDialog(frame, info, "Produktinfo", JOptionPane.INFORMATION_MESSAGE);
        		}
        	}
        });
        frame.setVisible(true);
    }
}