package org.mmisw.ont.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.OntConfig;
import org.mmisw.ont.OntServlet;
import org.mmisw.ont.OntConfig.Prop;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


/**
 * Helper class to generate events to the Google analytics engine.
 * It uses the Rhino implementation of JavaScript and the EnvJs environment.
 * 
 * If {@link OntConfig.Prop#GA_UA_NUMBER} is not defined, this class behaves as
 * a "null object".
 * 
 * <p>
 * Thread-safety: This class is not strictly thread-safe, but it is "effectively thread-safe"
 * in conjunction with {@link OntServlet} and other callers:
 * 
 * <ul>
 * <li> {@link #getInstance()} is only called by {@link OntServlet} at creation time
 * <li> {@link #init()} is only called by {@link OntServlet#init()}.
 * <li> internal state does not change anymore after initialization
 * </ul>
 * 
 * @author Carlos Rueda
 */
public class Analytics {
	
	private static final String GA_HTML = "ga.html";
	
	private static final String ENV_JS = "env.rhino.1.2.js";

	private static final String GA_JS = "ga.js";

	private static Analytics instance = null;
	
	/** Called by {@link OntServlet} and creation time. */
	public static Analytics getInstance() {
		if ( instance == null ) {
			instance = new Analytics();
		}
		return instance;
	}
	
	
	private final Log log = LogFactory.getLog(Analytics.class);
	
	/** these fields are set at {@link #init()} and not changed anymore */ 
	private boolean enabled;
	private String gaSnippet = null;
	private File gaDirectory;
	
	private Analytics() {
	}
	
	/**
	 * Initializes this module.
	 * <ul>
	 * <li> loads the GA snippet template
	 * <li> installs the HTML base page
	 * <li> installs the EnvJs script
	 * </ul>
	 */
	public void init() {
		Prop[] props = { 
				OntConfig.Prop.GA_UA_NUMBER, 
				OntConfig.Prop.GA_DOMAIN_NAME,
				OntConfig.Prop.GA_DIR
		};
		for ( Prop prop : props ) {
			String propValue = prop.getValue();
			if ( propValue == null || propValue.trim().length() == 0 ) {
				log.info("Analytics.init: property " +prop.getName()+ " not provided");
				return;
			}
		}
		String gaUaNumber = OntConfig.Prop.GA_UA_NUMBER.getValue();
		String gaDomainName = OntConfig.Prop.GA_DOMAIN_NAME.getValue();
		String gaDir = OntConfig.Prop.GA_DIR.getValue();
		enabled = _loadGaSnippet(gaUaNumber, gaDomainName, gaDir)
		       && _prepareGaDirectory(gaDir)
		       && _installHtml()
		       && _installEnvJs()
		;
		log.info("Analytics.init: enabled=" +enabled+ " (gaUaNumber = " +gaUaNumber+ ")");
	}
	
	/**
	 * Loads the GA snippet
	 */
	private boolean _loadGaSnippet(String gaUaNumber, String gaDomainName, String gaDir) {
		String original = _loadResource(GA_JS);
		if ( original == null ) {
			return false;
		}
		gaSnippet = original
					.replace("${ga.uanumber}", gaUaNumber)
					.replace("${ga.domainName}", gaDomainName)
					.replace("${ga.dir}", gaDir)
		;
		log.info(GA_JS+ ": " +gaSnippet);	
		return true;
	}

