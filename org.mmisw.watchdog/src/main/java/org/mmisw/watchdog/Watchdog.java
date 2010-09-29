package org.mmisw.watchdog;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.mmisw.ont.client.util.HttpUtil;
import org.mmisw.watchdog.onts.cf.Cf;
import org.mmisw.watchdog.onts.sweet.Sweet;
import org.mmisw.watchdog.onts.udunits.Udunits;

/**
 * Main dispatcher program.
 * 
 * @author Carlos Rueda
 */
public class Watchdog {

	/**
	 * A base class for the dispatched programs.
	 */
	public abstract static class BaseProgram {
		
		public abstract void run(String[] args) throws Exception;
		
		protected void _log(String msg) {
			String prefix = "[" +getClass().getSimpleName()+ "] ";
			System.out.println(prefix +msg.replaceAll("\n", "\n" +prefix));
		}
		
		/** assumed that never returns */
		protected abstract void _usage(String msg);
		
		protected String _getInputContents(URL inputUrl) throws Exception {
			_log("Loading " +inputUrl);
			String inputContents = HttpUtil.getAsString(inputUrl.toString());
			return inputContents;
		}

		protected File _prepareWorkspace(String workspace) {
			File workspaceDir = new File(workspace);
			if ( workspaceDir.exists() ) {
				if (  ! workspaceDir.isDirectory() ) {
					_usage("workspace exists but it's not a directory");
				}
			}
			else {
				if ( ! workspaceDir.mkdirs() ) {
					_usage("Cannot create workspace directory: " +workspaceDir);
				}
				_log(workspaceDir+ ": directory created.");
			}
			return workspaceDir;
		}

		protected String _prepareNamespace(String namespace) {
			char separator;
			if ( namespace.matches(".*(/|#)") ) {
				separator = namespace.charAt(namespace.length() - 1); 
			}
			else {
				separator = '/';
			}
			// make sure, namespace ends with the obtained separator:
			namespace = namespace.replaceAll("(/|#)+$", "") + separator;
			return namespace;
		}

		protected void _reportProps(Map<String, String> props) {
			for ( Entry<String, String> entry : props.entrySet() ) {
				_log(String.format("\t%20s : %s", entry.getKey(), entry.getValue()));
			}		
		}

		/** Used to create download filename and conversion output filename
		 * @return nx[0] = n ame w/o extension
		 *         nx[1] = extension including dot, if extension appears
		 */
		protected String[] _getFilenameAndExtension(URL inputUrl) {
			String[] nx = { "", "" };
			String filePortion = inputUrl.getFile();
			File file = new File(filePortion);
			String name = file.getName();
			int idx = name.lastIndexOf('.');
			if ( idx < 0 ) {
				nx[0] = name;
			}
			else {
				nx[0] = name.substring(0, idx);
				nx[1] = name.substring(idx);
			}
			return nx;
		}

	}
	
	private static Map<String,BaseProgram> programs = new LinkedHashMap<String,BaseProgram>();
	
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
	
	/** Never returns */
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
		BaseProgram program = programs.get(programName);
		if ( program == null ) {
			_usage("Unrecognized program: " +programName);
		}
		
		String[] programArgs = new String[args.length - 1];
		System.arraycopy(args, 1, programArgs, 0, programArgs.length);
		program.run(programArgs);
	}
}