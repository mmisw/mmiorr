package org.mmisw.ontmd.gwt.client.voc2rdf;

import java.util.HashSet;
import java.util.Set;

import org.mmisw.ontmd.gwt.client.Main;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;

/**
 * A editable, spreadsheet-like table.
 * 
 * @author Carlos Rueda
 */
public class CopyOfTermTable extends VerticalPanel {
	
	private static final int CONTROL_ROW = 0;
	private static final int HEADER_ROW = 1;
	private static final int FIRST_REGULAR_ROW = 2;
	private static final int CONTROL_COL = 0;
	private static final int FIRST_REGULAR_COL = 1;
	
	private ScrollPanel scrollPanel;
	private final FlexTable flexTable = new FlexTable();
	private int currRow;
	private int currCol;
	private TableCell currCell;
	
	private final boolean readOnly;

	/**
	 * Create a editable table for the terms.
	 * @param cols number of desired columns
	 */
	public CopyOfTermTable(int cols, boolean readOnly) {
		this.add(flexTable);
		this.readOnly = readOnly;
		
		flexTable.setBorderWidth(1);
		
		flexTable.setStylePrimaryName("TermTable");
		
		flexTable.addTableListener(new TableListener() {
			public void onCellClicked(SourcesTableEvents sender, int row, int col) {
				_cellClicked(row, col);
			}
		});
		
		addRow(cols);   // control row
		addRow(cols);   // header row

		_updateControlColumns(cols);

		_setCell(HEADER_ROW, 0, "");

		_updateStyles(1);
		_updatePositions(0, 0);
	}
	
	
	public void setScrollPanel(ScrollPanel scrollPanel) {
		this.scrollPanel = scrollPanel;
	}


	/**
	 * @param cols in client space
	 */
	private void _updateControlColumns(int cols) {
		_setCell(CONTROL_ROW, 0, "");
		for ( int col = 0; col < cols; col++ ) {
			String letter = String.valueOf( (char) ('a' + col));
			String controlCol = "<font color=\"gray\">" +letter+ "</font>";
			_setCell(CONTROL_ROW, CONTROL_COL + 1 + col, controlCol);
		}
	}

	public void setHeader(int col, String html) {
		_setCell(HEADER_ROW, CONTROL_COL + 1 + col, html);
		_updateControlColumns(1 + col);
//		_updatePositions(HEADER_ROW, CONTROL_COL + 1 + col);
	}
	
	
	/**
	 * Sets a cell in this table in client coordinates.
	 * @param row
	 * @param col
	 * @param text
	 */
	public void setCell(int row, int col, String text) {
		int actualRow = FIRST_REGULAR_ROW + row;
		int actualCol = col + 1;
		_setWidget(actualRow, actualCol, new TableCell(actualRow, actualCol, text));
//		_updatePositions(actualRow, actualCol);
	}

	
	private void _setWidget(int row, int col, Widget widget) {
		flexTable.setWidget(row, col, widget);
	}

	private Widget _getWidget(int row, int col) {
		return flexTable.getWidget(row, col);
	}

	/**
	 * Sets a cell in this table.
	 * @param row
	 * @param col
	 * @param text
	 */
	private void _setCell(int row, int col, String text) {
		if ( row >= HEADER_ROW && col > CONTROL_COL ) {
			TableCell tcell = new TableCell(row, col, text, true);
			_setWidget(row, col, tcell);
		}
		else if ( row > CONTROL_ROW || col > CONTROL_COL ) {
			HorizontalPanel hp = new HorizontalPanel();
			hp.setWidth("100%");
//			hp.setBorderWidth(1);
			hp.setVerticalAlignment(ALIGN_MIDDLE);
			_setWidget(row, col, hp);
			
			if ( row > CONTROL_ROW ) {
				HTML html = new HTML(text);
				HTML filler = new HTML("");
				
				hp.setCellHorizontalAlignment(html, ALIGN_RIGHT);
				
				if ( readOnly ) {
					hp.add(html);
					hp.add(filler);
					
					hp.setCellWidth(html, "80%");
					hp.setCellWidth(filler, "20%");
				}
				else {
					Image img = Main.images.tridown().createImage();
					
					hp.add(img);
					hp.add(html);
					hp.add(filler);
					
					hp.setCellWidth(img, "30%");
					hp.setCellWidth(html, "50%");
					hp.setCellWidth(filler, "20%");
					
					hp.setCellHorizontalAlignment(img, ALIGN_LEFT);
				}
			}
			else if ( ! readOnly ) {
				Image img = Main.images.tridown().createImage();
				hp.add(img);
			}
			
		}
		else {
			_setWidget(row, col, new HTML(text));
		}
	}


