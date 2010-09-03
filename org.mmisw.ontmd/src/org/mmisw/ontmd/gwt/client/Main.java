package org.mmisw.ontmd.gwt.client;

import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.ontmd.gwt.client.util.OrrUtil;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * The entry point.
 * 
 * @author Carlos Rueda
 */
public class Main implements EntryPoint {

	public String footer; 
	
	static AppInfo appInfo;
	
	/**
	 * This is the entry point for the ORR Portal application.
	 */
	public void onModuleLoad() {
		boolean withLogging = false;
		
		Map<String, String> params = OrrUtil.getParams();

		if (params != null) {
			String _log = (String) params.get("_log");
			if (_log != null) {
				withLogging = true;
				params.remove("_log");
			}
		}
		
		Orr.init(withLogging);
		
		Orr.launch(params);
	}

	/**
	 * Adds the given panel to the interface.
	 * @param params
	 * @param mainPanel
	 */
	public void addMainPanel(final Map<String, String> params, Widget mainPanel) {

		VerticalPanel panel = new VerticalPanel();
//		panel.setBorderWidth(1);
		panel.setWidth("100%");
		RootPanel.get().add(panel);

		HorizontalPanel hpanel = new HorizontalPanel();
		panel.add(hpanel);
		hpanel.setWidth("100%");
		hpanel.add(mainPanel);

		Widget logWidget = Orr.getLogWidget();
		if ( logWidget != null ) {
			panel.add(logWidget);
		}
		
		panel.add(OrrUtil.createHtml("<font color=\"gray\">" +footer+ "</font><br/><br/>", 10));

	}

	
}
