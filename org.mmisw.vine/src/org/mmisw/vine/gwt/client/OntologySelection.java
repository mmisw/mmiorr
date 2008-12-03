package org.mmisw.vine.gwt.client;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class OntologySelection extends VerticalPanel {
	
	OntologySelection() {
		super();
		
		// TODO: GUI to select working ontologies.
		// for now, choose the first 2:
		int num = Math.min(2, Main.allUris.size());
		for ( int i = 0;  i < num; i++ ) {
			Main.workingUris.add(Main.allUris.get(i));
		}

		
		CellPanel hp = new HorizontalPanel();
		add(hp);
		
		hp.add(new HTML("Working ontologies:"));
		
		CellPanel vp = new VerticalPanel();
		add(vp);
		
		
		for ( String s : Main.workingUris ) {
			vp.add(new Label(s));
		}
	}

}
