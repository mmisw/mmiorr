package org.mmisw.watchdog.cf;

import java.util.Map;


/**
 * CF-to-SKOS conversion interface.
 * 
 * @author Carlos Rueda
 */
public interface ICf2Skos {
	
	public void setNamespace(String namespace);
	
	public void setInput(String inputContents);
	
	/** Returns some properties of the input including version number */
	public Map<String,String> convert() throws Exception;
	
	public void save(String outputFile) throws Exception;

}
