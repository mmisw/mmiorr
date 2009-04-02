package org.mmisw.vine.gwt.client;

import org.mmisw.vine.gwt.client.rpc.OntologyInfo;

import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Holds the mapper page.
 * 
 * @author Carlos Rueda
 */
public class MultiPageEditor {
	
	private Widget widget;
	private MapperPage mapperPage;
	
	MultiPageEditor() {
		super();
		
		mapperPage = new MapperPage();
		
		if ( true ) {
			widget = mapperPage;
		}
		else {
			TabPanel tabPanel = new TabPanel();
			widget = tabPanel;
			tabPanel.add(mapperPage, "Map editor");
			tabPanel.add(new MappingResultsPage(), "Mapping results");
			tabPanel.selectTab(0);
		}

	}
	
	Widget getWidget() {
		return widget;
	}

	/** Call this to notify that a new ontology has been added to the working list */
	void notifyWorkingOntologyAdded(OntologyInfo ontologyInfo) {
		mapperPage.notifyWorkingOntologyAdded(ontologyInfo);
	}

}
