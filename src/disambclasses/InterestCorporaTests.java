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


public class InterestCorporaTests implements Runnable {
    
    
    private static int[] senses = {1, 2, 3, 4, 5, 7};
    private static String[] files = {"interest1", "interest2", "interest3", "interest4", "interest5", "interest6"};

    private static boolean onlyUseSamePos;
    private boolean normalize = true;
    private static int window;
    private static boolean withReportFile = false;
    private static Vector<Integer> interest1;
    private static Vector<Integer> interest2;
    private static Vector<Integer> interest3;
    private static Vector<Integer> interest4;
    private static Vector<Integer> interest5;
    private static Vector<Integer> interest6;
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
    public InterestCorporaTests()
    {
        initializeStuff();
    }

    private void initializeStuff()
    {
        window = 5;
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

        candidateGroups = new Vector[files.length];
        for (int i = 0; i < files.length; i++)
        {
            candidateGroups[i] = (Vector<Integer>)filesToSenseSets.get(files[i]);
        }

        correctResultsPerFile = new int[files.length];
        totalOccurences = new int[files.length];
        myResults = new int[files.length];
        recall = new float[files.length];
        precision = new float[files.length];
        for (int i = 0; i < files.length; i++)
        {
            correctResultsPerFile[i] = totalOccurences[i] = myResults[i] = 0;
            recall[i] = precision[i] = 0;
        }
       int size = files.length;
       confusionMatrix = new int[size][size];
       for (int i = 0; i < size; i++)
           for (int j = 0; j < size; j++)
               confusionMatrix[i][j] = 0;
    }

    public String getAsString(String fileName)
    {
        BufferedReader interestFile = null;
        String filesAsString = "";
        try {
          //  lineFile = new BufferedReader(new FileReader("testfiles\\" + fileName));
            /*
             * TODO: this won't work in Windows
             */
            interestFile = new BufferedReader(new FileReader("testfiles/" + fileName));

        String textLine;
        while((textLine = interestFile.readLine()) != null)
            filesAsString += textLine + "\n";

        } catch (IOException ex) {
            System.err.println("eroare la citire din fisier - " + ex.getMessage());
        }
        return filesAsString;

    }

    public String[] parseOriginalInterest(String text)
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

    public void setWithReportFile(boolean with)
    {
        withReportFile = with;
    }

    public void setNormalize (boolean normalize)
    {
        this.normalize = normalize;
    }
    public int[] testInterestCorpusFromFile(String file)
    {
        String[] contexts = parseOriginalInterest(getAsString(file));
        totalfor1 = 0; corectefor1 = 0;
        int senseNr = (Integer)filesToNrs.get(file);
        Vector senseSet = (Vector)filesToSenseSets.get(file);

        String raport; String diverse;
        if (onlyUseSamePos)
        {
            raport = "raport-" + file + "-samepos.txt";
            diverse = "date-" + file + "-samepos.txt";
        }
        else
        {
            raport = "raport-" + file + "-allpos.txt";
            diverse = "date-" + file + "-allpos.txt";
        }
        Disambiguate disambiguator;
        if (withReportFile)
           disambiguator = new Disambiguate(raport, diverse);
        else
            disambiguator = new Disambiguate();
        disambiguator.setUseOnlySamePos(onlyUseSamePos);
        disambiguator.setNormalize(normalize);
        String targetWord = "interest";
      //  String alternativeForms[] = {"lines", "lined"};   //??
      //  disambiguator.setAlternativeForms(alternativeForms);

        for (int i = 0; i < contexts.length; i++)
        {
            /*
            if (stopped)
            {
               // stopped = false;
                break;
            }
            if (paused)
            {
               // paused = false;
                contextNr = i;
                break;
            }        */                                    //fi liber-cuger el bloc


            disambiguator.setParameters(targetWord, SynsetType.NOUN, contexts[i], window, candidateGroups);
            disambiguator.disambiguate();

            
            if (disambiguator.getSenseDef() != null)
            {
                int resIndex = disambiguator.getSenseGroup();
                if (senseNr == resIndex)
                {
                    corectefor1++;
                    correctResultsPerFile[senseNr]++;
                }
                totalfor1++;
                totalOccurences[senseNr]++;

                myResults[resIndex]++;
                confusionMatrix[senseNr][resIndex]++;

            }
        }

        int[] result = {totalfor1, corectefor1};
        return result;
    }

