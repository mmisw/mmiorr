package org.mmisw.vine.gwt.client;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * This panel shows info about a given resource.
 * 
 * @author Carlos Rueda
 */
public class ResourceViewer extends VerticalPanel {
	
	private CellPanel p2;
	
	
	ResourceViewer() {
		super();
		
		CellPanel p = new VerticalPanel();
		
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(p);
	    add(decPanel);

	    p2 = new VerticalPanel();
		ScrollPanel scroller = new ScrollPanel(p2);
	    scroller.setSize("450px", "120px");
		p.add(scroller);
	}

	/** Updates the contents */
	void update(String text) {
		p2.add(new HTML("<b>" +text+ "</b>"));
		
//		String url = "http://mmisw.org/ont/mmi/MarineOrganism/zeidae.html";
//		p2.clear();
//		p2.add(new HTML(
//				"<iframe src=\"" +url+ "\" width=\"100%\" height=\"100%\">" +
//				"<p>Your browser does not support iframes.</p>" +
//				"</iframe>"
//		));
	}

}
