/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testcorpora;

import edu.smu.tspell.wordnet.SynsetType;
import java.util.ArrayList;
import java.util.HashMap;



/**
 *
 * @author HOME
 */

// TODO: make them singleton?


public class LineCorporaTests extends CorporaTests{

    private static ArrayList<Integer> product;
    private static ArrayList<Integer> text;
    private static ArrayList<Integer> phone;
    private static ArrayList<Integer> people;
    private static ArrayList<Integer> division;
    private static ArrayList<Integer> cord;


    LineCorporaTests()
    {
        super(CorpusType.LINE);
        specificStructures();
        initializeStuff();
    }
    
    @Override
    protected final void specificStructures() {
        
        targetWord = "line";
        String[] altline = {"lines", "lined"};
        alternativeForms = altline;
        synsetType = SynsetType.NOUN;
        
        // ? how optimal is this? does it create the arrays twice?
        int[] linesenses = {22, 28, 5, 27, 15, 3, 1, 29, 18};
        senses = linesenses;
        String[] linefiles = {"cord2", "division2", "formation2", "phone2", "product2", "text2"};
        files = linefiles;

        product = new ArrayList<Integer>();
        text = new ArrayList<Integer>();
        phone = new ArrayList<Integer>();
        people = new ArrayList<Integer>();
        division = new ArrayList<Integer>();
        cord = new ArrayList<Integer>();

        product.add(22);
        text.add(28); text.add(5); text.add(27);
        phone.add(15);
        people.add(1); people.add(3);
        division.add(29);
        cord.add(18);
        
        
        filesToSenseSets = new HashMap();
        filesToSenseSets.put("cord2", cord);
        filesToSenseSets.put("division2", division);
        filesToSenseSets.put("formation2", people);
        filesToSenseSets.put("phone2", phone);
        filesToSenseSets.put("text2", text);
        filesToSenseSets.put("product2", product);

        filesToNrs = new HashMap();
        filesToNrs.put("cord2", 0);
        filesToNrs.put("division2", 1);
        filesToNrs.put("formation2", 2);
        filesToNrs.put("phone2", 3);
        filesToNrs.put("product2", 4);
        filesToNrs.put("text2", 5);

        /*
         * 0 - cord
         * 1 - division
         * 2 - formation
         * 3 - phone
         * 4 - product
         * 5 - text
         */
    }
    
    @Override
    public String[] parseOriginal(String text) {
        String[] contexts;
        contexts = text.split("(w([0-9_])*:[0-9]*:)|(((art|rel)([0-9]*)?} )?aphb [0-9]*:)");
        for (int i = 0; i < contexts.length; i++)
            contexts[i] = contexts[i].replaceAll("(</?(s|p)>)|@", "");
        return contexts;
    }
  
}
