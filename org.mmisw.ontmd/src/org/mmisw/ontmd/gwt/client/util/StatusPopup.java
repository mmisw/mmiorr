package org.mmisw.ontmd.gwt.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * 
 * @author Carlos Rueda
 */
public class StatusPopup {
	
	private PopupPanel popup;

	private boolean useProgressAnimation;
	
	private final HTML statusHtml = new HTML();
	
	public StatusPopup(String width, boolean modal) {
		statusHtml.setWidth(width);
		popup = new PopupPanel(false, modal);
		popup.add(statusHtml);
	}
	
	public void show(int left, int top) {
		popup.setPopupPosition(left, top);
		popup.show();
	}
	
	public void setStatus(String msg) {
		String html = useProgressAnimation ? "<img src=\"" +GWT.getModuleBaseURL()+ "images/loading.gif\"> " : "" ;
		html += "<font color=\"blue\"><i>" +msg+ "</i></font>";
		
		statusHtml.setHTML(msg);
	}
	
	public void setStatusHtml(String html) {
		statusHtml.setHTML(html);
	}
	
	public void hide() {
		statusHtml.setHTML("");
		popup.hide();
	}

}
