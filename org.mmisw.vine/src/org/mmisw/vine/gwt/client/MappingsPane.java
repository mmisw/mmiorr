package org.mmisw.vine.gwt.client;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MappingsPane extends VerticalPanel {
	
	MappingsPane() {
		super();
		
		CellPanel p = new VerticalPanel();
		
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(p);
	    add(decPanel);
		
	    CellPanel p2 = new VerticalPanel();
		ScrollPanel scroller = new ScrollPanel(p2);
	    scroller.setSize("400px", "100px");
		p.add(scroller);

		
		DisclosurePanel d1 = new DisclosurePanel("someTermHere");
		d1.setOpen(true);
	    DisclosurePanel d2 = new DisclosurePanel("exactMatch");
	    d2.setOpen(true);
	    d1.add(d2);
	    
	    p2.add(d1);
	    
	    VerticalPanel vp = new VerticalPanel();
	    d2.add(vp);

	    HorizontalPanel hp1 = new HorizontalPanel();
	    vp.add(hp1);

	    HorizontalPanel hp2 = new HorizontalPanel();
	    vp.add(hp2);

	    hp1.add(Main.images.explicit().createImage());
	    hp1.add(new HTML("someOtherTerm"));

	    hp2.add(Main.images.inferred().createImage());
	    hp2.add(new HTML("someOtherTerm2"));

	    
	}

}
