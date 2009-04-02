package org.mmisw.vine.gwt.client;

import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * @author Carlos Rueda
 */
public class VocabularyForm extends VerticalPanel {
	
	private SearchVocabularySelection vocabularySelection;
	private SearchGroup searchGroup;
	private SearchResultsForm searchResultsForm;
	private ResourceViewer resourceViewer;
	
	
	VocabularyForm(int searchIndex) {
		super();
		
		vocabularySelection = new SearchVocabularySelection(searchIndex);
		resourceViewer = new ResourceViewer();
		searchResultsForm = new SearchResultsForm(resourceViewer);
		searchGroup = new SearchGroup(vocabularySelection, searchResultsForm);
		
		VerticalPanel layout = new VerticalPanel();
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(layout);
	    
	    add(decPanel);
		
	    layout.setSpacing(5);
	    
	    layout.add(vocabularySelection);
	    layout.add(searchGroup);
		layout.add(searchResultsForm);
		layout.add(resourceViewer);
		
//		layout.add(new MappingsPane());
	}
	
	/** Call this to notify that a new ontology has been added to the working list
	 * 
	 * @param searchIndex
	 */
	void notifyWorkingOntologyAdded(int searchIndex) {
		vocabularySelection.setToggleButtons(searchIndex);
	}


}
