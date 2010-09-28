package org.mmisw.orrportal.gwt.client.util.table.ontab;

import java.util.List;
//import com.smartgwt.client.data.Record;
//import com.smartgwt.client.data.RecordList;
//import com.smartgwt.client.widgets.Canvas;
//import com.smartgwt.client.widgets.grid.ListGrid;
//import com.smartgwt.client.widgets.grid.ListGridField;

import org.mmisw.orrclient.gwt.client.rpc.LoginResult;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrportal.gwt.client.util.table.IQuickInfo;

import com.google.gwt.user.client.ui.Widget;

/**
 * Possible implementation based on SmartGWT.
 * This has been tested with GWT 2.0.4, but currently all code is commented
 * out and operations throw UnsupportedOperationException, while we get to
 * decide when to upgrade and eventually use SmartGWT.
 * (Preliminary testing hasn't been as straightforward as expected, especially
 * regarding performance. Seems like the mix of standardt GWT widgets and
 * SmartGWT creates some bottleneck ...).
 * 
 * @author Carlos Rueda
 */
public class OntologyTableSc extends BaseOntologyTable {

//	static {
//	GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
//		public void onUncaughtException(Throwable e) {
//			GWT.log("something happened", e);
//		}
//	});
//}
	/**
	 * 
	 * @param quickInfo
	 * @param isVersionsTable
	 */
	public OntologyTableSc(IQuickInfo quickInfo, boolean isVersionsTable) {
		super(quickInfo, isVersionsTable);
		throw new UnsupportedOperationException();

//		countryGrid.setWidth(1000);
//		countryGrid.setHeight(600);
//		countryGrid.setShowAllRecords(true);
//		countryGrid.setShowRowNumbers(true);
//		countryGrid.setAlternateRecordStyles(true);

//		countryGrid.setFields(new ListGridField[] {
//				new ListGridField("uri", "URI"),
//				new ListGridField("name", "Name"),
//				new ListGridField("author", "Author"),
//				new ListGridField("version", "Version"), });
//		
//		canvas.addChild(countryGrid); 
	}

	public void clear() {
		throw new UnsupportedOperationException();
		// TODO 

	}

	public Widget getWidget() {
		throw new UnsupportedOperationException();
		// TODO 
//		return canvas;
	}

	public void setOntologyInfos(List<RegisteredOntologyInfo> ontologyInfos,
			LoginResult loginResult
	) {
		throw new UnsupportedOperationException();
//		// TODO 

//		RecordList recordList = new RecordList();
//		
//		for (RegisteredOntologyInfo oi : ontologyInfos) {
//			Record record = new Record();
//
//			record.setAttribute("uri", _getUri(oi));
//			record.setAttribute("name", _getName(oi));
//			record.setAttribute("author", _getAuthor(oi));
//			record.setAttribute("version", _getVersion(oi));
//
//			recordList.add(record);
//		}
//		countryGrid.setData(recordList);
//
//		canvas.redraw();
	}

	public void setSortColumn(String sortColumn, boolean down) {
		throw new UnsupportedOperationException();
		// TODO 

	}

	public void showProgress() {
		throw new UnsupportedOperationException();
		// TODO 

	}

	// ////////////////////////////////////////////////////////////////////
	// private
	// ////////////////////////////////////////////////////////////////////

//	private Canvas canvas = new Canvas();
//	private ListGrid countryGrid = new ListGrid();

}
