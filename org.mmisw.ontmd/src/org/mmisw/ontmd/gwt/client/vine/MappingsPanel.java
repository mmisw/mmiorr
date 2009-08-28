package org.mmisw.ontmd.gwt.client.vine;

import java.util.ArrayList;
import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.vine.Mapping;
import org.mmisw.iserver.gwt.client.rpc.vine.RelationInfo;
import org.mmisw.ontmd.gwt.client.vine.util.SelectAllNonePanel;
import org.mmisw.ontmd.gwt.client.vine.util.TLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosureEvent;
import com.google.gwt.user.client.ui.DisclosureHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListenerAdapter;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

/**
 * The panel containing the mappings.
 * 
 * @author Carlos Rueda
 */
public class MappingsPanel extends FocusPanel {
	
	private boolean readOnly;
	private FlexTable flexPanel = new FlexTable();
	
	private List<Mapping> mappings = new ArrayList<Mapping>();
	
	/**
	 * 
	 */
	MappingsPanel(boolean readOnly) {
		super();
		this.readOnly = readOnly;
		
//		flexPanel.setBorderWidth(1);
		
		add(flexPanel);
	    flexPanel.setWidth("100%");
	    
	    flexPanel.setStylePrimaryName("MappingsTable");
	    _setHeader(0);
	    _addNoMappingsRow(1);

	}

	
	public List<Mapping> getMappings() {
		return mappings;
	}


	public void setMappings(final List<Mapping> mappings) {
		this.mappings.clear();
		if ( mappings != null ) {
			AsyncCallback<List<RelationInfo>> callback = new AsyncCallback<List<RelationInfo>>() {

				public void onFailure(Throwable caught) {
					// Ignore here.  Should have been dispatched in VineMain
				}

				public void onSuccess(List<RelationInfo> result) {
					for ( Mapping mapping : mappings ) {
						RelationInfo relInfo = VineMain.getRelInfoMap().get(mapping.getRelation());
						
						String codedLeft = VineMain.getCodedTerm(mapping.getLeft());
						String codedRight = VineMain.getCodedTerm(mapping.getRight());
						
						addMapping(codedLeft, relInfo, codedRight);
					}
				}

			};
			VineMain.getRelationInfos(callback);
		}
	}
	
	
	/**
	 * Adds a new mapping to the table.
	 * @param leftKey
	 * @param relInfo
	 * @param rightKey
	 */
	public void addMapping(String leftKey, RelationInfo relInfo, String rightKey) {
		int row = mappings.size();
		if ( row == 0 ) {
			flexPanel.clear();
			_setHeader(0);
		}

		Widget center;
		Mapping mapping;
		
		if ( relInfo != null ) {
			String imgUri = GWT.getModuleBaseURL()+ "images/" +relInfo.getIconUri();
			Image img = new Image(imgUri);
			img.setTitle(relInfo.getDescription());
			center = img;
			mapping = new Mapping(leftKey, relInfo.getUri(), rightKey);
		}
		else {
			center = new HTML("?");
			mapping = new Mapping(leftKey, null, rightKey);
		}
		
		
		mappings.add(mapping);
		
		Widget left = new Label(leftKey);
		Widget right = new Label(rightKey);

		_addRow(left, center, right, "MappingsTable-row");
	}
	
	private void _setHeader(int row) {
		flexPanel.getRowFormatter().setStyleName(row, "MappingsTable-header");
		
		if ( !readOnly ) {
			SelectAllNonePanel selAllNonePanel = new SelectAllNonePanel() {
				@Override
				protected void updateAllNone(boolean selected) {
					// TODO Auto-generated method stub
					Window.alert("sorry, not implemented yet");
				}
			};
			flexPanel.setWidget(row, 0, selAllNonePanel);
		}
		else {
			flexPanel.setWidget(row, 0, new HTML());
		}

		Widget title = new TLabel("Mappings", 
					"Lists the current mappings.. "
		);
		
		
		FlexCellFormatter cf = flexPanel.getFlexCellFormatter();
		cf.setColSpan(row, 1, 3);
		cf.setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
		cf.setWidth(row, 1, "100%");
		flexPanel.setWidget(row, 1, title);
		row++;
		
	}
	
