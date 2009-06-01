package org.mmisw.ontmd.gwt.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mmisw.iserver.gwt.client.rpc.BaseOntologyData;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.EntityInfo;
import org.mmisw.iserver.gwt.client.rpc.IndividualInfo;
import org.mmisw.iserver.gwt.client.rpc.MappingOntologyData;
import org.mmisw.iserver.gwt.client.rpc.OntologyData;
import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.OtherOntologyData;
import org.mmisw.iserver.gwt.client.rpc.PropValue;
import org.mmisw.iserver.gwt.client.rpc.VocabularyOntologyData;
import org.mmisw.iserver.gwt.client.rpc.VocabularyOntologyData.ClassData;
import org.mmisw.ontmd.gwt.client.portal.IVocabPanel;
import org.mmisw.ontmd.gwt.client.util.IRow;
import org.mmisw.ontmd.gwt.client.util.UtilTable;
import org.mmisw.ontmd.gwt.client.voc2rdf.BaseOntologyPanel;
import org.mmisw.ontmd.gwt.client.voc2rdf.VocabClassPanel;

import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Main panel for viewing/editing data.
 * 
 * @author Carlos Rueda
 */
public class DataPanel extends VerticalPanel {

//	private static final char SEPARATOR = '|';
//	private static final char QUOTECHAR = '"';
	

	// created during refactoring process -- may be removed later
	private class MyVocabPanel implements IVocabPanel {

		public void enable(boolean enabled) {
			// TODO Auto-generated method stub
			
		}

		public void statusPanelsetHtml(String str) {
			// TODO Auto-generated method stub
			
		}

		public void statusPanelsetWaiting(boolean waiting) {
			// TODO Auto-generated method stub
			
		}
	}
	
	private MyVocabPanel myVocabPanel = new  MyVocabPanel();
	
	private boolean readOnly = true;
	
	private Set<BaseOntologyPanel> baseOntologyPanels = new HashSet<BaseOntologyPanel>();
	
	/**
	 * Creates the data panel
	 */
	public DataPanel(boolean readOnly) {
		super();
		this.readOnly = readOnly;
		setWidth("100%");
	}
	
	public void enable(boolean enabled) {
		// TODO
	}
	
	
	/**
	 * Updates this panel with the data associated to the given ontology 
	 * @param ontologyInfoPre
	 */
	public void updateWith(OntologyInfo ontologyInfo, boolean readOnly) {
		this.readOnly = readOnly;
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
	
	
	public void setReadOnly(boolean readOnly) {
		if ( this.readOnly == readOnly ) {
			return;
		}
		this.readOnly = readOnly;
		
		for ( BaseOntologyPanel baseOntologyPanel : baseOntologyPanels ) {
			baseOntologyPanel.setReadOnly(readOnly);
		}
	}
	



	private Widget _createVocabularyWidget(VocabularyOntologyData ontologyData) {

		Main.log("Creating VocabularyWidget");

		List<ClassData> classes = ontologyData.getClasses();
		
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(4);
		
		for ( ClassData classData : classes ) {
			List<String> classHeader = classData.getDatatypeProperties();

			
//			String[] colNames = classHeader.toArray(new String[classHeader.size()]);
			
//			StringBuilder termContents = new StringBuilder();
//			
//			// header line in contents:
//			String _sep = "";
//			for (int i = 0; i < colNames.length; i++) {
//				termContents.append(_sep + QUOTECHAR +colNames[i]+ QUOTECHAR);
//				_sep = String.valueOf(SEPARATOR);
//			}
//			termContents.append("\n");
			
			VocabClassPanel classPanel = new VocabClassPanel(classData, myVocabPanel, readOnly);
			baseOntologyPanels.add(classPanel);
			
//			ViewTable viewTable = new ViewTable(colNames);
//			tp.add(viewTable.getWidget());
			

			List<IndividualInfo> individuals = classData.getIndividuals();
			Main.log("num individuals: " +individuals.size());
			
			List<IRow> rows = new ArrayList<IRow>();
			
			for ( IndividualInfo entity : individuals ) {
				
				final Map<String, String> vals = new HashMap<String, String>();
				List<PropValue> props = entity.getProps();
				for ( PropValue pv : props ) {
					vals.put(pv.getPropName(), pv.getValueName());
				}

				vals.put("Name", entity.getLocalName());
				
//				_sep = "";
//				for (int i = 0; i < colNames.length; i++) {
//					String val = vals.get(colNames[i]);
//					if ( val == null ) {
//						val = "";
//					}
//					termContents.append(_sep + QUOTECHAR +val+ QUOTECHAR);
//					_sep = String.valueOf(SEPARATOR);
//				}
//				
//				termContents.append("\n");
				
				
				rows.add(new IRow() {
					public String getColValue(String sortColumn) {
						return vals.get(sortColumn);
					}
				});

			}
			
			classPanel.importContents(classHeader, rows);
//			classPanel.importContents(SEPARATOR, termContents.toString());
			
			vp.add(classPanel.getWidget());
			
		}
		
		return vp;
	}

	
	@SuppressWarnings("unchecked")
	private Widget _createOtherWidget(OtherOntologyData ontologyData) {
		
		Main.log("Creating OtherWidget");

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

		UtilTable utilTable = new UtilTable(colNames);
		List<IRow> rows = new ArrayList<IRow>();
		for ( EntityInfo entity : entities ) {
			final Map<String, String> vals = new HashMap<String, String>();
			List<PropValue> props = entity.getProps();
			for ( PropValue pv : props ) {
				vals.put(pv.getPropName(), pv.getValueName());
			}

			vals.put("Name", entity.getLocalName());
			
			rows.add(new IRow() {
				public String getColValue(String sortColumn) {
					return vals.get(sortColumn);
				}
			});
		}
		utilTable.setRows(rows);
		
		return utilTable.getWidget();
	}

	
	
	
	private Widget _createMappingWidget(MappingOntologyData ontologyData) {
		Main.log("Creating MappingWidget");

		return new HTML("<i>not implemented yet</i>");
	}

	public void cancel() {
		for ( BaseOntologyPanel baseOntologyPanel : baseOntologyPanels ) {
			baseOntologyPanel.cancel();
		}
	}

	public CreateOntologyInfo getCreateOntologyInfo() {
		
		if ( baseOntologyPanels.size() == 0 ) {
			return null;
		}
		BaseOntologyPanel baseOntologyPanel = baseOntologyPanels.iterator().next();
		if ( ! (baseOntologyPanel instanceof VocabClassPanel) ) {
			return null;
		}
		
		VocabClassPanel classPanel = (VocabClassPanel) baseOntologyPanel;

		return classPanel.getCreateOntologyInfo();
	}

}
