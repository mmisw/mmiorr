package org.mmisw.vine.gwt.client;

import java.util.ArrayList;
import java.util.List;

import org.mmisw.vine.gwt.client.rpc.OntologyInfo;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel to select the vocabularies to be used in the search.
 * 
 * @author Carlos Rueda
 */
public class SearchVocabularySelection extends VerticalPanel {
	
	private CellPanel buttons = new HorizontalPanel();
	
	
	SearchVocabularySelection(int searchIndex) {
		super();
		CellPanel hp = new HorizontalPanel();
		add(hp);

		HTML label = new HTML("Search the following ontologies:");
		hp.add(label);
		
		String tooltip = "Select the working ontologies to search";
		label.setTitle(tooltip);
		buttons.setTitle(tooltip);
		
		hp.add(buttons);
		
		setToggleButtons(searchIndex);
		
	}
	
	/**
	 * Returns a list of the Main.workingUris that are selected for search.
	 * @return
	 */
	List<OntologyInfo> getSelectedVocabularies() {
		List<OntologyInfo> selectedUris = new ArrayList<OntologyInfo>();
		int count = buttons.getWidgetCount();
		assert count == Main.getWorkingUris().size();
		
		List<OntologyInfo> workingUris = new ArrayList<OntologyInfo>(Main.getWorkingUris().values());
		
		for ( int i = 0; i < count; i++ ) {
			if ( ((ToggleButton) buttons.getWidget(i)).isDown() ) {
				selectedUris.add( workingUris.get(i) );
			}
		}
		return selectedUris;
	}

	/**
	 * Updates the toggle buttons according to the current list of working ontologies.
	 * @param searchIndex
	 */
	void setToggleButtons(int searchIndex) {
		buttons.clear();
		int idx = 0;
		for ( OntologyInfo s : Main.getWorkingUris().values() ) {
			char id = s.getCode();
			final ToggleButton sel = new ToggleButton("" +id);
			DOM.setElementAttribute(sel.getElement(), "id", "my-button-id");
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
			
			idx++;
		}
		
		if ( idx == 0 ) {
			buttons.add(new HTML(" <font color=\"gray\"><i>(no working ontologies)</i></font>"));
		}
		
	}

}
