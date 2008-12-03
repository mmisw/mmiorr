package org.mmisw.vine.gwt.client;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class VocabularySelection extends VerticalPanel {
	
	VocabularySelection(int searchIndex) {
		super();
		
		CellPanel hp = new HorizontalPanel();
		add(hp);

		hp.add(new HTML("Search the following ontologies:"));
		
		char id = 'A';
		int idx = 0;
		for ( String s : Main.workingUris ) {
			final ToggleButton sel = new ToggleButton("" +id);
			sel.addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					// TODO
				}
			});
			hp.add(sel);
			if ( searchIndex == idx ) {
				sel.setDown(true);
			}
			
			id++;
			idx++;
		}
		
	}

}