	private void _addNoMappingsRow(int row) {
		flexPanel.getRowFormatter().setStyleName(row, "MappingsTable-row");
		FlexCellFormatter cf = flexPanel.getFlexCellFormatter();
//		_setAlignments(row);
		cf.setColSpan(row, 1, 3);
		cf.setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
//		cf.setWidth(row, 1, "100%");
		HTML noYet = new HTML("<font color=\"gray\">(<i>No mappings</i>)</font>");
		flexPanel.setWidget(row, 0, noYet);
//		thisFp.setText(row, 1, ".");
//		thisFp.setText(row, 2, ".");
//		thisFp.setText(row, 3, ".");
	}

	private void _addRow(Widget left, Widget center, Widget right, String style) {
		final int row = flexPanel.getRowCount();
		FlexCellFormatter cf = flexPanel.getFlexCellFormatter();
		flexPanel.getRowFormatter().setStyleName(row, style);
		
		
		HorizontalPanel hp = new HorizontalPanel();
		
		if ( ! readOnly ) {
			hp.add(new CheckBox());
		}
		
//		hp.add(Main.images.metadata().createImage());
//		hp.add(Main.images.delete().createImage());
		
		DisclosurePanel disclosure = new DisclosurePanel("");
		disclosure.addEventHandler(new DisclosureHandler() {
			public void onClose(DisclosureEvent event) {
				flexPanel.setText(row + 1, 0, "");
			}

			public void onOpen(DisclosureEvent event) {
				flexPanel.setWidget(row + 1, 0, disclosureOpen());	
			}
		});

		hp.add(disclosure);
		
		
		flexPanel.setWidget(row, 0,
				new FocusableRowElement(row, hp)
//				hp
		);
		
		flexPanel.setWidget(row, 1, 
				new FocusableRowElement(row, left)
				//left
		);
		
		flexPanel.getCellFormatter().setStyleName(row, 2, "MappingsTable-row");
		flexPanel.setWidget(row, 2,
				new FocusableRowElement(row, center)
				//center
		);
				
		flexPanel.setWidget(row, 3,
				new FocusableRowElement(row, right)
				//right
		);

		if ( center instanceof Image ) {
			String width = "30";
			Image img = (Image) center;
			width = "" +img.getWidth();
			cf.setWidth(row, 2, width);
		}		
		_setAlignments(row);
		
		
		// add (empty) row for expansion
		flexPanel.getRowFormatter().setStyleName(row + 1, style);
		cf.setColSpan(row + 1, 0, 4);
		cf.setAlignment(row + 1, 0, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);

	}
	
	private void _setAlignments(int row) {
		FlexCellFormatter cf = flexPanel.getFlexCellFormatter();
		cf.setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		cf.setWidth(row, 1, "50%");
		cf.setWidth(row, 3, "50%");
			
		cf.setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		cf.setAlignment(row, 2, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
		cf.setAlignment(row, 3, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
	}
	

	private Widget disclosureOpen() {
		// TODO fill in mapping metadata widget
		String comment = "TODO";
		String confidence = "TODO";
		
		return new HTML(
			""
			+ "<b>Comment</b>: " +comment+ "<br/>"
			+ "<b>Confidence</b>: " +confidence+ "<br/>"
		);	
	}

	

	private class FocusableRowElement extends FocusPanel {
		int row;
		
		FocusableRowElement(int row, Widget child) {
			super(child);
			this.row = row;
			
			addMouseListener(new MouseListenerAdapter() {
				  public void onMouseEnter(Widget sender) {
					  _focus(true);
				  }
				  public void onMouseLeave(Widget sender) {
					  _focus(false);
				  }
			});
		}
		
		private void _focus(boolean focus) {
			if ( focus ) {
				flexPanel.getRowFormatter().setStyleName(row + 0, "MappingsTable-focused");
				flexPanel.getRowFormatter().setStyleName(row + 1, "MappingsTable-focused");
			}
			else {
				flexPanel.getRowFormatter().setStyleName(row + 0, "MappingsTable-row");
				flexPanel.getRowFormatter().setStyleName(row + 1, "MappingsTable-row");
//				flexPanel.getRowFormatter().removeStyleName(row + 0, "MappingsTable-focused");
//				flexPanel.getRowFormatter().removeStyleName(row + 1, "MappingsTable-focused");
			}
		}
	}
}
