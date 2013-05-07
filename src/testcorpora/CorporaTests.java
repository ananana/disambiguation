/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testcorpora;

import disambclasses.Disambiguate;
import edu.smu.tspell.wordnet.SynsetType;
import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;

/**
 *
 * @author ana
 */

public abstract class CorporaTests implements TestCorpus {
    
    protected CorpusType corpusType = null;
    
    // TODO: make these compulsory to implement (not leave null) somehow? - abstract fields?
    
    // TODO: make them static? make singleton subclasses - how?
    protected String targetWord;
    protected String alternativeForms[] = null;
    protected SynsetType synsetType;
    
    protected int[] senses;
    protected String[] files;
   
    protected boolean onlyUseSamePos;
    protected boolean normalize;
    protected int window;
    protected boolean withReportFile;
    protected HashMap filesToSenseSets;
    protected HashMap filesToNrs;

    protected ArrayList<Integer>[] candidateGroups;

    protected int totalfor1, corectefor1;
    protected int[] myResults;
    protected int[] correctResultsPerFile;
    protected int[] totalOccurences;
    protected float[] recall;
    protected float[] precision;
    protected int[][] confusionMatrix;
    
    protected int contextNr;
    protected int fileNr;

    public boolean stopped = false;
    public boolean paused = false;
    

    protected CorporaTests(CorpusType type)
    {
        corpusType = type;
    }
    
    /**
     * 
     * abstract method
     * will contain declaration and initialization to structures specific to each subclass
     * (and will be implemented in subclasses)
     * */
    abstract protected void specificStructures();
    
    // must be called after specificStructures() (initializez them)
    protected void initializeStuff()
    {
        window = 5;
        candidateGroups = new ArrayList[files.length];
        for (int i = 0; i < files.length; i++)
        {
            candidateGroups[i] = (ArrayList<Integer>)filesToSenseSets.get(files[i]);
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
        BufferedReader lineFile = null;
        String filesAsString = "";
        try {
            String dirpath = "";
            if (System.getProperty("os.name").contains("Windows"))
                dirpath = "testfiles\\";
            else
                dirpath = "testfiles/";

            lineFile = new BufferedReader(new FileReader(dirpath + fileName));

        String textLine;
        while((textLine = lineFile.readLine()) != null)
            filesAsString += textLine + "\n";

        } catch (IOException ex) {
            System.err.println("eroare la citire din fisier - " + ex.getMessage());
        }
        return filesAsString;

    }
    
    abstract public String[] parseOriginal(String text);

    public void setWithReportFile(boolean with)
    {
        withReportFile = with;
    }
    public void setNormalize (boolean normalize)
    {
        this.normalize = normalize;
    }

    public int[] testCorpusFromFile(String file)
    {
        String[] contexts = parseOriginal(getAsString(file));
        totalfor1 = 0; corectefor1 = 0;
        int senseNr = (Integer)filesToNrs.get(file);
        ArrayList senseSet = (ArrayList)filesToSenseSets.get(file);
     
        String raport; String diverse;
        
            raport = "raport-" + file + ".txt";
            diverse = "date-" + file + ".txt";
        
        Disambiguate disambiguator;
        if (withReportFile)
            disambiguator = new Disambiguate(raport, diverse);
        else
            disambiguator = new Disambiguate();
        disambiguator.setUseOnlySamePos(onlyUseSamePos);
        disambiguator.setNormalize(normalize);
       
        if (alternativeForms != null)
            disambiguator.setAlternativeForms(alternativeForms);

        for (int i = contextNr; i < contexts.length; i++)
        {
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
            }

            disambiguator.setParameters(targetWord, synsetType, contexts[i], window, candidateGroups);
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
        //reseet result ArrayLists && confusion matrix
        for (int i = 0; i < files.length; i++)
            correctResultsPerFile[i] = totalOccurences[i] = myResults[i] = 0;
        for (int i = 0; i < files.length; i++)
           for (int j = 0; j < files.length; j++)
               confusionMatrix[i][j] = 0;

        contextNr = 0;
        fileNr = 0;
    }

    public int[] testCorpusFromAll()
    {
        if (!paused) reset();
        int[] result = new int[2 * files.length];
        int[] interres;
        int i = 0;
        int percent = 0;
        for (int f = fileNr; f < files.length; f++)
        {
            if (stopped)
            {
                //stopped = false;
                return result;
            }
            if (paused)
            {
                //paused = false;
                fileNr = f;
                return result;
            }
            System.out.print(percent + "% ");
            interres = testCorpusFromFile(files[f]);
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
       testCorpusFromAll();
   }
    
}
