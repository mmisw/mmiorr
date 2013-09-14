package org.mmisw.orrportal.gwt.client.vine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mmisw.orrclient.gwt.client.rpc.BaseOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrportal.gwt.client.Orr;
import org.mmisw.orrportal.gwt.client.vine.util.TLabel;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
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
	private final CellPanel workingUrisPanel = new VerticalPanel();
	
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
		
		layout.add(workingUrisPanel);
		
		refreshListWorkingUris();
	}
	
	void refreshListWorkingUris() {
		List<String> workingUris = VineMain.getWorkingUris();
		Orr.log("refreshListWorkingUris: " + workingUris.size());
		workingUrisPanel.clear();
		int nn = 0;
		for ( String uri : workingUris ) {
			String code = VineMain.index2code(nn++);
			BaseOntologyInfo ontologyInfo = VineMain.getOntologyInfo(uri);

			String label = ontologyInfo != null ? ontologyInfo.getDisplayLabel() : uri;
			label = label.replaceAll("&", "&amp;").replaceAll("<", "&lt;");

			workingUrisPanel.add(new HTML("<b>" +code+ "</b>: " 
					+ "<a target=\"_blank\" href=\"" +uri+ "\">" +uri+ "</a>" 
					+ " -- <i>" +label+ "</i>"
			));
		}
	}

	/**
	 * Allows the user to choose a vocabulary that is not yet a working one, and
	 * the loads it as a working vocabulary.
	 * @param x Position for the popup
	 * @param y Position for the popup 
	 */
	private void addVocabulary(final int xx, final int yy) {

		final String width = "700px";
		
		// #194: Can't access all ontologies from VINE drop-down
		// ListBox added as a fix to this issue.
		final ListBox listBox = new ListBox();
		
//		MultiWordSuggestOracle oracle = new MultiWordSuggestOracle("/ :"); 
		
		// A map from a suggestion to its corresponding RegisteredOntologyInfo:
		final Map<String,RegisteredOntologyInfo> suggestions = new HashMap<String,RegisteredOntologyInfo>();
		
		List<BaseOntologyInfo> allUris = VineMain.getAllUris();
		
		for ( int index = 0, count = allUris.size(); index < count; index++ ) {
			BaseOntologyInfo ontologyInfo = allUris.get(index);
			if ( VineMain.containsWorkingUri(ontologyInfo.getUri()) ) {
				// do not add any suggestion for an entry that is already in the workingUris
				continue;
			}
			
			if ( ! (ontologyInfo instanceof RegisteredOntologyInfo)) {
				// only add suggestions for registered ontologies
				continue;
			}
			
			RegisteredOntologyInfo registeredOntologyInfo = (RegisteredOntologyInfo) ontologyInfo;
			
			String lab = ontologyInfo.getDisplayLabel();
			String uri = ontologyInfo.getUri();
			
//			// include a star as a convenience to see the whole list if the user types in a star: 
//			String suggestion = "* " +uri+ " : " +lab;
			String suggestion = uri;
			
			listBox.addItem(uri+ " : " +lab, suggestion);
			suggestions.put(suggestion, registeredOntologyInfo);
//			oracle.add(suggestion);
		}

//		final SuggestBox box = new SuggestBox(oracle);
//		box.setWidth(width);
		
		
		CellPanel hp = new VerticalPanel();
		final MyDialog popup = new MyDialog(hp) {
			public boolean onKeyUpPreview(char key, int modifiers) {
				if ( key == KeyboardListener.KEY_ESCAPE ) {
					hide();
					return false;
				}
			    return true;
			  }
		};
		hp.add(new TLabel("Ontology URI:", 
				"Select or enter the URL of the ontology you want to include in the list of working ontologies. " +
				"<br/>" +
				"As you type, URIs are displayed according to matching components in the " +
				"URI or the associated title."
		));
		
//		hp.add(box);
//		box.addEventHandler(new SuggestionHandler() {
//			public void onSuggestionSelected(SuggestionEvent event) {
//				String suggestion = event.getSelectedSuggestion().getReplacementString();
//				RegisteredOntologyInfo ontologyInfo = suggestions.get(suggestion);
//				mainPanel.notifyWorkingOntologyAdded(OntologySelection.this, ontologyInfo, popup);
//			}
//		});
		
		final TextBox textBox = createTextBox(suggestions, listBox, popup);
		textBox.setWidth(width);
		hp.add(textBox);
		
//		// we use the star (*) to show the whole list of vocabs. If the user enters something
//		// different, then remove the star:
//		box.addKeyboardListener(new KeyboardListener() {
//			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
//				// FIXME: if the user wants to use the down arrow to pick a suggestion after
//				// entering *, it cannot do it!
//				if ( keyCode != '*' && box.getText().trim().equals("*") ) {
//					box.setText("");
//				}
//			}
//			public void onKeyDown(Widget sender, char keyCode, int modifiers) {}
//			
//			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
//				if ( keyCode == KeyboardListener.KEY_ENTER ) {
//					String selectedUri = box.getText().trim();
//					Orr.log("addVocabulary: ENTER: '" + selectedUri + "'");
//					if (selectedUri.length() > 0) {
//						if ( VineMain.containsWorkingUri(selectedUri) ) {
//							// ignore the Enter
//							return;
//						}
//						BaseOntologyInfo ontologyInfo = VineMain.getOntologyInfo(selectedUri);
//						if (ontologyInfo instanceof RegisteredOntologyInfo) {
//							// It is a registered ontology -- load it as a working one:
//							RegisteredOntologyInfo roi = (RegisteredOntologyInfo) ontologyInfo;
//							mainPanel.notifyWorkingOntologyAdded(OntologySelection.this, roi, popup);
//						}
//						else {
//							// try as an external ontology:
//							OntologySelection.this.mainPanel.addExternalOntology(selectedUri, popup);
//						}
//					}
//				}				
//			}
//		});

		popup.setText("Select an ontology");
//		hp.add(new HTML("Elements are displayed as you type. Enter * to see the full list."));

		listBox.setWidth(width);
		listBox.setVisibleItemCount(Math.min(listBox.getItemCount() + 2, 12));
		hp.add(listBox);

//		listBox.addChangeListener(new ChangeListener () {
//			public void onChange(Widget sender) {
//				String value = listBox.getValue(listBox.getSelectedIndex());
//				RegisteredOntologyInfo ontologyInfo = suggestions.get(value);
//				mainPanel.notifyWorkingOntologyAdded(OntologySelection.this, ontologyInfo, popup);
//			}
//		});

		
		
		// use a timer to request for focus in the suggest-box:
		new Timer() {
			public void run() {
//				box.setFocus(true);
				textBox.setFocus(true);
			}
		}.schedule(500);
		    
		
		popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				int left = (Window.getClientWidth() - offsetWidth) / 2;
				int top = yy;
//				int top = Math.min(yy, (Window.getClientHeight() - offsetHeight) / 2);
				popup.setPopupPosition(left, top);
			}
		});
	}
	
	
	private TextBox createTextBox(final Map<String,RegisteredOntologyInfo> suggestions, final ListBox listBox, final MyDialog popup) {
		final TextBox textBox = new TextBox();
		KeyboardListener kb = new KeyboardListenerAdapter() {
			String[] lastEntered = {""};
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				String enteredText = textBox.getText().trim();
				if ( keyCode == KeyboardListener.KEY_ENTER ) {
					Orr.log("ENTER: '" + enteredText + "'");
					if (listBox.getItemCount() > 0) {
						// it means there are items in the listBox that match the entered text.
						// Take the selected item:
						enteredText = listBox.getValue(listBox.getSelectedIndex());
					}
					
					handleEnteredText(enteredText, popup);
				}	
				else if ( sender == textBox && listBox.getItemCount() > 0 && 
						(keyCode == KeyboardListener.KEY_UP || keyCode == KeyboardListener.KEY_DOWN) ) {
					
					if ( keyCode == KeyboardListener.KEY_UP) {
						if (listBox.getSelectedIndex() > 0) {
							listBox.setSelectedIndex(listBox.getSelectedIndex() - 1);
						}
					}
					else if ( keyCode == KeyboardListener.KEY_DOWN) {
						if (listBox.getSelectedIndex() < listBox.getItemCount() - 1) {
							listBox.setSelectedIndex(listBox.getSelectedIndex() + 1);
						}
					}
				}
				else if (! enteredText.equalsIgnoreCase(lastEntered[0])) {
					lastEntered[0] = enteredText;
					updateListBox(suggestions, listBox, enteredText);
				}
			}
			
		};
		
		textBox.addKeyboardListener(kb);
		listBox.addKeyboardListener(kb);
		
		listBox.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				if (listBox.getSelectedIndex() > 0) {
					String enteredText = listBox.getValue(listBox.getSelectedIndex());
					handleEnteredText(enteredText, popup);
				}				
			}
		});
		
		return textBox;	
	}
	
	private void handleEnteredText(String enteredText, final MyDialog popup) {
		if (enteredText.length() > 0) {
			if ( VineMain.containsWorkingUri(enteredText) ) {
				// ignore the Enter
				return;
			}
			BaseOntologyInfo ontologyInfo = VineMain.getOntologyInfo(enteredText);
			if (ontologyInfo instanceof RegisteredOntologyInfo) {
				// It is a registered ontology -- load it as a working one:
				RegisteredOntologyInfo roi = (RegisteredOntologyInfo) ontologyInfo;
				mainPanel.notifyWorkingOntologyAdded(OntologySelection.this, roi, popup);
			}
			else {
				// Try as an external ontology:
				OntologySelection.this.mainPanel.addExternalOntology(enteredText, popup);
			}
		}
	}
	
	private void updateListBox(final Map<String,RegisteredOntologyInfo> suggestions, final ListBox listBox, String enteredText) {
		int selIndex = listBox.getSelectedIndex();
		String currentSelected = selIndex >= 0 ? listBox.getItemText(selIndex) : null;
		
		String[] toks = enteredText.toLowerCase().split("\\s+");
		listBox.clear();
		int newIndexToSelect = -1;
		int index = 0;
		for ( Entry<String, RegisteredOntologyInfo> entry : suggestions.entrySet() ) {
			String uri = entry.getKey();
			RegisteredOntologyInfo roi = entry.getValue();
			String entryText = uri + ": " + roi.getDisplayLabel();
			String entryTextLc = entryText.toLowerCase();
			
			int pos = 0;
			for (String tok : toks) {
				pos = entryTextLc.indexOf(tok, pos);
				if (pos < 0) {
					break;
				}
				else {
					pos += tok.length();
				}
			}
			
			if (pos >= 0) {
				listBox.addItem(entryText, uri);
				
				// update the newIndexToSelect only of there is non-empty text entered:
				if (enteredText.length() > 0 && entryText.equals(currentSelected)) {
					newIndexToSelect = index;
				}
				
				index++;
			}
		}
		

		if (newIndexToSelect >= 0) {
			listBox.setSelectedIndex(newIndexToSelect);
		}
		else if (listBox.getItemCount() >= 1) {
			listBox.setSelectedIndex(0);
		}
	}
}
