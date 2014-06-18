package org.mmisw.orrportal.gwt.client;

import org.mmisw.orrportal.gwt.client.util.OrrUtil;

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
	OrrLogger(String logSpec) {
		String str = logSpec == null ? "" : logSpec.trim().toLowerCase();
		if (str.length() == 0 || str.equals("*") || str.equals("y") ) {
			str = null;  // do not filter anything
		}
		_filter = str;
		_logBuffer = new StringBuffer();
	}

	/**
	 * Logs a message.
	 * @param msg
	 */
	void log(String msg) {
		if (_filter == null) {
			_logBuffer.append(msg + "\n");			
		}
		else {
			String[] lines = msg.split("\n");
			for (String line : lines) {
				if (line.toLowerCase().contains(_filter) ) {
					_logBuffer.append(line).append("\n");
				}
			}
		}
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
	
	// to filter messages
	private final String _filter;
	
	// buffer for keeping the logging info.
	private final StringBuffer _logBuffer;
	
	private Widget _logWidget;

}
