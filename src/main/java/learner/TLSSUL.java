package learner;

import de.learnlib.api.SUL;
import tlstestservice.TLSTestService;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
class TLSSUL implements SUL<String, String> {
    public TLSTestService tls;

    public TLSSUL(TLSConfig config) throws Exception {
        tls = new TLSTestService();

        tls.setTarget(config.target);
        tls.setHost(config.host);
        tls.setPort(config.port);
        tls.setCommand(config.cmd);
        tls.setRequireRestart(config.restart);
        tls.setReceiveMessagesTimeout(config.timeout);

        if (config.version.equals("tls10")) {
            tls.useTLS10();
        } else {
            tls.useTLS12();
        }

        tls.start();
    }

    public void reset() {
        try {
            tls.reset();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }


    @Override
    public String step(String symbol) {
        String result = null;
        try {
            result = tls.processSymbol(symbol);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
