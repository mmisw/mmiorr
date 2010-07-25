package org.mmisw.watchdog.cf;

import java.util.LinkedHashMap;
import java.util.Map;



/**
 * ICf2Skos base implementation.
 * 
 * @author Carlos Rueda
 */
public abstract class Cf2SkosBase implements ICf2Skos {
	
	public void setInput(String inputContents) {
		this.inputContents = inputContents;
	}
	
	public void setNamespace(String NS) {
		this.namespace = NS;
	}

	public Map<String,String> convert() throws Exception {
		props = new LinkedHashMap<String, String>();
		_doConvert();
		return props;
	}

	public void save(String outputFile) throws Exception {
		this.outputFile = outputFile;
		_doSave();
	}

	
	///////////////////////////////////////////////////////////////////////////////
	// protected
	///////////////////////////////////////////////////////////////////////////////
	
	protected String namespace;
	
	protected String inputContents;
	
	protected String outputFile;
	
	protected Map<String,String> props;
	
	protected void _log(String msg) {
		System.out.println(msg);
	}
	
	protected abstract void _doConvert() throws Exception ;

	protected abstract void _doSave() throws Exception ;
	
}
