package org.mmisw.watchdog;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.mmisw.watchdog.onts.cf.Cf;
import org.mmisw.watchdog.onts.sweet.Sweet;
import org.mmisw.watchdog.onts.udunits.Udunits;

/**
 * Main dispatcher program.
 * 
 * @author Carlos Rueda
 */
public class Watchdog {
	
	public interface IProgram {
		public void run(String[] args) throws Exception;
	}
	
	private static Map<String,IProgram> programs = new LinkedHashMap<String,IProgram>();
	
	static {
		programs.put(Cf.class.getSimpleName(), new Cf());
		programs.put(Udunits.class.getSimpleName(), new Udunits());
		programs.put(Sweet.class.getSimpleName(), new Sweet());
	}

	
	/**
	 * Main dispatcher program.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		new Watchdog().run(args);
	}
	
	private void _usage(String msg) {
		if ( msg == null ) {
			System.out.println(
					"USAGE: " +getClass().getName()+ " program [options]\n" +
					"  program:  one of: " +programs.keySet()+ "\n" +
					"  options:  depend on the specific program.  Call with --help\n" +
					"");
			System.exit(0);
		}
		else {
			System.err.println("Error: " +msg);
			System.err.println("Try " +getClass().getName()+ " --help\n");
			System.exit(1);
		}
	}

	private void run(String[] args) throws Exception {
		if ( args.length == 0 || args[0].matches(".*help") ) {
			_usage(null);
		}
		
		String programName = args[0];
		IProgram program = programs.get(programName);
		if ( program == null ) {
			_usage("Unrecognized program: " +programName);
		}
		
		String[] programArgs = new String[args.length - 1];
		System.arraycopy(args, 1, programArgs, 0, programArgs.length);
		program.run(programArgs);
	}
}