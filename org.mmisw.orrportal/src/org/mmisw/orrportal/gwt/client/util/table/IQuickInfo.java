package org.mmisw.orrportal.gwt.client.util.table;

import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;

import com.google.gwt.user.client.ui.Widget;

public interface IQuickInfo {
	/**
	 * 
	 * @param name  Used to show a label (in particular, for numbering)
	 * @param oi
	 * @param includeVersionInLinks
	 * @param includeVersionsMenu
	 * @return
	 */
	Widget getWidget(String name, RegisteredOntologyInfo oi, boolean includeVersionInLinks, boolean includeVersionsMenu);

}
