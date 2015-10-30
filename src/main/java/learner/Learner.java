package learner;


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 * @author Mark Janssen (mark@ch.tudelft.nl)
 */
public class Learner {

    private static final Logger log = LoggerFactory.getLogger(Learner.class);

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

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();
        JoranConfigurator configurator = new JoranConfigurator();
        loggerContext.putProperty("logDirectory", config.output_dir);
        configurator.setContext(loggerContext);
        configurator.doConfigure(getClass().getClassLoader().getResource("log.xml"));

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

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

        log.info("Using learning algorithm " + config.learning_algorithm);

        sulEquivalenceOracle = new BasicEquivalenceOracle(sul);
        statsEQOracle = new MealyCounterOracle<>(sulEquivalenceOracle, "equivalence queries to SUL");

        if (config.eqtest_caching.equalsIgnoreCase("regular") || config.eqtest_caching.equalsIgnoreCase("errormapping")) {
            log.info("Using caching for equivalence oracle");

            if (config.eqtest_caching.equalsIgnoreCase("errormapping")) {
                log.info("Using error mapping for equivalence oracle");
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

        log.info("Using equivalence oracle " + config.eqtest);

        //WpMethodEQOracle.MealyWpMethodEQOracle<String, String> eqOracle = new WpMethodEQOracle.MealyWpMethodEQOracle<String, String>(maxDepth, sulEquivalenceOracle);
    }

    public void writeModel(MealyMachine<?, String, ?, String> model, String name) throws IOException, InterruptedException {
        // Write output to file
        File dotFile = new File(config.output_dir + "/" + name + ".dot");
        PrintStream psDotFile = new PrintStream(dotFile);
        GraphDOT.write(model, alphabet, psDotFile);

        // Convert .dot to .pdf
        Runtime.getRuntime().exec("dot -Tpdf -o " + config.output_dir + "/" + name + ".pdf " + config.output_dir + "/" + name + ".dot");
    }

    public MealyMachine<?, String, ?, String> learn() throws Exception {
        ModifiedMealyExperiment<String, String> experiment = new ModifiedMealyExperiment<>(learner, eqOracle, alphabet);
        //MealyExperiment<String, String> experiment = new MealyExperiment<String, String>(learner, eqOracle, alphabet);

        log.info("Starting learning");

        experiment.setProfile(true);
        experiment.setLogModels(true);

        long start = System.currentTimeMillis();
        experiment.run(this);
        long end = System.currentTimeMillis();

        result = experiment.getFinalHypothesis();

        ((TLSSUL) sul).tls.close();
        // report results
        log.info("-------------------------------------------------------");
        // profiling
        log.info(SimpleProfiler.getResults());
        log.info("Total time: " + (end - start) + "ms (" + ((end - start) / 1000) + " s)");
        // learning statistics
        log.info(experiment.getRounds().getSummary());

        log.info(statsMemOracle.getStatisticalData().getSummary());
        log.info(statsEQOracle.getStatisticalData().getSummary());
        log.info(statsCacheEQOracle.getStatisticalData().getSummary());
        log.info("States in final hypothesis: " + result.size());

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
