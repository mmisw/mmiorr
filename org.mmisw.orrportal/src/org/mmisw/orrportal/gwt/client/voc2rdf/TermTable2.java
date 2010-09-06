package org.mmisw.orrportal.gwt.client.voc2rdf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mmisw.orrportal.gwt.client.Orr;
import org.mmisw.orrportal.gwt.client.util.TLabel;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;

/**
 * A editable, spreadsheet-like table.
 * 
 * @author Carlos Rueda
 */
public class TermTable2 extends VerticalPanel {
	
	private static final String CONTENTS_TOOTIP =
		"<b>Terms</b>:<br/>" +
		"The 'words' (concepts, labels, unique IDs or code, or similar unique tags) of your vocabulary. " +
		"The contents should contain a one line header with the descriptive titles for each column. " +
		"Each line (row) should contain the unique label for each term followed by a set of values, " +
		"corresponding to the header descriptive titles. The first column should contain the unique " +
		"label for each term. It will be used to create a unique identifier. (Typical column titles " +
		"include Description, Notes, See Also, or others -- these all add information to help " +
		"describe your terms. These are treated as annotations in the ontology.) " +
//		"Column values should be separated by comma characters; empty fields are " +
//		"represented by two commas or tabs in a row. Each record (row) should be separated by a " +
//		"return or end of line character. " +
		"All term labels must be unique. " +
		"<br/>" +
		"<br/>" +
		"Type Enter to edit a cell. Type Enter again to complete the change (or just move to a different " +
		"field in the form using the navigation keys or the mouse). " +
		"<br/>" +
		"<br/>" +
		"Use the Import button to set the contents of the table from CSV formatted text. <br/>" +
		"Use the Export button to get a text version of the current contents of the table. " 
		;

	private static final int CONTROL_ROW = 0;
	private static final int HEADER_ROW = 1;
	private static final int FIRST_REGULAR_ROW = 2;
	private static final int CONTROL_COL = 0;
	private static final int FIRST_REGULAR_COL = 1;
	
	// disable this while we better assess peformance impact
	private static final boolean SET_EVEN_N_ODD_ROW_STYLE = false;
	
	private ScrollPanel scrollPanel;
	private final FlexTable flexTable = new FlexTable();
	
//	private int currRow;
//	private int currCol;
//	private TableCell currCell;
	
	private boolean readOnly;
	
	private final TLabel termsTLabel = new TLabel("", false, CONTENTS_TOOTIP);


	private Set<Image> imgs = new HashSet<Image>();
	
	private TermTableInterface tti;
	

	/**
	 * Create a editable table for the terms.
	 * @param cols number of desired columns
	 * @param termsTLabel 
	 */
	public TermTable2(TermTableInterface tti, int cols, boolean readOnly) {
		this.tti = tti;
		this.add(flexTable);
		this.readOnly = readOnly;
		
		flexTable.setBorderWidth(1);
		flexTable.setWidth("100%");
		
		flexTable.setStylePrimaryName("TermTable");
		
//		flexTable.addTableListener(new TableListener() {
//			public void onCellClicked(SourcesTableEvents sender, int row, int col) {
//				_cellClicked(row, col);
//			}
//		});
		
		addRow(cols);   // control row
		addRow(cols);   // header row

		_updateControlColumns(cols);

		_setCell(HEADER_ROW, 0, "");

		_updateStyles(1);
		_updatePositions(0, 0);
	}
	
	public List<String> getHeaderCols() {
		List<String> headerCols = new ArrayList<String>();
		int cols = flexTable.getCellCount(0);
		for ( int col = CONTROL_COL + 1; col < cols; col++ ) {
			TableCell html = (TableCell) _getWidget(HEADER_ROW, col);
			String text = html.getText();
			headerCols.add(text);
		}
		return headerCols;
	}
	
	public void setReadOnly(boolean readOnly) {
		if ( this.readOnly == readOnly ) {
			return;
		}
		this.readOnly = readOnly;
		
		for ( Image img : imgs ) {
			img.setVisible(!readOnly);
		}
		
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
	
	
	Image _createMenu(final int row, final int col) {
		Image img = Orr.images.tridown().createImage();
		img.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				_cellClicked(row, col);
			}
		});
		img.setVisible(!readOnly);
		
		imgs.add(img);
		
		return img;
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
				
//				if ( readOnly ) {
//					hp.add(html);
//					hp.add(filler);
//					
//					hp.setCellWidth(html, "80%");
//					hp.setCellWidth(filler, "20%");
//				}
//				else {
					Image img = _createMenu(row, col);
					
					hp.add(img);
					hp.add(html);
					hp.add(filler);
					
					hp.setCellWidth(img, "30%");
					hp.setCellWidth(html, "50%");
					hp.setCellWidth(filler, "20%");
					
					hp.setCellHorizontalAlignment(img, ALIGN_LEFT);