      public void reset()
    {
        //reseet result vectors && confusion matrix
        for (int i = 0; i < files.length; i++)
            correctResultsPerFile[i] = totalOccurences[i] = myResults[i] = 0;
        for (int i = 0; i < files.length; i++)
           for (int j = 0; j < files.length; j++)
               confusionMatrix[i][j] = 0;
    }

    public int[] testInterestCorpusFromAll()
    {
        reset();
        int[] result = new int[2 * files.length];
        int[] interres;
        int i = 0;
        int percent = 0;
        for (int f = 0; f < files.length; f++)
        {
            System.out.print(percent + "% ");
            interres = testInterestCorpusFromFile(files[f]);
            result[i++] = interres[0];
            result[i++] = interres[1];
            percent += 100/files.length;
        }
        System.out.println();
        return result;

    }

    public void setWindow(int w)
   {
       window = w;
   }

   public void setOnlyUseSamePos(boolean onlySamePos)
   {
       onlyUseSamePos = onlySamePos;
   }

   /*
    * TODO: add accuracy to computed statistics
    * 
    */
   public String getAllResults()
   {
       String results = "windowSize: " + window + " only same pos: " + onlyUseSamePos + "\n";
       if (normalize) results += "cu "; else results += "fara ";
       results += "normalizare \n";
       results += "RESULTS:\n";
       int total = 0, correctperfile = 0; float totalrecall, meanrecall = 0, meanprecision = 0, fmeasure;
       for (int i = 0; i < files.length; i++)
       {
           total += totalOccurences[i];
           correctperfile += correctResultsPerFile[i];
           recall[i] = (float)correctResultsPerFile[i]/totalOccurences[i];
           precision[i] = (float)correctResultsPerFile[i]/myResults[i];
           meanrecall += recall[i];
           meanprecision += precision[i];
           results += "  " + files[i] + "\n" +
            "    " + "total " + totalOccurences[i] + "\n" +
            "    " + "corecte " + correctResultsPerFile[i] + "\n" +
            "    " + "recall " + recall[i] + "\n" +
            "    " + "precision " + precision[i] + "\n" +
            "   cate am zis ca sunt asa in total " + myResults[i] + "\n" + "\n";
       }
       totalrecall = (float)correctperfile/total;   //nu exista total precision, nu? sper ca am facut bine, am ametit
       meanrecall /= files.length;
       meanprecision /= files.length;
       fmeasure = (2 * meanrecall * meanprecision) / (meanrecall + meanprecision);
       results += "\nconfusion matrix: \n\n" + confusionMatrixString() + "\n";
       results += "  all\n" +
        "    total " + total + "\n" +
        "    corecte " + correctperfile + "\n" +
        "  total recall " + totalrecall + "\n" +
        "  mean recall " + meanrecall + "\n" +
        "  mean precision " + meanprecision + "\n" +
        "  F-measure " + fmeasure + "\n";

       return results;
   }

   private String confusionMatrixString()
   {
       String matrixs = String.format("%1$10s", "");
       for (int i = -1; i < confusionMatrix.length; i++)
       {
           if (i == -1)
               for (String s : files)
                   matrixs += String.format("%1$10s", s);
           else
               for (int j = -1; j < confusionMatrix[0].length; j++)
                   if (j == -1)
                       matrixs += String.format("%1$10s", files[i]);
                   else matrixs += String.format("%1$10s", confusionMatrix[i][j]);
           matrixs += "\n";
       }
       return matrixs;
   }

   public String getOneResult()
   {
       float recallfor1 = (float)corectefor1/totalfor1;
       String results = "window:  " + window +"\nonlySamePos: " + onlyUseSamePos
               + "\ntotal " + totalfor1 + "\ncorecte " + corectefor1 + "\nrecall " + recallfor1 +"\n";
       return results;
   }

   public void run()
   {
       testInterestCorpusFromAll();
   }
    
    
    
}
