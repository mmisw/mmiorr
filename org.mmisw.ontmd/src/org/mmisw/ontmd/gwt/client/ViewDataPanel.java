package org.mmisw.ontmd.gwt.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mmisw.iserver.gwt.client.rpc.BaseOntologyData;
import org.mmisw.iserver.gwt.client.rpc.EntityInfo;
import org.mmisw.iserver.gwt.client.rpc.IndividualInfo;
import org.mmisw.iserver.gwt.client.rpc.MappingOntologyData;
import org.mmisw.iserver.gwt.client.rpc.OntologyData;
import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.OtherOntologyData;
import org.mmisw.iserver.gwt.client.rpc.PropValue;
import org.mmisw.iserver.gwt.client.rpc.VocabularyOntologyData;
import org.mmisw.iserver.gwt.client.rpc.VocabularyOntologyData.ClassData;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

/**
 * The main data panel.
 * 
 * @author Carlos Rueda
 */
public class ViewDataPanel extends VerticalPanel {

	/**
	 * Creates the data panel
	 */
	public ViewDataPanel() {
		super();
		setWidth("800");
	}
	
	public void enable(boolean enabled) {
		// TODO
	}
	
	
	/**
	 * Updates this panel with the data associated to the given ontology 
	 * @param ontologyInfoPre
	 */
	public void updateWith(OntologyInfo ontologyInfo) {
		
		this.clear();
		
		OntologyData ontologyData = ontologyInfo.getOntologyData();
		
		String type;
		Widget widget;
		
		if ( ontologyData instanceof VocabularyOntologyData ) {
			type = "Vocabulary contents:";
			widget = _createVocabularyWidget((VocabularyOntologyData) ontologyData);
		}
		else if ( ontologyData instanceof MappingOntologyData ) {
			type = "Mapping contents:";
			widget = _createMappingWidget((MappingOntologyData) ontologyData);
		}
		else {
			type = "Synopsis of ontology contents:";
			widget = _createOtherWidget((OtherOntologyData) ontologyData);
		}
		
		VerticalPanel vp = new VerticalPanel();
		vp.add(new Label(type));
		vp.add(widget);
		
		add(vp);
	}


