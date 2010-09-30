package org.mmisw.watchdog.onts.udunits;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.mmisw.watchdog.Watchdog.BaseProgram;
import org.mmisw.watchdog.conversion.IConverter;
import org.mmisw.watchdog.onts.udunits.jena.UdunitsUnitConverterJena;
import org.mmisw.watchdog.onts.udunits.jena.UdunitsPrefixConverterJena;

/**
 * Main program for UDUnits conversion.
 * 
 * @author Carlos Rueda
 */
public class Udunits extends BaseProgram {
	
	
	// authority: "mmitest" here, which can be adjusted at time of registration.
	private static final String AUTHORITY = "mmitest";
	

	/** the various involved input files and associated converters */
	private enum Input {
		PREFIX("http://www.unidata.ucar.edu/software/udunits/udunits-2/udunits2-prefixes.xml",
				"http://mmisw.org/ont/" +AUTHORITY+ "/udunits2-prefix/",
				new UdunitsPrefixConverterJena()),
				
		BASE    ("http://www.unidata.ucar.edu/software/udunits/udunits-2/udunits2-base.xml",
				"http://mmisw.org/ont/" +AUTHORITY+ "/udunits2-base/",
				new UdunitsUnitConverterJena()),
		
		DERIVED ("http://www.unidata.ucar.edu/software/udunits/udunits-2/udunits2-derived.xml",
				"http://mmisw.org/ont/" +AUTHORITY+ "/udunits2-derived/",
				new UdunitsUnitConverterJena()),
						
		ACCEPTED ("http://www.unidata.ucar.edu/software/udunits/udunits-2/udunits2-accepted.xml",
				"http://mmisw.org/ont/" +AUTHORITY+ "/udunits2-accepted/",
				new UdunitsUnitConverterJena()),
						
		COMMON ("http://www.unidata.ucar.edu/software/udunits/udunits-2/udunits2-common.xml",
				"http://mmisw.org/ont/" +AUTHORITY+ "/udunits2-common/",
				new UdunitsUnitConverterJena()),
				
		;
		
		final String defaultUrl;
		final String defaultNamespace;
		final IConverter creator;

		String url;
		String namespace;
		
		Input(String defaultUrl, String defaultNamespace, IConverter creator) {
			this.defaultUrl = this.url = defaultUrl;
			this.defaultNamespace = this.namespace = defaultNamespace;
			this.creator = creator;
		}
		
		public static void show(boolean defaults) {
			final String format = "%10s  %-50s  %s%n";
			System.out.printf(format,  "-what-",  "-namespace-",  "-url-");	
			for ( Input input : Input.values() ) {
				String what = input.name().toLowerCase();
				if ( defaults ) {
					System.out.printf(format, what, input.defaultNamespace, input.defaultUrl);
				}
				else {
					System.out.printf(format, what, input.namespace, input.url);
				}
			}
		}
	}

	/** Message indicating default output filename  */
	private static final String DEFAULT_OUTPUT_MSG = "${original-basename}.owl (under ${workspace})";

	private static final boolean DEFAULT_FORCE = false;

	

	/** Never returns */
	protected void _usage(String msg) {
		if ( msg == null ) {
			System.out.println(
					"USAGE: " +getClass().getName()+ " --ws <directory> [options]\n" +
					"  --ws <directory>        workspace directory (required)\n" +
					"  options: (default value in parenthesis)\n" +
					"    --input <what> <url>      (*)\n" +
					"    --ns <what> <namespace>   (*)\n" +
					"    --output <filename>       (" +DEFAULT_OUTPUT_MSG+ ")\n" +
					"    --force                   (" +DEFAULT_FORCE+ ")\n" +
					"");
			
			System.out.println("(*) The default inputs are:");
			Input.show(true);
			System.exit(0);
		}
		else {
			System.err.println("Error: " +msg);
			System.err.println("Try " +getClass().getName()+ " --help\n");
			System.exit(1);
		}
	}

	public void run(String[] args) throws Exception {

		if ( args.length == 0 || args[0].matches(".*help") ) {
			_usage(null);
		}
		
		String workspace = null;
		String output = null;
		boolean force = DEFAULT_FORCE;
	
		int arg = 0;
		for ( ; arg < args.length && args[arg].startsWith("--"); arg++ ) {
			if ( args[arg].equals("--ws") ) {
				workspace = args[++arg]; 
			}
			else if ( args[arg].equals("--input") ) {
				String what = args[++arg]; 
				String url = args[++arg]; 
				Input.valueOf(what.toUpperCase()).url = url;
			}
			else if ( args[arg].equals("--ns") ) {
				String what = args[++arg]; 
				String namespace = args[++arg]; 
				Input.valueOf(what.toUpperCase()).namespace = namespace;
			}
			else if ( args[arg].equals("--output") ) {
				output = args[++arg]; 
			}
			else if ( args[arg].equals("--force") ) {
				force = true;
			}
			else {
				_usage("unrecognized parameter: " +args[arg]);
			}
		}
		if ( arg < args.length ) {
			String uargs = "";
			for ( ; arg< args.length; arg++ ) {
				uargs += args[arg] + " ";
			}
			_usage("Unexpected arguments: " +uargs);
		}
		if ( workspace == null ) {
			_usage("Missing required --ws parameter");
		}
		
		System.out.println("Running with these inputs:");
		Input.show(false);
		
		File workspaceDir = _prepareWorkspace(workspace);
		
		
		for ( Input input : Input.values() ) {
			IConverter creator = input.creator;
			URL inputUrl = new URL(input.url);
			String inputContents = _getInputContents(inputUrl);
			
			String namespace = _prepareNamespace(input.namespace);
			
			Map<String, String> props = _convert(creator, inputContents, namespace);
			
			_reportProps(props);
			
			File outputFile = _writeOutputs(
					creator, workspaceDir, inputUrl, inputContents, 
					output, force);
			
			if ( outputFile == null ) {
				// outputs not written. Nothing else to do:
				continue;
			}
		}
		
	}
	
	/**
	 * Does the conversion using the given creator object.
	 */
	private Map<String, String> _convert(
			IConverter creator, 
			String inputContents, 
			String namespace
	) throws Exception {
		
		creator.setInput(inputContents);
		creator.setNamespace(namespace);
		Map<String, String> props = creator.convert();
		return props;
	}
	

	/**
	 * Writes the outputs. Returns the output file iff outputs effectively written.
	 */
	private File _writeOutputs(IConverter creator, File workspaceDir, 
			URL inputUrl, String inputContents,
			String output, boolean force
	) throws Exception {
		
		String[] nx = _getFilenameAndExtension(inputUrl);
		String downloadName = nx[0] + nx[1];
		File downloadFile = new File(workspaceDir, downloadName);
		
		if ( downloadFile.exists() ) {
			if ( force ) {
				_log("Overwriting " +downloadFile);
			}
			else {
				_log(downloadFile+ " already exists.");
				_log("Exiting. No output written.");
				return null;
			}
		}
		
		// save downloaded file:
		IOUtils.write(inputContents, new FileOutputStream(downloadFile), "UTF-8");
		_log(downloadFile+ ": input saved");
		
		// save result of conversion:
		if ( output == null ) {
			output = nx[0] + ".owl";
		}
		File outputFile = new File(workspaceDir, output);
		
		creator.save(outputFile.toString());
		_log(outputFile+ ": resulting ontology saved");
		
		return outputFile;
	}


}
