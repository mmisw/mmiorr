package org.mmisw.ontmd.gwt.client;

import org.mmisw.ontmd.gwt.client.rpc.OntologyInfo;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The main metadata panel.
 * 
 * @author Carlos Rueda
 */
public class DataPanel extends VerticalPanel {

	/**
	 * Creates the metadata panel
	 * @param mainPanel
	 * @param editing true for the editing interface; false for the vieweing interface.
	 */
	DataPanel(MainPanel mainPanel, boolean editing) {
		super();
		setWidth("800");

		add(new HTML("here I am"));
	}
	
	void enable(boolean enabled) {
		// TODO
	}
	
	void updateWith(OntologyInfo ontologyInfo) {
		add(new Label("DataPanel.updateWith: " +ontologyInfo.getFullPathCsv()));
	}
}
