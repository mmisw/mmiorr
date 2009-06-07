package org.mmisw.ontmd.gwt.client.metadata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.OntologyMetadata;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.iserver.gwt.client.vocabulary.AttrGroup;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.portal.IOntologyPanel;
import org.mmisw.ontmd.gwt.client.portal.PortalControl;
import org.mmisw.ontmd.gwt.client.portal.TempOntologyInfoListener;
import org.mmisw.ontmd.gwt.client.rpc.OntologyInfoPre;
import org.mmisw.ontmd.gwt.client.rpc.ReviewResult_Old;
import org.mmisw.ontmd.gwt.client.util.TLabel;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The main metadata panel.
 * 
 * @author Carlos Rueda
 */
public class MetadataPanel extends FlexTable implements TempOntologyInfoListener {

private static final String INFO = 
					"Fields marked " +TLabel.requiredHtml+ " are required. " +
					"Use commas to separate values in multi-valued fields.";

//	private MainPanel mainPanel;
	private DockPanel container = new DockPanel();
	private TabPanel tabPanel = new TabPanel();
	
	private boolean enabled = true;
	
	
//	private boolean editing;
	
	//- remove NewUri related stuff
//	/** Little helper to show the ontology URI in two formats: the original
//	 * and the "HTML" one (to be resolved by "Ont").
//	 */
//	private static class NewUri extends HorizontalPanel {
//		private HTML html = new HTML();
//		NewUri() {
//			setSpacing(5);
//			setVerticalAlignment(ALIGN_MIDDLE);
//			setWidth("600");
////			setBorderWidth(1);
//			add(html);
//		}
//		
//		void updateText(String text) {
//			html.setText(text);
//		}
//		
//		void setUri(String uri, boolean link) {
//			
//			// remove any trailing fragments:
//			uri = uri.replaceAll("(#|/)+$", "");
//			
//			String str;
//			if ( link ) {
//				str = "<a href=\"" +uri+ "\" target=\"_blank\" >" +uri+ "</a>";
//				String htmlLink = uri+ "?form=html";
//				str += " (<a href=\"" +htmlLink+ "\" target=\"_blank\" >Resolve in HTML</a>)";
//			}
//			else {
//				str = "<font color=\"" +"gray"+ "\">" +uri+ "</font>";
//			}
//			html.setHTML(str);
//		}
//		
//		void showProgressMessage(String msg) {
//			html.setHTML("<font color=\"" +"blue"+ "\">" +msg+ "</font>");
//		}
//	}
//
//	private NewUri newUri = new NewUri();


	/**
	 * Creates the metadata panel
	 * @param mainPanel
	 * @param editing true for the editing interface; false for the vieweing interface.
	 */
	public MetadataPanel(IOntologyPanel mainPanel, boolean editing) {
		super();
		setWidth("100%");
//		this.editing = editing;
		
		int row = 0;

//		setBorderWidth(1);
//		container.setBorderWidth(1);
		container.setWidth("1000px");
//		container.setSize("850px", "350px");
		
		//- remove NewUri related stuff
//		HorizontalPanel hp = new HorizontalPanel();
//		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
//		this.getFlexCellFormatter().setColSpan(row,0, 2);
//		this.setWidget(row, 0, hp);
//		this.getFlexCellFormatter().setAlignment(row, 0, 
//				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP
//		);
//		
//		String tooltip = "The ontology base URI";
//		if ( editing ) {
//			tooltip = "Will show the new base URI for the generated ontology";
//			Label lbl = new Label("New base URI:");
//			lbl.setTitle(tooltip);
//			hp.add(lbl);
//		}
//		
//		newUri.setTitle(tooltip);
//		hp.add(newUri);
		
		row++;
		
		
		if ( editing ) {
			this.setWidget(row, 0, new HTML(INFO));
			row++;
		}

		this.getFlexCellFormatter().setColSpan(row,0, 2);
		this.setWidget(row, 0, container);
		this.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP
		);

//	    tabPanel.setAnimationEnabled(true);
	    
		DockPanel dockPanel = new DockPanel();
//		dockPanel.setBorderWidth(1);

		
		dockPanel.add(tabPanel, DockPanel.NORTH);
	    container.add(dockPanel, DockPanel.CENTER);
	    