	private void _updateStyles(int fromRow) {
		HTMLTable.RowFormatter rf = flexTable.getRowFormatter();
		rf.addStyleName(CONTROL_ROW, "TermTable-controlRow");
		rf.addStyleName(HEADER_ROW, "TermTable-header");
		CellFormatter cf = flexTable.getCellFormatter();
		
		for (int row = fromRow, rows = flexTable.getRowCount(); row < rows; ++row) {
			
			if ( row >= FIRST_REGULAR_ROW ) {
				rf.setStyleName(row, (row % 2) != 0 ? "TermTable-OddRow" : "TermTable-EvenRow");
			}
			cf.setAlignment(row, 0, ALIGN_CENTER, ALIGN_MIDDLE);
		}
		
		_updateControlColumns(flexTable.getCellCount(0) - 1);
	}


	/**
	 * @param col 
	 * @param row 
	 * 
	 */
	private void _cellClicked(int row, int col) {
		if ( row == CONTROL_ROW ) {
			if ( col > 0 ) {
				_dispatchColumnHeader(col);
			}
		}
		else {
			if ( col == 0 ) {
				_dispatchRowMenu(row);
			}
			else {
				// dispatch regular cell:
				currRow = row;
				currCol = col;
				currCell = (TableCell) _getWidget(currRow, currCol);
				
				//_editCell(true);
				currCell.setFocus(true);
			}
		}
	}
	
	/**
	 * 
	 */
	private void _dispatchRowMenu(final int row) {
		if ( readOnly ) {
			return;
		}
		
		Widget ww = _getWidget(row, 0);
		int left = ww.getAbsoluteLeft();
		int top = ww.getAbsoluteTop();

	    MenuBar menu = new MenuBar(true);
	    final PopupPanel menuPopup = new PopupPanel(true);
	    
	    if ( row >= FIRST_REGULAR_ROW ) {
	    	menu.addItem(new MenuItem("Insert row above", new Command() {
	    		public void execute() {
	    			_insertRow(row, flexTable.getCellCount(row));
	    			menuPopup.hide();
	    		}
	    	}));
	    }
	    menu.addItem(new MenuItem("Insert row below", new Command() {
			public void execute() {
				_insertRow(row + 1, flexTable.getCellCount(row));
				menuPopup.hide();
			}
	    }));
	    
	    if ( row >= FIRST_REGULAR_ROW ) {
	    	menu.addSeparator();
	    	menu.addItem(new MenuItem("Delete row", new Command() {
	    		public void execute() {
	    			_deleteRow(row);
	    			menuPopup.hide();
	    		}
	    	}));
	    }

	    menuPopup.setWidget(menu);
	    menuPopup.setPopupPosition(left, top);
		menuPopup.show();
	}
	
	/**
	 * @param cols number of client columns
	 */
	public void addRow(int cols) {
		_insertRow(flexTable.getRowCount(), cols + 1);   // +1 for control column
	}
	
