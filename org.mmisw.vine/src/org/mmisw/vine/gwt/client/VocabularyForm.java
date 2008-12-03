package org.mmisw.vine.gwt.client;

import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class VocabularyForm extends VerticalPanel {
	
	VocabularyForm(int searchIndex) {
		super();
		
		VerticalPanel layout = new VerticalPanel();
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(layout);
	    
	    add(decPanel);
		
	    layout.setSpacing(5);
	    layout.add(new VocabularySelection(searchIndex));
	    
	    ResultsForm resultsForm = new ResultsForm();
	    
	    layout.add(new SearchGroup(resultsForm));
	    
		layout.add(resultsForm);
	    
		layout.add(new ResourceViewer());

		layout.add(new MappingsPane());
	}

}
