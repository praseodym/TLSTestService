package learner;

import de.learnlib.api.SUL;
import de.learnlib.oracles.SULOracle;
import net.automatalib.words.Word;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class BasicEquivalenceOracle extends SULOracle<String, String> {
    int nrQueries = 0;
    LearnLogger log;

    public BasicEquivalenceOracle(SUL<String, String> sul) {
        super(sul);
        log = LearnLogger.getLogger(this.getClass());
    }

    public Word<String> answerQuery(Word<String> prefix, Word<String> suffix) {
        nrQueries++;
        log.logQuery("Equivalence query " + nrQueries + ": " + prefix + " | " + suffix);
        Word<String> answer = super.answerQuery(prefix, suffix);
        log.logQuery("Answer: " + answer);
        return answer;

    }
}
