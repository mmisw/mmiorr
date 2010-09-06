package org.mmisw.orrportal.gwt.client.util.table;

/**
 * Interface to get column values from a table row.
 * 
 * @author Carlos Rueda
 */
public interface IRow {

	/**
	 * Gets the value in a column.
	 * 
	 * @param colName Column name
	 * @return the value
	 */
	String getColValue(String colName);
	
	/**
	 * Tells whether the value of a column is in HTML
	 * 
	 * @param colName Column name
	 * @return true iff value in HTML
	 */
	boolean isHtml(String colName);
}
