package org.mmisw.orrportal.gwt.client.util;

import org.mmisw.orrportal.gwt.client.Orr;

/**
 * Google analytics utilities.
 * 
 * @author Carlos Rueda
 */
public class GaUtil {
	private GaUtil() {}
	
	private static Boolean enabled = null;
	
	private static boolean enabled() {
		if ( enabled == null ) {
			String gaUaNumber = Orr.getPortalBaseInfo().getGaUaNumber();
			enabled = Boolean.valueOf(gaUaNumber != null && gaUaNumber.trim().length() > 0);
			Orr.log("GA enabled=" +enabled+ " (gaUaNumber = " +gaUaNumber+ ")");
		}
		return enabled;
	}

	/** See <a href="http://code.google.com/apis/analytics/docs/gaJS/gaJSApiEventTracking.html#_gat.GA_EventTracker_._trackEvent"
	 * >_trackEvent</a>
	 */
	public static void trackEvent(String category, String action, String opt_label, String opt_value) {
		if ( enabled() ) {
			_trackEvent(category, action, opt_label, opt_value);
		}
	}
	
	private static native void _trackEvent(String category, String action, String opt_label, String opt_value) /*-{
		$wnd._gaq.push(['_trackEvent', action, opt_label, opt_value]);
	}-*/ ;
	

	public static void trackPageview() {
		if ( enabled() ) {
			_trackPageview();
		}
	}
	
	private static native void _trackPageview() /*-{
		$wnd._gaq.push(['_trackPageview']);
	}-*/ ;
	
	public static void trackPageview(String pageName) {
		if ( enabled() ) {
			if ( ! pageName.startsWith("/") ) {
				pageName = "/" +pageName;
			}
			_trackPageview(pageName);
		}
	}
	
	private static native void _trackPageview(String pageName) /*-{
		$wnd._gaq.push(['_trackPageview', pageName]);
	}-*/ ;
	
}
