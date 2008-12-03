package org.mmisw.vine.gwt.client;

import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.PushButton;


public class MappingToolbar extends VerticalPanel {
	
	MappingToolbar() {
		super();
		
		VerticalPanel layout = new VerticalPanel();
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(layout);
	    
	    add(decPanel);
		
	    layout.setSpacing(2);
		//setWidth("10%");
		
		PushButton b1 = new PushButton(Main.images.exactMatch28().createImage());
		PushButton b2 = new PushButton(Main.images.closeMatch28().createImage());
		PushButton b3 = new PushButton(Main.images.broadMatch28().createImage());
		PushButton b4 = new PushButton(Main.images.narrowMatch28().createImage());
		PushButton b5 = new PushButton(Main.images.relatedMatch28().createImage());
		
		layout.add(b1);
		layout.add(b2);
		layout.add(b3);
		layout.add(b4);
		layout.add(b5);
	}

}
