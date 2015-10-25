package learner;

import net.automatalib.words.Word;
import de.learnlib.api.SUL;
import de.learnlib.logging.LearnLogger;
import de.learnlib.oracles.SULOracle;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class BasicMembershipOracle extends SULOracle<String, String> {
	int nrQueries = 0;
	LearnLogger log; 
	
	public BasicMembershipOracle(SUL<String, String> sul) {
		super(sul);
		log = LearnLogger.getLogger(this.getClass().getName());
		System.out.println(this.getClass().getName());
	}
	
	public Word<String> answerQuery(Word<String> prefix, Word<String> suffix) {
		nrQueries++;
		log.logQuery("Membership query " + nrQueries + ": " + prefix + " | " + suffix);
		Word<String> answer = super.answerQuery(prefix, suffix);
		log.logQuery("Answer: " + answer);
		return answer;
		
	}
}
