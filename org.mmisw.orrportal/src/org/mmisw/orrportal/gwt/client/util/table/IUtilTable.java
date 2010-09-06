package org.mmisw.orrportal.gwt.client.util.table;

import java.util.List;


import com.google.gwt.user.client.ui.Widget;

public interface IUtilTable {
	
	public void clear();
	
	public void showProgress();
	
	public void setRows(final List<IRow> rows);
	
	public Widget getWidget();

}
