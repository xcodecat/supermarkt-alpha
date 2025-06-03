import javax.swing.*;
import java.awt.*;

//GeschäftsführerView
public class GeschaeftsfuehrerView {
    public GeschaeftsfuehrerView(Geschaeftsfuehrer geschaeftsfuehrer) {
        JFrame frame = new JFrame("Geschaeftsfuehrer");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(500, 250);
        frame.setLayout(new GridLayout(1, 2));
        frame.setLocationRelativeTo(null);

        JButton btnProduktverwaltung = new JButton("Produktverwaltung");
        JButton btnFinanzen = new JButton("Finanzen");

        btnProduktverwaltung.addActionListener(e -> new ProduktverwaltungsView(geschaeftsfuehrer));
        btnFinanzen.addActionListener(e -> new FinanzenView(geschaeftsfuehrer));

        frame.add(btnProduktverwaltung);
        frame.add(btnFinanzen);
        frame.setVisible(true);
    }
}