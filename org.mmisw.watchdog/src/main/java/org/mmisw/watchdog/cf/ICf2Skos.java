package org.mmisw.watchdog.cf;

import java.net.MalformedURLException;

/**
 * CF to SKOS conversion interface.
 * @author carueda
 */
public interface ICf2Skos {
	
	public void setNamespace(String NS);
	public void setInput(String fileIn) throws MalformedURLException;
	public void convert() throws Exception;
	public void save(String fileOut) throws Exception;

}
