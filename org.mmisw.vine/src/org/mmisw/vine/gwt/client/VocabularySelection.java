package org.mmisw.vine.gwt.client;

import org.mmisw.vine.gwt.client.rpc.OntologyInfo;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class VocabularySelection extends VerticalPanel {
	
	private CellPanel buttons = new HorizontalPanel();
	
	VocabularySelection(int searchIndex) {
		super();
		CellPanel hp = new HorizontalPanel();
		add(hp);

		HTML label = new HTML("Search the following ontologies:");
		hp.add(label);
		label.setTitle("Select the working ontologies to search");
		
		hp.add(buttons);
		
		setToggleButtons(searchIndex);
		
	}
	
	void setToggleButtons(int searchIndex) {
		buttons.clear();
		char id = 'A';
		int idx = 0;
		for ( OntologyInfo s : Main.workingUris ) {
			final ToggleButton sel = new ToggleButton("" +id);
			sel.setTitle(s.getDisplayLabel());
			sel.addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					// TODO update some variable indicating the selected ontologies for search
				}
			});
			buttons.add(sel);
			if ( searchIndex == idx ) {
				sel.setDown(true);
			}
			
			id++;
			idx++;
		}
		
		if ( idx == 0 ) {
			buttons.add(new HTML(" <i>(no working ontologies)</i>"));
		}
		
	}

}
