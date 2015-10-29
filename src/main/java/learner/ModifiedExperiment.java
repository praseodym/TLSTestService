package learner;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.statistics.Counter;
import de.learnlib.statistics.SimpleProfiler;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.util.Collections;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 *         <p>
 *         runs a learning experiment. based on original by falkhowar
 */
public class ModifiedExperiment<A> {

    public static class ModifiedDFAExperiment<I> extends ModifiedExperiment<DFA<?, I>> {
        public ModifiedDFAExperiment(
                LearningAlgorithm<? extends DFA<?, I>, I, Boolean> learningAlgorithm,
                EquivalenceOracle<? super DFA<?, I>, I, Boolean> equivalenceAlgorithm,
                Alphabet<I> inputs) {
            super(learningAlgorithm, equivalenceAlgorithm, inputs);
        }
    }

    public static class ModifiedMealyExperiment<I, O> extends ModifiedExperiment<MealyMachine<?, I, ?, O>> {
        public ModifiedMealyExperiment(
                LearningAlgorithm<? extends MealyMachine<?, I, ?, O>, I, Word<O>> learningAlgorithm,
                EquivalenceOracle<? super MealyMachine<?, I, ?, O>, I, Word<O>> equivalenceAlgorithm,
                Alphabet<I> inputs) {
            super(learningAlgorithm, equivalenceAlgorithm, inputs);
        }

    }

    private final class ModifiedExperimentImpl<I, D> {
        private final LearningAlgorithm<? extends A, I, D> learningAlgorithm;
        private final EquivalenceOracle<? super A, I, D> equivalenceAlgorithm;
        private final Alphabet<I> inputs;

        public ModifiedExperimentImpl(LearningAlgorithm<? extends A, I, D> learningAlgorithm, EquivalenceOracle<? super A, I, D> equivalenceAlgorithm, Alphabet<I> inputs) {
            this.learningAlgorithm = learningAlgorithm;
            this.equivalenceAlgorithm = equivalenceAlgorithm;
            this.inputs = inputs;
        }

        public A run(Learner l) throws Exception {
            long start = System.currentTimeMillis();
            rounds.increment();
            logger.logPhase("Starting round " + rounds.getCount());
            logger.logPhase("Learning");
            profileStart("Learning");
            learningAlgorithm.startLearning();
            profileStop("Learning");

            boolean done = false;
            boolean refined = true;
            A hyp = null;
            while (!done) {
                hyp = learningAlgorithm.getHypothesisModel();
                if (logModels && refined) {
                    l.writeModel((MealyMachine<?, String, ?, String>) hyp, "hyp" + rounds.getCount());
                    logger.logModel(hyp);
                }

                logger.logPhase("Searching for counterexample");
                profileStart("Searching for counterexample");
                DefaultQuery<I, D> ce = equivalenceAlgorithm.findCounterExample(hyp, inputs);
                if (ce == null) {
                    done = true;
                    continue;
                }
                profileStop("Searching for counterexample");

                logger.logCounterexample(ce.getInput().toString());
                logger.logCounterexample(ce.getOutput().toString());

                // next round ...
                rounds.increment();
                logger.logPhase("Starting round " + rounds.getCount());
                logger.logPhase("Learning");
                profileStart("Learning");
                refined = learningAlgorithm.refineHypothesis(ce);
                if (!refined) {
                    logger.logCounterexample("Counterexample no refinement");
                    logger.logCounterexample("Counterexample input: " + ce.getInput().toString());
                    logger.logCounterexample("Counterexample output: " + ce.getOutput().toString());

                    DefaultQuery<String, Word<String>> query = new DefaultQuery<>((Word<String>) ce.getInput());
                    l.sulMembershipOracle.processQueries(Collections.singleton(query));
                    logger.logCounterexample("SUL output: " + query.getOutput().toString());

                    Word<String> hypOutput = ((Output<String, Word<String>>) hyp).computeOutput((Word<String>) ce.getInput());
                    logger.logCounterexample("Hypothesis output: " + hypOutput.toString());

                    throw new Exception("Counterexample is no refinement");
                }
                profileStop("Learning");
            }
            long end = System.currentTimeMillis();
            logger.logPhase("Total time: " + (end - start) + "ms (" + ((end - start) / 1000) + " s)");

            return hyp;
        }
    }

    private static LearnLogger logger = LearnLogger.getLogger(ModifiedExperiment.class);

    private boolean logModels = false;
    private boolean profile = false;
    private Counter rounds = new Counter("rounds", "#");
    private A finalHypothesis = null;
    private final ModifiedExperimentImpl<?, ?> impl;

    public <I, D> ModifiedExperiment(LearningAlgorithm<? extends A, I, D> learningAlgorithm, EquivalenceOracle<? super A, I, D> equivalenceAlgorithm, Alphabet<I> inputs) {
        this.impl = new ModifiedExperimentImpl<>(learningAlgorithm, equivalenceAlgorithm, inputs);
    }


    /**
     * @throws Exception
     */
    public A run(Learner l) throws Exception {
        finalHypothesis = impl.run(l);
        return finalHypothesis;
    }

    public A getFinalHypothesis() {
        if (finalHypothesis == null) {
            throw new IllegalStateException("Experiment has not yet been run");
        }
        return finalHypothesis;
    }


    private void profileStart(String taskname) {
        if (profile) {
            SimpleProfiler.start(taskname);
        }
    }

    private void profileStop(String taskname) {
        if (profile) {
            SimpleProfiler.stop(taskname);
        }
    }

    /**
     * @param logModels the logModels to set
     */
    public void setLogModels(boolean logModels) {
        this.logModels = logModels;
    }

    /**
     * @param profile the profile to set
     */
    public void setProfile(boolean profile) {
        this.profile = profile;
    }

    /**
     * @return the rounds
     */
    public Counter getRounds() {
        return rounds;
    }
}

