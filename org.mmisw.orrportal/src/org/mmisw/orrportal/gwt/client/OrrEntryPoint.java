package org.mmisw.orrportal.gwt.client;

import java.util.Map;

import org.mmisw.orrportal.gwt.client.util.OrrUtil;

import com.google.gwt.core.client.EntryPoint;


/**
 * The entry point for the ORR Portal application.
 * 
 * @author Carlos Rueda
 */
public class OrrEntryPoint implements EntryPoint {

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
	
}
