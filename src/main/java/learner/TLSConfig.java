package learner;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.automatalib.words.impl.SimpleAlphabet;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 */
public class TLSConfig {
	String target = "server";
	String cmd = null;
	String cmd_version = null;
	String version = "tls12";
	
	String host = "localhost";
	int port = 4433;
	
	boolean restart = false;
	int timeout = 100;
	
	String output_dir = "output";
	
	SimpleAlphabet<String> alphabet = new SimpleAlphabet<String>();
	
	String learning_algorithm = "lstar";
	String eqtest = "randomwords";
	String eqtest_caching = "none";
	
	// Used for W-Method
	int max_depth = 10;
	
	// Used for Random words
	int min_length = 5;
	int max_length = 10;
	int nr_queries = 100;
	int seed = 1;
	
	public TLSConfig(String filename) throws IOException {
		Properties properties = new Properties();

		InputStream input = new FileInputStream(filename);
		properties.load(input);

		if(properties.getProperty("target").equalsIgnoreCase("client") || properties.getProperty("target").equalsIgnoreCase("server"))
			target = properties.getProperty("target").toLowerCase();
		
		if(properties.getProperty("cmd") != null)
			cmd = properties.getProperty("cmd");

		if(properties.getProperty("version") != null)
			version = properties.getProperty("version");
		
		if(properties.getProperty("cmd_version") != null)
			cmd_version = properties.getProperty("cmd_version");

		if(properties.getProperty("host") != null)
			host = properties.getProperty("host");
		
		if(properties.getProperty("port") != null)
			port = Integer.parseInt(properties.getProperty("port"));
		
		if(properties.getProperty("restart") != null)
			restart = Boolean.parseBoolean(properties.getProperty("restart"));
		
		if(properties.getProperty("timeout") != null)
			timeout = Integer.parseInt(properties.getProperty("timeout"));
		
		if(properties.getProperty("output_dir") != null)
			output_dir = properties.getProperty("output_dir");
		
		if(properties.getProperty("alphabet") != null) {
			String[] list  = properties.getProperty("alphabet").split(" ");
			
			alphabet = new SimpleAlphabet<String>();
			for(String s: list) {
				alphabet.add(s);
			}
		}
		
		if(properties.getProperty("learning_algorithm").equalsIgnoreCase("lstar"))
			learning_algorithm = properties.getProperty("learning_algorithm").toLowerCase();
		
		if(properties.getProperty("eqtest").equalsIgnoreCase("wmethod") || properties.getProperty("eqtest").equalsIgnoreCase("wpmethod") || properties.getProperty("eqtest").equalsIgnoreCase("modifiedwmethod") ||  properties.getProperty("eqtest").equalsIgnoreCase("modifiedwpmethod") || properties.getProperty("eqtest").equalsIgnoreCase("randomwords"))
			eqtest = properties.getProperty("eqtest").toLowerCase();
		
		if(properties.getProperty("eqtest_caching") != null && (properties.getProperty("eqtest_caching").equalsIgnoreCase("none") || properties.getProperty("eqtest_caching").equalsIgnoreCase("regular") || properties.getProperty("eqtest_caching").equalsIgnoreCase("errormapping")))
			eqtest_caching = properties.getProperty("eqtest_caching").toLowerCase();
		
		if(properties.getProperty("max_depth") != null)
			max_depth = Integer.parseInt(properties.getProperty("max_depth"));
		
		if(properties.getProperty("min_length") != null)
			min_length = Integer.parseInt(properties.getProperty("min_length"));
		
		if(properties.getProperty("max_length") != null)
			max_length = Integer.parseInt(properties.getProperty("max_length"));
		
		if(properties.getProperty("nr_queries") != null)
			nr_queries = Integer.parseInt(properties.getProperty("nr_queries"));
		
		if(properties.getProperty("seed") != null)
			seed = Integer.parseInt(properties.getProperty("seed"));
	}

}
