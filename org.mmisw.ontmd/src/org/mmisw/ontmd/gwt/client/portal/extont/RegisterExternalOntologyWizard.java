package org.mmisw.ontmd.gwt.client.portal.extont;

import java.util.HashMap;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.OtherDataCreationInfo;
import org.mmisw.iserver.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.portal.PortalMainPanel;
import org.mmisw.ontmd.gwt.client.portal.md.MetadataSection1;
import org.mmisw.ontmd.gwt.client.portal.md.MetadataSection2;
import org.mmisw.ontmd.gwt.client.portal.md.MetadataSection3;
import org.mmisw.ontmd.gwt.client.portal.wizard.WizardBase;
import org.mmisw.ontmd.gwt.client.portal.wizard.WizardPageBase;

import com.google.gwt.user.client.Window;

/**
 * Sequence of wizard pages to register an external ontology.
 * 
 * <p>
 * TODO complete implementation
 * 
 * @author Carlos Rueda
 */
public class RegisterExternalOntologyWizard extends WizardBase {
	
	private final RegisterExternalOntologyPage1 page1 = new RegisterExternalOntologyPage1(this);
	
	private final RegisterExternalOntologyPage2 page2 = new RegisterExternalOntologyPage2(this);
	
	private RegisterExternalOntologyPageFullyHosted pageFullyHosted;
	
	private RegisterExternalOntologyPageReHosted pageReHosted;
	
	private RegisterExternalOntologyPageIndexed pageIndexed;
	
	
	private RegisterExternalOntologyMetadataPage metadataPage1;
	private RegisterExternalOntologyMetadataPage metadataPage2;
	private RegisterExternalOntologyMetadataPage metadataPage3;
	
	
	private RegisterExternalOntologyPageFullyHostedConfirmation pageFullyHostedConfirmation;
	
	// TODO
	//private RegisterExternalOntologyPageReHostedConfirmation pageReHostedConfirmation;
	
	// TODO
//	private private RegisterExternalOntologyPageIndexedConfirmation pageIndexedConfirmation;
	
	
	
	// provided by page1
	TempOntologyInfo tempOntologyInfo;
	
	
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
	 * @param portalMainPanel 
	 */
	public RegisterExternalOntologyWizard(PortalMainPanel portalMainPanel) {
		super(portalMainPanel);
		contents.setSize("650px", "300px");
		
		contents.add(page1.getWidget());
		statusLoad.setText("");
	}
	
	
	void ontologyInfoObtained(TempOntologyInfo tempOntologyInfo) {
		assert tempOntologyInfo.getError() == null;
		
		this.tempOntologyInfo = tempOntologyInfo;
	}
	
	@Override
	protected void pageNext(WizardPageBase cp) {
		RegisterExternalOntologyPageBase currentPage = (RegisterExternalOntologyPageBase) cp;
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
				if ( pageFullyHosted == null ) {
					pageFullyHosted = new RegisterExternalOntologyPageFullyHosted(this);
					pageReHosted = null;
					pageIndexed = null;
				}
				nextPage = pageFullyHosted;
				break;
			case RE_HOSTED:
				if ( pageReHosted == null ) {
					pageReHosted = new RegisterExternalOntologyPageReHosted(this);
					pageFullyHosted = null;
					pageIndexed = null;
				}
				nextPage = pageReHosted;
				break;
			case INDEXED:
				if ( pageIndexed == null ) {
					pageIndexed = new RegisterExternalOntologyPageIndexed(this);
					pageReHosted = null;
					pageFullyHosted = null;

				}
				nextPage = pageIndexed;
				break;
			}
			
