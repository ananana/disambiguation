/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testcorpora;

/**
 *
 * @author ana
 */
public class TestCorporaFactory {
    
    public static TestCorpus getTestingInstance(CorpusType type) {
        TestCorpus test = null;
        switch (type) {
            case LINE:
                test = new LineCorporaTests();
                break;
            case HARD:
                test = new HardCorporaTests();
                break;
            case SERVE:
                test = new ServeCorporaTests();
                break;
            case INTEREST:
                test = new InterestCorporaTests();
                break;
        }
        return test;
    }
}
