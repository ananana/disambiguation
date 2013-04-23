/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package disambclasses;

import edu.smu.tspell.wordnet.*;
import java.util.Arrays;
import java.util.Vector;
import java.io.*;
import java.util.ArrayList;

import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author HOME
 */
public class Disambiguate {

    /**
     * cuvantul tinta (de dezambiguizat)
     */
    private String targetWord;
    /**
     * forme diferite de cea de baza in care poate aparea cuvantul-tinta in context
     */
    private String[] alternativeForms;
    /**
     * tablou care contine cuvintele din fereastra de context
     */
    private String[] windowWords;
 
    /**
     * partea de vorbire a cuvantului tinta
     */
    private SynsetType pos;
    
    /**
     * sensul cuvantului tinta (asa cum a fost stabilit in urma dezambiguizarii) - synsetul care il reprezinta
     */
    private Synset sense;
    /**
     * vector care contine numarul synseturilor candidate pentru sensul cuvantului tinta \
     * (sensurilor din wordnet le este asociat un numar in sensesnrs) \
     * in cazul cel mai general - numerele corespunzatoare tuturor sensurilor din wordnet asociate cuvantului tinta
     */
    private Vector<Integer>[] candidateSynsetNrs;
    /**
     * numarul din candidateSynsetNrs al sensului (grupului de sensuri) gasit la dezambiguizare
     */
    private int senseNr;
    /**
     * asociaza fiecarui synset din care face parte cuvantul tinta un numar de ordine
     */
    private Hashtable sensesNrs;
    /**
     * o instanta a bazei de date wordnet,
     * care va fi folosita in functiile care acceseaza baza de date
     */
    private static final WordNetDatabase database = WordNetDatabase.getFileInstance();
    /**
     * fisierul de raport
     */
    private BufferedWriter reportFile = null;
    /**
     * fisierul de date diverse
     */
    private BufferedWriter variousFile = null;
    /**
     * variabila care retine daca in fereastra de context vor fi folosite doar
     * cuvinte cu aceeasi parte de vorbire ca a cuvantului tinta (in acest caz are valoarea true)
     */
    private boolean useOnlySamePos = false;
 
    /**
     * variabila care indica daca se va folosi normalizare in calcularea scorului
     */
    private boolean normalize = false;

    /**
     * constructorul trivial
     */
    public Disambiguate() {
    }

    /**
     * constructorul pentru cazul in care se doreste si generarea fisierelor de raport
     * se creeaza o instanta a clasei si se creeaza fisierele de raport cu numele primite ca argumente
     * @see createFiles(String file1, String file2)
     */
    public Disambiguate(String variousFileStr, String reportFileStr) {
        createFiles(reportFileStr, variousFileStr);

    }

    /**
     * creeaza fisierele de raport cu numele primite ca argumente
     */
    private void createFiles(String file1, String file2) {
        try {
            reportFile = new BufferedWriter(new FileWriter(file1));
        } catch (IOException ex) {
            System.err.println("error opening report file - " + ex.getMessage());
        } catch (NullPointerException ex) {
            System.err.println("error opening report file - " + ex.getMessage());
        }

        try {
            variousFile = new BufferedWriter(new FileWriter(file2));

        } catch (IOException ex) {

            System.err.println("error opening various file - " + ex.getMessage());
        } catch (NullPointerException ex) {
            System.err.println("error opening various file - " + ex.getMessage());
        }

    }

    /**
     * seteaza diverse campuri ale clasei - parametri care vor fi folositi in dezambiguizare:
     * @parameters:
     * @param targetWord cuvantul tinta
     * @param pos partea de vorbire a cuvantului tinta
     * @param surroundingText contextul in care apare cuvantul tinta - va fi folosit pentru a crea tabloul care contine cuvintele din fereastra
     * @param windowSize dimensiunea ferestrei
     */
    public void setParameters(String targetWord, SynsetType pos, String surroundingText, int windowSize) {

        this.targetWord = targetWord;

        this.pos = pos;
        //daca e adjectiv ar putea fi si satelit
        if ((database.getSynsets(targetWord, pos).length == 0) && (pos == SynsetType.ADJECTIVE)) {
            this.pos = SynsetType.ADJECTIVE_SATELLITE;
        }

        if (candidateSynsetNrs == null) //daca apelarea nu provine din celalalt setParameters
        {

            mapSynsetsToSenses();
            candidateSynsetNrs = new Vector[sensesNrs.size()]; 
            for (int i = 0; i < sensesNrs.size(); i++) {
                candidateSynsetNrs[i] = new Vector<Integer>();
                candidateSynsetNrs[i].add(i + 1);
            }

            buildWordList(surroundingText, targetWord, windowSize);
        }

        writeToFile("DISAMBIGUATING word: " + targetWord, true);
        writelnToFile(true);
        writeToFile("from context: " + surroundingText, true);
        writelnToFile(true);
    }

    /**
     * seteaza parametrii necesari pentru dezambiguizare, in plus, seteaza sensuri candidate pentru cuvantul cheie
     * @param targetWord cuvantul tinta
     * @param pos partea de vorbire a cuvantului tinta
     * @param surroundingText contextul in care apare cuvantul tinta - va fi folosit pentru a crea tabloul care contine cuvintele din fereastra
     * @param windowSize dimensiunea ferestrei
     * @param candidateSenses sensuri candidate pentru cuvantul cheie, de obicei o submultime a tuturor sensurilor posibile
     *      - tablou in care fiecare element este un vector care contine numerele de ordine pentru fiecare din sensurile din wordnet asociate uneia dintre clasele in care va fi incadrat cuvantul tinta (reprezentand sensuri posibile)
     * @see setParameters(String targetWord, SynsetType pos, String surroundingText, int windowSize)
     */
    public void setParameters(String targetWord, SynsetType pos, String surroundingText, int windowSize, Vector<Integer>[] candidateSenses) {
        setParameters(targetWord, pos, surroundingText, windowSize);
        candidateSynsetNrs = candidateSenses;
        mapSynsetsToSenses();
        buildWordList(surroundingText, targetWord, windowSize);


    }

