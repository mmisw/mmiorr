package org.mmisw.ontmd.gwt.client.vine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.ontmd.gwt.client.vine.util.TLabel;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;

import com.google.gwt.user.client.DOM;
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

/**
 * The panel for the display/selection of the ontologies whose entities
 * can be/are mapped.
 * 
 * @author Carlos Rueda
 */
public class OntologySelection extends VerticalPanel {
	
	private  VineEditorPanel mainPanel;
	private CellPanel vp = new VerticalPanel();
	
	OntologySelection(VineEditorPanel mainPanel) {
		super();
		this.mainPanel = mainPanel;
		
		
		VerticalPanel layout = new VerticalPanel();
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(layout);
	    add(decPanel);

	    layout.setSpacing(5);
		
		
	    HorizontalPanel hp = new HorizontalPanel();
	    hp.setVerticalAlignment(ALIGN_MIDDLE);
		layout.add(hp);
		
		if ( mainPanel.isReadOnly() ) {
			hp.add(new TLabel("Mapped ontologies:", 
					"The ontologies where the mapped entities where taken from. " +
					"<br/>" +
					"These ontologies are given " +
					"codes, starting from 'A', to identify them in " +
					"the rest of the VINE interface. "
			));
		}
		else {
			hp.add(new TLabel("Working ontologies:", 
					"This section lists the ontologies whose entities can be mapped. " +
					"Use the \"Add\" button to add a working ontology. " +
					"<br/>" +
					"These ontologies are given " +
					"codes, starting from 'A', to identify them in " +
					"the rest of the VINE interface. "
			));
			
			final PushButton addButton = new PushButton("Add...");
			addButton.setTitle("Allows to add a working ontology");
			DOM.setElementAttribute(addButton.getElement(), "id", "my-button-id");
			addButton.addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					int x = addButton.getAbsoluteLeft();
					int y = addButton.getAbsoluteTop();
					addVocabulary(x, y + 20);
				}
			});
			hp.add(addButton);
		}
		
		layout.add(vp);
		
		int nn = 0;
		for ( String uri : VineMain.getWorkingUris() ) {
			char code = (char) ((int) 'A' + (nn++));
			RegisteredOntologyInfo ontologyInfo = VineMain.getRegisteredOntologyInfo(uri);
			_addWorkingUriHtml(code, uri, ontologyInfo.getDisplayLabel());
		}
		
	}
	
	void ontologySucessfullyLoaded(char code, RegisteredOntologyInfo ontologyInfo) {
		String uri = ontologyInfo.getUri();
		_addWorkingUriHtml(code, uri, ontologyInfo.getDisplayLabel());
	}

	private void _addWorkingUriHtml(char code, String uri, String label) {
		vp.add(new HTML("<b>" +code+ "</b>: " 
				+ "<a target=\"_blank\" href=\"" +uri+ "\">" +uri+ "</a>" 
				+ " -- "
				+ "<i>" +label+ "</i>"
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
		
		// A map from a suggestion to its corresponding RegisteredOntologyInfo:
		final Map<String,RegisteredOntologyInfo> suggestions = new HashMap<String,RegisteredOntologyInfo>();
		
		List<RegisteredOntologyInfo> allUris = VineMain.getAllUris();
		for ( int index = 0, count = allUris.size(); index < count; index++ ) {
			RegisteredOntologyInfo ontologyInfo = allUris.get(index);
			if ( VineMain.containsWorkingUri(ontologyInfo) ) {
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
		hp.add(new TLabel("Ontology URI:", 
				"Select the ontology to include in the list of working ontologies. " +
				"<br/>" +
				"As you type, URIs are displayed according to matching components in the " +
				"URI or the associated title."
		));
		hp.add(box);
		
		box.addEventHandler(new SuggestionHandler() {
			public void onSuggestionSelected(SuggestionEvent event) {
				String suggestion = event.getSelectedSuggestion().getReplacementString();
				RegisteredOntologyInfo ontologyInfo = suggestions.get(suggestion);
				mainPanel.notifyWorkingOntologyAdded(OntologySelection.this, ontologyInfo, popup);
			}
		});
		
		// we use the star (*) to show the whole list of vocabs. If the user enters something
		// different, then remove the star:
		box.addKeyboardListener(new KeyboardListener() {
			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				// FIXME: if the user wants to use the down arrow to pick a suggestion after
				// entering *, it cannot do it!
				if ( keyCode != '*' && box.getText().trim().equals("*") ) {
					box.setText("");
				}
			}
			public void onKeyDown(Widget sender, char keyCode, int modifiers) {}
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {}
		});

		popup.setText("Select a vocabulary");
//		hp.add(new HTML("Elements are displayed as you type. Enter * to see the full list."));

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
