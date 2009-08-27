package org.mmisw.ontmd.gwt.client.portal;

import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.DataCreationInfo;
import org.mmisw.iserver.gwt.client.rpc.MappingOntologyData;
import org.mmisw.iserver.gwt.client.rpc.vine.Mapping;
import org.mmisw.ontmd.gwt.client.vine.VineEditorPanel;
import org.mmisw.ontmd.gwt.client.vine.VineMain;

import com.google.gwt.user.client.ui.Widget;

/**
 * Container of the VINE panel.
 * 
 * @author Carlos Rueda
 */
public class VineOntologyContentsPanel extends BaseOntologyContentsPanel {

	private VineEditorPanel vineEditorPanel;
	
	public VineOntologyContentsPanel(MappingOntologyData ontologyData, boolean readOnly) {
		super(readOnly);
		
		VineMain.getWorkingUris().clear();
		
		vineEditorPanel = new VineEditorPanel(ontologyData, readOnly);
	}
	
	@Override
	public String checkData(boolean isNewVersion) {
		
		List<Mapping> mappings = vineEditorPanel.getMappings();
		if ( mappings.size() == 0 ) {
			return "No mappings have been defined.";
		}
		
		return null;
	}

	@Override
	public DataCreationInfo getCreateOntologyInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Widget getWidget() {
		return vineEditorPanel;
	}
	
	@Override
	public void setReadOnly(boolean readOnly) {
		if ( isReadOnly() == readOnly ) {
			return;
		}
		super.setReadOnly(readOnly);
		vineEditorPanel.setReadOnly(readOnly);
	}

	@Override
	public void cancel() {
		super.cancel();
		VineMain.getWorkingUris().clear();
	}

}
