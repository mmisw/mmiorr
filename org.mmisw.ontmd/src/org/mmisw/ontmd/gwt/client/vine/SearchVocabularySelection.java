package org.mmisw.ontmd.gwt.client.vine;

import java.util.ArrayList;
import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.vine.util.TLabel;

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
		HorizontalPanel hp = new HorizontalPanel();
		hp.setVerticalAlignment(ALIGN_MIDDLE);
		add(hp);

		
		hp.add(new TLabel("Search the following ontologies:", 
				"Check the working ontologies you want to search. " +
				"Each found entity will be abbreviated by using the corresponding " +
				"ontology code given in the working ontologies section."
		));
		
		
		hp.add(buttons);
		
		setToggleButtons(searchIndex);
		
	}
	
	/**
	 * Returns a list of the Main.workingUris that are selected for search.
	 * @return
	 */
	List<RegisteredOntologyInfo> getSelectedVocabularies() {
		List<RegisteredOntologyInfo> selectedUris = new ArrayList<RegisteredOntologyInfo>();
		int count = buttons.getWidgetCount();
		assert count == VineMain.getWorkingUris().size();
		
		for ( int i = 0; i < count; i++ ) {
			if ( ((ToggleButton) buttons.getWidget(i)).isDown() ) {
				
				String uri = VineMain.getWorkingUris().get(i);
				RegisteredOntologyInfo roi = VineMain.getRegisteredOntologyInfo(uri);
				if ( roi != null ) {
					selectedUris.add(roi);
				}
				else {
					char code = (char) ('A' + i);
					Main.log("Button " +code+ " selected but not registed uri: " +uri);
				}
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
		for ( String uri : VineMain.getWorkingUris() ) {
			char id = (char) ('A' + idx);
			final ToggleButton sel = new ToggleButton("" +id);
			DOM.setElementAttribute(sel.getElement(), "id", "my-button-id");
			buttons.add(sel);
			
			RegisteredOntologyInfo s = VineMain.getRegisteredOntologyInfo(uri);
			if ( s != null ) {
				sel.setTitle(s.getDisplayLabel());
				sel.addClickListener(new ClickListener() {
					public void onClick(Widget sender) {
						// TODO update some variable indicating the selected ontologies for search
					}
				});
				if ( searchIndex == idx ) {
					sel.setDown(true);
				}
			}
			else {
				sel.setEnabled(false);
				sel.setTitle("This is not a registered ontology");
			}
			
			idx++;
		}
		
		if ( idx == 0 ) {
			buttons.add(new HTML(" <font color=\"gray\"><i>(no working ontologies)</i></font>"));
		}
		
	}

}
