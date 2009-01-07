package org.mmisw.vine.gwt.client;

import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VocabularyForm extends VerticalPanel {
	
	private SearchVocabularySelection vocabularySelection;
	
	VocabularyForm(int searchIndex) {
		super();
		
		VerticalPanel layout = new VerticalPanel();
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(layout);
	    
	    add(decPanel);
		
	    layout.setSpacing(5);
	    layout.add(vocabularySelection = new SearchVocabularySelection(searchIndex));
	    
	    ResultsForm resultsForm = new ResultsForm();
	    
	    layout.add(new SearchGroup(vocabularySelection, resultsForm));
	    
		layout.add(resultsForm);
	    
		layout.add(new ResourceViewer());

		layout.add(new MappingsPane());
	}
	
	void notifyWorkingOntologyAdded(int searchIndex) {
		vocabularySelection.setToggleButtons(searchIndex);
	}


}