	private void _insertRow(final int row, int cols) {
		int rows = flexTable.getRowCount();
//		int cols = rows > 0 ? flexTable.getCellCount(0): 2;
		flexTable.insertRow(row);
		for ( int c = 0; c < cols; c++ ) {
			flexTable.insertCell(row, c);
			_setCell(row, c, "");
		}
		rows = flexTable.getRowCount();
		for ( int r = row; r < rows; r++ ) {
			int displayRow = r - FIRST_REGULAR_ROW + 1;
			if ( displayRow >= 1 ) {
				_setCell(r, 0, "<font color=\"gray\">" +displayRow + "</font>");
			}
		}
		_updateStyles(row);
		_updatePositions(row, 0);
	}
	private void _deleteRow(final int row) {
		flexTable.removeRow(row);
		for ( int r = row, rows = flexTable.getRowCount(); r < rows; r++ ) {
			_setCell(r, 0, "<font color=\"gray\">" +(r - FIRST_REGULAR_ROW + 1)+ "</font>");
		}
		_updateStyles(row);
		_updatePositions(row, 0);
	}

	
	private void _dispatchColumnHeader(final int col) {
		if ( readOnly ) {
			return;
		}

		Widget ww = _getWidget(CONTROL_COL, col);
		int left = ww.getAbsoluteLeft();
		int top = ww.getAbsoluteTop();

	    MenuBar menu = new MenuBar(true);
	    final PopupPanel menuPopup = new PopupPanel(true);
	    
	    menu.addItem(new MenuItem("Insert column right", new Command() {
			public void execute() {
				_insertCol(col + 1);
				menuPopup.hide();
			}
	    }));
	    menu.addItem(new MenuItem("Insert column left", new Command() {
			public void execute() {
				_insertCol(col);
				menuPopup.hide();
			}
	    }));
	    
	    // do not allow to remove the client column if it's the only one:
	    if ( flexTable.getCellCount(0) > 2 ) {
		    menu.addSeparator();
		    menu.addItem(new MenuItem("Delete column", new Command() {
				public void execute() {
					_deleteCol(col);
					menuPopup.hide();
				}
		    }));
	    }
	    
	    menuPopup.setWidget(menu);
	    menuPopup.setPopupPosition(left, top);
		menuPopup.show();
		
	}

	private void _insertCol(int col) {
		for ( int r = 0, rows = flexTable.getRowCount(); r < rows; r++ ) {
			flexTable.insertCell(r, col);
			_setCell(r, col, "");
		}
		_updateControlColumns(flexTable.getCellCount(0) - 1);
		_updatePositions(0, 0);
	}
	private void _deleteCol(int col) {
		for ( int r = 0, rows = flexTable.getRowCount(); r < rows; r++ ) {
			flexTable.removeCell(r, col);
		}
		_updateControlColumns(flexTable.getCellCount(0) - 1);
		_updatePositions(0, 0);
	}
	
	private void _updatePositions(int row, int col) {
		for ( int r = row, rows = flexTable.getRowCount(); r < rows; r++ ) {
			for ( int c = col, cols = flexTable.getCellCount(r); c < cols; c++ ) {
				Widget widget = _getWidget(r, c);
				if ( widget instanceof TableCell ) {
					((TableCell) widget).updatePosition(r, c);
				}
			}
			col = 0;  // for next row
		}
	}

	

