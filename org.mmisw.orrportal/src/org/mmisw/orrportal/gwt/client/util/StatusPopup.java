package org.mmisw.orrportal.gwt.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * A simple popup window to show progress information.
 * 
 * <p>
 * It has an internal timer so the popup is automatically hidden every
 * few seconds but displayed again upon any update to the status.
 * This mechanism helps deal with cases where long running tasks (via
 * IncrementalCommand's, for example) fail to call the finalization part where normally an
 * explicit call to hide() will be done.
 * This was a quick fix to 
 * <a href="http://code.google.com/p/mmisw/issues/detail?id=186">issue 186</a>.
 * <p>
 * TODO a more robust mechanism in general.
 * 
 * @author Carlos Rueda
 */
public class StatusPopup {
	
	private PopupPanel popup;

	private boolean useProgressAnimation;
	
	private final HTML statusHtml = new HTML();
	
	private boolean showCalled;
	private boolean hideCalled;
	
	private final Timer autoHideTimer = new Timer() {
		public void run() {
			popup.hide();
			if ( hideCalled ) {
				autoHideTimer.cancel();
			}
		}
	};
	
	public StatusPopup(String width, boolean modal) {
		statusHtml.setWidth(width);
		popup = new PopupPanel(false, modal);
		popup.add(statusHtml);
	}
	
	public void show(int left, int top) {
		popup.setPopupPosition(left, top);
		popup.show();
		showCalled = true;
		hideCalled = false;
		autoHideTimer.scheduleRepeating(2*2000);
	}
	
	public void setStatus(String msg) {
		String html = useProgressAnimation ? "<img src=\"" +GWT.getModuleBaseURL()+ "images/loading.gif\"> " : "" ;
		html += "<font color=\"blue\"><i>" +msg+ "</i></font>";
		
		show();

		statusHtml.setHTML(msg);
	}

	private void show() {
		if ( showCalled && ! hideCalled ) {
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					popup.show();
				}
			});
		}
	}

	public void setStatusHtml(String html) {
		show();
		statusHtml.setHTML(html);
	}
	
	public void hide() {
		hideCalled = true;
		popup.hide();
	}

}
