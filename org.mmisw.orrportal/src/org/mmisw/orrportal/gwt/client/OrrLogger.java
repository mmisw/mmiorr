package org.mmisw.orrportal.gwt.client;

import org.mmisw.orrportal.gwt.client.util.OrrUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * A simple logger to facilitate debugging especially when the application is deployed
 * in an actual browser.
 * 
 * @author Carlos Rueda
 */
class OrrLogger {
	
	/**
	 * Initializes the logger.
	 */
	OrrLogger() {
		
		_logBuffer = new StringBuffer();
		
		log("Util.getLocationProtocol() = " + OrrUtil.getLocationProtocol());
		log("Util.getLocationHost()     = " + OrrUtil.getLocationHost());
		log("GWT.getHostPageBaseURL()   = " + GWT.getHostPageBaseURL());
		log("GWT.getModuleBaseURL()     = " + GWT.getModuleBaseURL());
		log("GWT.getModuleName()        = " + GWT.getModuleName());
//		String baseUrl = OrrUtil.getLocationProtocol() + "//" + OrrUtil.getLocationHost();
//		baseUrl = baseUrl.replace("/+$", ""); // remove trailing slashes
//		Orr.log("baseUrl = " + baseUrl);
	}

	/**
	 * Logs a message.
	 * @param msg
	 */
	void log(String msg) {
		_logBuffer.append(msg + "\n");
	}

	/**
	 * Gets a widget with controls for the logging information.
	 */
	Widget getWidget() {
		if ( _logWidget == null ) {
			HorizontalPanel buttons = new  HorizontalPanel();
			final HTML logLabel = OrrUtil.createHtml("", 10);
			ButtonBase buttonLog = OrrUtil.createButton("Refresh Log",
					"Refresh log info", new ClickListener() {
				public void onClick(Widget sender) {
					logLabel.setHTML("<pre>" + _logBuffer.toString()
							+ "</pre>");
				}
			});
			ButtonBase buttonClear = OrrUtil.createButton("Clear Log",
					"Clear log info", new ClickListener() {
				public void onClick(Widget sender) {
					_logBuffer.setLength(0);
					logLabel.setHTML("");
				}
			});
			buttons.add(buttonLog);
			buttons.add(buttonClear);
			VerticalPanel vpanel = new  VerticalPanel();
			vpanel.add(buttons);
			vpanel.add(logLabel);
			_logWidget = vpanel;
		}
		
		return _logWidget;
	}
	
    
	///////////////////////////////////////////////////////////////////////////////
	// private
	///////////////////////////////////////////////////////////////////////////////
	
	// buffer for keeping the logging info.
	private final StringBuffer _logBuffer;
	
	private Widget _logWidget;

}
