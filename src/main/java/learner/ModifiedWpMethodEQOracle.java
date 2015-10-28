package learner;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import java.util.*;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 *         <p>
 *         Based on the original by Malte Isberner
 */
public class ModifiedWpMethodEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D>
        implements EquivalenceOracle<A, I, D> {

    public static class ModifiedDFAWpMethodEQOracle<I> extends ModifiedWpMethodEQOracle<DFA<?, I>, I, Boolean>
            implements DFAEquivalenceOracle<I> {
        public ModifiedDFAWpMethodEQOracle(int maxDepth, int maxStates,
                                           MembershipOracle<I, Boolean> sulOracle) {
            super(maxDepth, maxStates, sulOracle);
        }
    }

    public static class ModifiedMealyWpMethodEQOracle<I, O> extends ModifiedWpMethodEQOracle<MealyMachine<?, I, ?, O>, I, Word<O>> {
        public ModifiedMealyWpMethodEQOracle(int maxDepth, int maxStates,
                                             MembershipOracle<I, Word<O>> sulOracle) {
            super(maxDepth, maxStates, sulOracle);
        }
    }

    private int maxDepth;
    private int maxStates = 0;
    private final MembershipOracle<I, D> sulOracle;

    /**
     * Constructor.
     *
     * @param maxDepth  the maximum length of the "middle" part of the test cases
     * @param sulOracle interface to the system under learning
     */
    public ModifiedWpMethodEQOracle(int maxDepth, int maxStates, MembershipOracle<I, D> sulOracle) {
        this.maxDepth = maxDepth;
        this.maxStates = maxStates;
        this.sulOracle = sulOracle;
    }

    /*
     * (non-Javadoc)
     * @see de.learnlib.api.EquivalenceOracle#findCounterExample(java.lang.Object, java.util.Collection)
     */
    @Override
    public DefaultQuery<I, D> findCounterExample(A hypothesis,
                                                 Collection<? extends I> inputs) {
        UniversalDeterministicAutomaton<?, I, ?, ?, ?> aut = hypothesis;
        Output<I, D> out = hypothesis;
        return doFindCounterExample(aut, out, inputs);
    }

    public void setMaxStates(int maxStates) {
        this.maxStates = maxStates;
    }


    /*
     * Delegate target, used to bind the state-parameter of the automaton
     */
    private <S> DefaultQuery<I, D> doFindCounterExample(UniversalDeterministicAutomaton<S, I, ?, ?, ?> hypothesis,
                                                        Output<I, D> output, Collection<? extends I> inputs) {
        if (maxStates > 0) {
            maxDepth = Math.max(0, maxStates - hypothesis.size());
        }


        List<Word<I>> stateCover = new ArrayList<>(hypothesis.size());
        List<Word<I>> transitions = new ArrayList<>(hypothesis.size() * (inputs.size() - 1));

        Automata.cover(hypothesis, inputs, stateCover, transitions);

        List<Word<I>> globalSuffixes = Automata.characterizingSet(hypothesis, inputs);
        if (globalSuffixes.isEmpty())
            globalSuffixes = Collections.singletonList(Word.<I>epsilon());

        WordBuilder<I> wb = new WordBuilder<>();

        DefaultQuery<I, D> query;
        D hypOutput;
        String output2;
        Word<I> queryWord;

        // Phase 1: state cover * middle part * global suffixes
        for (Word<I> as : stateCover) {
            // If query(as) ends with closed symbol break
            query = new DefaultQuery<>(as);
            sulOracle.processQueries(Collections.singleton(query));

            hypOutput = output.computeOutput(as);
            if (!Objects.equals(hypOutput, query.getOutput()))
                return query;

            output2 = query.getOutput().toString();

            //System.out.println("trans ends with " + output);
            if (output2.endsWith("ConnectionClosed") || output2.endsWith("ConnectionClosedEOF") || output2.endsWith("ConnectionClosedException")) {
                // Remove trans from transCover
                //removeFromTransCover.add(trans);
                //System.out.println("trans ends with ConnectionClosed: continue");
                continue;
            }

            for (List<? extends I> middle : CollectionsUtil.allTuples(inputs, 1, maxDepth)) {
                // If query(as||middle) ends with closed symbol break
                wb.append(as).append(middle);
                queryWord = wb.toWord();
                wb.clear();
                query = new DefaultQuery<>(queryWord);

                sulOracle.processQueries(Collections.singleton(query));

                hypOutput = output.computeOutput(queryWord);
                if (!Objects.equals(hypOutput, query.getOutput()))
                    return query;

                output2 = query.getOutput().toString();

                //System.out.println("trans ends with " + output);
                if (output2.endsWith("ConnectionClosed") || output2.endsWith("ConnectionClosedEOF") || output2.endsWith("ConnectionClosedException")) {
                    // Remove trans from transCover
                    //removeFromTransCover.add(trans);
                    //System.out.println("trans ends with ConnectionClosed: continue");
                    continue;
                }

                for (Word<I> suffix : globalSuffixes) {
                    wb.append(as).append(middle).append(suffix);
                    queryWord = wb.toWord();
                    wb.clear();
                    query = new DefaultQuery<>(queryWord);
                    hypOutput = output.computeOutput(queryWord);
                    sulOracle.processQueries(Collections.singleton(query));
                    if (!Objects.equals(hypOutput, query.getOutput()))
                        return query;
                }
            }
        }

        // Phase 2: transitions (not in state cover) * middle part * local suffixes
        MutableMapping<S, List<Word<I>>> localSuffixSets
                = hypothesis.createStaticStateMapping();

        for (Word<I> trans : transitions) {
            // If query(trans) ends with closed symbol break
            query = new DefaultQuery<>(trans);
            sulOracle.processQueries(Collections.singleton(query));

            hypOutput = output.computeOutput(trans);
            if (!Objects.equals(hypOutput, query.getOutput()))
                return query;

            output2 = query.getOutput().toString();

            //System.out.println("trans ends with " + output);
            if (output2.endsWith("ConnectionClosed") || output2.endsWith("ConnectionClosedEOF") || output2.endsWith("ConnectionClosedException")) {
                // Remove trans from transCover
                //removeFromTransCover.add(trans);
                //System.out.println("trans ends with ConnectionClosed: continue");
                continue;
            }

            S state = hypothesis.getState(trans);
            List<Word<I>> localSuffixes = localSuffixSets.get(state);
            if (localSuffixes == null) {
                localSuffixes = Automata.stateCharacterizingSet(hypothesis, inputs, state);
                if (localSuffixes.isEmpty())
                    localSuffixes = Collections.singletonList(Word.<I>epsilon());
                localSuffixSets.put(state, localSuffixes);
            }

            for (List<? extends I> middle : CollectionsUtil.allTuples(inputs, 1, maxDepth)) {
                // If query(as||middle) ends with closed symbol break
                wb.append(trans).append(middle);
                queryWord = wb.toWord();
                wb.clear();
                query = new DefaultQuery<>(queryWord);

                sulOracle.processQueries(Collections.singleton(query));

                hypOutput = output.computeOutput(queryWord);
                if (!Objects.equals(hypOutput, query.getOutput()))
                    return query;

                output2 = query.getOutput().toString();

                //System.out.println("trans ends with " + output);
                if (output2.endsWith("ConnectionClosed") || output2.endsWith("ConnectionClosedEOF") || output2.endsWith("ConnectionClosedException")) {
                    // Remove trans from transCover
                    //removeFromTransCover.add(trans);
                    //System.out.println("trans ends with ConnectionClosed: continue");
                    continue;
                }

                for (Word<I> suffix : localSuffixes) {
                    wb.append(trans).append(middle).append(suffix);
                    queryWord = wb.toWord();
                    wb.clear();
                    query = new DefaultQuery<>(queryWord);
                    hypOutput = output.computeOutput(queryWord);
                    sulOracle.processQueries(Collections.singleton(query));
                    if (!Objects.equals(hypOutput, query.getOutput()))
                        return query;
                }
            }
        }

        return null;
    }

}
