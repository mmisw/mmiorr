package org.mmisw.ontmd.gwt.client.util.table;


import java.util.List;

import org.mmisw.orrclient.gwt.client.rpc.LoginResult;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;

public interface IOntologyTable {

	public Widget getWidget();
	
	public void showProgress();
	
	public void setIncludeVersionInLinks(boolean includeVersionInLinks);
	
	public void setSortColumn(String sortColumn, boolean down);
	
	public void addClickListenerToHyperlinks(ClickListener clickListenerToHyperlinks);
	
	public void setQuickInfo(IQuickInfo quickInfo);
	
	public void clear() ;
	
	public void setOntologyInfos(final List<RegisteredOntologyInfo> ontologyInfos, LoginResult loginResult);
}
