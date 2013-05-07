/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testcorpora;

/**
 *
 * @author ana
 */
public interface TestCorpus extends Runnable {
    
   // TODO: is it ok to extend Runnable?
    
   /*
     * return file contents as a String
     */
   public String getAsString(String fileName);
   
   /*
    * return array of Strings = sentences/contexts in which the target word is found
    * in parsed input file
    */
   public String[] parseOriginal(String text);

   /*
    * return results after testing on one file (containing target word used with one sense)
    * as a pair {total, correct} counting total number of contexts,
    * and number of contexts for which target word sense was correctly classified
    */
   public int[] testCorpusFromFile(String file);
   
   /*
    * return results for testing on all files (for current corpus) - one sense of target word per file
    * return value contains a list of values total and correct for each file (word sense)
    * representing: total number of contexts in that file,
    * and number of contexts in that file for which target word sense was correctly classified
    */
   public int[] testCorpusFromAll();

   /*
    * reset computed test results values to 0
    */
   public void reset();
   
   /*
    * set parameters functions
    */
   
   public void setWithReportFile(boolean with);
   
   public void setNormalize (boolean normalize);

   public void setWindow(int w);

   public void setOnlyUseSamePos(boolean onlySamePos);
   
   /*
    * get String describing results for one file (the one that has been tested on)
    */
   public String getOneResult();

   /*
    * get String describing results for entire corpus
    */
   public String getAllResults();
   
}
