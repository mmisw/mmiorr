package org.mmisw.vine.gwt.client;

import org.mmisw.vine.gwt.client.rpc.OntologyInfo;

import com.google.gwt.user.client.ui.DockPanel;

public class MapperPage extends DockPanel {
	
	VocabularyForm vocabularyFormLeft;
	VocabularyForm vocabularyFormRight;
	
	MapperPage() {
		super();
		
		setSpacing(5);
		setVerticalAlignment(ALIGN_MIDDLE);
		
		int workingOntsSize = Main.workingUris.size();
		int chooseLeft = workingOntsSize > 0 ? 0 : -1;
		int chooseRight = workingOntsSize > 1 ? 1 : chooseLeft;
		add(vocabularyFormLeft = new VocabularyForm(chooseLeft), WEST);
		add(new MappingToolbar(), CENTER);
		add(vocabularyFormRight = new VocabularyForm(chooseRight), EAST);
	}

	
	void notifyWorkingOntologyAdded(OntologyInfo ontologyInfo) {
		int workingOntsSize = Main.workingUris.size();
		int chooseLeft = workingOntsSize > 0 ? 0 : -1;
		int chooseRight = workingOntsSize > 1 ? 1 : chooseLeft;
		vocabularyFormLeft.notifyWorkingOntologyAdded(chooseLeft);
		vocabularyFormRight.notifyWorkingOntologyAdded(chooseRight);
	}

}
