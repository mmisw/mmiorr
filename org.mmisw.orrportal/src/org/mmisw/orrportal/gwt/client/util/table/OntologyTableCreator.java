package org.mmisw.orrportal.gwt.client.util.table;

import org.mmisw.orrportal.gwt.client.util.table.ontab.OntologyTable;
import org.mmisw.orrportal.gwt.client.util.table.ontab.OntologyTableSc;


public class OntologyTableCreator {
	
	private static final boolean USE_SMARTGWT = false;


	public static IOntologyTable create(IQuickInfo quickInfo, boolean isVersionsTable) {
		
		if ( USE_SMARTGWT ) {
			return new OntologyTableSc(quickInfo, isVersionsTable);
		}
		else {
			return new OntologyTable(quickInfo, isVersionsTable);
		}
	}
	

	private OntologyTableCreator() {}
}
