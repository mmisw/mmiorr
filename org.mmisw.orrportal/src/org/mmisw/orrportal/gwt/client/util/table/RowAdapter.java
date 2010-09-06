package org.mmisw.orrportal.gwt.client.util.table;

/**
 * Base class for IRow.
 * 
 * @author Carlos Rueda
 */
public abstract class RowAdapter implements IRow {
	/**
	 * returns false
	 */
	public boolean isHtml(String colName) {
		return false;
	}
}
