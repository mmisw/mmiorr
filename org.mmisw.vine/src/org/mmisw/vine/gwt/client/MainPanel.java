package org.mmisw.vine.gwt.client;

import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MainPanel extends VerticalPanel {
	
	MainPanel() {
		super();
		
		VerticalPanel layout = new VerticalPanel();
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(layout);
	    
	    add(decPanel);

	    
		// TODO: GUI to select working ontologies.
		// for now, choose the first 2:
		int num = Math.min(2, Main.allUris.size());
		for ( int i = 0;  i < num; i++ ) {
			Main.workingUris.add(Main.allUris.get(i));
		}

		
		OntologySelection ontSel = new OntologySelection();
		ontSel.setBorderWidth(1);
	    layout.add(ontSel);
	    layout.add(new MultiPageEditor());

	    layout.setCellHorizontalAlignment(ontSel, ALIGN_RIGHT);
	}

}
