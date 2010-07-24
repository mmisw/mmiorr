package org.mmisw.watchdog.cf;

import java.net.MalformedURLException;

/**
 * CF-to-SKOS conversion interface.
 * 
 * @author Carlos Rueda
 */
public interface ICf2Skos {
	
	public void setNamespace(String namespace);
	public void setInput(String inputUri) throws MalformedURLException;
	public void convert() throws Exception;
	public void save(String outputFile) throws Exception;

}
