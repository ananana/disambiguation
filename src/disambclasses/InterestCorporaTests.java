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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ana
 */


/*
 * TODO: create superclass for test classes to inherit from
 */


public class InterestCorporaTests extends TestCorpora {

    protected static Vector<Integer> interest1;
    protected static Vector<Integer> interest2;
    protected static Vector<Integer> interest3;
    protected static Vector<Integer> interest4;
    protected static Vector<Integer> interest5;
    protected static Vector<Integer> interest6;


    @SuppressWarnings("empty-statement")
    public InterestCorporaTests()
    {
        super();
    }
    
    protected void specificStructures() {
        targetWord = "interest";
        type = SynsetType.NOUN;
        
        int[] interestsenses = {1, 2, 3, 4, 5, 7};
        senses = interestsenses;
        String[] interestfiles = {"interest1", "interest2", "interest3", "interest4", "interest5", "interest6"};
        files = interestfiles;
        
        interest1 = new Vector<Integer>();
        interest2 = new Vector<Integer>();
        interest3 = new Vector<Integer>();
        interest4 = new Vector<Integer>();
        interest5 = new Vector<Integer>();
        interest6 = new Vector<Integer>();

        interest1.add(1);
        interest2.add(3);
        interest3.add(7);
        interest4.add(2);
        interest5.add(5);
        interest6.add(4);


        filesToSenseSets = new Hashtable();
        filesToSenseSets.put("interest1", interest1);
        filesToSenseSets.put("interest2", interest2);
        filesToSenseSets.put("interest3", interest3);
        filesToSenseSets.put("interest4", interest4);
        filesToSenseSets.put("interest5", interest5);
        filesToSenseSets.put("interest6", interest6);
       
        filesToNrs = new Hashtable();
        filesToNrs.put("interest1", 0);
        filesToNrs.put("interest2", 1);
        filesToNrs.put("interest3", 2);
        filesToNrs.put("interest4", 3);
        filesToNrs.put("interest5", 4);
        filesToNrs.put("interest6", 5);
      
        
        /*
         * TODO: add definitions (corpus and worndet) to these comments
         */
        /*
         * 0 - interest1: 
         * 1 - interest2:
         * 2 - interest3:
         * 3 - interest4:
         * 4 - interest5:
         * 5 - interest6:
         */
    }

    public String[] parseOriginal(String text)
    {
        String[] contexts;
        text = text.replaceAll("<tag \".*\">", "");
        text = text.replaceAll("</>", "");
       
        String patternStr = "<s>(.*)</s>";
        Pattern p = Pattern.compile(patternStr);
        Matcher m = p.matcher(text);
        
        Vector<String> contextsv = new Vector<String>();
        
        while (m.find()) {
            String context = m.group(0);
            contextsv.add(context.replaceAll("</?s>", ""));
        }
        contexts = new String[contextsv.size()];
        
        contexts = (String[])contextsv.toArray(contexts);
 
                                                    
        return contexts;                              
    }

}
