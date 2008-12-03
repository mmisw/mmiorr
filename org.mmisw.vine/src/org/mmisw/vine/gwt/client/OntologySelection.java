package org.mmisw.vine.gwt.client;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class OntologySelection extends VerticalPanel {
	
	OntologySelection() {
		super();
		
		CellPanel hp = new HorizontalPanel();
		add(hp);
		
		hp.add(new HTML("Working ontologies:"));
		
		hp.add(new PushButton("Add..."));
		
		CellPanel vp = new VerticalPanel();
		add(vp);
		
		
		char id = 'A';
		for ( String s : Main.workingUris ) {
			vp.add(new HTML("<b>" +id+ "</b>: " + s));
			id++;
		}
	}

}
