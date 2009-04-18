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
	private ScrollPanel scroller;
	
	/**
	 * 
	 */
	ResourceViewer(boolean useDecoratorPanel, boolean useScroller) {
		super();
		
		bodyHtml = new HTML();
//	    bodyHtml.setWidth("700px");
		
		if ( useDecoratorPanel ) {
			CellPanel p = new VerticalPanel();
			DecoratorPanel decPanel = new DecoratorPanel();
			decPanel.setWidget(p);
			add(decPanel);
			if ( useScroller ) {
				scroller = new ScrollPanel(bodyHtml); 
//				scroller.setSize("450px", "120px");
//				scroller.setWidth("450px");
				p.add(scroller);
			}
			else {
				p.add(decPanel);
			}
		}
		else if ( useScroller ) {
			scroller = new ScrollPanel(bodyHtml); 
			add(scroller);
		}
		else {
			add(bodyHtml);
		}
		
		_update("", "", "", "");
	}
	
	public void setWidth(String width) {
		if ( scroller != null ) {
			scroller.setWidth(width);
		}
		else {
			super.setWidth(width);
		}
	}

	public void setSize(String width, String height) {
		if ( scroller != null ) {
			scroller.setSize(width, height);
		}
		else {
			super.setSize(width, height);
		}
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
