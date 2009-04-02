package org.mmisw.vine.gwt.client;

import java.util.ArrayList;
import java.util.List;

import org.mmisw.vine.gwt.client.rpc.EntityInfo;
import org.mmisw.vine.gwt.client.rpc.OntologyInfo;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Dispatches the search of entities.
 * It uses an oracle to remember the strings of sucessful searches.
 * 
 * @author Carlos Rueda
 */
public class SearchGroup extends VerticalPanel {
	
	private SearchVocabularySelection vocabularySelection;
	private SearchResultsForm searchResultsForm;
	
	private MultiWordSuggestOracle oracle;
	private ToggleButton regex;
	private SuggestBox box;

	SearchGroup(SearchVocabularySelection vocabularySelection, SearchResultsForm searchResultsForm) {
		super();
		this.vocabularySelection = vocabularySelection;
		this.searchResultsForm = searchResultsForm;
		
		HorizontalPanel hp0 = new HorizontalPanel();
		add(hp0);
		hp0.setSpacing(10);
		hp0.add(new HTML("Search for:"));
		
		// TODO implement REGEX search
		regex = new ToggleButton("REGEX");
		DOM.setElementAttribute(regex.getElement(), "id", "my-button-id");
		regex.setTitle("Check this to apply a regular expression search - NOT IMPLEMENTED YET");
		
		
		
		oracle = new MultiWordSuggestOracle("/");  
		
		box = new SuggestBox(oracle);
		box.setWidth("250px");
		hp0.add(box);
		
		box.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				search();
			}
		});
		
		PushButton b = new PushButton(Main.images.search().createImage());
		b.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				search();
			}
		});
		
		hp0.add(b);

		hp0.add(regex);
	}

	private void search() {
		if ( Main.getWorkingUris() == null || Main.getWorkingUris().size() == 0 )  {
			Window.alert("No selected vocabularies to search");
			return;
		}
		
		final String text = box.getText().trim();
		
		Main.log("searching: " +text);
		searchResultsForm.searching();
		
		new Timer() {
			public void run() {
				executeSearch(text);
			}
		}.schedule(300);

//		DeferredCommand.addCommand(new Command() {
//			public void execute() {
//				executeSearch(text);
//			}
//		});
	}
	
	private void executeSearch(String text) {
		List<OntologyInfo> selectedVocabs = vocabularySelection.getSelectedVocabularies();
		Main.log("search: vocabularySelection " +vocabularySelection);
		Main.log("search: selectedVocabs " +selectedVocabs);
		List<EntityInfo> entities = search(text, selectedVocabs );
		
		Main.log("search: retrieved " +entities.size()+ " terms");
		if ( text.length() > 0 ) {
			oracle.add(text);
		}
		searchResultsForm.updateEntities(entities);
	}
	
	private List<EntityInfo> search(String text, List<OntologyInfo> uris) {
		
		// TODO use a parameter to apply case-sensitive or not
		text = text.toLowerCase();
		
		// TODO get this flags from parameters
		boolean useLocalName = true;
		boolean useLabel = true;
		boolean useComment = true;
		
		
		List<EntityInfo> foundEntities = new ArrayList<EntityInfo>();
		for (OntologyInfo ont : uris ) {
			List<EntityInfo> entities = ont.getEntities();
			if ( entities == null || entities.size() == 0 ) {
				continue;
			}
			
			for ( EntityInfo entityInfo : entities ) {
				boolean add = false;
				
				// check localName
				if ( (useLocalName && entityInfo.getLocalName().toLowerCase().indexOf(text) >= 0) ) {
					add = true;
				}
				
				// check label
				if ( !add && useLabel ) {
					String str = entityInfo.getDisplayLabel();
					add = str != null && str.toLowerCase().indexOf(text) >= 0;
				}
				
				// check comment
				if ( !add && useComment ) {
					String str = entityInfo.getComment();
					add = str != null && str.toLowerCase().indexOf(text) >= 0;
				}
				
				if ( add ) {
					entityInfo.setCode(ont.getCode());
					foundEntities.add(entityInfo);
				}
			}
		}
		return foundEntities;
	}


}
