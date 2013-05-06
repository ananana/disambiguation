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
public class ServeCorporaTests extends TestCorpora {

 
    protected static Vector<Integer> food;
    protected static Vector<Integer> office;
    protected static Vector<Integer> function;
    protected static Vector<Integer> provide;
    protected static Hashtable filesToSenseSets;
    protected static Hashtable filesToNrs;


    @SuppressWarnings("empty-statement")
    public ServeCorporaTests()
    {
        super();
    }
    
    protected void specificStructures() {
        
        targetWord = "serve";
        String[] altserve = {"service", "services"};
        alternativeForms = altserve;
        type = SynsetType.VERB;
        
        int[] servesenses = {1, 2, 4, 5, 6};
        senses = servesenses;
        String[] servefiles = {"serve10", "serve12", "serve2", "serve6"};
        files = servefiles;
        
        food = new Vector<Integer>();
        office = new Vector<Integer>();
        function = new Vector<Integer>();
        provide = new Vector<Integer>();

        food.add(5); food.add(6);
        office.add(2);
        function.add(1);
        provide.add(4);


        filesToSenseSets = new Hashtable();
        filesToSenseSets.put("serve10", food);
        filesToSenseSets.put("serve12", office);
        filesToSenseSets.put("serve2", function);
        filesToSenseSets.put("serve6", provide);

        filesToNrs = new Hashtable();
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
