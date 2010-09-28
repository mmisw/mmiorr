package org.mmisw.watchdog.onts.sweet;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.mmisw.ont.client.util.HttpUtil;
import org.mmisw.watchdog.Watchdog.BaseProgram;
import org.mmisw.watchdog.orr.RegisterOntology;
import org.mmisw.watchdog.orr.RegisterOntology.RegistrationResult;
import org.mmisw.watchdog.util.WdConstants;
import org.mmisw.watchdog.util.jena.AdHocUtil;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * Dispatches registration of SWEET ontologies.
 * 
 * @author Carlos Rueda
 */
public class Sweet extends BaseProgram {

	/** 
	 * URI (URL) of the ontology that imports all the SWEET ontologies, 
	 * "http://sweet.jpl.nasa.gov/2.0/sweetAll.owl" 
	 */
	public static final String DEFAULT_SWEET_ALL_URI = "http://sweet.jpl.nasa.gov/2.0/sweetAll.owl";

	
	/**
	 * Main program for CF conversion.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		new Sweet().run(args);
	}
	
	/** Never returns */
	private void _usage(String msg) {
		if ( msg == null ) {
			System.out.println(
					"USAGE:\n" +
					"   " +getClass().getSimpleName()+ " --ws <directory> [options]\n" +
					"  --ws <directory>           workspace directory (required)\n" +
					"  options: (default value in parenthesis)\n" +
					"    --sweetAll <URL>           (" +DEFAULT_SWEET_ALL_URI+ ")\n" +
					"    --listImported <filename>  Lists the imported ontologies in sweetAll to given file in workspace\n" +
					"    --download <sweetUri>      downloads the given ontology. Start with @ to indicate file with list of ontologies\n" +
					"    --register <sweetUri>      registers the given ontology. Start with @ to indicate file with list of ontologies\n" +
					"  for registration:\n" +
					"    --username <username>      (" +WdConstants.ORR_DEFAULT_USERNAME+ ")\n" +
					"    --password <password>\n" +
					"    --formAction <action>      (" +WdConstants.DEFAULT_DIREG_SERVICE_URL+ ")\n" +
					"  Operation performed depends on provided arguments.\n" +
					"");
			System.exit(0);
		}
		else {
			System.err.println("Error: " +msg);
			System.err.println("Try " +getClass().getSimpleName()+ " --help\n");
			System.exit(1);
		}
	}

	private String workspace = null;
	private File workspaceDir = null;
	private String sweetAllUri = DEFAULT_SWEET_ALL_URI;
	private String listImported = null;
	private String downloadSpec = null;
	private String registerOrUnregisterSpec = null;
	private boolean doRegister = true;

	private OntModel sweetAllModel;
	private List<String> sweetUris;
	

