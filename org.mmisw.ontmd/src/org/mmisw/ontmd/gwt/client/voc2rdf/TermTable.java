package org.mmisw.ontmd.gwt.client.voc2rdf;

import org.mmisw.ontmd.gwt.client.Util;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author Carlos Rueda
 */
public class TermTable extends VerticalPanel {
	
	private final FlexTable flexTable = new FlexTable();
	private PopupPanel popup;
	private TextBoxBase textBox;
	private int currRow;
	private int currCol;
	private HTML currCell;
	

	/**
	 * Create a table for the terms.
	 */
	TermTable() {
		this.add(flexTable);
		flexTable.setBorderWidth(1);
		flexTable.setWidth("700");
//		flexTable.setHeight("200");
		
		flexTable.setStylePrimaryName("TermTable");
		
		HTMLTable.RowFormatter rf = flexTable.getRowFormatter();
		rf.addStyleName(0, "TermTable-header");
		
		flexTable.addTableListener(new TableListener() {
			public void onCellClicked(SourcesTableEvents sender, int row, int col) {
				if ( col > 0 ) {
					currRow = row;
					currCol = col;
					editCell(true);
				}
			}
		});
		preparePopUp();
	}
	
	/**
	 * Create a table for the terms with entries for testing.
	 */
	TermTable(int rows, int cols) {
		this();
		for (int row = 0; row <= rows; row++ ) {
			for (int col = 0; col < cols; col++ ) {
				String str = "cell_" +row+ "," +col;
				Widget cell = new HTML(str);
				setWidget(row, col, cell);
			}
		}
	}
	
	/**
	 * Sets a cell in this table.
	 * @param row
	 * @param col
	 * @param cell
	 */
	void setWidget(int row, int col, Widget cell) {
		flexTable.setWidget(row, col, cell);
	}
	
	/**
	 * Sets a cell in this table.
	 * @param row
	 * @param col
	 * @param cell
	 */
	void setHtml(int row, int col, String html) {
		flexTable.setWidget(row, col, new HTML(html));
	}
	
	/**
	 * Call this when all rows have been inserted.
	 */
	void prepareStyles() {
		HTMLTable.RowFormatter rf = flexTable.getRowFormatter();
		rf.addStyleName(0, "TermTable-header");
		for (int row = 1, rows = flexTable.getRowCount(); row < rows; ++row) {
			rf.addStyleName(row, (row % 2) != 0 ? "TermTable-OddRow" : "TermTable-EvenRow");
		}
	}

	/**
	 * 
	 */
	private void preparePopUp() {
		popup = new DecoratedPopupPanel(true, true) {
			public boolean onKeyUpPreview(char keyCode, int modifiers) {
				if ( keyCode == KeyboardListener.KEY_ESCAPE
				||  keyCode == KeyboardListener.KEY_ENTER 
				) {
					hide();
					return false;
				}
				return dispatchKey(keyCode, modifiers);
			}
		};
		popup.setWidth("210");
		textBox = Util.createTextBoxBase(1, "200", null);
		popup.setWidget(textBox);
		
		textBox.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				currCell.setText(textBox.getText());
			}
		});
		
//		textBox.addKeyboardListener(new KeyboardListenerAdapter() {
//			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
//				dispatchKey(keyCode, modifiers);
//			}
//		});
		
	}

	/**
	 * 
	 * @param keyCode
	 * @param modifiers
	 * @return
	 */
	private boolean dispatchKey(char keyCode, int modifiers) {
		if ( keyCode == KeyboardListener.KEY_TAB
		||  keyCode == KeyboardListener.KEY_ENTER
		) {
			int rows = flexTable.getRowCount();
			int cols = flexTable.getCellCount(0);
			
			int nextRow = currRow;
			int nextCol = currCol + 1;
			
			if ( 0 != (modifiers & KeyboardListener.KEY_SHIFT) ) {
				nextCol = currCol - 1;
				if ( nextCol < 1 ) {
					nextCol = cols - 1;
					nextRow--;
				}
			}
			else {
				if ( nextCol >= cols ) {
					nextCol = 1;
					nextRow++;
				}
			}					
			if ( 0 < nextRow && nextRow < rows 
			&&   0 < nextCol && nextCol < cols
			&&   currRow != nextRow
			&&   currCol != nextCol
			) {
				// OK
				
				currRow = nextRow;
				currCol = nextCol;
				new Timer() { @Override
					public void run() {
						editCell(false);
					}
				}.schedule(180);

			}
			
			return false;
		}
		
		return true;
	}

	/**
	 * 
	 */
	private void editCell(boolean show) {
		currCell = (HTML) flexTable.getWidget(currRow, currCol);
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
	
	  
}