    /**
     * construieste sensesNrs
     * @see sensesNrs
     */
    private void mapSynsetsToSenses() {
        sensesNrs = new Hashtable();
        Synset[] senses, senses1, senses2;
        if (pos == SynsetType.ADJECTIVE || pos == SynsetType.ADJECTIVE_SATELLITE) {
            senses1 = database.getSynsets(targetWord, SynsetType.ADJECTIVE, true);
            senses2 = database.getSynsets(targetWord, SynsetType.ADJECTIVE_SATELLITE, true);
            senses = new Synset[senses1.length + senses2.length];
            for (int i = 0; i < senses1.length; i++) {
                senses[i] = senses1[i];
            }
            for (int i = 0; i < senses2.length; i++) {
                senses[i + senses1.length] = senses2[i];
            }
        } else {
            senses = database.getSynsets(targetWord, pos, true);
        }

        for (int i = 0; i < senses.length; i++) {
            sensesNrs.put(senses[i], new Integer(i + 1));
            
            
            /**TEST**/
            
            
            //System.out.println((i+1) + senses[i].getDefinition());
            
        }
    }

    /**
     * seteaza campul useOnlySamePos
     * @param onlySamePos
     * @see useOnlySamePos
     */
    public void setUseOnlySamePos(boolean onlySamePos) {
        useOnlySamePos = onlySamePos;
    }

    /**
     * seteaza campul alternativeForms
     * @param alternativeForms
     * @see alternativeForms
     */
    public void setAlternativeForms(String[] alternativeForms) {
        this.alternativeForms = alternativeForms;
    }

    /**
     * seteaza campul normalize
     * @param normalize variabila booleana cu care va fi setat campul normalize
     * @see normalize
     */
    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    /**
     * realizeaza dezambiguizarea, seteaza campul sense
     * @see sense
     */
    public void disambiguate() {


        findSense();
        endFiles();
    }

    /**
     * scrie in fisierul de rapoarte pentru toReport = true, in cel de date diverse pentru toReport = false
     * @param text textul de scris in fisier
     * @param toReport variabila booleana care specifica daca se va scrie in fisierul de raport sau in cel de date diverse
     *                  (true - raport)
     */
    private void writeToFile(String text, boolean toReport) {
        if (reportFile != null && variousFile != null) {
            try {
                if (toReport) {
                    reportFile.write(text);
                    reportFile.flush();
                } else {

                    variousFile.write(text);
                    variousFile.flush();
                }

            } catch (IOException ex) {
                System.err.println("error writing to file - " + ex.getMessage());
            } catch (NullPointerException ex) {
                System.err.println("error writing to file - " + ex.getMessage());
            }
        }
    }

    /**
     * @see writeToFile(String text, boolean toReport)
     * @param text
     */
    private void writeToFile(String text) {
        writeToFile(text, false);
    }

    /**
     * scrie un sfarsit de rand in fisier
     * @param toReport variabila booleana care specifica daca se va scrie in fisierul de raport sau in cel de date diverse
     *                  (true - raport)
     */
    private void writelnToFile(boolean toReport) {
        if (reportFile != null && variousFile != null) {
            try {
                if (toReport) {
                    reportFile.newLine();
                    reportFile.flush();
                } else {
                    variousFile.newLine();
                    variousFile.flush();
                }

            } catch (IOException ex) {
                System.err.println("error writing to file - " + ex.getMessage());
            }
        }
    }

    /**
     * @see writelnToFile(boolean toReport)
     */
    private void writelnToFile() {
        writelnToFile(false);
    }

