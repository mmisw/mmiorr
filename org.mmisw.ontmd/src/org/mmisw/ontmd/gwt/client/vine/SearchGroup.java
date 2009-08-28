package org.mmisw.ontmd.gwt.client.vine;

import java.util.ArrayList;
import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.BaseOntologyData;
import org.mmisw.iserver.gwt.client.rpc.EntityInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyData;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.PropValue;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.vine.util.TLabel;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
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
		hp0.setVerticalAlignment(ALIGN_MIDDLE);
		add(hp0);
		hp0.setSpacing(3);
		
		hp0.add(new TLabel("Search for:", 
				"Enter the string you want to search in the selected ontologies and click " +
				"the search button. " +
				"Leave the field blank to retrieve all associated entities. " +
				"<br/>" +
				"Check the REGEX button if you are entering a regular expression for your search " +
				"(not implemented yet)."
		));

		
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
		
		PushButton b = new PushButton(VineMain.images.search().createImage());
		b.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				search();
			}
		});
		
		hp0.add(b);

		hp0.add(regex);
	}

	private void search() {
		if ( VineMain.getWorkingUris() == null || VineMain.getWorkingUris().size() == 0 )  {
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
		List<RegisteredOntologyInfo> selectedVocabs = vocabularySelection.getSelectedVocabularies();
		List<EntityInfo> entities = search(text, selectedVocabs );
		
		Main.log("search: retrieved " +entities.size()+ " terms");
		if ( text.length() > 0 ) {
			oracle.add(text);
		}
		searchResultsForm.updateEntities(entities);
	}
	
	private List<EntityInfo> search(String text, List<RegisteredOntologyInfo> uris) {
		
		// TODO use a parameter to apply case-sensitive or not
		text = text.toLowerCase();
		
		// TODO get this flags from parameters
		boolean useLocalName = true;
		boolean useProps = true;
		
		
		List<EntityInfo> foundEntities = new ArrayList<EntityInfo>();
		for (RegisteredOntologyInfo ont : uris ) {
			
			if ( ont.getError() != null ) {
				Main.log("Error: " +ont.getError());
				continue;
			}
			
			OntologyData ontologyData = ont.getOntologyData();
			if ( ontologyData == null ) {
				Main.log("search: data not yet retrieved for " +ont.getUri());
				continue;
			}
			
			BaseOntologyData baseOntologyData = ontologyData.getBaseOntologyData();
			
			Object[] entityArray = {
					baseOntologyData.getClasses(),
					baseOntologyData.getProperties(),
					baseOntologyData.getIndividuals(),
			};
			
			for (Object object : entityArray) {
				
				@SuppressWarnings("unchecked")
				List<? extends EntityInfo> entities = (List<? extends EntityInfo>) object;


				if ( entities == null || entities.size() == 0 ) {
					continue;
				}

				for ( EntityInfo entityInfo : entities ) {
					boolean add = false;

					// check localName
					if ( (useLocalName && entityInfo.getLocalName().toLowerCase().indexOf(text) >= 0) ) {
						add = true;
					}

					// check props
					if ( !add && useProps ) {
						List<PropValue> props = entityInfo.getProps();
						for ( PropValue pv : props ) {
							String str = pv.getValueName();
							add = str != null && str.toLowerCase().indexOf(text) >= 0;
							if ( add ) {
								break;
							}
						}
					}

					if ( add ) {
						foundEntities.add(entityInfo);
					}
				}
			} 
		}
		return foundEntities;
	}


}
