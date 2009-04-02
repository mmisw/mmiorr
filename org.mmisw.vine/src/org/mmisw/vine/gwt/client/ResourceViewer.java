package org.mmisw.vine.gwt.client;

import org.mmisw.vine.gwt.client.rpc.EntityInfo;

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
	
	private HTML bodyHtml;
	
	/**
	 * 
	 */
	ResourceViewer() {
		super();
		
		CellPanel p = new VerticalPanel();
		
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(p);
	    add(decPanel);

	    bodyHtml = new HTML();
	    bodyHtml.setWidth("700px");
	    
		ScrollPanel scroller = new ScrollPanel(bodyHtml); 
//	    scroller.setSize("450px", "120px");
		scroller.setWidth("450px");
		p.add(scroller);
		
		_update("", "", "", "");
	}

	/** Updates the contents */
	void update(EntityInfo entityInfo) {
		String name = entityInfo.getLocalName();
		String code = "" + entityInfo.getCode();
		String uri = Main.getWorkingUris().get(code).getUri() + name;
		
		String label = entityInfo.getDisplayLabel();
		String comment = entityInfo.getComment();
		
		if ( label == null ) {
			label = "";
		}
		if ( comment == null ) {
			comment = "";
		}
		
		_update(uri, name, label, comment);
	}
	
	private void _update(String uri, String name, String label, String comment) {
		bodyHtml.setHTML(""
				+ "URI: <a target=\"_blank\" href=\"" +uri+ "\">" +uri+ "</a><br/>"
				+ "Name: <b>" +name+ "</b><br/>"
				+ "Label: <b>" +label + "</b><br/>"
				+ "Comment: <b>" +comment + "</b><br/>"
		);
		
	}

}
