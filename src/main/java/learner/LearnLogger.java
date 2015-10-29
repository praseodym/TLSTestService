package learner;

import de.learnlib.statistics.StatisticData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * slf4j LearnLogger
 */
public class LearnLogger {

    // FIXME: ensure certain loggers append to file
    private static final Logger logPhase = LoggerFactory.getLogger("Phase");
    private static final Logger logQuery = LoggerFactory.getLogger("Query");
    private static final Logger logConfig = LoggerFactory.getLogger("Config");
    private static final Logger logCounterexample = LoggerFactory.getLogger("Counterexample");
    private static final Logger logEvent = LoggerFactory.getLogger("Event");
    private static final Logger logProfilingInfo = LoggerFactory.getLogger("ProfilingInfo");
    private static final Logger logStatistic = LoggerFactory.getLogger("Statistic");
    private static final Logger logModel = LoggerFactory.getLogger("Model");
    private static final Logger logDataStructure = LoggerFactory.getLogger("DataStructure");

    private static final LearnLogger INSTANCE = new LearnLogger();

    private LearnLogger() {
    }

    public static LearnLogger getLogger() {
        // TODO: maybe do something with class name
        return INSTANCE;
    }

    public static LearnLogger getLogger(Object o) {
        // TODO: maybe do something with class name
        return INSTANCE;
    }

    /**
     * logs a learning phase at level INFO.
     *
     * @param phase
     */
    public void logPhase(String phase) {
//        LearnLogRecord rec = new LearnLogRecord(Level.INFO, phase, Category.PHASE);
        logPhase.info(phase);
    }

    /**
     * logs a learning query at level INFO.
     *
     * @param phase
     */
    public void logQuery(String phase) {
//        LearnLogRecord rec = new LearnLogRecord(Level.INFO, phase, Category.QUERY);
        logQuery.info(phase);
    }

    /**
     * logs setup details
     *
     * @param config
     */
    public void logConfig(String config) {
//        LearnLogRecord rec = new LearnLogRecord(Level.INFO, config, Category.CONFIG);
        logConfig.info(config);
    }

    /**
     * log counterexample
     *
     * @param ce
     */
    public void logCounterexample(String ce) {
//        LearnLogRecord rec = new LearnLogRecord(Level.INFO, ce, Category.COUNTEREXAMPLE);
        logCounterexample.info(ce);
    }

    /**
     * logs an event. E.g., creation of new table row
     *
     * @param desc
     */
    public void logEvent(String desc) {
//        LearnLogRecord rec = new LearnLogRecord(Level.INFO, desc, Category.EVENT);
        logEvent.info(desc);
    }

    /**
     * log a piece of profiling info
     *
     * @param profiling
     */
    public void logProfilingInfo(StatisticData profiling) {
//        LearnLogRecord rec = new StatisticLogRecord(Level.INFO, profiling, Category.PROFILING);
        logProfilingInfo.info(profiling.getSummary());
    }

    /**
     * log statistic info
     *
     * @param statistics
     */
    public void logStatistic(StatisticData statistics) {
//        LearnLogRecord rec = new StatisticLogRecord(Level.INFO, statistics, Category.STATISTIC);
        logStatistic.info(statistics.getSummary());
    }

    /**
     * log a model
     *
     * @param o
     */
    public void logModel(Object o) {
//        LearnLogRecord rec = new PlottableLogRecord(Level.INFO, o, Category.MODEL);
        logModel.info(o.toString());
    }

    /**
     * log a data structure
     *
     * @param o
     */
    public void logDataStructure(Object o) {
//        LearnLogRecord rec = new PlottableLogRecord(Level.INFO, o, Category.DATASTRUCTURE);
        logDataStructure.info(o.toString());
    }
}

