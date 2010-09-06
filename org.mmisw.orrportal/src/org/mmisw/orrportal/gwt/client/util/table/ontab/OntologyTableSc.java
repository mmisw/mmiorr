package org.mmisw.orrportal.gwt.client.util.table.ontab;

import java.util.List;

import org.mmisw.orrclient.gwt.client.rpc.LoginResult;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrportal.gwt.client.util.table.IQuickInfo;

import com.google.gwt.user.client.ui.Widget;
//import com.smartgwt.client.data.Record;
//import com.smartgwt.client.data.RecordList;
//import com.smartgwt.client.types.Alignment;
//import com.smartgwt.client.types.ListGridFieldType;
//import com.smartgwt.client.widgets.Canvas;
//import com.smartgwt.client.widgets.grid.ListGrid;
//import com.smartgwt.client.widgets.grid.ListGridField;

/**
 * Implementation (TO BE) based on smartgwt.
 * All methods here throw UnsupportedOperationException
 * 
 * @author Carlos Rueda
 */
public class OntologyTableSc extends BaseOntologyTable {

	/**
	 * 
	 * @param quickInfo
	 * @param isVersionsTable
	 */
	public OntologyTableSc(IQuickInfo quickInfo, boolean isVersionsTable) {
		super(quickInfo, isVersionsTable);
		throw new UnsupportedOperationException();

//		countryGrid.setWidth(1000);
//		countryGrid.setHeight(224);
//		countryGrid.setShowAllRecords(true);
//
//		ListGridField countryCodeField = new ListGridField("countryCode",
//				"Flag", 50);
//		countryCodeField.setAlign(Alignment.CENTER);
//		countryCodeField.setType(ListGridFieldType.IMAGE);
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
		// TODO 

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
