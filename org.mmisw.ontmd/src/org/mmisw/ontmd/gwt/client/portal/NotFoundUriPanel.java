package org.mmisw.ontmd.gwt.client.portal;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A simple panel to dispatch the case where a requested URI could not be
 * resolved against the repository.
 * It includes a link, when the URI is a URL, so the user can try to open the
 * URL directly.
 * 
 * @author Carlos Rueda
 */
public class NotFoundUriPanel extends VerticalPanel {

	public NotFoundUriPanel(String uri, boolean isUrl) {
		super();
		
		setSpacing(20);
		
		CellPanel pan = new VerticalPanel();
		DecoratorPanel decPanel = new DecoratorPanel();
		decPanel.setWidget(pan);
		add(decPanel);
		
		String html = uri+ ": <font color=\"red\">URI not found in the registry.</font>";
		
		if ( isUrl ) {
		    html += "</br/><br/> " +
		    		"<a target=\"_blank\" href=\"" +uri+ "\">Click here</a> " +
		    		"to open the link directly.";
		}
		
		pan.setSpacing(20);
		pan.add(new HTML(html));
	}
	

}