	/**
	 * 
	 */
	private boolean _prepareGaDirectory(String gaDir) {
		gaDirectory = new File(gaDir);
		if ( ! gaDirectory.isDirectory() ) {
			if ( ! gaDirectory.mkdirs() ) {
				log.error(OntConfig.Prop.GA_DIR+ " could not create directory.");
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 */
	private String _loadResource(String resource) {
		InputStream rsr = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
		try {
			return IOUtils.toString(rsr);
		}
		catch (IOException e) {
			log.error("could not load resource " +resource, e);
			return null;
		}
	}

	/**
	 * 
	 */
	private boolean _installResource(String contents, String to) {
		File outputFile = new File(gaDirectory, to);
		OutputStream output = null;
		try {
			output = new FileOutputStream(outputFile);
			IOUtils.copy(new StringReader(contents), output);
			return true;
		}
		catch (IOException e) {
			log.error("Could not install contents to " +outputFile, e);
		}
		finally {
			IOUtils.closeQuietly(output);
		}
		return false;
	}
	
	/**
	 * 
	 */
	private boolean _installEnvJs() {
		String contents = _loadResource(ENV_JS);
		if ( contents != null ) {
			return _installResource(contents, ENV_JS);
		}
		return false;
	}

	/**
	 * 
	 */
	private boolean _installHtml() {
		String contents = _loadResource(GA_HTML);
		if ( contents != null ) {
			return _installResource(contents, GA_HTML);
		}
		return false;
	}
	
	
	/**
	 * Triggers a trackPageview.
	 * @param pageName
	 */
	public void trackPageview(String pageName) {
		if ( !enabled ) {
			return;
		}
		
		Context cx = Context.enter();
		try {
			cx.setOptimizationLevel(-1);
			Scriptable scope = cx.initStandardObjects();
			
			_addAdHocSymbols(cx, scope);
			
			_loadEnvJsScript(cx, scope);
			_loadGaScript(cx, scope);
			

			// TODO temporarily prefixing this with "/uri:", for now to distinguish this event
			// from the ones generated by the ORR module. The general scheme TBD.
			pageName = "/uri:" +pageName;
			_trackPageview(cx, scope, pageName);
		}
		catch (Throwable thr) {
			log.error("_trackPageview: Error.", thr);
		}
		finally {
			Context.exit();
		}
	}
	
	/**
	 * Defines some symbols.
	 * @param cx
	 * @param scope
	 */
	private void _addAdHocSymbols(Context cx, Scriptable scope) {
		try {
			Object wrappedLog = Context.javaToJS(log, scope);
			ScriptableObject.putProperty(scope, "__ont_log", wrappedLog);
			
			String source = 
//				"function print(str) { java.lang.System.out.println(\"Analytics.print: \" +str); }\n" +
				"function print(str) { __ont_log.debug(\"Analytics.print: \" +str); }\n" +
				"function __trim__(str) { return (str || \"\").replace( /^\\s+|\\s+$/g, \"\" ); }"
				;
			Object result = cx.evaluateString(scope, source, "<symbols>", 1, null);
			log.debug("_addMissingSymbols: result: " +result);
		}
		catch (Throwable thr) {
			log.warn("_addMissingSymbols error: " +thr.getMessage(), thr);
		}
	}
	
	private void _trackPageview(Context cx, Scriptable scope, String pageName) {
		String gaDir = gaDirectory.getAbsolutePath();
		String source = 
			gaSnippet.replace("${ga.pageName}", pageName) + "\n" +
			"window.location = \"file://" +gaDir+ "/" + GA_HTML+ "\";"
			;
		
		log.debug("_trackPageview: about to evaluate string: [\n" +source+ "\n]");
		
		try {
			Object result = cx.evaluateString(scope, source, GA_JS, 1, null);
			log.debug("_trackPageview: result: " +result);
		}
		catch (Throwable thr) {
			log.warn("_trackPageview error: " +thr.getMessage(), thr);
		}
	}
	
	
	private void _loadEnvJsScript(Context cx, Scriptable scope) throws IOException {
		File file = new File(gaDirectory, ENV_JS);
	    Reader in = new FileReader (file);
	    cx.evaluateReader(scope, in, file.getAbsolutePath(), 1, null);
	    log.debug("_addEnvJsScript: evaluated.");
	}
	
	private void _loadGaScript(Context cx, Scriptable scope) throws IOException {
		URL url = new URL("http://www.google-analytics.com/ga.js");
		InputStream is = url.openStream();
		Reader in = new InputStreamReader(is);
		cx.evaluateReader(scope, in, url.toString(), 1, null);
		log.debug("_addGaScript: evaluated.");
	}
}