			if ( nextPage != null ) {
				contents.clear();
				contents.add(nextPage.getWidget());
			}
		}
		else if ( currentPage == pageFullyHosted ) {
			if ( metadataPage1 == null ) {
				metadataPage1 = new RegisterExternalOntologyMetadataPage(this, new MetadataSection1());
			}
			RegisterExternalOntologyPageBase nextPage = metadataPage1;
			contents.clear();
			contents.add(nextPage.getWidget());
		}
		else if ( currentPage == metadataPage1 ) {
			if ( metadataPage2 == null ) {
				metadataPage2 = new RegisterExternalOntologyMetadataPage(this, new MetadataSection2());
			}
			RegisterExternalOntologyPageBase nextPage = metadataPage2;
			contents.clear();
			contents.add(nextPage.getWidget());
		}
		else if ( currentPage == metadataPage2 ) {
			if ( metadataPage3 == null ) {
				metadataPage3 = new RegisterExternalOntologyMetadataPage(this, new MetadataSection3());
			}
			RegisterExternalOntologyPageBase nextPage = metadataPage3;
			contents.clear();
			contents.add(nextPage.getWidget());
		}
		else if ( currentPage == metadataPage3 ) {
			if ( pageFullyHostedConfirmation == null ) {
				pageFullyHostedConfirmation = new RegisterExternalOntologyPageFullyHostedConfirmation(this);
			}
			RegisterExternalOntologyPageBase nextPage = pageFullyHostedConfirmation;
			contents.clear();
			contents.add(nextPage.getWidget());
		}
		
	}
	
	@Override
	protected void pageBack(WizardPageBase cp) {
		RegisterExternalOntologyPageBase currentPage = (RegisterExternalOntologyPageBase) cp;
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
		else if ( currentPage == metadataPage1 ) {
			RegisterExternalOntologyPageBase nextPage = null;
			if ( pageFullyHosted != null ) {
				nextPage = pageFullyHosted;
			}
			else if ( pageReHosted != null ) {
				nextPage = pageReHosted;
			}
			else if ( pageIndexed != null ) {
				nextPage = pageIndexed;
			}
			
			if ( nextPage != null ) {
				contents.clear();
				contents.add(nextPage.getWidget());
			}
		}
		else if ( currentPage == metadataPage2 ) {
			contents.clear();
			contents.add(metadataPage1.getWidget());
		}
		else if ( currentPage == metadataPage3 ) {
			contents.clear();
			contents.add(metadataPage2.getWidget());
		}
		else if ( currentPage == pageFullyHostedConfirmation ) {
			contents.clear();
			contents.add(metadataPage3.getWidget());
		}
	}


	void hostingTypeSelected(HostingType hostingType) {
		this.hostingType = hostingType;
		Main.log("hostingTypeSelected: " +hostingType);
	}


	@Override
	protected void finish(WizardPageBase cp) {
		RegisterExternalOntologyPageBase currentPage = (RegisterExternalOntologyPageBase) cp;
		
		assert currentPage == pageFullyHostedConfirmation
//		    || currentPage == pageReHostedConfirmation   TODO
//		    || currentPage == pageIndexedConfirmation    TODO
		;
		
		
		// Finish: fully hosted registration
		if ( currentPage == pageFullyHostedConfirmation ) {
			
			// TODO collect information and run the "review and register"
			// ...
			
			String error;
			
			Map<String, String> newValues = new HashMap<String, String>();
			if ( (error = pageFullyHosted.authorityShortNamePanel.putValues(newValues, true)) != null
			||   (error = metadataPage1.mdSection.putValues(newValues, true)) != null
			||   (error = metadataPage2.mdSection.putValues(newValues, true)) != null
			||   (error = metadataPage3.mdSection.putValues(newValues, true)) != null
			) {
				// Should not happen
				Window.alert(error);
				return;
			}
			
			
			
			CreateOntologyInfo createOntologyInfo = new CreateOntologyInfo();
			
			createOntologyInfo.setMetadataValues(newValues);
			
			OtherDataCreationInfo dataCreationInfo = new OtherDataCreationInfo();
			dataCreationInfo.setTempOntologyInfo(tempOntologyInfo);
			createOntologyInfo.setDataCreationInfo(dataCreationInfo);
			
			// set info of original ontology:
			createOntologyInfo.setBaseOntologyInfo(tempOntologyInfo);
			
			// set the desired authority/shortName combination:
			createOntologyInfo.setAuthority(pageFullyHosted.getAuthority());
			createOntologyInfo.setShortName(pageFullyHosted.getShortName());

			
			RegisterExternalOntologyExecute execute = new RegisterExternalOntologyExecute(createOntologyInfo);
			
			execute.reviewAndRegister();
		}
		
		// TODO Finish: re-hosted registration
//		else if ( currentPage == pageReHostedConfirmation ) {
//			
//		}
		// TODO Finish: indexed registration
//		else if ( currentPage == pageIndexedConfirmation ) {
//			
//		}
		
		
		// OLD preliminary dispatch TO BE DELETED
//		if ( currentPage == pageFullyHosted ) {
//			
//			// initialize info for the eventual submission
//			CreateOntologyInfo createOntologyInfo = new CreateOntologyInfo();
//			
//			// set info of original ontology:
//			createOntologyInfo.setBaseOntologyInfo(tempOntologyInfo);
//			
//			// set the desired authority/shortName combination:
//			String authority = pageFullyHosted.getAuthority();
//			String shortName = pageFullyHosted.getShortName();
//			createOntologyInfo.setAuthority(authority);
//			createOntologyInfo.setShortName(shortName);
//			
//			// and dispatch the rest of the process
//			portalMainPanel.createNewFromFile(createOntologyInfo);
//		}
	}


	String getOntologyUri() {
		return pageFullyHosted.getOntologyUri();
	}

}
