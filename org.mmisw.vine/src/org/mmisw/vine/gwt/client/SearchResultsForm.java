package org.mmisw.vine.gwt.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mmisw.vine.gwt.client.rpc.EntityInfo;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MouseListenerAdapter;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The panel where the search results are displayed.
 * 
 * @author Carlos Rueda
 */
public class SearchResultsForm extends VerticalPanel {
	
	
	private Map<String,Row> currentRows = new HashMap<String, Row>();
	
	private Set<String> selectedRows = new HashSet<String>();


	
	private ResourceViewer resourceViewer;
	
	private HTML status = new HTML("Selected: " +selectedRows.size()+ " out of " +currentRows.size()+ " element(s)");
	
	private List<CheckBox> cbs;
	
	private CellPanel rowPanel;
	private ScrollPanel scroller;

	
	/**
	 * @param resourceViewer 
	 * 
	 */
	SearchResultsForm(ResourceViewer resourceViewer) {
		super();
		this.resourceViewer = resourceViewer;
		
		SelectAllNonePanel selAllNonePanel = new SelectAllNonePanel() {
			@Override
			void updateAllNone(boolean selected) {
				updateAllNone(selected);
			}
		};

		CellPanel hp = new HorizontalPanel();
		add(hp);
//		hp.setSpacing(5);	
		hp.setStylePrimaryName("MappingsTable-header");
		hp.setWidth("100%");
		
		hp.add(selAllNonePanel);
		hp.add(status);
		hp.setCellHorizontalAlignment(status, ALIGN_LEFT);
		
		
		CellPanel p = new VerticalPanel();
		
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(p);
	    add(decPanel);

	    cbs = new ArrayList<CheckBox>();
	    
	    rowPanel = new VerticalPanel();
	    rowPanel.setSpacing(1);
	    rowPanel.setStylePrimaryName("SearchResultsTable");
	    scroller = new ScrollPanel(rowPanel);
	    scroller.setSize("450px", "150px");
		p.add(scroller);
		
	}
	
	void updateAllNone(boolean selected) {
		for ( Row row : currentRows.values()  ) {
			row.setSelected(selected);
		}
		updateStatus();
	}
	
	void updateStatus() {
		status.setText("Selected: " +selectedRows.size()+ " out of " +currentRows.size()+ " element(s)");
	}

	public void searching() {
		rowPanel.clear();
		rowPanel.add(new HTML(
				"<img src=\"images/loading.gif\"> <i>Searching...</i>"
		));
	}

	public void updateEntities(List<EntityInfo> entities) {
		rowPanel.clear();
		cbs.clear();
		currentRows.clear();
		
//		final FlexTable flexTable = new FlexTable();
//		FlexCellFormatter cellFormatter = flexTable.getFlexCellFormatter();
//		cellFormatter.s
		
		for ( EntityInfo entity : entities ) {
			Row row = new Row(entity);
			currentRows.put(row.getKey(), row);
			cbs.add(row.checkBox);
			rowPanel.add(row);
		}
		updateStatus();
		
		if ( currentRows.size() == 0 ) {
			rowPanel.add(new HTML("<i>No entities found</i>"));
		}
	}

	
	
	
	Set<String> getSelectedRows() {
		return selectedRows;
	}




	private Row lastFocusedRow;
	
	
	/**
	 * Creates a row for the search results area. 
	 */
	private class Row extends FocusPanel {
		
		EntityInfo entity;
		CheckBox checkBox;
		TextBox textBox;
		
		Row(EntityInfo entity) {
			super();
			this.entity = entity;
			checkBox = new CheckBox();
			textBox = new TextBox();
			textBox.setStylePrimaryName("SearchResultsTable-TextBox");
			
			String str = entity.getCode()+ ":" +entity.getLocalName();
			textBox.setText(str);
			textBox.setReadOnly(true);
//			textBox.
//			addFocusListener(new FocusListener() {
//				public void onFocus(Widget sender) {
//					_focus(true);
//				}
//
//				public void onLostFocus(Widget sender) {
//					_focus(false);
//				}
//			});
			textBox.addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
//					textBox.selectAll();
					checkBox.setChecked(!checkBox.isChecked());
					checkBoxClicked();
				}
			});
			addMouseListener(new MouseListenerAdapter() {
				  public void onMouseEnter(Widget sender) {
					  if ( lastFocusedRow == Row.this ) {
						  return;
					  }
					  if ( lastFocusedRow != null ) {
						  lastFocusedRow._focus(false);
					  }
					  lastFocusedRow = Row.this;
					  _focus(true);
				  }
//				  public void onMouseLeave(Widget sender) {
//					  _focus(false);
//				  }
			});

			checkBox.addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					checkBoxClicked();
				}
			});

			this.setTitle(entity.getLocalName());

			HorizontalPanel hp = new HorizontalPanel();
			this.add(hp);
			hp.add(checkBox);
			hp.add(textBox);
		}
		
		private void checkBoxClicked() {
			boolean selected = checkBox.isChecked();
			setSelected(selected);
			updateStatus();
		}

		public String getKey() {
			return entity.getCode()+ ":" +entity.getLocalName()+ ".";
		}

		public void setSelected(boolean selected) {
			checkBox.setChecked(selected);
			if ( selected ) {
				selectedRows.add(getKey());
			}
			else {
				selectedRows.remove(getKey());
			}
		}
		
		private void _focus(boolean focus) {
			if ( focus ) {
				resourceViewer.update(Row.this.entity);	
				setStyleName("SearchResultsTable-selected");
				textBox.addStyleName("SearchResultsTable-TextBox-selected");
//				scroller.setScrollPosition(this.getAbsoluteTop() + this.getOffsetHeight());
			}
			else {
				removeStyleName("SearchResultsTable-selected");
				textBox.removeStyleName("SearchResultsTable-TextBox-selected");
			}
		}
	}
	
}
