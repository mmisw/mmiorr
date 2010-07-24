package org.mmisw.watchdog.cf;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * Cf2Skos base implementation.
 * @author carueda
 */
public abstract class Cf2SkosBase implements ICf2Skos {
	
	public void setInput(String fileIn) throws MalformedURLException {
		inputUrl = new URL(fileIn);
	}
	
	public void setNamespace(String NS) {
		this.NS = NS;
	}

	public void convert() throws Exception {
		_doConvert();
	}

	public void save(String fileOut) throws Exception {
		this.fileOut = fileOut;
		_doSave();
	}

	
	///////////////////////////////////////////////////////////////////////////////
	// protected
	///////////////////////////////////////////////////////////////////////////////
	
	protected String NS;
	
	protected URL inputUrl;
	
	protected String fileOut;
	
	protected void _log(String action) {
		System.out.println(action);
	}
	
	protected abstract void _doConvert() throws Exception ;

	protected abstract void _doSave() throws Exception ;
	
	
	///////////////////////////////////////////////////////////////////////////////
	// private
	///////////////////////////////////////////////////////////////////////////////
	

}
