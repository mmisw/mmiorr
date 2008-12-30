package org.mmisw.vine.gwt.client;

import org.mmisw.vine.gwt.client.rpc.OntologyInfo;

import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MainPanel extends VerticalPanel {
	
	private MultiPageEditor multiPageEditor;
	
	MainPanel() {
		super();
		
		VerticalPanel layout = new VerticalPanel();
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(layout);
	    
	    add(decPanel);

		OntologySelection ontSel = new OntologySelection(this);
		ontSel.setBorderWidth(1);
	    layout.add(ontSel);
	    layout.add(multiPageEditor = new MultiPageEditor());

	    layout.setCellHorizontalAlignment(ontSel, ALIGN_CENTER);
	}

	void notifyWorkingOntologyAdded(OntologyInfo ontologyInfo) {
		Main.workingUris.add(ontologyInfo);
		multiPageEditor.notifyWorkingOntologyAdded(ontologyInfo);
	}
}
