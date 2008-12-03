package org.mmisw.vine.gwt.client;

import com.google.gwt.user.client.ui.TabPanel;


public class MultiPageEditor extends TabPanel {
	
	MultiPageEditor() {
		super();
		add(new MapperPage(), "Map editor");
		add(new MappingResultsPage(), "Mapping results");
		
	    selectTab(0);

	}

}
