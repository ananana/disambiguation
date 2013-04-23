/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package disambclasses;

import edu.smu.tspell.wordnet.*;
import java.io.*;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author HOME
 */
public class TryStuff {

    public void testAllFiles()
    {

        LineCorporaTests test = new LineCorporaTests();
        test.setWindow(3);
        test.setWithReportFile(false);
        
        test.setOnlyUseSamePos(false);
        test.testLineCorpusFromAll();

        System.out.println(test.getAllResults());

        test.setOnlyUseSamePos(true);
        test.testLineCorpusFromAll();

        System.out.println(test.getAllResults());

    }

    public void testFile(String file)
    {

        InterestCorporaTests test = new InterestCorporaTests();
        test.setWindow(3);
        test.setWithReportFile(true);
        System.out.println(file);

        test.setOnlyUseSamePos(true);
        test.testInterestCorpusFromFile(file);

        System.out.println(test.getOneResult());

//        test.setOnlyUseSamePos(false);
//        test.testInterestCorpusFromFile(file);
//
//        System.out.println(test.getOneResult());

    }

    public void getRelatedForms(String word, SynsetType type)
    {
        WordNetDatabase wnd = WordNetDatabase.getFileInstance();
        Synset[] synsets = wnd.getSynsets(word, type);
        WordSense[] senses;
        System.out.println("derivationally related:");
        for (Synset s : synsets)
        {
            senses = s.getDerivationallyRelatedForms(word);
            for (WordSense sense : senses)
                System.out.println(sense.getWordForm() + " ");
        }
        String[] cand = wnd.getBaseFormCandidates(word, type);
        System.out.println("baseform:");
        for (String c : cand)
            System.out.println(c + " ");
    }

    public void getSensesFor(String word, SynsetType type)
    {
        int i = 1;
        WordNetDatabase wnd = WordNetDatabase.getFileInstance();
        Synset[] synsets = wnd.getSynsets(word, type);
        for (Synset s : synsets)
        {
            System.out.print(i++ + ". ");
            String[] wordforms = s.getWordForms();
            for (String wf : wordforms)
                System.out.print(wf + " ");
            System.out.print(" = ");
            System.out.println(s.getDefinition());
            String[] examples = s.getUsageExamples();
            for (String ex : examples)
                System.out.println("ex: " + ex);
            System.out.println();
        }
       /* synsets = wnd.getSynsets(word, SynsetType.ADJECTIVE_SATELLITE);
        for (Synset s : synsets)
        {
            System.out.print(i++ + ". ");
            String[] wordforms = s.getWordForms();
            for (String wf : wordforms)
                System.out.print(wf + " ");
            System.out.print(" = ");
            System.out.println(s.getDefinition());
            String[] examples = s.getUsageExamples();
            for (String ex : examples)
                System.out.println("ex: " + ex);
            System.out.println();
        }
        * */
   
    }

    public void getRelatedForNoun(String word)
    {
        int i = 1;
        WordNetDatabase wnd = WordNetDatabase.getFileInstance();
        Synset[] synsets = wnd.getSynsets(word, SynsetType.NOUN);
        for (Synset s : synsets)
        {
            System.out.print(i++ + ". ");
            String[] wordforms = s.getWordForms();
            for (String wf : wordforms)
                System.out.print(wf + ", ");
            System.out.print(" = ");
            System.out.println(s.getDefinition());
            String[] examples = s.getUsageExamples();
            for (String ex : examples)
                System.out.println("ex: " + ex);
            NounSynset ns = (NounSynset)s;
            NounSynset[] hypo = ns.getHyponyms();
            System.out.print("hyponyms:");
            for (Synset hs : hypo)
            {
                System.out.print("\n ");
                for (String wf : hs.getWordForms())
                    System.out.print(wf + ", ");
                System.out.print(" = " + hs.getDefinition());
            }
            System.out.println();

            hypo = ns.getInstanceHyponyms();
            System.out.print("hyponyms:");
            for (Synset hs : hypo)
            {
                System.out.print("\n ");
                for (String wf : hs.getWordForms())
                    System.out.print(wf + ", ");
                System.out.print(" = " + hs.getDefinition());
            }
            System.out.println();

            NounSynset[] mero = ns.getPartMeronyms();
            System.out.print("meronyms:");
            for (Synset ms : mero)
            {
                System.out.print("\n ");
                for (String wf : ms.getWordForms())
                    System.out.print(wf + ", ");
                System.out.print(" = " + ms.getDefinition());
            }
            System.out.println();

            mero = ns.getMemberMeronyms();
            System.out.print("meronyms:");
            for (Synset ms : mero)
            {
                System.out.print("\n ");
                for (String wf : ms.getWordForms())
                    System.out.print(wf + ", ");
                System.out.print(" = " + ms.getDefinition());
            }
            System.out.println();
            mero = ns.getSubstanceMeronyms();
            System.out.print("meronyms:");
            for (Synset ms : mero)
            {
                System.out.print("\n ");
                for (String wf : ms.getWordForms())
                    System.out.print(wf + ", ");
                System.out.print(" = " + ms.getDefinition());
            }


            System.out.println("\n");
        }
    }

