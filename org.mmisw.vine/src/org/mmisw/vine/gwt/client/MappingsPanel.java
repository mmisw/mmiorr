package org.mmisw.vine.gwt.client;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class MappingsPanel extends VerticalPanel {
	
	MappingsPanel() {
		super();
	    setWidth("100%");
	}

	void addMapping(String leftKey, String rightKey, Widget sender) {
		// TODO implement well!
		
		DockPanel mapping = new DockPanel();
		mapping.add(new Label(leftKey), DockPanel.WEST);
		mapping.add(sender, DockPanel.CENTER);
		mapping.add(new Label(rightKey), DockPanel.EAST);
		mapping.setWidth("100%");
		
		add(mapping);
	}

}
