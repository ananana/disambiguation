/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package testcorpora;

import edu.smu.tspell.wordnet.SynsetType;
import java.util.HashMap;
import java.util.ArrayList;

/**
 *
 * @author ana
 */
public class ServeCorporaTests extends CorporaTests {

 
    private static ArrayList<Integer> food;
    private static ArrayList<Integer> office;
    private static ArrayList<Integer> function;
    private static ArrayList<Integer> provide;

    
    ServeCorporaTests()
    {
        super(CorpusType.SERVE);
        specificStructures();
        initializeStuff();
    }
    
    protected final void specificStructures() {
        
        targetWord = "serve";
        String[] altserve = {"service", "services"};
        alternativeForms = altserve;
        synsetType = SynsetType.VERB;
        
        int[] servesenses = {1, 2, 4, 5, 6};
        senses = servesenses;
        String[] servefiles = {"serve10", "serve12", "serve2", "serve6"};
        files = servefiles;
        
        food = new ArrayList<Integer>();
        office = new ArrayList<Integer>();
        function = new ArrayList<Integer>();
        provide = new ArrayList<Integer>();

        food.add(5); food.add(6);
        office.add(2);
        function.add(1);
        provide.add(4);


        filesToSenseSets = new HashMap();
        filesToSenseSets.put("serve10", food);
        filesToSenseSets.put("serve12", office);
        filesToSenseSets.put("serve2", function);
        filesToSenseSets.put("serve6", provide);

        filesToNrs = new HashMap();
        filesToNrs.put("serve10", 0);
        filesToNrs.put("serve12", 1);
        filesToNrs.put("serve2", 2);
        filesToNrs.put("serve6", 3);

        /*
         * 0 - supply with food
         * 1 - hold an office
         * 2 - function as someting
         * 3 - provide a service
         */

    }

    public String[] parseOriginal(String text)
    {
        String[] contexts;
        contexts = text.split("((aphb)|(w7))_([0-9]*)_([0-9]*)");   //cam taraneste, nu stiu sigur daca am impartit si dupa niste neimpartitoare (in mijlocul unei instante); nu stiu daca e ala fara duplicate
                                                    //nu stiu ce e cu ' ' " "  //(box) (check) (hbox) (1)
        return contexts;                               //niste &equals si crap nu inteleg, in hard3
    }

}
