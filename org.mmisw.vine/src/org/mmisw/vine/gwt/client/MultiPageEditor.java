package org.mmisw.vine.gwt.client;

import org.mmisw.vine.gwt.client.rpc.OntologyInfo;

import com.google.gwt.user.client.ui.TabPanel;


public class MultiPageEditor extends TabPanel {
	
	private MapperPage mapperPage;
	
	MultiPageEditor() {
		super();
		add(mapperPage = new MapperPage(), "Map editor");
		add(new MappingResultsPage(), "Mapping results");
		
	    selectTab(0);

	}

	void notifyWorkingOntologyAdded(OntologyInfo ontologyInfo) {
		mapperPage.notifyWorkingOntologyAdded(ontologyInfo);
	}

}
