package org.mmisw.orrportal.gwt.client.util.table.ontab;

import java.util.List;

import org.mmisw.orrclient.gwt.client.rpc.LoginResult;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrportal.gwt.client.util.table.IQuickInfo;

//import com.google.gwt.gen2.table.client.FixedWidthFlexTable;
//import com.google.gwt.gen2.table.client.FixedWidthGrid;
//import com.google.gwt.gen2.table.client.ScrollTable;
//import com.google.gwt.gen2.table.client.SelectionGrid.SelectionPolicy;
//import com.google.gwt.gen2.table.override.client.FlexTable.FlexCellFormatter;
//import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;

/**
 * Possible implementation based on GWT incubator.
 * This is only a partial implementation. Needs improvement for when time permits.
 * 
 * <p>
 * NOTE: all actual code commented -- DO NOT USE.
 *
 * @author Carlos Rueda
 */
public class OntologyTableGwtIncubator extends BaseOntologyTable {

	/**
	 * 
	 * @param quickInfo
	 * @param isVersionsTable
	 */
	public OntologyTableGwtIncubator(IQuickInfo quickInfo, boolean isVersionsTable) {
		super(quickInfo, isVersionsTable);
		throw new UnsupportedOperationException();
		
//		FixedWidthFlexTable headerTable = createHeaderTable();
//		dataTable = _createDataTable();
//
//		// Combine the components into a ScrollTable
//		scrollTable = new ScrollTable(dataTable, headerTable);
//
//		// Set some options in the scroll table
////		scrollTable.setSortingEnabled(true);
//		scrollTable.setResizePolicy(ScrollTable.ResizePolicy.FILL_WIDTH);
//		scrollTable.setSize("100%", "100%");
	}

	public void clear() {
		// TODO 
//		dataTable.resize(0, 4);

	}

	public Widget getWidget() {
//		return scrollTable;
		return null;
//		return xx.getWidget();
	}

	public void setOntologyInfos(List<RegisteredOntologyInfo> ontologyInfos,
			LoginResult loginResult
	) {

//		dataTable.resize(ontologyInfos.size(), 4);
//
//		int row = 0;
//		for (RegisteredOntologyInfo oi : ontologyInfos) {
//		
//			String[] cols = { _getUri(oi), _getName(oi), _getAuthor(oi), _getVersion(oi) };
//			
//			for ( int col = 0; col < cols.length; col++ ) {
//				dataTable.setHTML(row, col, cols[col]);
//				dataTable.getCellFormatter().setWordWrap(row, col, true);
//			}
//
//			row++;
//		}
//		
//		dataTable.setColumnWidth(0, 500);
//		dataTable.setColumnWidth(1, 300);
		
//		dataTable.sortColumn(3, false);

	}

	public void setSortColumn(String sortColumn, boolean down) {
		// TODO 
	}

	public void showProgress() {
		// TODO 
//		dataTable.resize(1, 1);
//		dataTable.setHTML(0, 0, "<i>one moment...</i>");

	}

	// ////////////////////////////////////////////////////////////////////
	// private
	// ////////////////////////////////////////////////////////////////////

	/**
	 * Create the data table.
	 * 
	 * @return a data table
	 */
//	private FixedWidthGrid _createDataTable() {
//		FixedWidthGrid dataTable = new FixedWidthGrid();
//		dataTable.setSelectionPolicy(SelectionPolicy.ONE_ROW);
//		return dataTable;
//	}

	
//	/**
//	 * Create the header table.
//	 * 
//	 * @return a header table
//	 */
//	private FixedWidthFlexTable createHeaderTable() {
//		// Create a new table
//		FixedWidthFlexTable headerTable = new FixedWidthFlexTable();
//		FlexCellFormatter formatter = headerTable.getFlexCellFormatter();
//
//		final int row = 0;
//		headerTable.setHTML(row, 0, "URI");
//		formatter.setHorizontalAlignment(row, 0,
//				HasHorizontalAlignment.ALIGN_CENTER);
//		headerTable.setHTML(row, 1, "Name");
//		formatter.setHorizontalAlignment(row, 1,
//				HasHorizontalAlignment.ALIGN_CENTER);
//		headerTable.setHTML(row, 2, "Author");
//		formatter.setHorizontalAlignment(row, 2,
//				HasHorizontalAlignment.ALIGN_CENTER);
//		headerTable.setHTML(row, 3, "Version");
//		formatter.setHorizontalAlignment(row, 3,
//				HasHorizontalAlignment.ALIGN_CENTER);
//
//		return headerTable;
//	}

	
//	private ScrollTable scrollTable;
//	private FixedWidthGrid dataTable;

}
