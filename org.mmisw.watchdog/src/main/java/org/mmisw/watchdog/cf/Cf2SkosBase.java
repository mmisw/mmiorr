package org.mmisw.watchdog.cf;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jdom.Element;



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
	
	/** entries in input XML */
	protected int numEntries;

	protected Map<String,String> props;
	
	
	protected void _log(String msg) {
		System.out.println(msg);
	}
	
	protected abstract void _doConvert() throws Exception ;

	protected abstract void _doSave() throws Exception ;
	
	/**
	 * Gets the value of an entity and put the corresp. entry in the props map.
	 * @param standard_name_table
	 * @param propName
	 */
	protected void _getProperty(Element standard_name_table, String propName) {
		Iterator<?> iterator = standard_name_table.getChildren(propName).iterator();
		if ( iterator.hasNext() ) {
			Element ele = (Element)iterator.next();
			String propValue = ele.getTextNormalize();
			props.put(propName, propValue);
		}
	}


	
}
