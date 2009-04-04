package org.mmisw.vine.gwt.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mmisw.vine.gwt.client.rpc.EntityInfo;
import org.mmisw.vine.gwt.client.util.SelectAllNonePanel;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DisclosureEvent;
import com.google.gwt.user.client.ui.DisclosureHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
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


	
	private VocabularyForm vocabularyForm;
	
	private HTML status = new HTML("Selected: " +selectedRows.size()+ " out of " +currentRows.size()+ " element(s)");
	
	private List<CheckBox> cbs;
	
	private CellPanel rowPanel;
	private ScrollPanel scroller;

	
	/**
	 * @param vocabularyForm 
	 * 
	 */
	SearchResultsForm(VocabularyForm vocabularyForm) {
		super();
		this.vocabularyForm = vocabularyForm;
		
		SelectAllNonePanel selAllNonePanel = new SelectAllNonePanel() {
			@Override
			protected void updateAllNone(boolean selected) {
				for ( Row row : currentRows.values()  ) {
					row.setSelected(selected);
				}
				updateStatus();
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
	    scroller.setSize("500px", "300px");
		p.add(scroller);
		
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
		selectedRows.clear();
		cbs.clear();
		currentRows.clear();
		
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
		
		DisclosurePanel disclosure;
		
		Row(EntityInfo entity) {
			super();
			this.entity = entity;
			
			checkBox = new CheckBox();
			
//			Image infoImg = Main.images.metadata().createImage();
//			infoImg.addClickListener(new ClickListener() {
//				public void onClick(Widget sender) {
//					_pickedForInfo();				}
//			});
			
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
					  _pickedForInfo();
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

//			this.setTitle(entity.getLocalName());

			
			disclosure = new DisclosurePanel(str);
			disclosure.setWidth("450");
			disclosure.addEventHandler(new DisclosureHandler() {
				public void onClose(DisclosureEvent event) {
					disclosure.setContent(null);
				}

				public void onOpen(DisclosureEvent event) {
					disclosureOpen();
				}
			});
			
			HorizontalPanel hp = new HorizontalPanel();
			this.add(hp);
			hp.add(checkBox);
			
//			hp.add(infoImg);
			
			
			//hp.add(textBox);
			hp.add(disclosure);
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
		
		
		private void _pickedForInfo() {
			  if ( lastFocusedRow == Row.this ) {
				  return;
			  }
			  if ( lastFocusedRow != null ) {
				  lastFocusedRow._focus(false);
			  }
			  lastFocusedRow = Row.this;
			  _focus(true);			
		}
		
		
		private void disclosureOpen() {
			String name = entity.getLocalName();
			String code = "" + entity.getCode();
			String uri = Main.getWorkingUris().get(code).getUri() + name;
			
			String label = entity.getDisplayLabel();
			String comment = entity.getComment();
			
			if ( label == null ) {
				label = "";
			}
			if ( comment == null ) {
				comment = "";
			}

			disclosure.setContent(new HTML(
				""
				+ "URI: <a target=\"_blank\" href=\"" +uri+ "\">" +uri+ "</a><br/>"
				+ "Name: <b>" +name+ "</b><br/>"
				+ "Label: <b>" +label + "</b><br/>"
				+ "Comment: <b>" +comment + "</b><br/>"
			));	
		}
		
		private void _focus(boolean focus) {
			if ( focus ) {
				if ( disclosure != null ) {
					// nothing here
				}
				else {
					vocabularyForm.entityFocused(entity);
				}
				setStyleName("SearchResultsTable-focused");
				textBox.addStyleName("SearchResultsTable-TextBox-focused");
//				scroller.setScrollPosition(this.getAbsoluteTop() + this.getOffsetHeight());
			}
			else {
				removeStyleName("SearchResultsTable-focused");
				textBox.removeStyleName("SearchResultsTable-TextBox-focused");
			}
		}
	}
	
}
