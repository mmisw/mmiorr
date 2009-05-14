package org.mmisw.ontmd.gwt.client.portal;

import java.util.Map;

import org.mmisw.ontmd.gwt.client.Main;

import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Portal header panel.
 * 
 * @author Carlos Rueda
 */
public class HeaderPanel extends VerticalPanel {

	
	HeaderPanel(final Map<String, String> params) {
		super();
		
		VerticalPanel vp = new VerticalPanel();
		vp.add(Main.images.mmior().createImage());
		
		DecoratorPanel decPanel = new DecoratorPanel();
		add(decPanel);
	    decPanel.setWidget(vp);
	    

	}
	

}
