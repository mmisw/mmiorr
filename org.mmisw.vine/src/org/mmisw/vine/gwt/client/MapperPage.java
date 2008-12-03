package org.mmisw.vine.gwt.client;

import com.google.gwt.user.client.ui.DockPanel;

public class MapperPage extends DockPanel {
	
	MapperPage() {
		super();
		
		setSpacing(5);
		setVerticalAlignment(ALIGN_MIDDLE);
		
		int workingOntsSize = Main.workingUris.size();
		int chooseLeft = workingOntsSize > 0 ? 0 : -1;
		int chooseRight = workingOntsSize > 1 ? 1 : chooseLeft;
		add(new VocabularyForm(chooseLeft), WEST);
		add(new MappingToolbar(), CENTER);
		add(new VocabularyForm(chooseRight), EAST);
	}

}
