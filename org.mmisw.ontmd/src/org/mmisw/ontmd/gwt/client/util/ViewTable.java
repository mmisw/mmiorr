package org.mmisw.ontmd.gwt.client.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;

/**
 * A view table utility.
 * 
 * @author Carlos Rueda
 */
public class ViewTable {

	public static interface IRow {

		String getColValue(String sortColumn);
		
	}
	
	
	private List<IRow> rows;

	
	private final FlexTable flexPanel = new FlexTable();

	private List<HTML> headerHtmls;
	
	private String sortColumn;
	
	private int sortFactor = 1;
	
	private Comparator<IRow> cmp = new Comparator<IRow>() {
		public int compare(IRow o1, IRow o2) {
			String s1 = o1.getColValue(sortColumn);
			String s2 = o2.getColValue(sortColumn);
			
			int baseRes = 0;
			if ( s1 == null ) {
				baseRes = s2 == null ? 0 : -1;
			}
			else if ( s2 == null ) {
				baseRes = +1;
			}
			else {
				baseRes = s1.compareToIgnoreCase(s2);
			}
			
			return sortFactor * baseRes;
		}
	};

	private ClickListener columnHeaderClickListener = new ClickListener() {
		public void onClick(Widget sender) {
			String colName = ((HTML) sender).getText().toLowerCase();
			if ( sortColumn.equalsIgnoreCase(colName) ) {
				sortFactor *= -1;
			}
			else {
				sortColumn = colName;
			}

			showProgress();
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					Collections.sort(rows, cmp);
					update();
				}
			});
		}
	};

	public ViewTable(List<String> colNames) {
		this(colNames.toArray(new String[colNames.size()]));
	}
	
	public ViewTable(String... colLabels) {
		super();
		
		flexPanel.setBorderWidth(1);
		flexPanel.setWidth("100%");
		flexPanel.setStylePrimaryName("OntologyTable");
		
		headerHtmls = new ArrayList<HTML>();
		sortColumn = colLabels.length > 0 ? colLabels[0] : "";
		
		for ( int i = 0; i < colLabels.length; i++ ) {
			HTML html = new HTML(colLabels[i]);
			headerHtmls.add(html);
			html.addClickListener(columnHeaderClickListener);
		}
		
		prepareHeader();
	}
	
	public void clear() {
		flexPanel.clear();
		while ( flexPanel.getRowCount() > 0 ) {
			flexPanel.removeRow(0);
		}
	}
	
	public void showProgress() {
		clear();
		prepareHeader();
		int row = 0;
		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 3);
		flexPanel.setWidget(row, 0, new HTML("<i>one moment...</i>"));
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);

	}

	private void prepareHeader() {
		int row = 0;
		
		flexPanel.getRowFormatter().setStylePrimaryName(row, "OntologyTable-header");
		
		for ( int i = 0, count = headerHtmls.size(); i < count; i++ ) {
			HTML html = headerHtmls.get(i);
			flexPanel.setWidget(row, i, html);
			flexPanel.getFlexCellFormatter().setAlignment(row, i, 
					HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
			);
		}
		row++;
		
	}
	
	public void setRows(final List<IRow> rows) {
		this.rows = rows;
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				Collections.sort(rows, cmp);
				update();
			}
		});
	}
	
	private void update() {
		
		clear();
		prepareHeader();
		
		if ( rows == null ) {
			return;
		}
		
		if ( true ) {
			// incrementally update the interface (preferred):
			UpdateCommand cmd = new UpdateCommand(0);
			DeferredCommand.addCommand(cmd);			
		}
		else {
			// old mechanism (one single interface update -- the script may be stopped)
			int row = 1;
			for ( IRow irow : rows ) {
				_setRow(row, irow);
				row++;
			}
		}
	}

	/**
	 * 
	 * @param row Row in the flexTable
	 * @param irow
	 */
	private void _setRow(int row, IRow irow) {
		flexPanel.getRowFormatter().setStylePrimaryName(row, "OntologyTable-row");
		
		for ( int i = 0, count = headerHtmls.size(); i < count; i++ ) {
			HTML html = headerHtmls.get(i);
			String name = html.getText();
			
			String value = irow.getColValue(name);
			if ( value == null ) value = "";
			
			HTML valHtml = new HTML(value);
			flexPanel.setWidget(row, i, valHtml);
			flexPanel.getFlexCellFormatter().setAlignment(row, i, 
					HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
			);
		}
	}
	
	public Widget getWidget() {
		return flexPanel;
	}


	/**
	 * Incremental command to create the resulting table.
	 */
	private class UpdateCommand implements IncrementalCommand {
		private static final int rowIncrement = 34;

		private int currFromRow;
		
		private boolean preDone;

		UpdateCommand(int fromRow) {
			currFromRow = fromRow;
		}
		
		public boolean execute() {
			if ( preDone ) {
				done();
				return false;
			}
			
			// add a chunk of rows:
			if ( _addRows(currFromRow, currFromRow + rowIncrement) ) {
				preDone();
			}
			else {
				currFromRow += rowIncrement;
			}
			return true;
		}
		
		private void preDone() {
			preDone = true;
		}

		private void done() {
		}
		
		private boolean _addRows(int fromRow, int toRow) {
			int row = fromRow;
			for ( int count = rows.size(); row < count && row < toRow; row++ ) {
				
				IRow irow = rows.get(row);
				_setRow(row + 1, irow );
	
			}
			
			return row >= rows.size();
		}
	}
}
