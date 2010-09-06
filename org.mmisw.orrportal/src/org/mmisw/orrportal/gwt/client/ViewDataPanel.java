package org.mmisw.orrportal.gwt.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mmisw.orrclient.gwt.client.rpc.BaseOntologyData;
import org.mmisw.orrclient.gwt.client.rpc.EntityInfo;
import org.mmisw.orrclient.gwt.client.rpc.IndividualInfo;
import org.mmisw.orrclient.gwt.client.rpc.MappingOntologyData;
import org.mmisw.orrclient.gwt.client.rpc.OntologyData;
import org.mmisw.orrclient.gwt.client.rpc.OtherOntologyData;
import org.mmisw.orrclient.gwt.client.rpc.PropValue;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.VocabularyOntologyData;
import org.mmisw.orrclient.gwt.client.rpc.VocabularyOntologyData.ClassData;
import org.mmisw.orrportal.gwt.client.util.table.IRow;
import org.mmisw.orrportal.gwt.client.util.table.IUtilTable;
import org.mmisw.orrportal.gwt.client.util.table.RowAdapter;
import org.mmisw.orrportal.gwt.client.util.table.UtilTableCreator;

import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Main panel for viewing data.
 * 
 * @author Carlos Rueda
 */
public class ViewDataPanel extends VerticalPanel {

	/**
	 * Creates the data panel
	 */
	public ViewDataPanel() {
		super();
		setWidth("100%");
	}
	
	public void enable(boolean enabled) {
		// TODO
	}
	
	
	/**
	 * Updates this panel with the data associated to the given ontology 
	 * @param ontologyInfoPre
	 */
	public void updateWith(RegisteredOntologyInfo ontologyInfo) {
		
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

		Orr.log("Creating VocabularyWidget");

		List<ClassData> classes = ontologyData.getClasses();
		
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(4);
		
		for ( ClassData classData : classes ) {
			String classUri = classData.getClassUri();
			List<String> classHeader = classData.getDatatypeProperties();

			String className = classUri;  // TODO just the name, not the whole URI
			
			VerticalPanel tp = new VerticalPanel();
			tp.add(new Label("Class: " +className));
			
			String[] colNames = classHeader.toArray(new String[classHeader.size()]);
			
			IUtilTable utilTable = UtilTableCreator.create(colNames);
			tp.add(utilTable.getWidget());

			List<IndividualInfo> individuals = classData.getIndividuals();
			Orr.log("num individuals: " +individuals.size());
			
			List<IRow> rows = new ArrayList<IRow>();
			for ( IndividualInfo entity : individuals ) {
				
				final Map<String, String> vals = new HashMap<String, String>();
				List<PropValue> props = entity.getProps();
				for ( PropValue pv : props ) {
					vals.put(pv.getPropName(), pv.getValueName());
				}

				vals.put("Name", entity.getLocalName());
				
				rows.add(new RowAdapter() {
					public String getColValue(String sortColumn) {
						return vals.get(sortColumn);
					}
				});
			}
			
			utilTable.setRows(rows);
			
			vp.add(tp);
			
		}
		
		return vp;
	}

	
	@SuppressWarnings("unchecked")
	private Widget _createOtherWidget(OtherOntologyData ontologyData) {
		
		Orr.log("Creating OtherWidget");

		BaseOntologyData baseData = ontologyData.getBaseOntologyData();
		
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(4);
		
		Object[] entityGroups = {  
				"Classes", baseData.getClasses(),
				"Properties", baseData.getProperties(),
				"Individuals", baseData.getIndividuals(),
		};

		for (int i = 0; i < entityGroups.length; i += 2) {
			String title = entityGroups[i].toString();
			List<?extends EntityInfo> entities = (List<?extends EntityInfo>) entityGroups[i + 1];
			
			title += " (" +entities.size()+ ")";
			
			DisclosurePanel disclosure = new DisclosurePanel(title);
			disclosure.setAnimationEnabled(true);
			
			Widget entsWidget = _createOtherWidgetForEntities(ontologyData, entities);
			
			disclosure.setContent(entsWidget);
			
			vp.add(disclosure);
			
		}
		
		return vp;
	}
	
	private Widget _createOtherWidgetForEntities(OtherOntologyData ontologyData, 
			List<? extends EntityInfo> entities) {

		
		if ( entities.size() == 0 ) {
			return new HTML();
		}
		
		Set<String> header = new HashSet<String>();
		
		for ( EntityInfo entity : entities ) {
			List<PropValue> props = entity.getProps();
			for ( PropValue pv : props ) {
				header.add(pv.getPropName());
			}
		}
		
		List<String> colNames = new ArrayList<String>();
		colNames.addAll(header);
		colNames.add(0, "Name");

		IUtilTable utilTable = UtilTableCreator.create(colNames);
		List<IRow> rows = new ArrayList<IRow>();
		for ( EntityInfo entity : entities ) {
			final Map<String, String> vals = new HashMap<String, String>();
			List<PropValue> props = entity.getProps();
			for ( PropValue pv : props ) {
				vals.put(pv.getPropName(), pv.getValueName());
			}

			vals.put("Name", entity.getLocalName());
			
			rows.add(new RowAdapter() {
				public String getColValue(String sortColumn) {
					return vals.get(sortColumn);
				}
			});
		}
		utilTable.setRows(rows);
		
		return utilTable.getWidget();
	}

	
	
	
	private Widget _createMappingWidget(MappingOntologyData ontologyData) {
		Orr.log("Creating MappingWidget");

		return new HTML("<i>not implemented yet</i>");
	}

}