//				}
			}
			else { //if ( ! readOnly ) {
				Image img = _createMenu(row, col);
				hp.add(img);
			}
			
		}
		else if ( row == CONTROL_ROW && col == CONTROL_COL ) {
			HorizontalPanel hp = new HorizontalPanel();
			hp.setVerticalAlignment(ALIGN_MIDDLE);
			Image img = _createMenu(row, col);
			hp.add(img);
			if ( termsTLabel != null ) {
				hp.add(termsTLabel);
			}
			_setWidget(row, col, hp);	
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
			
			if ( SET_EVEN_N_ODD_ROW_STYLE && row >= FIRST_REGULAR_ROW ) {
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
			else {
				_dispatchRootCell();
			}
		}
		else {
			if ( col == 0 ) {
				_dispatchRowMenu(row);
			}
			// No need to set focus regular cell, as each cell here is now a FocusPanel, so it gets
			// its focus from the system
//			else {
//				// dispatch regular cell:
//				currRow = row;
//				currCol = col;
//				currCell = (TableCell) _getWidget(currRow, currCol);
//				
//				//_editCell(true);
//				currCell.setFocus(true);
//			}
		}
	}
	
	/**
	 * 
	 */
	private void _dispatchRowMenu(final int row) {
//		if ( readOnly ) {
//			return;
//		}
		
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

	
	private void _dispatchRootCell() {

		Widget ww = _getWidget(CONTROL_COL, CONTROL_ROW);
		int left = ww.getAbsoluteLeft();
		int top = ww.getAbsoluteTop();
		tti.dispatchTableMenu(left, top);
	}
	
	
	private void _dispatchColumnHeader(final int col) {

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
		
		// to check for duplicate header labels, and then for duplicate keys (values in first column):
		Set<String> keys = new HashSet<String>();
		

		// header
		int cols = flexTable.getCellCount(0);
		for ( int col = CONTROL_COL + 1; col < cols; col++ ) {
			TableCell tcell = (TableCell) _getWidget(HEADER_ROW, col);
			String text = tcell.getText().trim();
			
			// missing label?
			if ( text.length() == 0 ) {
				tcell.setFocus(true);
				return new TermError(0, col, "Missing column header in term table");
			}
			
			// duplicate label?
			if ( keys.contains(text) ) {
				tcell.setFocus(true);
				return new TermError((HEADER_ROW + 1), col, "Duplicate label in term table: " +text);
			}
			else {
				keys.add(text);
			}
		}

		
		keys.clear();
		
		int actualRowsWithContents = 0;
		
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
	
	
	private class TableCell extends HorizontalPanel  {
//		private TextBox contents = new TextBox();
		private HTML contents = new HTML();

		private int actualRow;
		private int actualCol;
		
		private final FocusPanel focusPanel;
		
		
		
		TableCell(int actualRow, int actualCol, String text) {
			this(actualRow, actualCol, text, false);
		}
		
		TableCell(int row, int col, String text, boolean html) {
			this.actualRow = row;
			this.actualCol = col;
			
			if ( text.trim().length() > 0 ) {
				contents.setSize("100%", "100%");
			}
			else {
				contents.setSize("30px", "16px");
			}
			contents.setText(text);

			focusPanel = new FocusPanel(contents);
			focusPanel.setStylePrimaryName("TermTable-termField");
			focusPanel.setSize("100%", "100%");
			
			
			focusPanel.addFocusListener(new FocusListener() {
				public void onFocus(Widget sender) {
					if ( readOnly ) {
						return;
					}
					
					if ( scrollPanel != null ) {
						if ( actualRow <= FIRST_REGULAR_ROW ) {
							scrollPanel.scrollToTop();
						}
						if ( actualCol <= FIRST_REGULAR_COL ) {
							scrollPanel.scrollToLeft();
						}
					}
					focusPanel.addStyleDependentName("focused");
//					contents.addStyleDependentName("focused");
//					contents.selectAll();
				}

				public void onLostFocus(Widget sender) {
					if ( readOnly ) {
						return;
					}
					
					focusPanel.removeStyleDependentName("focused");
				}
			});

			focusPanel.addKeyboardListener(new KeyboardListenerAdapter() {
				boolean processed;
				public void onKeyDown(Widget sender, char keyCode, int modifiers) {
					processed = false;
					if ( readOnly ) {
						return;
					}
					
					Orr.log("onKeyUp: keyCode=" +keyCode+ ", modifiers=" +modifiers+ " sender=" +sender.getClass().getName());
					
					int row = actualRow;
					int col = actualCol;
					if ( keyCode == KEY_DOWN || keyCode == KEY_UP ) {
						processed = true;
						row = actualRow + (keyCode == KEY_DOWN ? 1 : -1);
//						focusPanel.cancelKey();
					}
					else if ( keyCode == KEY_RIGHT || keyCode == KEY_LEFT ) {
						processed = true;
						col = actualCol + (keyCode == KEY_RIGHT ? 1 : -1);
//						contents.cancelKey();
					}	
					else if ( keyCode == KEY_TAB ) {
						processed = true;
						// let default behavior act by itself
						return;
					}
					else {
						// see if we should enter editing mode, either upon ENTER key or by
						// receiving a regular character. Otherwise, just return.
						boolean enterEditing = false;
						char firstKeyForEditing = 0;
						if ( keyCode == KEY_ENTER ) {
							enterEditing = true;
						}
//						else if ( modifiers == KeyboardListener.MODIFIER_SHIFT || modifiers == 0 ) {
//							enterEditing = true;
//							firstKeyForEditing = keyCode;
//						}
						
						if ( enterEditing ) {
							processed = true;
							_processEnterEditing(firstKeyForEditing);
						}

						return;
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

					Widget widget = flexTable.getWidget(row, col);
					if ( widget instanceof TableCell ) {
//						Orr.log("focusing TableCell row,col= " +row+ "," +col);
						((TableCell) widget).setFocus(true);
					}
					
				}
				public void onKeyPress(Widget sender, char keyCode, int modifiers) {
					if ( processed ) {
						return;
					}
					
					if ( readOnly ) {
						return;
					}

					
//					Orr.log("onKeyPress: keyCode=" +keyCode+ ", modifiers=" +modifiers);


					if ( keyCode == KEY_TAB ) {
						// let default behavior act by itself
						return;
					}
					else {
						// se if we should enter editing mode, either upon ENTER key or by
						// receiving a regular character. Otherwise, just return.
						boolean enterEditing = false;
						char firstKeyForEditing = 0;
						if ( modifiers == KeyboardListener.MODIFIER_SHIFT || modifiers == 0 ) {
							enterEditing = true;
							firstKeyForEditing = keyCode;
						}
						
						if ( enterEditing ) {
							_processEnterEditing(firstKeyForEditing);
						}

						return;
					}

				}
			});

			
			add(focusPanel);
		}

		protected void _processEnterEditing(final char firstKeyForEditing) {
			
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					final TextArea ta = new TextArea();
					String text = contents.getText().trim();
					
					if ( firstKeyForEditing != 0 ) {
						text += firstKeyForEditing;
					}
					ta.setText(text);
					
					int ww = focusPanel.getOffsetWidth();
					int hh = (int) (1.3 * focusPanel.getOffsetHeight());
					
					hh = Math.max(hh, 40);
					ww = Math.max(ww, 100);
					
//					ta.setHeight(hh+ "px");
					ta.setSize(ww+ "px", hh+ "px");
//					ta.setCharacterWidth(text.length());
					
					ta.addFocusListener(new FocusListener() {
						public void onFocus(Widget sender) {
						}
						public void onLostFocus(Widget sender) {
							RootPanel.get().remove(ta);
							focusPanel.setFocus(true);
						}
					});

					ta.addKeyboardListener(new KeyboardListenerAdapter() {
						public void onKeyPress(Widget sender, char keyCode, int modifiers) {
							String text = ta.getText();
							Orr.log("onKeyPress: keyCode=" +keyCode+ ", modifiers=" +modifiers+ " text=[" +text+ "]");
							
							if ( keyCode == KEY_ENTER || keyCode == KEY_TAB ) {
								if ( keyCode == KEY_ENTER ) {
									ta.cancelKey();
								}
								contents.setText(text);
								if ( text.length() > 0 ) {
									contents.setSize("100%", "100%");
								}
								RootPanel.get().remove(ta);
								
								// add new row automatically if we are in the last row:
								if ( actualRow == flexTable.getRowCount() -1
								&&   actualCol == flexTable.getCellCount(actualRow) -1
								) {
									addRow(flexTable.getCellCount(actualRow) - 1);
									Widget widget = flexTable.getWidget(actualRow +1, FIRST_REGULAR_COL);
//									Orr.log("widget= " +widget.getClass().getName());
									if ( widget instanceof TableCell ) {
										((TableCell) widget).setFocus(true);
									}
								}
								else {
									focusPanel.setFocus(true);
								}
								return;
							}
							if ( keyCode == KEY_ESCAPE ) {
								RootPanel.get().remove(ta);
								ta.cancelKey();
								focusPanel.setFocus(true);
								return;
							}
						}
					});
					
					int left = focusPanel.getAbsoluteLeft();
					int top = focusPanel.getAbsoluteTop();
					
					RootPanel.get().add(ta, left, top);
					ta.setFocus(true);
					ta.setCursorPos(text.length());
				}
			});
		}

		
		public void setFocus(boolean b) {
//			contents.setFocus(b);
			focusPanel.setFocus(b);
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

	public int getNumRows() {
		return flexTable.getRowCount();
	}
	
	public List<List<String>> getRows() {
		List<List<String>> rows = new ArrayList<List<String>>();
		
		for (int rr = FIRST_REGULAR_ROW, numRows = flexTable.getRowCount(); rr < numRows; ++rr) {
			List<String> row = new ArrayList<String>();
			
			for ( int cc = FIRST_REGULAR_COL, numCols = flexTable.getCellCount(rr); cc < numCols; cc++ ) {
				TableCell html = (TableCell) _getWidget(rr, cc);
				String text = html.getText();
				row.add(text);
			}
			
			// add the row only if not empty:
			for ( String text : row ) {
				if ( text.trim().length() > 0 ) {
					rows.add(row);
					break;
				}
			}
		}

		return rows;
	}
}
