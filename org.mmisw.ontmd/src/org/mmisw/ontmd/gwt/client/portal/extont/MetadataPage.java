package org.mmisw.ontmd.gwt.client.portal.extont;

import java.util.Map;

import org.mmisw.ontmd.gwt.client.portal.md.MetadataSection;

/**
 * <p>
 * TODO complete implementation
 * 
 * @author Carlos Rueda
 */
class MetadataPage extends BasePage {
	
	MetadataSection mdSection;
	
	MetadataPage(BaseWizard wizard, MetadataSection mdSection) {
		super(wizard, true, true);
		this.mdSection = mdSection;
		addContents(mdSection.getWidget());
	}
	
	protected boolean preCheckNextClicked() {
		statusHtml.setHTML("");
		boolean checkMissing = true;
		Map<String, String> values = null;  //don't need the value now.
		String str = mdSection.putValues(values, checkMissing);
		
		if ( str != null ) {
			statusHtml.setHTML("<font color=\"red\">" +str+ "</font");
			return false;
		}
		return true;
	}
	
	protected void formChanged() {
		preCheckNextClicked();
	}
}
