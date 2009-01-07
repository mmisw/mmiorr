package org.mmisw.vine.gwt.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.vine.gwt.client.rpc.OntologyInfo;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PopupPanel;
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
		
		final PushButton addButton = new PushButton("Add...");
		addButton.setTitle("Allows to add a working ontology");
		hp.add(addButton);
		
		layout.add(vp);
		
		
		for ( OntologyInfo s : Main.getWorkingUris() ) {
			vp.add(new HTML("<b>" +s.getCode()+ "</b>: " + s.getUri()));
		}
		
		addButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				int x = addButton.getAbsoluteLeft();
				int y = addButton.getAbsoluteTop();
				addVocabulary(x, y + 20);
			}
		});
	}
	
	void ontologySucessfullyLoaded(OntologyInfo ontologyInfo) {
		char code = ontologyInfo.getCode();
		String uri = ontologyInfo.getUri();
		
		vp.add(new HTML("<b>" +code+ "</b>: " 
				+ "<a target=\"_blank\" href=\"" +uri+ "\">" +uri+ "</a>" 
				+ " -- "
				+ "<i>" +ontologyInfo.getDisplayLabel()+ "</i>"
		));
	}

	
	/**
	 * Allows the user to choose a vocabulary that is not yet a working one, and
	 * the loads it as a working vocabulary.
	 * @param x Position for the popup
	 * @param y Position for the popup 
	 */
	private void addVocabulary(final int x, final int y) {
		//
		// Use a SuggestBox wirh a MultiWordSuggestOracle.
		//
		
		MultiWordSuggestOracle oracle = new MultiWordSuggestOracle("/ :"); 
		
		// A map from a suggestion to its corresponding OntologyInfo:
		final Map<String,OntologyInfo> suggestions = new HashMap<String,OntologyInfo>();
		
		List<OntologyInfo> allUris = Main.getAllUris();
		for ( int index = 0, count = allUris.size(); index < count; index++ ) {
			OntologyInfo ontologyInfo = allUris.get(index);
			if ( Main.containsWorkingUri(ontologyInfo) ) {
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
		
		
		CellPanel hp = new VerticalPanel();
		final MyDialog popup = new MyDialog(hp) {
			// Mydialog closes when ENTER is pressed-- avoid that:
			public boolean onKeyUpPreview(char key, int modifiers) {
				if ( key == KeyboardListener.KEY_ESCAPE ) {
					hide();
					return false;
				}
			    return true;
			  }
		};
		hp.add(box);
		
		box.addEventHandler(new SuggestionHandler() {
			public void onSuggestionSelected(SuggestionEvent event) {
				String suggestion = event.getSelectedSuggestion().getReplacementString();
				OntologyInfo ontologyInfo = suggestions.get(suggestion);
				mainPanel.notifyWorkingOntologyAdded(OntologySelection.this, ontologyInfo, popup);
			}
		});
		
		// we use the star (*) to show the whole list of vocabs. If the user enters something
		// different, then remove the star:
		box.addKeyboardListener(new KeyboardListener() {
			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
				if ( keyCode != '*' && box.getText().trim().equals("*") ) {
					box.setText("");
				}
			}

			public void onKeyPress(Widget sender, char keyCode, int modifiers) {}
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {}
		});

		popup.setText("Select a vocabulary");
		hp.add(new HTML("Elements are displayed as you type. Enter * to see the full list."));

		// use a timer to request for focus in the suggest-box:
		new Timer() {
			public void run() {
				box.setFocus(true);
			}
		}.schedule(500);
		    
		
		popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				int left = (Window.getClientWidth() - offsetWidth) / 2;
				int top = Math.min(y, (Window.getClientHeight() - offsetHeight) / 2);
				popup.setPopupPosition(left, top);
			}
		});
	}

}
