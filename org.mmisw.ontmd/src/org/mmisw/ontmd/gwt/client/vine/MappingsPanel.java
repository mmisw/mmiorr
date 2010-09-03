package org.mmisw.ontmd.gwt.client.vine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mmisw.iserver.gwt.client.rpc.vine.Mapping;
import org.mmisw.iserver.gwt.client.rpc.vine.RelationInfo;
import org.mmisw.ontmd.gwt.client.Orr;
import org.mmisw.ontmd.gwt.client.vine.util.SelectAllNonePanel;
import org.mmisw.ontmd.gwt.client.vine.util.TLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
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
	private FlexTable flexPanel;
	
	private List<RelationInfo> relInfos;
	
	private final Map<String, RelationInfo> relInfoMap = new LinkedHashMap<String,RelationInfo>();
	
	
	/**
	 * Info about a mapping currently inserted in this panel.
	 */
	private static class MappingAssoc {
		Mapping mapping;
		CheckBox cb;
		
		MappingMetadataPanel mdPanel;
		
		MappingAssoc(Mapping mapping, boolean readOnly) {
			super();
			this.mapping = mapping;
			if ( ! readOnly ) {
				cb = new CheckBox();
			}
		}
	}
	
	/**
	 * The list of mappings currently inserted in this panel.
	 */
	private final List<MappingAssoc> mappingAssocs = new ArrayList<MappingAssoc>();
	
	
	/**
	 * Creates a mapping panel
	 * 
	 * @param readOnly  true to operate in view-only model; false to allow editing of the mappings.
	 */
	MappingsPanel(List<RelationInfo> relInfos, boolean readOnly) {
		super();
		this.relInfos = relInfos;
		this.readOnly = readOnly;
		
		_prepareFlexPanel();
		_setRelationInfos();
	}
	
	private void _prepareFlexPanel() {
		this.clear();
		
		flexPanel = new FlexTable();
//		flexPanel.setBorderWidth(1);
		
		add(flexPanel);
	    flexPanel.setWidth("100%");
	    
	    flexPanel.setStylePrimaryName("MappingsTable");
	    _setHeader(0);
	    _addNoMappingsRow(1);

	}

	
	private void _deleteRows() {
		final List<Mapping> newMappings = new ArrayList<Mapping>();
		for ( int i = 0, cnt = mappingAssocs.size(); i< cnt; i++ ) {
			MappingAssoc mappingAssoc = mappingAssocs.get(i);
			CheckBox cb = mappingAssoc.cb;
			if ( ! cb.isChecked() ) {
				newMappings.add(mappingAssoc.mapping);
			}
		}
		if ( newMappings.size() < mappingAssocs.size() ) {
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					_prepareFlexPanel();
					setMappings(newMappings);
				}
			});
		}
	}
	
	public List<Mapping> getMappings() {
		final List<Mapping> newMappings = new ArrayList<Mapping>();
		for ( int i = 0, cnt = mappingAssocs.size(); i< cnt; i++ ) {
			MappingAssoc mappingAssoc = mappingAssocs.get(i);
			newMappings.add(mappingAssoc.mapping);
		}
		return newMappings;
	}

	
	public void setMappings(final List<Mapping> newMappings) {
		this.mappingAssocs.clear();
		if ( newMappings == null ) {
			return;
		}

		for ( Mapping mapping : newMappings ) {
			RelationInfo relInfo = relInfoMap.get(mapping.getRelation());

			String codedLeft = VineMain.getCodedTerm(mapping.getLeft());
			String codedRight = VineMain.getCodedTerm(mapping.getRight());

			_addMapping(codedLeft, relInfo, codedRight, mapping.getMetadata());
		}
	}
	
	/**
	 * Gets the list of mappings that can be added without duplications.
	 * NO mappings are added to the panel.
	 * Call {@link #_addMapping(String, RelationInfo, String)} to do the actual addition.
	 * 
	 * @param leftKeys
	 * @param relInfo
	 * @param rightKeys
	 */
	public List<Mapping> preAddMappings(Set<String> leftKeys, RelationInfo relInfo, Set<String> rightKeys) {
		List<Mapping> preMappings = new ArrayList<Mapping>();
		
		// TODO a more efficient implementation to check for duplicates.
		
		for ( String leftKey: leftKeys ) {
			for ( String rightKey: rightKeys ) {
		
				boolean duplicate = false;
				for ( MappingAssoc mappingAssoc : mappingAssocs ) {
					Mapping mapping = mappingAssoc.mapping;
					
					if ( mapping.getLeft().equals(leftKey) && mapping.getRight().equals(rightKey)
					&& mapping.getRelation().equals(relInfo.getUri()) ) {
						duplicate = true;
						break;
					}
				}
				
				if ( ! duplicate ) {
					preMappings.add(new Mapping(leftKey, relInfo.getUri(), rightKey));
				}
			}
		}
		
		return preMappings;
	}
	
	/**
	 * Adds the given list of mappings to this panel.
	 * 
	 * @param relInfo The relation to be used (note: the relation attribute of the member of the
	 *                    given list is IGNORED).
	 * @param mappings  The mappings to add. NO check for duplications is done here.
	 */
	public void addMappings(RelationInfo relInfo, List<Mapping> mappings) {
		for ( Mapping mapping : mappings ) {
			_addMapping(mapping.getLeft(), relInfo, mapping.getRight(), mapping.getMetadata());
		}

	}
	
	
	/**
	 * Adds a new mapping to the table.
	 * @param leftKey
	 * @param relInfo
	 * @param rightKey
	 * @param metadata 
	 */
	private void _addMapping(String leftKey, RelationInfo relInfo, String rightKey, Map<String, String> metadata) {
		int row = mappingAssocs.size();
		if ( row == 0 ) {
			flexPanel.clear();
			_setHeader(0);
		}

		Widget center;
		Mapping mapping;
		
		if ( relInfo != null ) {
			String imgUri = GWT.getModuleBaseURL()+ "images/" +relInfo.getIconUri();
			Image img = new Image(imgUri);
			// NOTE: firefox does not put the newlines in the tooltip (perhaps firefox takes this as html?),
			// so, I'm replacing each "\n" with " \n", so at least I see a space:
			img.setTitle(relInfo.getDescription().replaceAll("\n", " \n"));
			center = img;
			mapping = new Mapping(leftKey, relInfo.getUri(), rightKey);
		}
		else {
			center = new HTML("?");
			mapping = new Mapping(leftKey, null, rightKey);
		}
		
		mapping.setMetadata(metadata);
		
		MappingAssoc ma = new MappingAssoc(mapping, readOnly);
		mappingAssocs.add(ma);
		
		Widget left = new Label(leftKey);
		Widget right = new Label(rightKey);

		_addRow(ma.cb, left, center, right, "MappingsTable-row", ma);
	}
	
	private void _setHeader(int row) {
		flexPanel.getRowFormatter().setStyleName(row, "MappingsTable-header");
		
		if ( !readOnly ) {
			SelectAllNonePanel selAllNonePanel = new SelectAllNonePanel("Delete") {
				@Override
				protected void updateAllNone(boolean selected) {
					for ( MappingAssoc ma : mappingAssocs ) {
						ma.cb.setChecked(selected);
					}
				}
				
				@Override
				protected void clientButtonClicked(String str) {
					// "Delete" button clicked.
					_deleteRows();
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
		HTML noYet = new HTML("<font color=\"gray\">(<i>No&nbsp;mappings</i>)</font>");
		flexPanel.setWidget(row, 0, noYet);
//		thisFp.setText(row, 1, ".");
//		thisFp.setText(row, 2, ".");
//		thisFp.setText(row, 3, ".");
	}

	private void _addRow(CheckBox cb, Widget left, Widget center, Widget right, String style,
			final MappingAssoc ma
	) {
		final int row = flexPanel.getRowCount();
		FlexCellFormatter cf = flexPanel.getFlexCellFormatter();
		flexPanel.getRowFormatter().setStyleName(row, style);
		
		
		HorizontalPanel hp = new HorizontalPanel();
		
		if ( cb != null ) {
			hp.add(cb);
		}
		
//		hp.add(Main.images.delete().createImage());
		
		// #169: metadata association per mapping
		Widget mdWidget = _prepareMappingMetadata(row, ma);
		hp.add(mdWidget);
		
		flexPanel.setWidget(row, 0, new FocusableRowElement(row, hp));
		
		flexPanel.setWidget(row, 1, new FocusableRowElement(row, left));
		
		flexPanel.getCellFormatter().setStyleName(row, 2, "MappingsTable-row");
		flexPanel.setWidget(row, 2, new FocusableRowElement(row, center));
				
		flexPanel.setWidget(row, 3, new FocusableRowElement(row, right));

		if ( center instanceof Image ) {
			Image img = (Image) center;
			String width = String.valueOf(img.getWidth());
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
	
	
	private Widget _prepareMappingMetadata(final int row, final MappingAssoc ma) {
		Image img = VineMain.images.metadata().createImage(); 
		final DisclosurePanel disclosure = new DisclosurePanel(img);  // DisclosurePanel("");
		disclosure.setTitle("Mapping metadata");
		disclosure.addEventHandler(new DisclosureHandler() {
			public void onClose(DisclosureEvent event) {
				flexPanel.setText(row + 1, 0, "");
				// Note: we can release the metadata panel as any value change there
				// is captured in the corresponding entry in the mapping's metadata map.
				ma.mdPanel = null;
			}

			public void onOpen(DisclosureEvent event) {
				Widget widget = _openMappingMetadata(ma); 
//				flexPanel.setWidget(row + 1, 0, widget);	
				flexPanel.setWidget(row + 1, 0, new FocusableRowElement(row, widget));
				// the FocusableRowElement is such that the positioning of the mouse over the
				// metadata panel also higlhlights the whole mapping element.
			}
		});
		
		return disclosure;
	}
	
	/**
	 * Returns the mapping metadata panel associated with the given mapping.
	 * This is created on demand.
	 */
	private Widget _openMappingMetadata(MappingAssoc ma) { 
		if ( ma.mdPanel == null ) {
			ma.mdPanel = new MappingMetadataPanel(ma.mapping, readOnly);
		}
		
		return ma.mdPanel.getWidget();
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

	private void _setRelationInfos() {
		relInfoMap.clear();
		for ( RelationInfo relInfo : relInfos ) {
			Orr.log("setRelationInfos: URI=" +relInfo.getUri());
			relInfoMap.put(relInfo.getUri(), relInfo);
		}
	}

}
