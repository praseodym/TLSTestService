package learner;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 *         <p>
 *         Based on the original by Malte Isberner
 */
public class ModifiedWMethodEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D>
        implements EquivalenceOracle<A, I, D> {
    public static class DFAModifiedWMethodEQOracle<I> extends
            ModifiedWMethodEQOracle<DFA<?, I>, I, Boolean> implements
            DFAEquivalenceOracle<I> {
        public DFAModifiedWMethodEQOracle(int maxDepth,
                                          MembershipOracle<I, Boolean> sulOracle) {
            super(maxDepth, sulOracle);
        }
    }

    public static class MealyModifiedWMethodEQOracle<I, O> extends
            ModifiedWMethodEQOracle<MealyMachine<?, I, ?, O>, I, Word<O>> implements
            MealyEquivalenceOracle<I, O> {
        public MealyModifiedWMethodEQOracle(int maxDepth,
                                            MembershipOracle<I, Word<O>> sulOracle) {
            super(maxDepth, sulOracle);
        }
    }

    private int maxDepth;
    private final MembershipOracle<I, D> sulOracle;

    /**
     * Constructor.
     *
     * @param maxDepth  the maximum length of the "middle" part of the test cases
     * @param sulOracle interface to the system under learning
     */
    public ModifiedWMethodEQOracle(int maxDepth, MembershipOracle<I, D> sulOracle) {
        this.maxDepth = maxDepth;
        this.sulOracle = sulOracle;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.learnlib.api.EquivalenceOracle#findCounterExample(java.lang.Object,
     * java.util.Collection)
     */
    @Override
    public DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        List<Word<I>> transCover = Automata.transitionCover(hypothesis, inputs);
        List<Word<I>> charSuffixes = Automata.characterizingSet(hypothesis, inputs);

        // Special case: List of characterizing suffixes may be empty,
        // but in this case we still need to test!
        if (charSuffixes.isEmpty())
            charSuffixes = Collections.singletonList(Word.<I>epsilon());

        WordBuilder<I> wb = new WordBuilder<>();

        DefaultQuery<I, D> query;
        D hypOutput;
        String output;
        Word<I> queryWord;

        //List<Word<I>> removeFromTransCover = new ArrayList<Word<I>>();

        for (Word<I> trans : transCover) {

            // If query(trans) ends with closed symbol break
            query = new DefaultQuery<>(trans);
            sulOracle.processQueries(Collections.singleton(query));

            hypOutput = hypothesis.computeOutput(trans);
            if (!Objects.equals(hypOutput, query.getOutput()))
                return query;

            output = query.getOutput().toString();

            //System.out.println("trans ends with " + output);
            if (output.endsWith("ConnectionClosed") || output.endsWith("ConnectionClosedEOF") || output.endsWith("ConnectionClosedException")) {
                // Remove trans from transCover
                //removeFromTransCover.add(trans);
                //System.out.println("trans ends with ConnectionClosed: continue");
                continue;
            }

            for (List<? extends I> middle : CollectionsUtil.allTuples(inputs, 1, maxDepth)) {
                //transCover.removeAll(removeFromTransCover);
                //removeFromTransCover.clear();

//			for (Word<I> trans : transCover) {

                // If query(trans || middle) ends with closed symbol break 2
                wb.append(trans).append(middle);
                queryWord = wb.toWord();
                wb.clear();

                query = new DefaultQuery<>(queryWord);
                sulOracle.processQueries(Collections.singleton(query));

                hypOutput = hypothesis.computeOutput(queryWord);

                if (!Objects.equals(hypOutput, query.getOutput()))
                    return query;

                output = query.getOutput().toString();

                //System.out.println("trans||middle ends with " + output);
                if (output.endsWith("ConnectionClosed") || output.endsWith("ConnectionClosedEOF") || output.endsWith("ConnectionClosedException")) {
                    //System.out.println("trans||middle ends with ConnectionClosed: continue");
                    continue;
                }


                for (Word<I> suffix : charSuffixes) {
                    wb.append(trans).append(middle).append(suffix);
                    queryWord = wb.toWord();
                    wb.clear();
                    query = new DefaultQuery<>(queryWord);
                    hypOutput = hypothesis.computeOutput(queryWord);
                    sulOracle.processQueries(Collections.singleton(query));
                    if (!Objects.equals(hypOutput, query.getOutput()))
                        return query;
                }
            }
        }
        return null;
    }
}