package org.mmisw.vine.gwt.client;

import java.util.HashMap;
import java.util.Map;

import org.mmisw.vine.gwt.client.rpc.OntologyInfo;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestionEvent;
import com.google.gwt.user.client.ui.SuggestionHandler;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class OntologySelection extends VerticalPanel {
	
	private  MainPanel mainPanel;
	private CellPanel vp = new VerticalPanel();
	
	OntologySelection(MainPanel mainPanel) {
		super();
		this.mainPanel = mainPanel;
		
		
		VerticalPanel layout = new VerticalPanel();
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(layout);
	    add(decPanel);

	    layout.setSpacing(5);
		
		
		CellPanel hp = new HorizontalPanel();
		layout.add(hp);
		
		hp.add(new HTML("Working ontologies:"));
		
		PushButton addButton = new PushButton("Add...");
		addButton.setTitle("Allows to add a working ontology");
		hp.add(addButton);
		
		layout.add(vp);
		
		
		char id = 'A';
		for ( OntologyInfo s : Main.workingUris ) {
			vp.add(new HTML("<b>" +id+ "</b>: " + s.getUri()));
			id++;
		}
		
		addButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				addVocabulary();
			}

		});
	}
	
	private void ontologySelected(OntologyInfo ontologyInfo, MyDialog popup) {
		char id = (char) ((int) 'A' + Main.workingUris.size());
		String uri = ontologyInfo.getUri();
		
		vp.add(new HTML("<b>" +id+ "</b>: " 
				+ "<a target=\"_blank\" href=\"" +uri+ "\">" +uri+ "</a>" 
				+ " -- "
				+ "<i>" +ontologyInfo.getDisplayLabel()+ "</i>"
		));
		
		mainPanel.notifyWorkingOntologyAdded(ontologyInfo, popup);
	}

	
	/**
	 * Allows the user to choose a vocabulary that is not yet a working one, and
	 * the loads it as a working vocabulary.
	 */
	private void addVocabulary() {
		//
		// Use a SuggestBox wirh a MultiWordSuggestOracle.
		//
		
		MultiWordSuggestOracle oracle = new MultiWordSuggestOracle("/ :"); 
		
		// A map from a suggestion to its corresponding OntologyInfo:
		final Map<String,OntologyInfo> suggestions = new HashMap<String,OntologyInfo>();
		
		for ( int index = 0, count = Main.allUris.size(); index < count; index++ ) {
			OntologyInfo ontologyInfo = Main.allUris.get(index);
			if ( Main.workingUris.contains(ontologyInfo) ) {
				// do not add any suggestion for an entry that is already in the workingUris
				continue;
			}
			
			String lab = ontologyInfo.getDisplayLabel();
			String uri = ontologyInfo.getUri();
			
			// include a star as a convenience to see the whole list if the user types in a star: 
			String suggestion = "* " +uri+ " : " +lab;
			
			suggestions.put(suggestion, ontologyInfo);
			oracle.add(suggestion);
		}

		final SuggestBox box = new SuggestBox(oracle);
		box.setWidth("500px");
		
		
		HorizontalPanel hp = new HorizontalPanel();
		final MyDialog popup = new MyDialog(hp);
		hp.add(box);
		
		box.addEventHandler(new SuggestionHandler() {
			public void onSuggestionSelected(SuggestionEvent event) {
				String suggestion = event.getSelectedSuggestion().getReplacementString();
				OntologyInfo ontologyInfo = suggestions.get(suggestion);
				ontologySelected(ontologyInfo, popup);
			}
		});

		popup.setText("Select vocabulary to load");
		popup.center();

		// use a timer to request for focus in the suggest-box:
		new Timer() {
			public void run() {
				box.setFocus(true);
			}
		}.schedule(500);
		    
		popup.show();
	}

}
