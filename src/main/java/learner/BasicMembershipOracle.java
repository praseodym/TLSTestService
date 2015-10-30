package learner;

import de.learnlib.api.SUL;
import de.learnlib.oracles.SULOracle;
import net.automatalib.words.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class BasicMembershipOracle extends SULOracle<String, String> {

    private static final Logger log = LoggerFactory.getLogger(BasicMembershipOracle.class);

    int nrQueries = 0;

    public BasicMembershipOracle(SUL<String, String> sul) {
        super(sul);
    }

    public Word<String> answerQuery(Word<String> prefix, Word<String> suffix) {
        nrQueries++;
        log.info("Membership query {}: {} | {}", nrQueries,  prefix, suffix);
        Word<String> answer = super.answerQuery(prefix, suffix);
        log.info("Answer: {}", answer);
        return answer;

    }
}