	private Widget _createVocabularyWidget(VocabularyOntologyData ontologyData) {

		Main.log("Creating VocabularyWidget");

		List<ClassData> classes = ontologyData.getClasses();
		
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(4);
		
		for ( ClassData classData : classes ) {
			String classUri = classData.getClassUri();
			List<String> classHeader = classData.getDatatypeProperties();

			FlexTable flexPanel = new FlexTable();
			flexPanel.setStylePrimaryName("DataTable");
			flexPanel.setBorderWidth(1);
			flexPanel.setCellSpacing(4);
			FlexCellFormatter cf = flexPanel.getFlexCellFormatter();
			
			int row = 0;
			
			String[] colNames = classHeader.toArray(new String[classHeader.size()]);

			// CLASS NAME:
			String className = classUri;  // TODO just the name, not the whole URI
			cf.setColSpan(row, 0, colNames.length);
			flexPanel.setWidget(row, 0, new Label("Class: " +className));
			cf.setAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
			row++;
			
			// HEADER
			flexPanel.getRowFormatter().setStylePrimaryName(row, "DataTable-header");
			for ( int i = 0; i < colNames.length; i++ ) {
				String colName = colNames[i];
				flexPanel.setWidget(row, i, new Label(colName));
				cf.setAlignment(row, i, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
			}
			row++;
			
			// CONTENTS:
			
			List<IndividualInfo> individuals = classData.getIndividuals();
			
			Main.log("num individuals: " +individuals.size());
			
			
			for ( IndividualInfo individualInfo : individuals ) {
				
				flexPanel.getRowFormatter().setStylePrimaryName(row, "DataTable-row");
				Map<String, String> vals = new HashMap<String, String>();
				List<PropValue> props = individualInfo.getProps();
				for ( PropValue pv : props ) {
					vals.put(pv.getPropName(), pv.getValueName());
				}
				
				for ( int i = 0; i < colNames.length; i++ ) {
					String colName = colNames[i];
					String colValue = vals.get(colName);
					if ( colValue == null ) {
						colValue = "";
					}
					flexPanel.setWidget(row, i, new Label(colValue));
					cf.setAlignment(row, i, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
				}
				row++;
			}
			
			
			vp.add(flexPanel);
			
		}

		
		
		
		return vp;
	}

	
	private Widget _createOtherWidget(OtherOntologyData ontologyData) {
		
		Main.log("Creating OtherWidget");

		BaseOntologyData baseData = ontologyData.getBaseOntologyData();
		
		List<EntityInfo> entities = new ArrayList<EntityInfo>(); 
		
		entities.addAll(baseData.getIndividuals());
		entities.addAll(baseData.getProperties());
		entities.addAll(baseData.getClasses());
		
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(4);
		
		vp.add(_createOtherWidget(ontologyData, "Classes", baseData.getClasses()));
		vp.add(_createOtherWidget(ontologyData, "Properties", baseData.getProperties()));
		vp.add(_createOtherWidget(ontologyData, "Individuals", baseData.getIndividuals()));
		
		return vp;
	}
	
	private Widget _createOtherWidget(OtherOntologyData ontologyData, 
			String title, List<? extends EntityInfo> entities) {

		Set<String> header = new HashSet<String>();
		
		for ( EntityInfo entity : entities ) {
			List<PropValue> props = entity.getProps();
			for ( PropValue pv : props ) {
				header.add(pv.getPropName());
			}
		}

		FlexTable flexPanel = new FlexTable();
		flexPanel.setStylePrimaryName("DataTable");
		flexPanel.setBorderWidth(1);
		flexPanel.setCellSpacing(4);
		FlexCellFormatter cf = flexPanel.getFlexCellFormatter();
		
		List<String> colNames = new ArrayList<String>();
		colNames.addAll(header);
		colNames.add(0, "Name");

		int row = 0;
		
		// TITLE
		flexPanel.getRowFormatter().setStylePrimaryName(row, "DataTable-row");
		cf.setColSpan(row, 0, colNames.size());
		flexPanel.setWidget(row, 0, new HTML("<b>" +title+ "</b> (" +entities.size()+ ")"));
		cf.setAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
		row++;
		
		if ( entities.size() > 0 ) {
			// HEADER
			flexPanel.getRowFormatter().setStylePrimaryName(row, "DataTable-header");
			for ( int i = 0; i < colNames.size(); i++ ) {
				String colName = colNames.get(i);
				flexPanel.setWidget(row, i, new Label(colName));
				cf.setAlignment(row, i, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
			}
			row++;
			
			
			// CONTENTS:
			for ( EntityInfo entity : entities ) {
				flexPanel.getRowFormatter().setStylePrimaryName(row, "DataTable-row");
				Map<String, String> vals = new HashMap<String, String>();
				List<PropValue> props = entity.getProps();
				for ( PropValue pv : props ) {
					vals.put(pv.getPropName(), pv.getValueName());
				}
				
				vals.put("Name", entity.getLocalName());
				
				for ( int i = 0; i < colNames.size(); i++ ) {
					String colName = colNames.get(i);
					String colValue = vals.get(colName);
					if ( colValue == null ) {
						colValue = "";
					}
					flexPanel.setWidget(row, i, new Label(colValue));
					cf.setAlignment(row, i, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
				}
				row++;
			}
		}
		
		return flexPanel;
	}

	
	private Widget _createMappingWidget(MappingOntologyData ontologyData) {
		Main.log("Creating MappingWidget");

		// TODO Auto-generated method stub
		return new HTML("(<i>Mapping display not implemented</i>)");
	}

}
