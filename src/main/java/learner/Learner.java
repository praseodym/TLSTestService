package learner;


import de.learnlib.algorithms.dhc.mealy.MealyDHC;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies;
import de.learnlib.algorithms.lstargeneric.mealy.ClassicLStarMealy;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.SUL;
import de.learnlib.cache.mealy.MealyCacheOracle;
import de.learnlib.eqtests.basic.RandomWordsEQOracle.MealyRandomWordsEQOracle;
import de.learnlib.eqtests.basic.WMethodEQOracle.MealyWMethodEQOracle;
import de.learnlib.eqtests.basic.WpMethodEQOracle.MealyWpMethodEQOracle;
import de.learnlib.logging.LearnLogger;
import de.learnlib.oracles.CounterOracle.MealyCounterOracle;
import de.learnlib.statistics.SimpleProfiler;
import learner.ModifiedExperiment.ModifiedMealyExperiment;
import learner.ModifiedWMethodEQOracle.MealyModifiedWMethodEQOracle;
import learner.ModifiedWpMethodEQOracle.ModifiedMealyWpMethodEQOracle;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.mappings.MapMapping;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import util.SimplifyDot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.*;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class Learner {
    TLSConfig config;

    Alphabet<String> alphabet;
    SUL<String, String> sul;

    MapMapping<String, String> errorMapping;

    MembershipOracle<String, Word<String>> sulMembershipOracle;
    MembershipOracle<String, Word<String>> cacheMemOracle;
    MealyCounterOracle<String, String> statsMemOracle;

    LearningAlgorithm learner;


    MapMapping<String, String> errorMappingEquiv;

    MembershipOracle<String, Word<String>> sulEquivalenceOracle;
    MembershipOracle<String, Word<String>> cacheEQOracle;
    MealyCounterOracle<String, String> statsEQOracle;
    MealyCounterOracle<String, String> statsCacheEQOracle;
    EquivalenceOracle<MealyMachine<?, String, ?, String>, String, Word<String>> eqOracle;

    MealyMachine<?, String, ?, String> result;

    FileOutputStream logMemQueries;
    FileOutputStream logEquivQueries;

    public Learner(String configFile) throws Exception {
        config = new TLSConfig(configFile);

        // Create output directory if it doesn't exist
        Path path = Paths.get(config.output_dir);
        if (Files.notExists(path)) {
            Files.createDirectory(path);
        }

        LearnLogger loggerLearnlib = LearnLogger.getLogger("de.learnlib");
        loggerLearnlib.setLevel(Level.ALL);
        FileHandler fhLearnlibLog = new FileHandler(config.output_dir + "/learnlib.log");
        loggerLearnlib.addHandler(fhLearnlibLog);
        fhLearnlibLog.setFormatter(new SimpleFormatter());

        LearnLogger loggerExperiment = LearnLogger.getLogger("learner.ModifiedExperiment");
        loggerExperiment.setLevel(Level.ALL);
        FileHandler fhExperimentLog = new FileHandler(config.output_dir + "/experiment.log");
        loggerExperiment.addHandler(fhExperimentLog);
        fhExperimentLog.setFormatter(new SimpleFormatter());
        loggerExperiment.addHandler(new ConsoleHandler());

        LearnLogger loggerMembershipOracle = LearnLogger.getLogger("learner.BasicMembershipOracle");
        loggerMembershipOracle.setLevel(Level.ALL);
        FileHandler fhMembershipLog = new FileHandler(config.output_dir + "/memQueries.log");
        loggerMembershipOracle.addHandler(fhMembershipLog);
        fhMembershipLog.setFormatter(new SimpleFormatter());

        LearnLogger loggerEQOracle = LearnLogger.getLogger("learner.BasicEquivalenceOracle");
        loggerEQOracle.setLevel(Level.ALL);
        FileHandler fhEQLog = new FileHandler(config.output_dir + "/equivQueries.log");
        loggerEQOracle.addHandler(fhEQLog);
        fhEQLog.setFormatter(new SimpleFormatter());

        LearnLogger loggerLearner = LearnLogger.getLogger("learner.Learner");
        loggerLearner.setLevel(Level.ALL);
        FileHandler fhLearnerLog = new FileHandler(config.output_dir + "/learner.log");
        loggerLearner.addHandler(fhLearnerLog);
        fhLearnerLog.setFormatter(new SimpleFormatter());
        loggerLearner.addHandler(new ConsoleHandler());

        Logger log = Logger.getLogger(this.getClass().getName());

        sul = new TLSSUL(config);

        alphabet = config.alphabet;

        errorMapping = new MapMapping<>();
        //errorMapping.put("ConnectionClosed", "ConnectionClosed");

        sulMembershipOracle = new BasicMembershipOracle(sul);
        //cacheMemOracle = new MealyCacheOracle<String, String>(alphabet, errorMapping, sulMembershipOracle);
        statsMemOracle = new MealyCounterOracle<>(sulMembershipOracle, "membership queries");

        if (config.learning_algorithm.equalsIgnoreCase("lstar")) {
            List<Word<String>> emptyList = Collections.emptyList();
            learner = ClassicLStarMealy.createForWordOracle(alphabet, statsMemOracle, emptyList, ObservationTableCEXHandlers.CLASSIC_LSTAR, ClosingStrategies.CLOSE_FIRST);
        } else if (config.learning_algorithm.equalsIgnoreCase("dhc")) {
            learner = new MealyDHC<>(alphabet, statsMemOracle);
        } else {
            throw new Exception("Unknown learning algorithm");
        }

        log.log(Level.INFO, "Using learning algorithm " + config.learning_algorithm);

        sulEquivalenceOracle = new BasicEquivalenceOracle(sul);
        statsEQOracle = new MealyCounterOracle<>(sulEquivalenceOracle, "equivalence queries to SUL");

        if (config.eqtest_caching.equalsIgnoreCase("regular") || config.eqtest_caching.equalsIgnoreCase("errormapping")) {
            log.log(Level.INFO, "Using caching for equivalence oracle");

            if (config.eqtest_caching.equalsIgnoreCase("errormapping")) {
                log.log(Level.INFO, "Using error mapping for equivalence oracle");
                errorMapping.put("ConnectionClosed", "ConnectionClosed");
            }

            cacheEQOracle = new MealyCacheOracle<>(alphabet, errorMapping, statsEQOracle);
            statsCacheEQOracle = new MealyCounterOracle<>(cacheEQOracle, "equivalence queries to cache");
        } else {
            statsCacheEQOracle = new MealyCounterOracle<>(statsEQOracle, "equivalence queries");
        }

        if (config.eqtest.equalsIgnoreCase("wmethod")) {
            eqOracle = new MealyWMethodEQOracle<>(config.max_depth, statsCacheEQOracle);
        } else if (config.eqtest.equalsIgnoreCase("wpmethod")) {
            eqOracle = new MealyWpMethodEQOracle<>(config.max_depth, statsCacheEQOracle);
        } else if (config.eqtest.equalsIgnoreCase("modifiedwmethod")) {
            eqOracle = new MealyModifiedWMethodEQOracle<>(config.max_depth, statsCacheEQOracle);
        } else if (config.eqtest.equalsIgnoreCase("modifiedwpmethod")) {
            eqOracle = new ModifiedMealyWpMethodEQOracle<>(config.max_depth, 10, statsCacheEQOracle);
        } else if (config.eqtest.equalsIgnoreCase("randomwords")) {
            eqOracle = new MealyRandomWordsEQOracle<>(statsCacheEQOracle, config.min_length, config.max_length, config.nr_queries, new Random(config.seed));
        } else {
            throw new Exception("Unknown equality test: " + config.eqtest);
        }

        log.log(Level.INFO, "Using equivalence oracle " + config.eqtest);

        //WpMethodEQOracle.MealyWpMethodEQOracle<String, String> eqOracle = new WpMethodEQOracle.MealyWpMethodEQOracle<String, String>(maxDepth, sulEquivalenceOracle);
    }

    public void writeModel(MealyMachine<?, String, ?, String> model, String name) throws IOException, InterruptedException {
        // Write output to file
        File dotFile = new File(config.output_dir + "/" + name + ".dot");
        PrintStream psDotFile = new PrintStream(dotFile);
        GraphDOT.write(model, alphabet, psDotFile);

        // Convert .dot to .pdf
        Runtime.getRuntime().exec("dot -Tpdf -o " + config.output_dir + "/" + name + ".pdf " + config.output_dir + "/" + name + ".dot");

        // Simplify .dot and convert to .pdf
        //Runtime.getRuntime().exec("python simplify_dot.py " + config.output_dir + "/" + name + ".dot " + config.output_dir + "/" + name + "_simplified.dot").waitFor();
        //Runtime.getRuntime().exec("dot -Tpdf -o " + config.output_dir + "/" + name + "_simplified.pdf " + config.output_dir + "/" + name+ "_simplified.dot");

    }

    public MealyMachine<?, String, ?, String> learn() throws Exception {
        ModifiedMealyExperiment<String, String> experiment = new ModifiedMealyExperiment<>(learner, eqOracle, alphabet);
        //MealyExperiment<String, String> experiment = new MealyExperiment<String, String>(learner, eqOracle, alphabet);

        Logger log = Logger.getLogger(this.getClass().getName());
        log.log(Level.INFO, "Starting learning");

        experiment.setProfile(true);
        experiment.setLogModels(true);

        long start = System.currentTimeMillis();
        experiment.run(this);
        long end = System.currentTimeMillis();

        result = experiment.getFinalHypothesis();

        ((TLSSUL) sul).tls.close();
        // report results
        log.log(Level.INFO, "-------------------------------------------------------");
        // profiling
        log.log(Level.INFO, SimpleProfiler.getResults());
        log.log(Level.INFO, "Total time: " + (end - start) + "ms (" + ((end - start) / 1000) + " s)");
        // learning statistics
        log.log(Level.INFO, experiment.getRounds().getSummary());

        log.log(Level.INFO, statsMemOracle.getStatisticalData().getSummary());
        log.log(Level.INFO, statsEQOracle.getStatisticalData().getSummary());
        log.log(Level.INFO, statsCacheEQOracle.getStatisticalData().getSummary());
        log.log(Level.INFO, "States in final hyptothesis: " + result.size());

        return result;
    }

    public static void main(String[] args) throws Exception {
        String configFile;
        if (args.length > 0)
            configFile = args[0];
        else
            configFile = "config.properties";

        Learner learner = new Learner(configFile);

        MealyMachine<?, String, ?, String> result = learner.learn();

        // Copy configuration to output file
        Files.copy(Paths.get(configFile), Paths.get(learner.config.output_dir + "/config.properties"), StandardCopyOption.REPLACE_EXISTING);

        // Write output to file
        String outputFilename = learner.config.output_dir + "/learnedModel.dot";
        String outputFilenamePdf = outputFilename.replace(".dot", ".pdf");
        File dotFile = new File(outputFilename);
        PrintStream psDotFile = new PrintStream(dotFile);
        GraphDOT.write(result, learner.alphabet, psDotFile);

        // Convert .dot to .pdf
        Runtime.getRuntime().exec("dot -Tpdf -o " + outputFilenamePdf + " " + outputFilename);

        // Simplified .dot file
        List<String> lines = Files.readAllLines(Paths.get(outputFilename));
        List<String> simpified = SimplifyDot.simplifyDot(lines);
        String simplifiedOutputFilename = outputFilename.replace(".dot", "_simple.dot");
        Files.write(Paths.get(simplifiedOutputFilename), simpified, Charset.defaultCharset());

        // Convert .dot to .pdf
        String simplifiedOutputFilenamePdf = outputFilenamePdf.replace(".pdf", "_simple.pdf");
        Runtime.getRuntime().exec("dot -Tpdf -o " + simplifiedOutputFilenamePdf + " " + simplifiedOutputFilename);

        // Simplify .dot and convert to .pdf
        //Runtime.getRuntime().exec("python simplify_dot.py " + learner.config.output_dir + "/learnedModel.dot " + learner.config.output_dir + "/learnedModel_simplified.dot").waitFor();
        //Runtime.getRuntime().exec("dot -Tpdf -o " + learner.config.output_dir + "/learnedModel_simplified.pdf " + learner.config.output_dir + "/learnedModel_simplified.dot");

        // Display output on screen
        //Writer w = DOT.createDotWriter(true);
        //GraphDOT.write(result, learner.alphabet, w);
        //w.close();

        //System.out.println(statistics.getStatisticalData().getName());
        //System.out.println(statistics.getStatisticalData().getCount());
        //learner.getHypothesisModel().

        // show model

        //System.out.println();
        //System.out.println("Model: ");
        //GraphDOT.write(result, learner.alphabet, System.out); // may throw IOException!

    }
}
