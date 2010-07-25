package org.mmisw.watchdog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.mmisw.watchdog.cf.ICf2Skos;
import org.mmisw.watchdog.cf.jena.Cf2SkosJena;
import org.mmisw.watchdog.cf.skosapi.Cf2SkosSkosApi;
import org.mmisw.watchdog.util.WUtil;

/**
 * Main program for CF conversion.
 * 
 * @author Carlos Rueda
 */
public class Cf {
	/** Default input URI */
	private static final String DEFAULT_INPUT = 
		"http://cf-pcmdi.llnl.gov/documents/cf-standard-names/standard-name-table/current/cf-standard-name-table.xml";
	
	/** Template of output file */
	private static final String DEFAULT_OUTPUT = "${workspace}/${basename}-${version_number}.owl";
	
	/** Default namespace for resulting ontology */
	private static final String DEFAULT_NAMESPACE = "http://mmisw.org/ont/mmi/cf/parameter/";
	
	/** Default implementation code */
	private static final String DEFAULT_IMPL = "skosapi";

	private static final boolean DEFAULT_FORCE = false;

	
	/**
	 * Main program for CF conversion.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		new Cf().run(args);
	}
	
	private void _usage(String msg) {
		if ( msg == null ) {
			System.out.println(
					"USAGE: " +getClass().getName()+ " --ws <directory> [options]\n" +
					"  options:\n" +
					"    --input <url>         (" +DEFAULT_INPUT+ ")\n" +
					"    --ns <uri>            (" +DEFAULT_NAMESPACE+ ")\n" +
					"    --output <filename>   (" +DEFAULT_OUTPUT+ ")\n" +
					"    --force               (" +DEFAULT_FORCE+ ")\n" +
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

	private void run(String[] args) throws Exception {
		if ( args.length == 0 || args[0].matches(".*help") ) {
			_usage(null);
		}
		
		String workspace = null;
		String input = DEFAULT_INPUT;
		String output = null;
		boolean force = DEFAULT_FORCE;
		String namespace = DEFAULT_NAMESPACE;
		String impl = DEFAULT_IMPL;
		
		int arg = 0;
		for ( ; arg < args.length && args[arg].startsWith("--"); arg++ ) {
			if ( args[arg].equals("--ws") ) {
				workspace = args[++arg]; 
			}
			else if ( args[arg].equals("--input") ) {
				input = args[++arg]; 
			}
			else if ( args[arg].equals("--ns") ) {
				namespace = args[++arg]; 
			}
			else if ( args[arg].equals("--output") ) {
				output = args[++arg]; 
			}
			else if ( args[arg].equals("--force") ) {
				force = true;
			}
			else if ( args[arg].equals("--impl") ) {
				impl = args[++arg]; 
			}
		}
		if ( arg < args.length ) {
			_usage("Unexpected arguments");
		}
		if ( workspace == null ) {
			_usage("Missing required --ws parameter");
		}
		
		URL inputUrl = new URL(input);
		
		String inputContents = _getInputContents(inputUrl);
		
		File workspaceDir = _prepareWorkspace(workspace);
		
		namespace = _prepareNamespace(namespace);
		ICf2Skos creator = _prepareCreator(impl);
		
		Map<String, String> props = _convert(creator, workspaceDir, inputUrl, inputContents, namespace, output, force);
		
		for ( Entry<String, String> entry : props.entrySet() ) {
			_log("\t " +entry.getKey()+ ": " +entry.getValue());
		}
		
		String version_number = props.get("version_number");
		_writeOutputs(creator, workspaceDir, inputUrl, inputContents, version_number, output, force);
	}

	private void _log(String msg) {
		System.out.println(msg);
	}

	private String _getInputContents(URL inputUrl) throws Exception {
		_log("Loading " +inputUrl);
		String inputContents = WUtil.getAsString(inputUrl);
		return inputContents;
	}

	private ICf2Skos _prepareCreator(String impl) {
		if ( impl.equalsIgnoreCase("jena")) {
			return new Cf2SkosJena();
		}
		else if ( impl.equalsIgnoreCase("skosapi")) {
			return new Cf2SkosSkosApi();
		}
		else {
			_usage("No implementation available for " +impl);
		}
		return null;
	}

	private String _prepareNamespace(String namespace) {
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

	private File _prepareWorkspace(String workspace) {
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


	/** Used to create download filename and conversion output filename
	 * @return nx[0] = n ame w/o extension
	 *         nx[1] = extension including dot, if extension appears
	 */
	private String[] _getFilenameAndExtension(URL inputUrl) {
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

	
	/**
	 * Does the conversion using the given creator object.
	 */
	private Map<String, String> _convert(ICf2Skos creator, File workspaceDir, 
			URL inputUrl, String inputContents, 
			String namespace, String output, boolean force
	) throws Exception {
		
		creator.setInput(inputContents);
		creator.setNamespace(namespace);
		Map<String, String> props = creator.convert();
		return props;
	}
	
	/**
	 * Writes the outputs
	 */
	private void _writeOutputs(ICf2Skos creator, File workspaceDir, 
			URL inputUrl, String inputContents, String version_number,
			String output, boolean force
	) throws Exception {
		
		String[] nx = _getFilenameAndExtension(inputUrl);
		String downloadName = nx[0] + "-" +version_number + nx[1];
		File downloadFile = new File(workspaceDir, downloadName);
		
		if ( downloadFile.exists() ) {
			if ( force ) {
				_log("Overwriting " +downloadFile);
			}
			else {
				_log(downloadFile+ " already exists");
				_log("Exiting. No output written.");
				return;
			}
		}
		
		// save downloaded file:
		IOUtils.copy(new StringReader(inputContents), new FileOutputStream(downloadFile));
		_log(downloadFile+ ": input saved");
		
		// save result of conversion:
		if ( output == null ) {
			output = nx[0] + "-" +version_number + ".owl";
		}
		File outputFile = new File(workspaceDir, output);
		
		creator.save(outputFile.toString());
		_log(outputFile+ ": resulting ontology saved");
	}
	
}
