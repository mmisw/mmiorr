package org.mmisw.iserver.gwt.client.rpc;

import java.util.List;

/**
 * Info to create a vocabulary.
 * @author Carlos Rueda
 */
public class CreateVocabularyInfo extends CreateOntologyInfo {
	private static final long serialVersionUID = 1L;
	
	private String className;
	
	private List<String> colNames;
	
	
	private List<List<String>> rows;


	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}


	/**
	 * @param className the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}


	/**
	 * @return the colNames
	 */
	public List<String> getColNames() {
		return colNames;
	}


	/**
	 * @param colNames the colNames to set
	 */
	public void setColNames(List<String> colNames) {
		this.colNames = colNames;
	}


	/**
	 * @return the rows
	 */
	public List<List<String>> getRows() {
		return rows;
	}


	/**
	 * @param rows the rows to set
	 */
	public void setRows(List<List<String>> rows) {
		this.rows = rows;
	}


}