	/**
	 * Gets the contents in CSV format.
	 * 
	 * @param emptyFieldString If non-null, will be used for empty fields in the body (not on the header).
	 * 
	 * @param separator desired field separator; some descriptive names are recognized if they start with
	 *        one of "Comma", "Semi-colon", "Vertical bar", or "Tab" (ignoring case), so the corresponding
	 *        character will be used. Other separator value will be used exactly as given.
	 * 
	 * @return The contents in CSV format with the given separator.
	 */
	public String getCsv(String emptyFieldString, String separator) {
		if ( separator == null || separator.equals(",") || separator.toLowerCase().startsWith("comma") ) {
			separator = ",";
		}
		else if ( separator.equals(";") || separator.toLowerCase().startsWith("semi-colon") ) {
			separator = ";";
		}
		else if ( separator.equals("|") || separator.toLowerCase().startsWith("vertical bar") ) {
			separator = "|";
		}
		else if ( separator.equals("\t") || separator.toLowerCase().startsWith("tab") ) {
			separator = "\t";
		}
		
		StringBuffer sb = new StringBuffer();
		int rows = flexTable.getRowCount();
		
		// header
		int cols = flexTable.getCellCount(0);
		String sep = "";
		for ( int col = CONTROL_COL + 1; col < cols; col++ ) {
			TableCell html = (TableCell) _getWidget(HEADER_ROW, col);
			String text = html.getText();
			if ( text.indexOf(separator) >= 0 ) {
				text = '"' +text+ '"';
			}
			sb.append(sep + text);
			sep = separator;
		}
		sb.append('\n');

		// terms:
		for ( int row = FIRST_REGULAR_ROW; row < rows; row++ ) {
			//
			// Note: empty rows are ignored.
			
			StringBuffer line = new StringBuffer();
			boolean empty = true;
			cols = flexTable.getCellCount(row);
			sep = "";
			for ( int col = CONTROL_COL + 1; col < cols; col++ ) {
				TableCell html = (TableCell) _getWidget(row, col);
				String text = html.getText().trim();
				if ( text.length() > 0 ) {
					empty = false;
				}
				else if ( emptyFieldString != null ) {
					text = emptyFieldString;
				}
				if ( text.indexOf(separator) >= 0 ) {
					text = '"' +text+ '"';
				}
				line.append(sep + text);
				sep = separator;
			}
			if ( !empty ) {
				sb.append(line.toString() + '\n');
			}
		}
		return sb.toString();
	}

	
	static class TermError extends CheckError {
		int line;
		int column;
		TermError(int line, int column, String msg) {
			super(msg);
			this.line = line;
			this.column = column;
		}
		public String toString() {
			if ( line < 0 && column < 0 ) {
				return msg;
			}
			
			StringBuffer sb = new StringBuffer("[");
			if ( line >= 0 ) {
				sb.append(line == 0 ? "header" : "row " +line);
				sb.append(":");
			}
			
			if ( column >= 0 ) {
				sb.append("column " +column);
			}
			
			sb.append("] " +msg);
			
			return sb.toString();
		}

	}
	
	/**
	 * Check the contents of the table.
	 * @return An error; null if OK
	 */
	public TermError check() {
		int rows = flexTable.getRowCount();
		
		// header
		int cols = flexTable.getCellCount(0);
		for ( int col = CONTROL_COL + 1; col < cols; col++ ) {
			TableCell tcell = (TableCell) _getWidget(HEADER_ROW, col);
			String text = tcell.getText().trim();
			if ( text.length() == 0 ) {
				tcell.setFocus(true);
				return new TermError(0, col, "Missing column header in term table");
			}
		}

		int actualRowsWithContents = 0;
		
		// to check for duplicate keys
		Set<String> keys = new HashSet<String>();
		
		// terms:
		for ( int row = FIRST_REGULAR_ROW; row < rows; row++ ) {
			//
			// Note: empty rows are OK -- they're ignored.
			
			boolean empty = true;
			TermError error = null;    // will get the first error, if any
			
			cols = flexTable.getCellCount(row);
			for ( int col = CONTROL_COL + 1; col < cols; col++ ) {
				TableCell tcell = (TableCell) _getWidget(row, col);
				String text = tcell.getText().trim();
				if ( text.length() > 0 ) {
					empty = false;
					
					if ( col == FIRST_REGULAR_COL ) {
						// the key column;
						String key = text;
						if ( keys.contains(key) ) {
							tcell.setFocus(true);
							return new TermError((row -FIRST_REGULAR_ROW + 1), col, "Duplicate key value in term table: " +key);
						}
						else {
							keys.add(key);
						}
					}
				}
				else if ( col > CONTROL_COL + 1) {
					// empty column (except the first column, which is the key) is OK. See issue #119. 
				}
				else if ( error == null ) {
					tcell.setFocus(true);
					error = new TermError((row -FIRST_REGULAR_ROW + 1), col, "Missing value in column " +col+ 
							" in term table");
				}
			}
			
			if ( empty ) {
				// allow the last row to be empty.
				if ( row < rows -1 ) {
					// it is not the last row:
					TableCell tcell = (TableCell) _getWidget(row, FIRST_REGULAR_COL);
					tcell.setFocus(true);
					error = new TermError((row -FIRST_REGULAR_ROW + 1), -1, "Row is empty in term table");
				}
				// ELse: OK: accept last row empty.
			}
			else if ( error != null ) {
				return error;
			}
			else {
				actualRowsWithContents++;
			}
		}
		
		if ( actualRowsWithContents == 0 ) {
			return new TermError(-1, -1, "Empty contents in term table");
		}
		
		return null;
	}
	
	
	private class TableCell extends HorizontalPanel {
		private TextBox contents = new TextBox();