    public void disambiguateWord(String word, String context, String[] alternativeForms)
    {
        Disambiguate d = new Disambiguate("date.txt", "raport.txt");
        d.setParameters(word, SynsetType.NOUN, context, 3);
        d.setAlternativeForms(alternativeForms);
        d.setUseOnlySamePos(true);
        d.disambiguate();
        System.out.println(d.getSenseDef());
    }

    public void printConfMatrix()
    {
         LineCorporaTests test = new LineCorporaTests();
       //System.out.println(test.confusionMatrixString());
    }

    public void printtexttowords(String text)
    {
        Disambiguate d = new Disambiguate("date.txt", "raport.txt");

        String[] words = d.textToWords(text);
        for (String s : words)
            System.out.println("." + s + ".");
    }

    public void fctwordlist()
    {
   
      BufferedReader input = null;
      String line;
      String result = "";
       try {
            input = new BufferedReader(new FileReader("input.txt"));
        } catch (IOException ex) {
            System.err.println("eroare la scriere in fisier - " + ex.getMessage());
        } catch (NullPointerException ex) {
            System.err.println("eroare la scriere in fisier - " + ex.getMessage());
        }
        try {
            while((line = input.readLine()) != null)
                result += "" + line + ", ";
            input.close();
        } catch (IOException ex) {
            Logger.getLogger(TryStuff.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println(result);
    }

    public void printfile()
    {
        ServeCorporaTests serve = new ServeCorporaTests();
        String[] text = serve.parseOriginalServe(serve.getAsString("serve6"));
        for (String s : text)
            System.out.println(s + "\n");
    }

    public void overlap(String s1, String s2)
    {

        Disambiguate d = new Disambiguate();
        System.out.println(d.overlap(s1, s2));
    }
    
    public void parseInterestFiles(String bigfilename) {

       try {
          BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(bigfilename)));
          
          String outpath = "/home/ana/Downloads/interest/";
          BufferedWriter[] out = new BufferedWriter[6];
          for (int i = 0; i < 6; i++)
              out[i] = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outpath + "interest" + (i+1))));
          
          String line;
          String type="";
          int count=0;
          
          while ((line = in.readLine()) != null) {
         
              String patternStr="<tag \"interest_([1-6])\">interest.*</>";
              Pattern p = Pattern.compile(patternStr);
              Matcher m = p.matcher(line);
              
              if (m.find()) {
                    
                            type = m.group(1);
                            //System.out.println(type);
                            //System.out.println();
                            if (Integer.parseInt(type)==6) {
                                System.out.println(line);
                                System.out.println("----------------------------------");
                                count++;
                            }
                            out[Integer.parseInt(type) -1].write(line);
                            out[Integer.parseInt(type) -1].newLine();
                       // }
              }
              else {
                  //System.out.println("not found: " + line);
                  //System.out.println();
              }
              

                  //System.out.println(type);

        }
          
          System.out.println("count: " + count);
          
          
          in.close();
          for (BufferedWriter o : out)
              o.close();

           
        } catch (UnsupportedEncodingException ex) {
            System.err.println(ex);
       
        } catch (FileNotFoundException ex) {
              System.err.println(ex);
        } catch (IOException ex) {
                System.err.println(ex);
        }
    }
}
