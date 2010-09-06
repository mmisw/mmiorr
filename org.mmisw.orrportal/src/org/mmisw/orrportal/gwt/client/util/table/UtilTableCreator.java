package org.mmisw.orrportal.gwt.client.util.table;

import java.util.List;

import org.mmisw.orrportal.gwt.client.util.table.utab.UtilTable;

public class UtilTableCreator {
	
	public static IUtilTable create(List<String> colNames) {
		// TODO
		return new UtilTable(colNames);
	}
	
	public static IUtilTable create(String... colLabels) {
		// TODO
		return new UtilTable(colLabels);
	}
	

	private UtilTableCreator() {}
}
