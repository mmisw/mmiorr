package org.mmisw.ontmd.gwt.client.voc2rdf;

import org.mmisw.ontmd.gwt.client.util.Util;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;

/**
 * A editable, spreadsheet-like table.
 * 
 * @author Carlos Rueda
 */
public class TermTable extends VerticalPanel {
	
	private static final int CONTROL_ROW = 0;
	private static final int HEADER_ROW = 1;
	private static final int FIRST_REGULAR_ROW = 2;
	private static final int CONTROL_COL = 0;
	
	
	private final FlexTable flexTable = new FlexTable();
	private PopupPanel popup;
	private TextBoxBase textBox;
	private int currRow;
	private int currCol;
	private TableCell currCell;
	

	/**
	 * Create a editable table for the terms.
	 * @param cols number of desired columns
	 */
	TermTable(int cols) {
		this.add(flexTable);
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

		_preparePopUp();
		_updateStyles(1);
		_updatePositions(0, 0);
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
	 * 
	 */
	private void _preparePopUp() {
		popup = new DecoratedPopupPanel(true, true) {
			public boolean onKeyUpPreview(char keyCode, int modifiers) {
				if ( keyCode == KeyboardListener.KEY_ESCAPE
				||  keyCode == KeyboardListener.KEY_ENTER 
				) {
					hide();
					currCell.setFocus(true);
					return false;
				}
				return true;
			}
		};
		popup.setWidth("250");
		textBox = Util.createTextBoxBase(1, "250", null);
		popup.setWidget(textBox);
		
		textBox.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				currCell.setText(textBox.getText());
				currCell.setFocus(true);
			}
		});
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
	 * 
	 */
	private void _editCell(boolean show) {
		int left = currCell.getAbsoluteLeft();
		int top = currCell.getAbsoluteTop();

		textBox.setText(currCell.getText());
		
		popup.setPopupPosition(left, top);
		
		if ( show ) {
			new Timer() { @Override
				public void run() {
					textBox.setFocus(true);
				}
			}.schedule(180);
			
			popup.show();
		}
		else {
			textBox.setFocus(true);
		}
	}

	/**
	 * Gets the contents in CSV format.
	 * @param emptyFieldString If non-null, will be used for empty fields in the body (not on the header).
	 * @return
	 */
	public String getCsv(String emptyFieldString) {
		StringBuffer sb = new StringBuffer();
		int rows = flexTable.getRowCount();
		
		// header
		int cols = flexTable.getCellCount(0);
		String sep = "";
		for ( int col = CONTROL_COL + 1; col < cols; col++ ) {
			TableCell html = (TableCell) _getWidget(HEADER_ROW, col);
			String text = html.getText();
			if ( text.indexOf(',') >= 0 ) {
				text = '"' +text+ '"';
			}
			sb.append(sep + text);
			sep = ",";
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
				if ( text.indexOf(',') >= 0 ) {
					text = '"' +text+ '"';
				}
				line.append(sep + text);
				sep = ",";
			}
			if ( !empty ) {
				sb.append(line.toString() + '\n');
			}
		}
		return sb.toString();
	}

	/**
	 * Check the contents of the table.
	 * @return An error message; null if OK
	 */
	public String check() {
		int rows = flexTable.getRowCount();
		
		// header
		int cols = flexTable.getCellCount(0);
		for ( int col = CONTROL_COL + 1; col < cols; col++ ) {
			TableCell html = (TableCell) _getWidget(HEADER_ROW, col);
			String text = html.getText().trim();
			if ( text.length() == 0 ) {
				return "Missing column header: " +col;
			}
		}

		int actualRowsWithContents = 0;
		
		// terms:
		for ( int row = FIRST_REGULAR_ROW; row < rows; row++ ) {
			//
			// Note: empty rows are OK -- they're ignored.
			
			boolean empty = true;
			String error = null;    // will get the first error, if any
			
			cols = flexTable.getCellCount(row);
			for ( int col = CONTROL_COL + 1; col < cols; col++ ) {
				TableCell html = (TableCell) _getWidget(row, col);
				String text = html.getText().trim();
				if ( text.length() > 0 ) {
					empty = false;
				}
				else if ( col > CONTROL_COL + 1) {
					// empty column (except the first columns, which is the key) is OK. See issue #119. 
				}
				else if ( error == null ) {
					error = "Line " +(row -FIRST_REGULAR_ROW + 1)+ ": Missing value in column " +col;
				}
			}
			
			if ( empty ) {
				if ( row < rows -1 ) {
					error = "Line " +(row -FIRST_REGULAR_ROW + 1)+ " is empty";
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
			return "Empty contents";
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
			contents.setWidth("250px");
			
			contents.addFocusListener(new FocusListener() {
				public void onFocus(Widget sender) {
					contents.addStyleDependentName("focused");
					contents.selectAll();
				}

				public void onLostFocus(Widget sender) {
					contents.removeStyleDependentName("focused");
				}
			});

			contents.addKeyboardListener(new KeyboardListenerAdapter() {
				public void onKeyPress(Widget sender, char keyCode, int modifiers) {

					if ( keyCode == KEY_ENTER ) {
						if ( contents.isReadOnly() ) {
							contents.setReadOnly(false);
							contents.addStyleDependentName("focusedEdit");
							int len = contents.getText().length();
							
							
//							int vislen = contents.getVisibleLength();
//							if ( vislen < len ) {
//								contents.setVisibleLength(len);
//							}
							
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
