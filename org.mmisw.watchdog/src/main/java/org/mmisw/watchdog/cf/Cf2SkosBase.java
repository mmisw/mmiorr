package org.mmisw.watchdog.cf;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * ICf2Skos base implementation.
 * 
 * @author Carlos Rueda
 */
public abstract class Cf2SkosBase implements ICf2Skos {
	
	public void setInput(String inputUri) throws MalformedURLException {
		inputUrl = new URL(inputUri);
	}
	
	public void setNamespace(String NS) {
		this.namespace = NS;
	}

	public void convert() throws Exception {
		_doConvert();
	}

	public void save(String outputFile) throws Exception {
		this.outputFile = outputFile;
		_doSave();
	}

	
	///////////////////////////////////////////////////////////////////////////////
	// protected
	///////////////////////////////////////////////////////////////////////////////
	
	protected String namespace;
	
	protected URL inputUrl;
	
	protected String outputFile;
	
	protected void _log(String msg) {
		System.out.println(msg);
	}
	
	protected abstract void _doConvert() throws Exception ;

	protected abstract void _doSave() throws Exception ;
	
}