    /**
     * inchide fisierele de raport
     */
    @Override
    public void finalize() {
        try {
            reportFile.close();
            variousFile.close();
        } catch (IOException ex) {
            Logger.getLogger(Disambiguate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * construieste lista de cuvinte din fereastra
     * @param text
     * @param word
     * @param windowSize
     * @return lista de cuvinte din fereastra, fara cuvantul tinta
     */
    private int buildWordList(String text, String word, int windowSize) {
        String[] allWords = textToWords(text);
        windowSize = windowSize / 2 + 1;
        int wordPosition = -1;
        for (int i = 0; i < allWords.length; i++) {
            if (allWords[i].equalsIgnoreCase(word)) {
                wordPosition = i;
            } 
        }

        //cauta prin alternativeForms
        if (wordPosition == -1 && alternativeForms != null) {
            for (int i = 0; i < allWords.length; i++) {
                for (String alternativeForm : alternativeForms) {
                    if (allWords[i].equalsIgnoreCase(alternativeForm)) {
                        wordPosition = i;
                    }
                }
            }
        }

        //cauta prin formele din wordnet ale cuv din fereastra
        if (wordPosition == -1) {
            boolean found = false;
            for (int i = 0; i < allWords.length && !found; i++) {
                String[] baseForms = database.getBaseFormCandidates(allWords[i], pos);
                if (pos == SynsetType.ADJECTIVE || pos == SynsetType.ADJECTIVE_SATELLITE) {
                    String[] baseforms1 = database.getBaseFormCandidates(allWords[i], SynsetType.ADJECTIVE);
                    String[] baseforms2 = database.getBaseFormCandidates(allWords[i], SynsetType.ADJECTIVE_SATELLITE);
                    baseForms = new String[baseforms1.length + baseforms2.length];
                    for (int j = 0; j < baseforms1.length; j++) {
                        baseForms[j] = baseforms1[j];
                    }
                    for (int j = 0; j < baseforms2.length; j++) {
                        baseForms[j + baseforms1.length] = baseforms2[j];
                    }
                }
                for (String baseForm : baseForms) {
                    if (word.equalsIgnoreCase(baseForm)) {
                        wordPosition = i;
                        found = true;
                    }
                }

            }
        }

        //cauta si prin stemuri
        if (wordPosition == -1) {
            for (int i = 0; i < allWords.length; i++) {
                if (stem(allWords[i].toLowerCase()).equals(stem(targetWord.toLowerCase()))) {
                    wordPosition = i;
                }
            }
        }

        //cauta cuvinte compuse
        int wordStart = wordPosition;
        int wordEnd = wordPosition + 1;
        boolean compoundFound = false;
        String compoundWord = "";
        if (wordPosition != -1) {
            int compoundStart = wordStart - 4;  //4 cuv in stanga si 4 in dreapta
            int compoundEnd = wordEnd + 5;
            if (compoundStart < 0) {
                compoundStart = 0;
            }
            if (compoundEnd > allWords.length) {
                compoundEnd = allWords.length;
            }
            for (int i = wordPosition; i >= compoundStart && !compoundFound; i--) {
                for (int j = wordPosition; j < compoundEnd && !compoundFound; j++) {
                    compoundWord = "";
                    for (int k = i; k <= j; k++) {
                        compoundWord += " " + allWords[k];
                    }
                    compoundWord = compoundWord.trim();
                    if (((pos == SynsetType.ADJECTIVE || pos == SynsetType.ADJECTIVE_SATELLITE) && (database.getSynsets(compoundWord, SynsetType.ADJECTIVE).length > 0 || database.getSynsets(compoundWord, SynsetType.ADJECTIVE_SATELLITE).length > 0)) ||
                            (pos != SynsetType.ADJECTIVE && pos != SynsetType.ADJECTIVE_SATELLITE && database.getSynsets(compoundWord, pos).length > 0)) {
                        if (j - i >= 1 && isInCandidates(compoundWord, pos)) {
                            wordStart = i;
                            wordEnd = j + 1;

                            targetWord = compoundWord;
                            compoundFound = true;
                        }
                    }
                }
            }
        }

        int windowStart = wordStart - windowSize;
        int windowEnd = wordEnd + windowSize - 1;   

        if (wordPosition == -1 || windowSize < 1) {
            windowWords = null;
            // System.err.println("Cuvantul tinta nu se gaseste in text!");
            return -1;
        }

        boolean allOk = false;

        do {
            allOk = true;
            for (int i = wordEnd; i <= windowEnd && i < allWords.length; i++) {
                if ((!isValidWord(allWords[i], pos, true) && useOnlySamePos) || ((!isValidWord(allWords[i], null, true) && !useOnlySamePos))) //daca useOnlySamePos, atunci verifica daca sunt nule alea cu pos, daca nu, verifica daca sunt nule toate
                {
                    windowEnd++;
                    allOk = false;
                    allWords[i] = "";
                }
            }
            for (int i = wordStart - 1; i >= windowStart && i >= 0; i--) {
                if ((!isValidWord(allWords[i], pos, true) && useOnlySamePos) || ((!isValidWord(allWords[i], null, true) && !useOnlySamePos))) {
                    windowStart--;
                    allOk = false;
                    allWords[i] = "";
                }
            }

            if (windowStart < 0) {
                windowEnd -= windowStart;                                      
                windowStart = 0;
                if (windowEnd >= allWords.length) {
                    windowEnd = allWords.length - 1;
                }
            } else if (windowEnd >= allWords.length) {
                windowStart -= (allWords.length - windowEnd + 1);
                windowEnd = allWords.length - 1;
                if (windowStart < 0) {
                    windowStart = 0;
                }
            }
        } while (!((windowEnd == allWords.length - 1 && windowStart == 0) || allOk));

        if (wordPosition != -1) {
            windowWords = new String[windowEnd - windowStart + 1 - (wordEnd - wordStart)];              
        } else {
            windowWords = new String[windowEnd - windowStart + 1];          //daca nu am cuvantul tinta in lista las toate cuvintele in ea
        }
        int k = 0;

        for (int i = windowStart; i <= windowEnd; i++) {
            if ((i < wordStart || i >= wordEnd) || wordPosition == -1) {   
                windowWords[k++] = allWords[i];
            }
        }

        return 0;
    }

    /**
     * verifica daca un cuvant face parte din synseturile candidate pentru sensul cuvantului tinta
     * (folosit de obicei pentru a verifica validitatea cuvintelor compuse gasite in text)
     * @param word cuvantul
     * @param pos partea de vorbire cu care este cautat
     * @return true daca este un cuvant valid, false in caz contrar
     */
    private boolean isInCandidates(String word, SynsetType pos) {

        //aici  tratez separat daca e adjectiv
        if (pos == SynsetType.ADJECTIVE || pos == SynsetType.ADJECTIVE_SATELLITE) {
            for (Synset s : database.getSynsets(word, SynsetType.ADJECTIVE, true)) {
                for (int i = 0; i < candidateSynsetNrs.length; i++) {
                    for (int candidate : candidateSynsetNrs[i]) {
                        if (sensesNrs.get(s) != null && sensesNrs.get(s) != null && sensesNrs.get(s).equals((Object) candidate)) {
                            return true;
                        }
                    }
                }

            }

            for (Synset s : database.getSynsets(word, SynsetType.ADJECTIVE_SATELLITE, true)) {
                for (int i = 0; i < candidateSynsetNrs.length; i++) {
                    for (int candidate : candidateSynsetNrs[i]) {
                        if (sensesNrs.get(s) != null && sensesNrs.get(s).equals((Object) candidate)) {
                            return true;
                        }
                    }
                }

            }
        } else {
            for (Synset s : database.getSynsets(word, pos, true)) {

                for (int i = 0; i < candidateSynsetNrs.length; i++) {
                    for (int candidate : candidateSynsetNrs[i]) {
                        if (sensesNrs.get(s) != null && sensesNrs.get(s).equals((Object) candidate)) {  
                            return true;                                                    

                        }
                    }

                }
            }
        } 
       
        //daca nu il gasesc il caut in hipernimele imediate
       
        if (pos == SynsetType.NOUN) { 
            for (Synset s : database.getSynsets(word, pos)) {
                for (NounSynset hypernym : ((NounSynset) s).getHypernyms()) {
                    for (int i = 0; i < candidateSynsetNrs.length; i++) {
                        for (int candidate : candidateSynsetNrs[i]) { 
                            if (sensesNrs.get(hypernym) != null && sensesNrs.get(hypernym).equals((Object) candidate)) {
                               return true;
                            }
                        }
                    }
                }
               
            }
        }
        
        return false;
    }

    /**
     * construieste vectorul de sensuri candidate pentru cuvantul tinta
     * @param word cuvantul
     * @param pos partea de vorbire a acestuia
     * @return un tablou de vectori de synseturi in care fiecare element este un vector de synseturi care reprezinta
     *         sensuri din wordnet asociate unui sens posibil al cuvantului tinta, in conformitate cu
     *         sensurile candidate ale acestuia
     */
    private Vector[] candidates(String word, SynsetType pos) //calculeaza intersectia intre sensurile candidate si sensurile cuvantului la care sunt
    {
        Vector<Synset>[] candidateSynsets = new Vector[candidateSynsetNrs.length];
        for (int i = 0; i < candidateSynsets.length; i++) {
            candidateSynsets[i] = new Vector<Synset>();
        }

        //aici  tratez separat daca e adjectiv
        if (pos == SynsetType.ADJECTIVE || pos == SynsetType.ADJECTIVE_SATELLITE) {
            for (Synset s : database.getSynsets(word, SynsetType.ADJECTIVE, true)) {

                for (int i = 0; i < candidateSynsetNrs.length; i++) {
                    for (int candidate : candidateSynsetNrs[i]) {
                        if (sensesNrs.get(s) != null && sensesNrs.get(s).equals((Object) candidate)) {
                            candidateSynsets[i].add(s);
                            ;
                        }
                    }


                }
            }

            for (Synset s : database.getSynsets(word, SynsetType.ADJECTIVE_SATELLITE, true)) {

                for (int i = 0; i < candidateSynsetNrs.length; i++) {
                    for (int candidate : candidateSynsetNrs[i]) {
                        if (sensesNrs.get(s) != null && sensesNrs.get(s).equals((Object) candidate)) {
                            candidateSynsets[i].add(s);
                        }
                    }


                }
            }
        } else {
            for (Synset s : database.getSynsets(word, pos, true)) {

                for (int i = 0; i < candidateSynsetNrs.length; i++) {
                    for (int candidate : candidateSynsetNrs[i]) {
                        if (sensesNrs.get(s) != null && sensesNrs.get(s).equals((Object) candidate)) {  

                            candidateSynsets[i].add(s);
                          

                        }

                    }

                }

            }
        }
        
        //daca nu il gasesc il caut si printre hipernime
        boolean empty = true;
        for (Vector<Synset> candidate : candidateSynsets) {
            if (candidate.size() != 0) {
                empty = false;
            }
        }
        if (empty) {
            if (pos == SynsetType.NOUN) {
                for (Synset s : database.getSynsets(word, pos)) {
                    for (NounSynset hypernym : ((NounSynset) s).getHypernyms()) {
                        for (int i = 0; i < candidateSynsetNrs.length; i++) {
                            for (int candidate : candidateSynsetNrs[i]) {
                                if (sensesNrs.get(hypernym) != null && sensesNrs.get(hypernym).equals((Object) candidate)) {
                                    candidateSynsets[i].add(hypernym); 
                                }
                            }
                        }
                    }
                }
            }
        }

        return candidateSynsets;
    }

    /**
     * calculeaza scorul de suprapunere intre doua siruri
     * @param text1 primul sir
     * @param text2 al doilea sir
     * @return scorul total
     */
    public int overlap(String text1, String text2) {

        String words1[] = textToWords(text1);
        String words2[] = textToWords(text2);
        int score = 0;
        int[] nrofwords = {words1.length, words2.length};

        writeToFile("between: " + text1);
        writelnToFile();
        writeToFile("and: " + text2);
        writelnToFile();
        writeToFile("commonstring: ");
        int[][] indexes;
        int commonLength;
        int uniqueIndex = 0;
        do {
            indexes = longestCommonSubstring(words1, words2);
            commonLength = indexes[0][1] - indexes[0][0];

            int startPos = indexes[0][0], endPos = indexes[0][1];
            boolean allFunctionWords = true;

            for (int i = startPos; i < endPos && allFunctionWords; i++) {
                if (isFunctionWord(words1[i])) {
                    startPos++;
                } else {
                    allFunctionWords = false;
                }
            }
            allFunctionWords = true;
            for (int i = endPos - 1; i >= startPos && allFunctionWords; i--) {
                if (isFunctionWord(words1[i])) {
                    endPos--;
                } else {
                    allFunctionWords = false;
                }
            }

            int actualLength = endPos - startPos;

            if (actualLength > 0) {
                score += actualLength * actualLength;
                for (int i = startPos; i < endPos; i++) {
                    writeToFile(words1[i]);
                    if (i != indexes[0][1] - 1) {
                        writeToFile(" ");
                    }
                }

                writeToFile(", ");
            }


            for (int i = 0; i < commonLength; i++) {  
                words1[indexes[0][0] + i] = "rep" + uniqueIndex++;
                words2[indexes[1][0] + i] = "rep" + uniqueIndex++;
            }

        } while (commonLength > 0);

        writelnToFile();
        writelnToFile();

        int[] result = {score, nrofwords[0], nrofwords[1]};
        return score;
    }

    /**
     * calculeaza scorul pentru cuvinte care nu au aceeasi parte de vorbire; compar doar definitiile
     * @param wordSense1 synsetul 1
     * @param wordForm1 cuvantul 1
     * @param wordSense2 synsetul 2
     * @param wordForm2 cuvantul 2
     * @return scorul asociat perechii de synseturi
     * @see score(Synset wordSense1, String wordForm1, Synset wordSense2, String wordForm2, SynsetType pos)
     */
    private float simpleScore(Synset wordSense1, String wordForm1, Synset wordSense2, String wordForm2) {
        writeToFile("SCORE between " + wordForm1 + " and " + wordForm2 + ": ");
        writelnToFile();
        float score = 0;
        //pt definitie:
        score += overlap(glossAndExamples(wordSense1, true),
                glossAndExamples(wordSense2, true));
        return score;
    }

    /**
     * calculeaza scorul asociat unei perechi de synseturi, folosind toate perechile de relatii posibile formate din multimea de relatii: <br />
     * pentru substantive: hiponimie, meronimie <br />
     * pentru adjective: similar-to, antonimie, attribute, related <br />
     * pentru verbe: troponimie, causal, entailment <br />
     * general: glosa, exemple de utilizare
     *
     * @param wordSense1 synsetul 1
     * @param wordForm1 cuvantul 1
     * @param wordSense2 synsetul 2
     * @param wordForm2 cuvantul 2
     * @return scorul total al perechii de synseturi
     */
    private float scoreAll(Synset wordSense1, String wordForm1, Synset wordSense2, String wordForm2, int[] targetLength) {
        writeToFile("SCORE between " + wordForm1 + " and " + wordForm2 + ": ");
        writelnToFile();
        float score = 0;

        targetLength[0] = 0;
        int nrOfDefs = 0;
        int wwordLength = 0;
        boolean useEx = false;

        String gloss1 = glossAndExamples(wordSense1, true);
        String gloss2 = glossAndExamples(wordSense2, true);

        String[] hypogloss1 = null, merogloss1 = null, hypogloss2 = null, merogloss2 = null,
                simgloss1 = null, antgloss1 = null, attrgloss1 = null, relgloss1 = null, simgloss2 = null, antgloss2 = null, attrgloss2 = null, relgloss2 = null,
                tropgloss1 = null, entgloss1 = null, causgloss1 = null, tropgloss2 = null, entgloss2 = null, causgloss2 = null;


        if (wordSense1.getType() == SynsetType.NOUN) {

            NounSynset[] hyponyms1 = ((NounSynset) wordSense1).getHyponyms();
            NounSynset[] ihyponyms1 = ((NounSynset) wordSense1).getInstanceHyponyms();
            hypogloss1 = new String[hyponyms1.length + ihyponyms1.length];
            for (int i = 0; i < hyponyms1.length; i++) {
                hypogloss1[i] = hyponyms1[i].getDefinition();
            }
            
            for (int i = 0; i < ihyponyms1.length; i++) {
                hypogloss1[i + hyponyms1.length] = ihyponyms1[i].getDefinition();
            }
           


            NounSynset[] pmeronyms1 = ((NounSynset) wordSense1).getPartMeronyms();
            NounSynset[] mmeronyms1 = ((NounSynset) wordSense1).getMemberMeronyms();
            NounSynset[] smeronyms1 = ((NounSynset) wordSense1).getSubstanceMeronyms();
            merogloss1 = new String[pmeronyms1.length + mmeronyms1.length + smeronyms1.length];
            for (int i = 0; i < pmeronyms1.length; i++) {
                merogloss1[i] = pmeronyms1[i].getDefinition();
            }
          
            for (int i = 0; i < mmeronyms1.length; i++) {
                merogloss1[i + pmeronyms1.length] = mmeronyms1[i].getDefinition();
            }
            
            for (int i = 0; i < smeronyms1.length; i++) {
                merogloss1[i + pmeronyms1.length + mmeronyms1.length] = smeronyms1[i].getDefinition();
            }
            

        }

        if (wordSense2.getType() == SynsetType.NOUN) {

            NounSynset[] hyponyms2 = ((NounSynset) wordSense2).getHyponyms();
            NounSynset[] ihyponyms2 = ((NounSynset) wordSense2).getInstanceHyponyms();
            hypogloss2 = new String[hyponyms2.length + ihyponyms2.length];
            for (int i = 0; i < hyponyms2.length; i++) {
                hypogloss2[i] = hyponyms2[i].getDefinition();
            }
           
            for (int i = 0; i < ihyponyms2.length; i++) {
                hypogloss2[i + hyponyms2.length] = ihyponyms2[i].getDefinition();
            }
           

            NounSynset[] pmeronyms2 = ((NounSynset) wordSense2).getPartMeronyms();
            NounSynset[] mmeronyms2 = ((NounSynset) wordSense2).getMemberMeronyms();
            NounSynset[] smeronyms2 = ((NounSynset) wordSense2).getSubstanceMeronyms();
            merogloss2 = new String[pmeronyms2.length + mmeronyms2.length + smeronyms2.length];
            for (int i = 0; i < pmeronyms2.length; i++) {
                merogloss2[i] = pmeronyms2[i].getDefinition();
            }
           
            for (int i = 0; i < mmeronyms2.length; i++) {
                merogloss2[i + pmeronyms2.length] = mmeronyms2[i].getDefinition();
            }
            
            for (int i = 0; i < smeronyms2.length; i++) {
                merogloss2[i + pmeronyms2.length + mmeronyms2.length] = smeronyms2[i].getDefinition();
            }
           
        }

        if (wordSense1.getType() == SynsetType.ADJECTIVE || wordSense1.getType() == SynsetType.ADJECTIVE_SATELLITE) {

            AdjectiveSynset[] similar1 = ((AdjectiveSynset) wordSense1).getSimilar();
            simgloss1 = new String[similar1.length];
            for (int i = 0; i < similar1.length; i++) {
                simgloss1[i] = similar1[i].getDefinition();
            }
           
            WordSense[] antonyms1 = ((AdjectiveSynset) wordSense1).getAntonyms(wordForm1);
            antgloss1 = new String[antonyms1.length];
            for (int i = 0; i < antonyms1.length; i++) {
                antgloss1[i] = antonyms1[i].getSynset().getDefinition();
            }
            
            NounSynset[] attributes1 = ((AdjectiveSynset) wordSense1).getAttributes();    
            attrgloss1 = new String[attributes1.length];
            for (int i = 0; i < attributes1.length; i++) {
                attrgloss1[i] = attributes1[i].getDefinition();
            }
           
            AdjectiveSynset[] related1 = ((AdjectiveSynset) wordSense1).getRelated();
            relgloss1 = new String[related1.length];
            for (int i = 0; i < related1.length; i++) {
                relgloss1[i] = related1[i].getDefinition();
            }
            

        }

        if (wordSense2.getType() == SynsetType.ADJECTIVE || wordSense2.getType() == SynsetType.ADJECTIVE_SATELLITE) {
            AdjectiveSynset[] similar2 = ((AdjectiveSynset) wordSense2).getSimilar();
            simgloss2 = new String[similar2.length];
            for (int i = 0; i < similar2.length; i++) {
                simgloss2[i] = similar2[i].getDefinition();
            }
           
            WordSense[] antonyms2 = ((AdjectiveSynset) wordSense2).getAntonyms(wordForm2);
            antgloss2 = new String[antonyms2.length];
            for (int i = 0; i < antonyms2.length; i++) {
                antgloss2[i] = antonyms2[i].getSynset().getDefinition();
            }
           
            NounSynset[] attributes2 = ((AdjectiveSynset) wordSense2).getAttributes();   
            attrgloss2 = new String[attributes2.length];
            for (int i = 0; i < attributes2.length; i++) {
                attrgloss2[i] = attributes2[i].getDefinition();
            }
           
            AdjectiveSynset[] related2 = ((AdjectiveSynset) wordSense2).getRelated();
            relgloss2 = new String[related2.length];
            for (int i = 0; i < related2.length; i++) {
                relgloss2[i] = related2[i].getDefinition();
            }
           
        }

        if (wordSense1.getType() == SynsetType.VERB) {

            VerbSynset[] troponyms1 = ((VerbSynset) wordSense1).getTroponyms();
            tropgloss1 = new String[troponyms1.length];
            for (int i = 0; i < troponyms1.length; i++) {
                tropgloss1[i] = troponyms1[i].getDefinition();
            }
            
            VerbSynset[] entailment1 = ((VerbSynset) wordSense1).getEntailments();
            entgloss1 = new String[entailment1.length];
            for (int i = 0; i < entailment1.length; i++) {
                entgloss1[i] = entailment1[i].getDefinition();
            }
            
            VerbSynset[] causal1 = ((VerbSynset) wordSense1).getOutcomes();
            causgloss1 = new String[causal1.length];
            for (int i = 0; i < causal1.length; i++) {
                causgloss1[i] = causal1[i].getDefinition();
            }
            
        }

        if (wordSense2.getType() == SynsetType.VERB) {

            VerbSynset[] troponyms2 = ((VerbSynset) wordSense2).getTroponyms();
            tropgloss2 = new String[troponyms2.length];
            for (int i = 0; i < troponyms2.length; i++) {
                tropgloss2[i] = troponyms2[i].getDefinition();
            }
           
            VerbSynset[] entailment2 = ((VerbSynset) wordSense2).getEntailments();
            entgloss2 = new String[entailment2.length];
            for (int i = 0; i < entailment2.length; i++) {
                entgloss2[i] = entailment2[i].getDefinition();
            }
           
            VerbSynset[] causal2 = ((VerbSynset) wordSense2).getOutcomes();
            causgloss2 = new String[causal2.length];
            for (int i = 0; i < causal2.length; i++) {
                causgloss2[i] = causal2[i].getDefinition();
            }
           
        }

        String[][] relations1 = new String[10][];
        String[][] relations2 = new String[10][];
        String[][] glosses = {{gloss1}, {gloss2}};
        relations1[0] = glosses[0];
        relations2[0] = glosses[1];
        relations1[1] = hypogloss1;
        relations2[1] = hypogloss2;
        relations1[2] = merogloss1;
        relations2[2] = merogloss2;
        relations1[3] = antgloss1;
        relations2[3] = antgloss2;
        relations1[4] = attrgloss1;
        relations2[4] = attrgloss2;
        relations1[5] = simgloss1;
        relations2[5] = simgloss2;
        relations1[6] = relgloss1;
        relations2[6] = relgloss2;
        relations1[7] = tropgloss1;
        relations2[7] = tropgloss2;
        relations1[8] = entgloss1;
        relations2[8] = entgloss2;
        relations1[9] = causgloss1;
        relations2[9] = causgloss2;

        int iteration = 0;
        for (int i = 0; i < 10; i++) {
            iteration = 0;
            if (relations1[i] != null) {
                for (int j = 0; j < 10; j++) {
                    if (relations2[j] != null) {
                        for (String r1 : relations1[i]) {
                            iteration++;
                            for (String r2 : relations2[j]) {
                                score += overlap(r1, r2);

                            }
                            //doar prima data cand trece prin ele
                            if (iteration == 1) {
                                targetLength[0] += textToWords(r1).length;

                            }
                        }
                    }

                }
            }
        }


        return score;


    }

    /**
     * gaseste sensul cuvantului tinta, alegandu-l pe cel cu scorul maxim <br />
     * (calculeaza suma scorurilor tuturor perechilor intre un sens al cuvantului tinta si sensurile cuvintelor din fereastra, <br />
     * pentru fiecare sens al cuvantului tinta si fiecare cuvant din fereastra)
     * @return sensul cuvantului (synsetul, in cazul in care sensurile posibile sun sensuri din wordnet; unul din synseturile asociate sensului, in cazul in care sensurile posibile sunt grupuri de synseturi)
     */
    @SuppressWarnings("empty-statement")
    private Synset findSense() { 

        if (windowWords == null) //verificare daca a gasit cuvantul in lista
        {
            return null;
        }
        
        Vector<Synset>[] candidateSynsets = candidates(targetWord, pos); 


        float[] senseScores = new float[candidateSynsets.length];

        Vector<Integer> bestScoreNrs = new Vector<Integer>();
        Synset bestScoreS = null;

        boolean anyCandidates = true;
        if (candidateSynsets == null || candidateSynsets.length == 0) anyCandidates = false;
        if (anyCandidates) {
            anyCandidates = false;
            for (int i = 0; i < candidateSynsets.length; i++)
                if (candidateSynsets[i].size() != 0) anyCandidates = true;
        }

        if (!anyCandidates) { 
           
            writeToFile("the word is not found in the WordNet database!", true);
            writelnToFile(true);
        } else {
           
            //bestScoreNrs.add(0);
            float bestScore = 0;
            int[] targetLength = {0};
            int normalizeBy = 0;

            writeToFile("SENSES SCORES:", true);

            for (int i = 0; i < candidateSynsets.length; i++) {
                senseScores[i] = 0;

                normalizeBy = 0;
                for (int j = 0; j < candidateSynsets[i].size(); j++) {
                    targetLength[0] = 0;

                    for (String windowWord : windowWords) {
                      
                        for (Synset pairSense : database.getSynsets(windowWord)) {
                            senseScores[i] += scoreAll(candidateSynsets[i].elementAt(j), targetWord, pairSense, windowWord, targetLength);
                        }




                    }
                    
                    normalizeBy += targetLength[0]; 
                }
                if (normalizeBy != 0 && normalize) {
                    senseScores[i] /= normalizeBy;
                }
                if (senseScores[i] >= bestScore && candidateSynsets[i].size() > 0) {
                    if (senseScores[i] > bestScore) {
                        bestScoreNrs.clear();
                    }
                    bestScoreNrs.add(i); 
                    bestScore = senseScores[i]; 
                }

                //scrie in fisier
                if (candidateSynsets[i].size() != 0) {
                    writelnToFile(true);
                    writeToFile(senseScores[i] + " ", true);
                }
                for (int k = 0; k < candidateSynsets[i].size(); k++) {
                    if (k != 0) {
                        writeToFile(" / ", true);
                    }
                    writeToFile(candidateSynsets[i].elementAt(k).getDefinition(), true);

                }
            }



            if (bestScoreNrs.size() == 1) {   
                senseNr = bestScoreNrs.elementAt(0);
                bestScoreS = candidateSynsets[senseNr].elementAt(0); 
            } else {
                int mostUsedIndexi = bestScoreNrs.elementAt(0);
                int mostUsedIndexj = 0; 
                //initializam mostUsedIndexj cu primul sens candidat care nu e vid


                for (int i = 0; i < bestScoreNrs.size(); i++) {
                    for (int j = 0; j < candidateSynsets[bestScoreNrs.elementAt(i)].size(); j++) {
                        try {
                           
                            if ((candidateSynsets[bestScoreNrs.elementAt(i)].elementAt(j).getTagCount(targetWord)) >
                                    candidateSynsets[mostUsedIndexi].elementAt(mostUsedIndexj).getTagCount(targetWord)) {
                                mostUsedIndexi = bestScoreNrs.elementAt(i);
                                mostUsedIndexj = j;
                            }
                        } catch (WordNetException ex) {
                            System.err.println("Error: " + ex.getMessage());
                        }
                    }

                }
                bestScoreS = candidateSynsets[mostUsedIndexi].elementAt(mostUsedIndexj);  
                senseNr = mostUsedIndexi;
            }

            sense = bestScoreS; 

            writelnToFile(true);
            writeToFile("BEST SCORE: ", true);
            writelnToFile(true);
            writeToFile(bestScoreS.getDefinition(), true);
            writelnToFile(true);

        }
        
        
        // TEST
        //System.out.println(bestScoreS.getDefinition());
        
        
        
        return bestScoreS;
    }

    /**
     * calculeaza cel mai lung substring comun intre text1 si text2
     * @param text1
     * @param text2
     * @return pozitiile de la care incepe, respectiv se termina, sirul comun rezultat, in fiecare din sirurile de intrare: <br />
     *
     * [0][0] - pozitia pe care incepe in text1, [0][1] - pozitia pe care se termina in text1 <br />
     * [1][0] - pozitia pe care incepe in text2, [1][1] - pozitia pe care se termina in text2
     */
    private int[][] longestCommonSubstring(String[] text1, String[] text2) {
        int n = text1.length + 1;
        int m = text2.length + 1;
        int[][] lengthMatr = new int[n][m];

        int maxLength = 0;
        int startIndex1 = 0, endIndex1 = 0, startIndex2 = 0, endIndex2 = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (i == 0 || j == 0) {
                    lengthMatr[i][j] = 0;
                } else {
                    if (stem(text1[i - 1]).equalsIgnoreCase(stem(text2[j - 1]))) {
                        lengthMatr[i][j] = lengthMatr[i - 1][j - 1] + 1;
                        if (lengthMatr[i][j] > maxLength) {
                            maxLength = lengthMatr[i][j];
                            startIndex1 = i - maxLength + 1 - 1;
                            endIndex1 = i + 1 - 1;
                            startIndex2 = j - maxLength + 1 - 1;
                            endIndex2 = j + 1 - 1;
                        }
                    } else {
                        lengthMatr[i][j] = 0;
                    }
                }
            }
        }
        int[][] indexes = new int[2][2];
        indexes[0][0] = startIndex1;
        indexes[0][1] = endIndex1;
        indexes[1][0] = startIndex2;
        indexes[1][1] = endIndex2;

        return indexes;
    }

    /**
     * scrie in fisiere un text care marcheaza incheierea raportului
     */
    private void endFiles() {
        writelnToFile(true);
        writelnToFile(true);
        writeToFile("-----end of report file-----\n", true);
        writelnToFile();
        writelnToFile();
        writeToFile("-----end of various file-----\n");
    }

    /**
     *
     * @return definitia sensului care a fost ales ca sens probabil al cuvantului tinta
     * @see sense
     */
    public String getSenseDef() {
        if (sense != null) {
            return sense.getDefinition();
        } else {
            return null;
        }
    }

    /**
     * @return toate cuvintele care fac parte din synsetul gasit ca sens al cuvantului tinta
     */
    public String getSenseWordForms() {
        String[] sv;
        String s = "";
        if (sense != null) {
            sv = sense.getWordForms();
        } else {
            return null;
        }
        for (int i = 0; i < sv.length; i++) {
            s += sv[i];
            if (i != sv.length - 1) {
                s += ", ";
            }
        }
        return s;
    }

    /**
     *
     * @param sense synsetul pentru care se gasesc cuvintele
     * @return toate cuvintele care fac parte dintr-un synset dat ca parametru
     */
    public String getSenseWordForms(Synset sense) {
        String[] sv;
        String s = "";
        if (sense != null) {
            sv = sense.getWordForms();
        } else {
            return null;
        }
        for (int i = 0; i < sv.length; i++) {
            s += sv[i];
            if (i != sv.length - 1) {
                s += ", ";
            }
        }
        return s;
    }

    public Object getSynsetNr() {
        return sensesNrs.get(sense);
    }

    /**
     *
     * @return numarul grupului de sensuri alese de dezambiguizare (campul senseNr)
     */
    public int getSenseGroup() {
        return senseNr;
    }

    /**
     * calculeaza radacina (stem) cuvantului dat ca parametru
     * @param toStem
     * @return cuvantul toStem trecut prin stemmer
     * @see Stemmer
     */
    private String stem(String toStem) {
        Stemmer st = new Stemmer();
        st.add(toStem.toCharArray(), toStem.length());
        st.stem();
        return st.toString();
    }

    /**
     * verifica daca un cuvant este function word
     * folosind o lista de astfel de cuvinte, cu care il compara
     * @param word
     * @return
     */
    private boolean isFunctionWord(String word) {

        String[] stopWords = {"i", "a", "an", "as", "at", "by", "he", "his", "me", "or", "thou", "us", "who",
            "against", "amid", "amidst", "among", "amongst", "and", "anybody", "anyone", "because", "beside",
            "circa", "despite", "during", "everybody", "everyone", "for", "from", "her", "hers", "herself",
            "him", "himself", "hisself", "idem", "if", "into", "it", "its", "itself", "myself", "nor", "of",
            "oneself", "onto", "our", "ourself", "ourselves", "per", "she", "since", "than", "that", "the", "thee",
            "theirs", "them", "themselves", "they", "thine", "this", "thyself", "to", "tother", "toward", "towards",
            "unless", "until", "upon", "versus", "via", "we", "what", "whatall", "whereas", "which", "whichever",
            "whichsoever", "whoever", "whom", "whomever", "whomso", "whomsoever", "whose", "whosoever", "with",
            "without", "ye", "you", "you-all", "yours", "yourself", "yourselves", "aboard", "about", "above",
            "across", "after", "all", "along", "alongside", "although", "another", "anti", "any", "anything", "around",
            "astride", "aught", "bar", "barring", "before", "behind", "below", "beneath", "besides", "between", "beyond",
            "both", "but", "concerning", "considering", "down", "each", "either", "enough", "except", "excepting", "excluding",
            "few", "fewer", "following", "ilk", "in", "including", "inside", "like", "many", "mine", "minus", "more", "most",
            "naught", "near", "neither", "nobody", "none", "nothing", "notwithstanding", "off", "on", "opposite", "other",
            "otherwise", "outside", "over", "own", "past", "pending", "plus", "regarding", "round", "save", "self", "several",
            "so", "some", "somebody", "someone", "something", "somewhat", "such", "suchlike", "sundry", "there", "though",
            "through", "throughout", "till", "twain", "under", "underneath", "unlike", "up", "various", "vis-a-vis", "whatever",
            "whatsoever", "when", "wherewith", "wherewithal", "while", "within", "worth", "yet", "yon", "yonder"};

        ArrayList<String> stopWordsList = new ArrayList<String>(Arrays.asList(stopWords));
        return (stopWordsList.contains(word.toLowerCase()));
    }

    /**
     * creaaza o glosa extinsa, formata din cuvintele din synset,
     * definitia synsetului dat ca parametru,
     * si din exemplele de folosire
     * @param s
     * @return String in care se afla concatenate definitia si exemplele
     */
    private String glossAndExamples(Synset s, boolean examples) {
        String gloss = "";
        String[] forms = s.getWordForms();
        for (String wf : forms) {
            gloss += " " + wf;
        }
        gloss += " " + s.getDefinition();
        if (examples) {
            String[] usage = s.getUsageExamples();
            for (String ex : usage) {
                gloss += " " + ex;
            }
        }
        return (gloss + " ");
    }

    /**
     * transforma un string intr-un tablou de cuvinte,
     * anterior prelucreaza stringul astfel incat sa elimine caracterele suplimentare
     * @param text
     * @return tabloul de cuvinte
     */
    public String[] textToWords(String text) {
        String textaux = " " + text.replaceAll("[^a-zA-Z0-9\\s-]", "") + " ";
        
        textaux = textaux.replace(" " + targetWord + " ", " _ ");
        //textaux = textaux.replaceAll("-", " ");
        String[] words1 = textaux.split("\\s");
        int spaces = 0;
        for (String s : words1) {
            if (s.matches("\\s") || s.equals("")) {
                spaces++;
            }
        }
        String words[] = new String[words1.length - spaces];
        int i = 0;
        for (String s : words1) {
            if (!s.matches("\\s") && !s.equals("")) {
                words[i++] = s;
            }
        }
        for (i = 0; i < words.length; i++) {
            
           // if (words[i].equals("_")) words[i] = targetWord;
            words[i] = words[i].replace("_", targetWord);
            words[i] = words[i].trim();
            
            
            /** TEST**/
            
            
            //System.out.println(words[i]);
            
            
            
            
        }
        return words;
    }
    /*
    public void enumerateSenses() {
    Synset[] ss = database.getSynsets(targetWord, pos);
    System.out.println("Senses:");
    for (Synset s : ss) {
    System.out.println(sensesNrs.get(s) + " " + s.getDefinition() + " - " + getSenseWordForms(s));
    }
    System.out.println("cel corect: " + sensesNrs.get(sense) + " " + sense.getDefinition() + " - " + getSenseWordForms(sense));

    }

    public String getRelatedForms() {
    String s = "";
    WordSense[] wss = sense.getDerivationallyRelatedForms(targetWord);
    for (WordSense ws : wss) {
    s += ws.getWordForm() + " ";
    }
    return s;
    }

    public String[] getWordsWindow() {
    return windowWords;
    }
     */

    public boolean isValidWord(String word, SynsetType type, boolean useMorphology) {
        return (((database.getSynsets(word, type, useMorphology).length != 0) || word.equals("")) && !isFunctionWord(word));
    }
}