		for ( AttrGroup attrGroup: Main.metadataBaseInfo.getAttrGroups() ) {
			CellPanel groupPanel = new MetadataGroupPanel(mainPanel, attrGroup, editing);
			tabPanel.add(groupPanel, attrGroup.getName());
		}
		
	    tabPanel.selectTab(0);
	    enable(false);
	}
	
	
	/** Basically for viewing-only mode */
	public void showProgressMessage(String msg) {
		//- remove NewUri related stuff
//		newUri.showProgressMessage(msg);
	}
	
	/** Puts test values */
	void putTestValues(Map<String, String> values) {
		for ( int i = 0, c = tabPanel.getWidgetCount(); i < c; i++ ) {
			Widget w = tabPanel.getWidget(i);
			if ( w instanceof MetadataGroupPanel ) {
				((MetadataGroupPanel) w).putTestValues(values);
			}
		}
	}
	
	public String putValues(Map<String, String> values, boolean checkMissing) {
		for ( int i = 0, c = tabPanel.getWidgetCount(); i < c; i++ ) {
			Widget w = tabPanel.getWidget(i);
			if ( w instanceof MetadataGroupPanel ) {
				String m = ((MetadataGroupPanel) w).putValues(values, checkMissing);
				if ( m != null ) {
					return m;
				}
			}
		}
		return null;
	}
	
	/**
	 * Updates the metadata from the given temporary ontology info object,
	 * confirming with the user in case of any possible overwriting of attributes.
	 */
	public void tempOntologyInfoObtained(TempOntologyInfo tempOntologyInfo) {

		// get the new values from temp file:
		OntologyMetadata ontologyMetadata = tempOntologyInfo.getOntologyMetadata();
		
		
		// get current values in the editor:
		
		Map<String, String> valuesInEditor = new HashMap<String, String>();
		putValues(valuesInEditor, false);
		
		// and from the file
		Map<String, String> tempValues = ontologyMetadata.getOriginalValues();

		Set<String> commonKeysWithDiffValues = new HashSet<String>();
		
		// see if there're common attributes with different values in both the editor and the new file 
		for (String tempKey : tempValues.keySet() ) {
			if ( valuesInEditor.keySet().contains(tempKey) ) {
				String valueInEditor = valuesInEditor.get(tempKey);
				String valueInTemp = tempValues.get(tempKey);
				if ( ((valueInEditor == null) ^ (valueInTemp == null ))
				||   ! valueInEditor.equals(valueInTemp) 
				) {
					commonKeysWithDiffValues.add(tempKey);
				}
			}
		}

		if ( ! commonKeysWithDiffValues.isEmpty() ) {
			
			// prompt the user for the action to take:
			
			String confirmationMsg = 
				"There are different values in the uploaded file for " +commonKeysWithDiffValues.size()+ " of the " +
				"non-empty attributes in the metadata editor. " +
				"Do you want to replace the existing values in the editor with those from the file?\n" +
				"\n" +
				"Select Accept to replace those attributes with values from the file.\n" +
				"\n" +
				"Select Cancel to keep the values in the editor.\n" +
				"\n" +
				"In either case, missing attributes in the editor will be updated with " +
				"available values from the file. \n"
			;
			
			// add the attribute names for administrators:
			if ( PortalControl.getInstance().getLoginResult() != null ) {
				LoginResult loginResult = PortalControl.getInstance().getLoginResult();
				if ( loginResult.isAdministrator() ) {
					confirmationMsg +=
						"\n" +
						"The attributes are:\n" +
						commonKeysWithDiffValues
						;
				}
			}
			
			if ( true ) {  // this block is just a quick way to open the enclosing DisclosurePanel, if any
				Widget parent = this.getParent();
				while ( parent != null ) {
					if ( parent instanceof DisclosurePanel ) {
						((DisclosurePanel) parent).setOpen(true);
						break;
					}
					parent = parent.getParent();
				}
			}

			if ( Window.confirm(confirmationMsg) ) {
				// replace common attributes with values from the file.
				// Keep tempValues map as it is. See below.
			}
			else {
				// keep values in the editor, so remove the corresponding keys from tempValues:
				for ( String tempKey : commonKeysWithDiffValues ) {
					tempValues.remove(tempKey);
				}
			}
			
			// use valuesInEditor map to collect the final set of attributes:
			for ( String tempKey : tempValues.keySet() ) {
				valuesInEditor.put(tempKey, tempValues.get(tempKey));
			}
			
			// and set the final set of attributes:
			ontologyMetadata.setOriginalValues(valuesInEditor);
		}

		
		for ( int i = 0, c = tabPanel.getWidgetCount(); i < c; i++ ) {
			Widget w = tabPanel.getWidget(i);
			if ( w instanceof MetadataGroupPanel ) {
				((MetadataGroupPanel) w).resetToOriginalOrNewValues(ontologyMetadata, true, false);
			}
		}
		
		//- remove NewUri related stuff
//		newUri.updateText("");
//		String new_uri = tempOntologyInfo.getUri();
//		if ( new_uri != null ) {
//			newUri.setUri(new_uri, false);
//		}

	}

	
	
	
	
	public void example(boolean confirm) {
		for ( int i = 0, c = tabPanel.getWidgetCount(); i < c; i++ ) {
			Widget w = tabPanel.getWidget(i);
			if ( w instanceof MetadataGroupPanel ) {
				((MetadataGroupPanel) w).example(confirm);
			}
		}
	}
	
	/** Delegates to the metadata group panels and
	 * updates the internal 'enabled' flag.
	 */
	public void enable(boolean enabled) {
		for ( int i = 0, c = tabPanel.getWidgetCount(); i < c; i++ ) {
			Widget w = tabPanel.getWidget(i);
			if ( w instanceof MetadataGroupPanel ) {
				((MetadataGroupPanel) w).enable(enabled);
			}
		}
		this.enabled = enabled;
	}
	
	boolean isEnabled() {
		return enabled;
	}

	
	public void resetToOriginalValues(RegisteredOntologyInfo ontologyInfo, ReviewResult_Old reviewResult_Old, boolean confirm, boolean link) {
		OntologyMetadata ontologyMetadata = ontologyInfo.getOntologyMetadata();
		String ontologyUri = ontologyInfo.getUri();
		resetToOriginalOrNewValues(ontologyUri, ontologyMetadata, true, reviewResult_Old, confirm, link);
	}
	

	public void resetToOriginalValues(OntologyInfoPre ontologyInfoPre, ReviewResult_Old reviewResult_Old, boolean confirm, boolean link) {
		OntologyMetadata ontologyMetadata = ontologyInfoPre.getOntologyMetadata();
		String ontologyUri = ontologyInfoPre.getUri();
		resetToOriginalOrNewValues(ontologyUri, ontologyMetadata, true, reviewResult_Old, confirm, link);
	}
	
	void resetToNewValues(OntologyInfoPre ontologyInfoPre, ReviewResult_Old reviewResult_Old, boolean confirm, boolean link) {
		OntologyMetadata ontologyMetadata = ontologyInfoPre.getOntologyMetadata();
		String ontologyUri = ontologyInfoPre.getUri();
		resetToOriginalOrNewValues(ontologyUri, ontologyMetadata, false, reviewResult_Old, confirm, link);
	}
	
	private void resetToOriginalOrNewValues(
			String ontologyUri, OntologyMetadata ontologyMetadata, 
			boolean originalVals, 
			ReviewResult_Old reviewResult_Old, boolean confirm, boolean link) 
	{
		
		for ( int i = 0, c = tabPanel.getWidgetCount(); i < c; i++ ) {
			Widget w = tabPanel.getWidget(i);
			if ( w instanceof MetadataGroupPanel ) {
				((MetadataGroupPanel) w).resetToOriginalOrNewValues(ontologyMetadata, originalVals, confirm);
			}
		}
		
		//- remove NewUri related stuff
//		newUri.updateText("");
//		if ( reviewResult_Old != null ) {
//			String new_uri = reviewResult_Old.getUri();
//			if ( new_uri != null ) {
//				newUri.setUri(new_uri, link);
//			}
//		}
//		else if ( ! editing ) {
//			newUri.setUri(ontologyUri, link);
//		}
	}


	public void cancel() {
		// TODO Auto-generated method stub
		
	}
	
}
