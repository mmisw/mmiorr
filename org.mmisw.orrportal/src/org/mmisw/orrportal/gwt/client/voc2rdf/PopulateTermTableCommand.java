package org.mmisw.orrportal.gwt.client.voc2rdf;

import java.util.List;

import org.mmisw.orrportal.gwt.client.Orr;
import org.mmisw.orrportal.gwt.client.util.table.IRow;

import com.google.gwt.user.client.IncrementalCommand;

/**
 * Incremental command to populate a term table.
 */
abstract class PopulateTermTableCommand implements IncrementalCommand {
	/** (max) number of rows to populate at each execution */
	private static final int rowIncrement = 17;
	
	
	private TermTable termTable;
	
	private List<IRow> rows;

	private List<String> headerCols;

	private int rowInTermTable = -1;
	
	private int currFromRow;
	
	private boolean started;
	private boolean preDone;
	
	private boolean firstColIsUri;


	PopulateTermTableCommand(TermTable termTable, List<IRow> rows) {
		this.termTable = termTable;
		this.rows = rows;
		headerCols = termTable.getHeaderCols();
		
		firstColIsUri = headerCols.size() > 0 && headerCols.get(0).equalsIgnoreCase("uri");
		
		rowInTermTable = -1;

		currFromRow = 0;
	}

	/** return false to stop command. done() will be called.  */
	abstract boolean start() ;
	
	/** return false to stop command. done() will be called. */
	abstract boolean updateStatus() ;
	
	abstract void done() ;

	

	public boolean execute() {
		if ( ! started ) {
			started = true;
			if ( start() ) {
				return true;
			}
			else {
				preDone = true;
				return false;
			}
		}
		
		if ( preDone ) {
			done();
			return false;
		}
			

		// add a chunk of rows:
		if ( _addRows(currFromRow, currFromRow + rowIncrement) ) {
			preDone();
		}
		else {
			if ( updateStatus() ) {
				currFromRow += rowIncrement;
			}
			else {
				preDone = true;
			}
		}
		return true;
	}
	
	private void preDone() {
		updateStatus();
		preDone = true;
	}

	
	private boolean _addRows(int fromRow, int toRow) {
		int numRows = rows.size();
		int rr = fromRow;
		for ( ; rr < numRows && rr < toRow; rr++ ) {
			
			final int numCols = headerCols.size();
			
			IRow irow = rows.get(rr);

			// skip empty line
			boolean empty = true;
			for ( int c = 0; empty && c < numCols; c++ ) {
				String colVal = irow.getColValue(headerCols.get(c));
				if ( colVal != null && colVal.trim().length() > 0 ) {
					empty = false;
				}
			}
			if ( empty ) {
				continue;
			}
			
			rowInTermTable++;
			termTable.addRow(numCols);
			for ( int c = 0; c < numCols; c++ ) {
				String colName = headerCols.get(c);
				String colVal = irow.getColValue(colName);
				colVal = colVal != null ? colVal.trim() : "";
				
				if ( c == 0 && firstColIsUri && colVal.length() > 0 ) {
					String link = Orr.getPortalBaseInfo().getOntServiceUrl()+ "?form=html&uri=" +colVal;
					String str = "<a target=\"_blank\" href=\"" +link+ "\">" +colVal+ "</a>";
					termTable.setCell(rowInTermTable, c, str, true);
				}
				else {
					termTable.setCell(rowInTermTable, c, colVal, irow.isHtml(colName));
				}
			}
		}
		
		return rr >= numRows;   // DONE
	}
}
