package menus;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import testcorpora.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TestAllMenu.java
 *
 * Created on Apr 30, 2011, 9:55:48 PM
 */

/**
 *
 * @author ana
 */
public class TestAllMenu extends javax.swing.JFrame {

    static TestCorpus test = TestCorporaFactory.getTestingInstance(CorpusType.INTEREST);
    static Thread t;
    /** Creates new form TestAllMenu */
    public TestAllMenu() {
        String wnpath = "";
        if (System.getProperty("os.name").contains("Windows"))
            wnpath = ".\\libs_and_WordNet\\WordNet-3.0\\dict\\";
        else
            wnpath = "./libs_and_WordNet/WordNet-3.0/dict/";
        System.setProperty("wordnet.database.dir", wnpath);
        //System.setProperty("wordnet.database.dir", ".\\libs\\WordNet-3.0\\dict\\");
        initComponents();

       // test.setWindow(7);
        test.setWithReportFile(false);

        test.setOnlyUseSamePos(true);
        test.setNormalize(true);

        //sa prind filenotfoundexception? dar mai incolo
 
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ButtonPause = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        ButtonPause.setText("Show");
        ButtonPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonPauseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(ButtonPause)
                .addContainerGap(67, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(ButtonPause)
                .addContainerGap(30, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ButtonPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButtonPauseActionPerformed
        // TODO add your handling code here:
      //  test.paused = true;
        System.out.println(test.getAllResults());
    }//GEN-LAST:event_ButtonPauseActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TestAllMenu().setVisible(true);
            }
        });
       // System.out.println("cu toate relatiile; fereastra reparata!:");
    //    System.out.println("concatenate si folosind normalizarea: \n " +
    //            "score /= (textToWords(concatenated1).length * textToWords(concatenated2).length);");
       // System.out.println("fara normalizare");
        System.out.println("cu normalizare 2.0 (adun lungimile targeturilor din fiecare j)");
        System.out.println("cu grupurile de sensuri, scote un nr de sens catre clasele de teste");
     //   System.out.println("doar cu glosele celor care nu sunt aceeasi pos");
       // System.out.println("daca n-au acelasi pos nu face nimic");
       // System.out.println("cu definitiile cuvintelor din jur daca nu sunt acelasi pos; cu noua metoda de overlap intre toata lumea");
       // System.out.println("cu scoreAll");
       // System.out.println("cu greseala de la overlap cu tot");
      //  System.out.println("cu windowsize = windowsize/2 + 1");
      //  System.out.println("incercare cu scorul suma din n^2");

        t = new Thread(test);
        t.run();
        System.out.println(test.getAllResults());
        System.out.println(" - done. - ");

        String respath = "./interest results/";
        String filename = respath + "licenta1";
        File f = new File(filename);
        int nr = 1;
         BufferedWriter resultFile = null;
        while (f.exists())
        {
            filename = respath + "licenta1" + "(" + nr++ + ")";
            f = new File (filename);
           
        }
        try {
            resultFile = new BufferedWriter(new FileWriter(filename));
        } catch (IOException ex) {
            System.err.println("eroare la scriere in fisier - " + ex.getMessage());
        } catch (NullPointerException ex) {
            System.err.println("eroare la scriere in fisier - " + ex.getMessage());
        }

        try {
                resultFile.write("cu normalizare 2.0 (adun lungimile targeturilor din fiecare j");
                resultFile.write(test.getAllResults());
                resultFile.flush();  //?

            } catch (IOException ex) {
                System.err.println("eroare interna la scrierea in fisier - " + ex.getMessage());
            } catch (NullPointerException ex) {
                System.err.println("eroare interna la scriere in fisier - " + ex.getMessage());
            }
        try {
            resultFile.close();
        } catch (IOException ex) {
            Logger.getLogger(TestAllMenu.class.getName()).log(Level.SEVERE, null, ex);
        }
         
        try {
            t.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(TestAllMenu.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ButtonPause;
    // End of variables declaration//GEN-END:variables

}
