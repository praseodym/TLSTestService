package tlstestservice;

import java.io.BufferedOutputStream;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class TLSClient  implements Runnable {
    private Process p;
    private volatile boolean finished = false;

    public TLSClient(Process p) {
    	finished = false;
        this.p = p;
        new Thread(this).start();
    }

    public boolean isFinished() {
        return finished;
    }

    public void run() {
		try {
			Thread.sleep(10);

			BufferedOutputStream out = new BufferedOutputStream(p.getOutputStream());

			out.write("GET / HTTP/1.0\n\n".getBytes());
			out.flush();
			out.close();
			
			p.waitFor();
		} catch (Exception e) {
			finished = true;
		}
		finally {
			finished = true;
		}
    }

}