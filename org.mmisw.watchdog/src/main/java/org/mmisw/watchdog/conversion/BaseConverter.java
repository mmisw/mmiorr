package org.mmisw.watchdog.conversion;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jdom.Element;



/**
 * IConversor base implementation.
 * 
 * @author Carlos Rueda
 */
public abstract class BaseConverter implements IConverter {
	
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
	 * Gets the value of an entity and puts the corresp. entry in the props map.
	 * @param element
	 * @param propName
	 */
	protected void _getProperty(Element element, String propName) {
		Iterator<?> iterator = element.getChildren(propName).iterator();
		if ( iterator.hasNext() ) {
			Element ele = (Element)iterator.next();
			String propValue = ele.getTextNormalize();
			props.put(propName, propValue);
		}
	}


	
}
