package org.mmisw.vine.gwt.client;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ResourceViewer extends VerticalPanel {
	
	ResourceViewer() {
		super();
		
		CellPanel p = new VerticalPanel();
		
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(p);
	    add(decPanel);

	    CellPanel p2 = new VerticalPanel();
		ScrollPanel scroller = new ScrollPanel(p2);
	    scroller.setSize("400px", "100px");
		p.add(scroller);

		p2.add(new HTML("<b>http://marinemetadata.org/cf#air_temperature</b>"));
		p2.add(new HTML("<b>Label:</b> air_temperature"));
		p2.add(new HTML("<b>standard_name:</b> air_temperature"));
		p2.add(new HTML("<b>hasCanocical_Units:</b> K"));
	}

}