	// ORR registration
	private String orrUsername = WdConstants.ORR_DEFAULT_USERNAME;
	private String orrPassword = null;
	private String orrFormAction = WdConstants.DEFAULT_DIREG_SERVICE_URL;
			
	
	/**
	 * Executes this program.
	 */
	public void run(String[] args) throws Exception {
		if ( args.length == 0 || args[0].matches(".*help") ) {
			_usage(null);
		}
		
	
		int arg = 0;
		for ( ; arg < args.length && args[arg].startsWith("--"); arg++ ) {
			if ( args[arg].equals("--ws") ) {
				workspace = args[++arg]; 
			}
			else if ( args[arg].equals("--sweetAll") ) {
				sweetAllUri = args[++arg]; 
			}
			else if ( args[arg].equals("--listImported") ) {
				listImported = args[++arg]; 
			}
			else if ( args[arg].equals("--download") ) {
				downloadSpec = args[++arg]; 
			}
			else if ( args[arg].equals("--register") ) {
				registerOrUnregisterSpec = args[++arg];
				doRegister = true;
			}
			else if ( args[arg].equals("--unregister") ) {
				registerOrUnregisterSpec = args[++arg];
				doRegister = false;
			}
			
			// ORR
			else if ( args[arg].equals("--username") ) {
				orrUsername = args[++arg]; 
			}
			else if ( args[arg].equals("--password") ) {
				orrPassword = args[++arg]; 
			}
			else if ( args[arg].equals("--formAction") ) {
				orrFormAction = args[++arg]; 
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
			_usage("workspace directory is required.  Indicate --ws <dir>");
			return;
		}
		
		workspaceDir = new File(workspace);
		
		sweetAllModel = AdHocUtil.loadModel(sweetAllUri, false);
		sweetUris = _getSweetUris(sweetAllModel, false);
		
		if ( listImported != null ) {
			_listImported();
		}
		
		if ( downloadSpec != null ) {
			_download();
		}
				
		if ( registerOrUnregisterSpec != null ) {
			if ( orrPassword == null ) {
				_usage("--register requires unsername and password of user registering the ontology");
			}
			_registerOrUnregister();
		}
	}

	private void _listImported() throws IOException {
		if ( workspaceDir == null ) {
			_usage("--listImported: workspace directory required (indicate --ws <dir>)");
		}
		File file = new File(workspaceDir, listImported);
		_log("Listing " +sweetUris.size()+ " imported sweet ontology URIs to " +file);
		PrintWriter writer = new PrintWriter(file);
		writer.println("# Imported ontologies in " +sweetAllUri);
		IOUtils.writeLines(sweetUris, "\n", writer); 
		writer.flush();
		
	}

	@SuppressWarnings("unchecked")
	private void _registerOrUnregister() throws Exception {
		if ( registerOrUnregisterSpec.charAt(0) == '@' ) {
			String listFilename = registerOrUnregisterSpec.replaceAll("^@+", "");
			File listFile = new File(workspaceDir, listFilename);
			FileReader reader = new FileReader(listFile);
			List<String> list = IOUtils.readLines(reader);
			_log("_register: Reading list from " +listFile);
			for ( String line : list ) {
				line = line.trim();
				if ( ! line.startsWith("#") && line.trim().length() > 0 ) { 
					String ontologyUri = line.trim();
					if ( doRegister ) {
						_registerOntology(ontologyUri);
					}
					else {
						_unregisterOntology(ontologyUri);
					}
				}
			}
		}
		else {
			if ( doRegister ) {
				_registerOntology(registerOrUnregisterSpec);
			}
			else {
				_unregisterOntology(registerOrUnregisterSpec);
			}
		}
	}

	private void _registerOntology(String ontologyUri) throws Exception {
		URL url = new URL(ontologyUri);
		String ontName = new File(url.getPath()).getName();
		String contents = HttpUtil.getAsString(ontologyUri, "application/rdf+xml");
		_log("Registering " +ontologyUri+ " (" + contents.length() + ")");
		
		RegistrationResult result = RegisterOntology.register(orrUsername, orrPassword, ontologyUri, ontName, contents, "", orrFormAction);
		_log("RegistrationResult: status=" +result.status+ " message: " +result.message);
	}

	private void _unregisterOntology(String ontologyUri) throws Exception {
		URL url = new URL(ontologyUri);
		String ontName = new File(url.getPath()).getName();
		String contents = HttpUtil.getAsString(ontologyUri, "application/rdf+xml");
		_log("Unregistering " +ontologyUri);
		
		RegistrationResult result = RegisterOntology.register(orrUsername, orrPassword, ontologyUri, ontName, contents, "", orrFormAction);
		_log("UnregistrationResult: status=" +result.status+ " message: " +result.message);
	}
	
	@SuppressWarnings("unchecked")
	private void _download() throws Exception {
		if ( downloadSpec.charAt(0) == '@' ) {
			String listFilename = downloadSpec.replaceAll("^@+", "");
			File listFile = new File(workspaceDir, listFilename);
			FileReader reader = new FileReader(listFile);
			List<String> list = IOUtils.readLines(reader);
			_log("_download: Reading list from " +listFile);
			for ( String line : list ) {
				line = line.trim();
				if ( ! line.startsWith("#") && line.trim().length() > 0 ) { 
					String ontologyUri = line.trim();
					_downloadOntology(ontologyUri);
				}
			}
		}
		else {
			_downloadOntology(downloadSpec);
		}
	}
	
	private void _downloadOntology(String ontologyUri) throws Exception {
		URL url = new URL(ontologyUri);
		String ontName = new File(url.getPath()).getName();
		File fileOut = new File(workspaceDir + "/download/" + ontName);
		String contents = HttpUtil.getAsString(ontologyUri, "application/rdf+xml");
		_log("Downloading " +ontologyUri+ " (" + contents.length() + ") to " +fileOut);
		
		FileWriter writer = new FileWriter(fileOut);
		IOUtils.copy(new StringReader(contents), writer);
		writer.flush();
	}

	private List<String> _getSweetUris(OntModel sweetAllModel, boolean closure) {
		Set<String> sweetUris = sweetAllModel.listImportedOntologyURIs(closure);
		List<String> list = new ArrayList<String>();
		list.addAll(sweetUris);
		Collections.sort(list);
		return list;
	}


}
