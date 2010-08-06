package org.mmisw.watchdog.conversion;

import java.util.Map;


/**
 * Conversion interface.
 * 
 * @author Carlos Rueda
 */
public interface IConverter {
	
	public void setNamespace(String namespace);
	
	public void setInput(String inputContents);
	
	/** Returns some properties of the input */
	public Map<String,String> convert() throws Exception;
	
	public void save(String outputFile) throws Exception;

}
