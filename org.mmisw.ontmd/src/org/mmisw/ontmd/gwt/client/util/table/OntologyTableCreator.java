package org.mmisw.ontmd.gwt.client.util.table;

import org.mmisw.ontmd.gwt.client.util.table.ontab.OntologyTable;
import org.mmisw.ontmd.gwt.client.util.table.ontab.OntologyTableSc;


public class OntologyTableCreator {
	
	public static IOntologyTable create(IQuickInfo quickInfo, boolean isVersionsTable) {
		if ( true ) {
			return new OntologyTableSc(quickInfo, isVersionsTable);
		}
		else {
			return new OntologyTable(quickInfo, isVersionsTable);
		}
	}
	

	private OntologyTableCreator() {}
}
