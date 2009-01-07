package org.mmisw.vine.gwt.client;

import java.util.ArrayList;
import java.util.List;

import org.mmisw.vine.gwt.client.rpc.EntityInfo;
import org.mmisw.vine.gwt.client.rpc.OntologyInfo;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SuggestBox;
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
	private ResultsForm resultsForm;
	
	private MultiWordSuggestOracle oracle;
	private CheckBox cb;
	private SuggestBox box;

	SearchGroup(SearchVocabularySelection vocabularySelection, ResultsForm resultsForm) {
		super();
		this.vocabularySelection = vocabularySelection;
		this.resultsForm = resultsForm;
		
		HorizontalPanel hp0 = new HorizontalPanel();
		add(hp0);
		hp0.setSpacing(10);
		hp0.add(new HTML("Search for:"));
		cb = new CheckBox("REGEX");
		cb.setTitle("Check this to apply a regular expression search - NOT IMPLEMENTED YET");
		
		// TODO implement REGEX search
		cb.setEnabled(false);
		
		
		oracle = new MultiWordSuggestOracle("/");  
		
		box = new SuggestBox(oracle);
		box.setWidth("250px");
		hp0.add(box);
		
		PushButton b = new PushButton(Main.images.search().createImage());
		box.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				search();
			}
		});
		b.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				search();
			}
		});
		
		hp0.add(b);

		hp0.add(cb);
	}

	private void search() {
		final String text = box.getText().trim();
		
		Main.log("searching: " +text);
		resultsForm.searching();
		
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
		List<String> terms = search(text, vocabularySelection.getSelectedVocabularies());
		
		Main.log("search: retrieved " +terms.size()+ " terms");
		if ( text.length() > 0 ) {
			oracle.add(text);
		}
		resultsForm.updateTerms(terms);
	}
	
	private List<String> search(String text, List<OntologyInfo> uris) {
		
		// TODO get this flags from parameters
		boolean useLocalName = true;
		boolean useLabel = true;
		boolean useComment = true;
		
		
		List<String> terms = new ArrayList<String>();
		for (OntologyInfo ont : uris ) {
			char code = ont.getCode();
			List<EntityInfo> entities = ont.getEntities();
			if ( entities == null || entities.size() == 0 ) {
				continue;
			}
			
			for ( EntityInfo entityInfo : entities ) {
				boolean add = false;
				
				// check localName
				if ( (useLocalName && entityInfo.getLocalName().indexOf(text) >= 0) ) {
					add = true;
				}
				
				// check label
				if ( !add && useLabel ) {
					String str = entityInfo.getDisplayLabel();
					add = str != null && str.indexOf(text) >= 0;
				}
				
				// check comment
				if ( !add && useComment ) {
					String str = entityInfo.getComment();
					add = str != null && str.indexOf(text) >= 0;
				}
				
				if ( add ) {
					terms.add(code+ ":" +entityInfo.getLocalName());
				}
			}
		}
		return terms;
	}


}