		private int actualRow;
		private int actualCol;
		
		
		TableCell(int actualRow, int actualCol, String text) {
			this(actualRow, actualCol, text, false);
		}
		
		TableCell(int row, int col, String text, boolean html) {
			this.actualRow = row;
			this.actualCol = col;
			
			contents.setStylePrimaryName("TermTable-termField");
			contents.setReadOnly(true);
			contents.setWidth("270px");
			
			contents.addFocusListener(new FocusListener() {
				public void onFocus(Widget sender) {
					if ( scrollPanel != null ) {
						if ( actualRow <= FIRST_REGULAR_ROW ) {
							scrollPanel.scrollToTop();
						}
						if ( actualCol <= FIRST_REGULAR_COL ) {
							scrollPanel.scrollToLeft();
						}
					}
					contents.addStyleDependentName("focused");
					contents.selectAll();
				}

				public void onLostFocus(Widget sender) {
					contents.removeStyleDependentName("focusedEdit");
					contents.setReadOnly(true);
					contents.selectAll();

					contents.removeStyleDependentName("focused");
				}
			});

			if ( ! readOnly ) {
				contents.addKeyboardListener(new KeyboardListenerAdapter() {
					public void onKeyPress(Widget sender, char keyCode, int modifiers) {

						if ( keyCode == KEY_ENTER ) {
							if ( contents.isReadOnly() ) {
								contents.setReadOnly(false);
								contents.addStyleDependentName("focusedEdit");
								int len = contents.getText().length();

								contents.setCursorPos(len);

							}
							else {
								contents.removeStyleDependentName("focusedEdit");
								contents.setReadOnly(true);
								contents.selectAll();
							}
							return;
						}

						if ( !contents.isReadOnly() ) {
							return;
						}

						int row = actualRow;
						int col = actualCol;
						if ( keyCode == KEY_DOWN || keyCode == KEY_UP ) {
							row = actualRow + (keyCode == KEY_DOWN ? 1 : -1);
							contents.cancelKey();
						}
						else if ( keyCode == KEY_RIGHT || keyCode == KEY_LEFT ) {
							col = actualCol + (keyCode == KEY_RIGHT ? 1 : -1);
							contents.cancelKey();
						}	

						if ( row == actualRow && col == actualCol ) {
							return;
						}

						if ( row < 0 || col < 0 ) {
							return;
						}

						if ( row >= flexTable.getRowCount() ) {
							return;
						}

						if ( col >= flexTable.getCellCount(row) ) {
							return;
						}

						Widget widget = _getWidget(row, col);
						if ( ! (widget instanceof TableCell) ) {
							return;
						}

						TableCell tcell = (TableCell) widget;
						if ( tcell != null ) {
							tcell.setFocus(true);
						}
					}
				});
			}
			
			contents.setText(text);
			add(contents);
		}

		
		public void setFocus(boolean b) {
			contents.setFocus(b);
		}

		void updatePosition(int row, int col) {
			actualRow = row;
			actualCol = col;
		}

		void setText(String text) {
			contents.setText(text);
		}

		String getText() {
			return contents.getText();
		}
	}
}
