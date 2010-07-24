package org.mmisw.watchdog;

import java.io.IOException;

import org.mmisw.watchdog.cf.ICf2Skos;
import org.mmisw.watchdog.cf.jena.Cf2SkosJena;
import org.mmisw.watchdog.cf.skosapi.Cf2SkosSkosApi;

/**
 * Main program for CF conversion.
 * 
 * @author Carlos Rueda
 */
public class Cf {
	private static final String DEFAULT_INPUT = "file:src/main/resources/input/cf-standard-name-table.xml";
	private static final String DEFAULT_OUTPUT = "src/main/resources/output/cf.owl";
	private static final String DEFAULT_NS = "http://mmisw.org/ont/cf/";
	private static final String DEFAULT_IMPL = "jena";

	
	/**
	 * Main program for CF conversion.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		new Cf(args);
	}
	
	private Cf(String[] args) throws Exception {
		if ( args.length > 0 && args[0].matches(".*help") ) {
			_usage(null);
		}
		
		String input = DEFAULT_INPUT;
		String output = DEFAULT_OUTPUT;
		String ns = DEFAULT_NS;
		String impl = DEFAULT_IMPL;
		
		int arg = 0;
		for ( ; arg < args.length && args[arg].startsWith("--"); arg++ ) {
			if ( args[arg].equals("--input") ) {
				input = args[++arg]; 
			}
			else if ( args[arg].equals("--ns") ) {
				ns = args[++arg]; 
			}
			else if ( args[arg].equals("--output") ) {
				output = args[++arg]; 
			}
			else if ( args[arg].equals("--impl") ) {
				impl = args[++arg]; 
			}
		}
		if ( arg < args.length ) {
			_usage("Unexpected arguments");
		}
		
		ICf2Skos creator;
		if ( impl.equalsIgnoreCase("jena")) {
			creator = new Cf2SkosJena();
		}
		else if ( impl.equalsIgnoreCase("skosapi")) {
			creator = new Cf2SkosSkosApi();
		}
		else {
			throw new RuntimeException("No implementation available for " +impl);
		}
		
		_convert(creator, input, ns, output);
	}

	private void _usage(String msg) {
		if ( msg == null ) {
			System.out.println(
					"USAGE: " +getClass().getName()+ " [options]\n" +
					"  options:\n" +
					"    --input <url>         (" +DEFAULT_INPUT+ ")\n" +
					"    --ns <uri>            (" +DEFAULT_NS+ ")\n" +
					"    --output <filename>   (" +DEFAULT_OUTPUT+ ")\n" +
					"    --impl [jena|skosapi] (" +DEFAULT_IMPL+ ")\n" +
					"");
			System.exit(0);
		}
		else {
			System.err.println("Error: " +msg);
			System.err.println("Try " +getClass().getName()+ " --help\n");
			System.exit(1);
		}
	}

	/**
	 * Does the conversion using the given creator object.
	 */
	private void _convert(ICf2Skos creator, String input, String ns, String output) throws Exception {
		creator.setInput(DEFAULT_INPUT);
		creator.setNamespace(DEFAULT_NS);
		creator.convert();
		creator.save(DEFAULT_OUTPUT);
	}
	
}
