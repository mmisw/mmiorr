package org.mmisw.ontmd.gwt.client.portal.extont;

import org.mmisw.iserver.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.ontmd.gwt.client.Main;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Sequence of wizard pages to register an external ontology.
 * 
 * <p>
 * TODO complete implementation
 * 
 * @author Carlos Rueda
 */
public class RegisterExternalOntologyWizard {
	
	private final RegisterExternalOntologyPage1 page1 = new RegisterExternalOntologyPage1(this);
	
	private final RegisterExternalOntologyPage2 page2 = new RegisterExternalOntologyPage2(this);
	
	private final RegisterExternalOntologyPageFullyHosted pageFullyHosted =
		new RegisterExternalOntologyPageFullyHosted(this);
	
	private final RegisterExternalOntologyPageReHosted pageReHosted =
		new RegisterExternalOntologyPageReHosted(this);
	
	private final RegisterExternalOntologyPageIndexed pageIndexed =
		new RegisterExternalOntologyPageIndexed(this);
	
	
	private VerticalPanel contents = new VerticalPanel();
	
	private HTML statusLoad = new HTML();
	
	// provided by page1
	private TempOntologyInfo tempOntologyInfo;
	
	
	enum HostingType { 
		FULLY_HOSTED("Fully hosted ontology"), 
		RE_HOSTED("Re-hosted ontology"), 
		INDEXED("Indexed ontology"),
		;
		
		String label;
		HostingType(String label) {
			this.label = label;
		}
	}; 
	private HostingType hostingType;

	/**
	 */
	public RegisterExternalOntologyWizard() {
		contents.setSize("650px", "300px");
		
		contents.add(page1.getWidget());
		statusLoad.setText("");
	}
	
	
	public Widget getWidget() {
		return contents;
	}

	void ontologyInfoObtained(TempOntologyInfo tempOntologyInfo) {
		assert tempOntologyInfo.getError() == null;
		
		this.tempOntologyInfo = tempOntologyInfo;
	}
	
	void pageNext(RegisterExternalOntologyPageBase currentPage) {
//		if ( tempOntologyInfo == null ) { TODO apply after testing
//			return;
//		}
		
		if ( currentPage == page1 ) {
			contents.clear();
			contents.add(page2.getWidget());
		}
		else if ( currentPage == page2 ) {
			assert hostingType != null;
			
			RegisterExternalOntologyPageBase nextPage = null;
			
			switch ( hostingType ) {
			case FULLY_HOSTED:
				nextPage = pageFullyHosted;
				break;
			case RE_HOSTED:
				nextPage = pageReHosted;
				break;
			case INDEXED:
				nextPage = pageIndexed;
				break;
			}
			
			if ( nextPage != null ) {
				contents.clear();
				contents.add(nextPage.getWidget());
			}
		}
	}
	
	void pageBack(RegisterExternalOntologyPageBase currentPage) {
		if ( currentPage == page2 ) {
			contents.clear();
			contents.add(page1.getWidget());
		}
		else if ( currentPage == pageFullyHosted 
		||   currentPage == pageReHosted 
		||   currentPage == pageIndexed ) {
			contents.clear();
			contents.add(page2.getWidget());
		}
	}


	void hostingTypeSelected(HostingType hostingType) {
		this.hostingType = hostingType;
		Main.log("hostingTypeSelected: " +hostingType);
	}


	void finish(RegisterExternalOntologyPageBase currentPage) {
		if ( currentPage == pageFullyHosted ) {
			// TODO continue with regular OntologyPanel...
		}
		else if ( currentPage == pageReHosted ) {
			// TODO continue with regular OntologyPanel...
		}
		else if ( currentPage == pageIndexed ) {
			// TODO continue with completing the registration
		}
		
	}

}
