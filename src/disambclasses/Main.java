/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package disambclasses;
import edu.smu.tspell.wordnet.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author HOME
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

       String wnpath = "";
        if (System.getProperty("os.name").contains("Windows"))
            wnpath = ".\\libs_and_WordNet_and_WordNet\\WordNet-3.0\\dict\\";
        else
            wnpath = "./libs/WordNet-3.0/dict/";
        System.setProperty("wordnet.database.dir", wnpath);
        TryStuff trier = new TryStuff();

    //    System.out.println("Fara normalizare dupa lungimea totala a sirului inruditelor cu cuv target;\nignora cuvintele cu alta parte de vorbire; " +
    //            "cu hipo-hipo si mero-mero, dar fara hipo-mero si mero-hipo; function word completat, /*scos din stringul comun si primele sau ultimele cuvinte daca sunt function words*/:");
      //  trier.testAllFiles();
      // trier.printConfMatrix();
       // trier.testFile("text2");
       // trier.fctwordlist();
        //trier.getRelatedForNoun("green line");
      //  trier.overlap("house on the beach", "house on the beach");
       // trier.getSensesFor("interest", SynsetType.NOUN);
       // trier.parseInterestFiles("/home/ana/Downloads/interest/interest.cor");
     //   trier.getRelatedForms("lines of products", SynsetType.NOUN);
        //String[] alt = {"windows"};
        //trier.disambiguateWord("line", "there was a line of people at the store today, they were cool", alt);
       // trier.disambiguateWord("window", "there are too many open windows on my computer screen", alt);
      //trier.printfile();
        
//        disambclasses.InterestCorporaTests test = new disambclasses.InterestCorporaTests();
//        String[] contexts = test.parseOriginalInterest(test.getAsString("interest2"));
//        for (String s : contexts)
//            System.out.println(s + "------------------------------");
        trier.testFile("interest2");
    }

}
