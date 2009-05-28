package org.mmisw.ontmd.gwt.client.portal;

import org.mmisw.iserver.gwt.client.vocabulary.AttrDef;


/** created for refactoring purposes.  may be removed later */
public interface IVocabPanel {
	
	public AttrDef getResourceTypeAttrDef();

	public void statusPanelsetWaiting(boolean waiting);
	
	public void statusPanelsetHtml(String str);
	
	public void enable(boolean enabled);
}
