package org.mmisw.orrportal.gwt.client.util.table.utab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.mmisw.orrportal.gwt.client.Orr;
import org.mmisw.orrportal.gwt.client.util.table.IRow;
import org.mmisw.orrportal.gwt.client.util.table.IUtilTable;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * A table with sortable columns.
 * 
 * @author Carlos Rueda
 */
public class UtilTable implements IUtilTable {

	private boolean editMode;
	
	private List<IRow> rows;

	
	private final FlexTable flexPanel = new FlexTable();

	private List<ColHeader> colHeaders;
	
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

	public UtilTable(List<String> colNames) {
		this(colNames.toArray(new String[colNames.size()]));
	}
	
	public UtilTable(String... colLabels) {
		super();
		
		flexPanel.setCellPadding(4);
		
		flexPanel.setBorderWidth(1);
		flexPanel.setWidth("100%");
		flexPanel.setStylePrimaryName("OntologyTable");
		
		colHeaders = new ArrayList<ColHeader>();
		sortColumn = colLabels.length > 0 ? colLabels[0] : "";
		
		for ( int i = 0; i < colLabels.length; i++ ) {
			ColHeader colHeader = new ColHeader(colLabels[i]);
			colHeaders.add(colHeader);
		}
		
		prepareHeader();
	}
	
	private class ColHeader {
		HTML html;
		ColHeader(String colLabel) {
			html = new HTML(colLabel);
			if ( colLabel.length() > 0 ) {
				html.addClickListener(columnHeaderClickListener);
			}
		}

		Widget getWidget() {
			return html;
		}

		String getText() {
			return html.getText();
		}
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
		
		for ( int i = 0, count = colHeaders.size(); i < count; i++ ) {
			ColHeader colHeader = colHeaders.get(i);
			flexPanel.setWidget(row, i, colHeader.getWidget());
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
		
		// incrementally update the interface (preferred):
		UpdateCommand cmd = new UpdateCommand(0);
		DeferredCommand.addCommand(cmd);			
	}

	
	private class Cell {
		private int actualRow;
		private int actualCol;
		private HTML valHtml;
		private FocusPanel focusPanel;
		
		private boolean readOnly = true;
		
		Cell(int row, int col, HTML valHtml) {
			this.actualRow = row;
			this.actualCol = col;
			this.valHtml = valHtml;
			focusPanel = new FocusPanel(valHtml);

			// this was added as a quick "fix" to limit the width of columns
			// specially useful in the case of the CF vocabulary:
			valHtml.setStylePrimaryName("cellTextBreakWord");
			
			focusPanel.setStylePrimaryName("TermTable-termField");
//			focusPanel.setSize("100%", "100%"); <- unnecessary
			
			
			focusPanel.addFocusListener(new FocusListener() {
				public void onFocus(Widget sender) {
					if ( editMode ) {
						focusPanel.addStyleDependentName("focused");
					}
				}

				public void onLostFocus(Widget sender) {
					if ( editMode ) {
						focusPanel.removeStyleDependentName("focused");
						readOnly = true;
					}
				}
			});
			
			
			focusPanel.addKeyboardListener(new KeyboardListenerAdapter() {
				public void onKeyPress(Widget sender, char keyCode, int modifiers) {
					
					if ( ! editMode ) {
						return;
					}

					if ( keyCode == KEY_ENTER ) {
						_processEnter();
						return;
					}

					if ( ! readOnly ) {
						return;
					}

					int row = actualRow;
					int col = actualCol;
					if ( keyCode == KEY_DOWN || keyCode == KEY_UP ) {
						row = actualRow + (keyCode == KEY_DOWN ? 1 : -1);
//						contents.cancelKey();
					}
					else if ( keyCode == KEY_RIGHT || keyCode == KEY_LEFT ) {
						col = actualCol + (keyCode == KEY_RIGHT ? 1 : -1);
//						contents.cancelKey();
					}	

					if ( row == actualRow && col == actualCol ) {
						return;
					}

					if ( row < 0 || col < 0 ) {
						return;
					}

					if ( row >= flexPanel.getRowCount() ) {
						return;
					}

					if ( col >= flexPanel.getCellCount(row) ) {
						return;
					}

					Widget widget = flexPanel.getWidget(row, col);
					if ( widget instanceof FocusPanel ) {
						Orr.log("focusing row,col= " +row+ "," +col);
						((FocusPanel) widget).setFocus(true);
					}
				}
			});
		}
		
		FocusPanel getWidget() {
			return focusPanel;
		}

		protected void _processEnter() {
			readOnly = ! readOnly;
			if ( readOnly ) {
				return;
			}
			
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					final TextArea ta = new TextArea();
					String text = valHtml.getText();
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

							if ( keyCode == KEY_ENTER ) {
								ta.cancelKey();
								valHtml.setText(ta.getText());
								RootPanel.get().remove(ta);
								focusPanel.setFocus(true);
								return;
							}
							if ( keyCode == KEY_ESCAPE ) {
								RootPanel.get().remove(ta);
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

	}
	
	/**
	 * 
	 * @param row Row in the flexTable
	 * @param irow
	 */
	private void _setRow(final int row, IRow irow) {
		flexPanel.getRowFormatter().setStylePrimaryName(row, "OntologyTable-row");
		
		for ( int col = 0, count = colHeaders.size(); col < count; col++ ) {
			ColHeader colHeader = colHeaders.get(col);
			String name = colHeader.getText();
			
			String value = irow.getColValue(name);
			if ( value == null ) value = "";
			
			final HTML valHtml = new HTML(value);
			
			if ( false ) {
				flexPanel.setWidget(row, col, valHtml);
			}
			else {
				Cell cell = new Cell(row, col, valHtml);
				flexPanel.setWidget(row, col, cell.getWidget());
			}

			flexPanel.getFlexCellFormatter().setAlignment(row, col, 
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
