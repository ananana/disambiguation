/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package disambclasses;

import edu.smu.tspell.wordnet.SynsetType;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author ana
 */
public class HardCorporaTests extends TestCorpora {

    private static boolean onlyUseSamePos;
    private boolean normalize;
    private static int window;
    private static boolean withReportFile = false;
    private static Vector<Integer> difficult;
    private static Vector<Integer> notsoft1;
    private static Vector<Integer> notsoft2;
    private static Hashtable filesToSenseSets;
    private static Hashtable filesToNrs;

    private static Vector<Integer>[] candidateGroups;

    private static int totalfor1, corectefor1;
    private static int[] myResults;
    private static int[] correctResultsPerFile;
    private static int[] totalOccurences;
    private static float[] recall;
    private static float[] precision;
    private static int[][] confusionMatrix;


    @SuppressWarnings("empty-statement")
    public HardCorporaTests()
    {
        super();
    }
    
    protected void specificStructures() {
        targetWord = "hard";
        type = SynsetType.ADJECTIVE;
        
        int[] hardsenses = {1, 2, 8, 3, 12};
        senses = hardsenses;
        String[] hardfiles = {"hard1", "hard2", "hard3"};
        files = hardfiles;
        
        difficult = new Vector<Integer>();
        notsoft1 = new Vector<Integer>();
        notsoft2 = new Vector<Integer>();

        difficult.add(1);
        notsoft1.add(2); notsoft1.add(8);
        notsoft2.add(3); notsoft2.add(12);


        filesToSenseSets = new Hashtable();
        filesToSenseSets.put("hard1", difficult);
        filesToSenseSets.put("hard2", notsoft1);
        filesToSenseSets.put("hard3", notsoft2);
       
        filesToNrs = new Hashtable();
        filesToNrs.put("hard1", 0);
        filesToNrs.put("hard2", 1);
        filesToNrs.put("hard3", 2);
      
        /*
         * 0 - difficult
         * 1 - notsoft1 (metaforic)
         * 2 - notsoft2 (fizic)
         */

    }

    public String[] parseOriginal(String text)
    {
        String[] contexts;
        contexts = text.split("(((sjm-)|(w[0-9]*_))([0-9]*:))|(lob-[A-Z][0-9]:)|(br-[a-z][0-9]*:<s(.)*>)|(t([0-9])*:)");   //cam taraneste, nu stiu sigur daca am impartit si dupa niste neimpartitoare (in mijlocul unei instante); nu stiu daca e ala fara duplicate
                                                    //nu stiu ce e cu ' ' " "  //(box) (check) (hbox) (1)
        return contexts;                               //niste &equals si crap nu inteleg, in hard3
    }

}
