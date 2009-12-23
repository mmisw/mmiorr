package org.mmisw.ontmd.gwt.client.portal.extont;

import java.util.HashMap;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.HostingType;
import org.mmisw.iserver.gwt.client.rpc.OtherDataCreationInfo;
import org.mmisw.iserver.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.portal.PortalControl;
import org.mmisw.ontmd.gwt.client.portal.PortalMainPanel;
import org.mmisw.ontmd.gwt.client.portal.md.MetadataSection1;
import org.mmisw.ontmd.gwt.client.portal.md.MetadataSection2;
import org.mmisw.ontmd.gwt.client.portal.md.MetadataSection3;
import org.mmisw.ontmd.gwt.client.portal.wizard.WizardPageBase;

import com.google.gwt.user.client.Window;

/**
 * Sequence of wizard pages to register an external ontology.
 * 
 * @author Carlos Rueda
 */
public class RegisterNewWizard extends BaseWizard {
	
	private final RegisterNewPage1 page1 = new RegisterNewPage1(this);
	
	private final RegisterNewPage2 page2 = new RegisterNewPage2(this);

	///////////////////////////////////////////////////////////////////////////////////
	// fully-hosted type pages
	private RegisterNewPageFullyHosted pageFullyHosted;
	private MetadataPage pageFullyHostedMetadataPage1;
	private MetadataPage pageFullyHostedMetadataPage2;
	private MetadataPage pageFullyHostedMetadataPage3;
	private RegisterNewPageFullyHostedConfirmation pageFullyHostedConfirmation;

	
	///////////////////////////////////////////////////////////////////////////////////
	// re-hosted type pages
	private RegisterNewPageReHosted pageReHosted;
	private MetadataPage pageReHostedMetadataPage1;
	private MetadataPage pageReHostedMetadataPage2;
	private MetadataPage pageReHostedMetadataPage3;
	private RegisterNewPageReHostedConfirmation pageReHostedConfirmation;

	
	///////////////////////////////////////////////////////////////////////////////////
	// indexed type pages
	private RegisterNewPageIndexed pageIndexed;
	
	
	
	// TODO
	//private RegisterExternalOntologyPageReHostedConfirmation pageReHostedConfirmation;
	
	// TODO
//	private private RegisterExternalOntologyPageIndexedConfirmation pageIndexedConfirmation;
	
	
	
	
	private HostingType hostingType;

	/**
	 * @param portalMainPanel 
	 */
	public RegisterNewWizard(PortalMainPanel portalMainPanel) {
		super(portalMainPanel);
		contents.setSize("650px", "300px");
		
		contents.add(page1.getWidget());
		statusLoad.setText("");
	}
	
	
	void ontologyInfoObtained(TempOntologyInfo tempOntologyInfo) {
		assert tempOntologyInfo.getError() == null;
		
		this.setTempOntologyInfo(tempOntologyInfo);
	}
	
	@Override
	protected void pageNext(WizardPageBase cp) {
		BasePage currentPage = (BasePage) cp;
//		if ( tempOntologyInfo == null ) { TODO apply after testing
//			return;
//		}
		
		if ( currentPage == page1 ) {
			contents.clear();
			contents.add(page2.getWidget());
		}
		else if ( currentPage == page2 ) {
			assert hostingType != null;
			
			BasePage nextPage = null;
			
			switch ( hostingType ) {
			case FULLY_HOSTED:
				if ( pageFullyHosted == null ) {
					pageFullyHosted = new RegisterNewPageFullyHosted(this);
					pageReHosted = null;
					pageIndexed = null;
				}
				nextPage = pageFullyHosted;
				break;
			case RE_HOSTED:
				if ( pageReHosted == null ) {
					pageReHosted = new RegisterNewPageReHosted(this);
					pageFullyHosted = null;
					pageIndexed = null;
				}
				pageReHosted.updateTempOntologyInfo(getTempOntologyInfo());
				nextPage = pageReHosted;
				break;
			case INDEXED:
				if ( pageIndexed == null ) {
					pageIndexed = new RegisterNewPageIndexed(this);
					pageReHosted = null;
					pageFullyHosted = null;
				}
				pageIndexed.updateUri(getTempOntologyInfo().getUri());
				nextPage = pageIndexed;
				break;
			}
			
			if ( nextPage != null ) {
				contents.clear();
				contents.add(nextPage.getWidget());
				nextPage.activate();
			}
		}
		
		///////////////////////////////////////////////////////////////////////////////////
		// fully-hosted type pages

		else if ( currentPage == pageFullyHosted ) {
			if ( pageFullyHostedMetadataPage1 == null ) {
				pageFullyHostedMetadataPage1 = new MetadataPage(this, 
				new MetadataSection1(HostingType.FULLY_HOSTED) {
					protected void formChanged() {
						pageFullyHostedMetadataPage1.formChanged();
					}
				});
			}
			contents.clear();
			contents.add(pageFullyHostedMetadataPage1.getWidget());
		}
		else if ( currentPage == pageFullyHostedMetadataPage1 ) {
			if ( pageFullyHostedMetadataPage2 == null ) {
				pageFullyHostedMetadataPage2 = new MetadataPage(this, 
				new MetadataSection2() {
					protected void formChanged() {
						pageFullyHostedMetadataPage2.formChanged();
					}
				});
			}
			contents.clear();
			contents.add(pageFullyHostedMetadataPage2.getWidget());
		}
		else if ( currentPage == pageFullyHostedMetadataPage2 ) {
			if ( pageFullyHostedMetadataPage3 == null ) {
				pageFullyHostedMetadataPage3 = new MetadataPage(this, 
				new MetadataSection3()  {
					protected void formChanged() {
						pageFullyHostedMetadataPage3.formChanged();
					}
				});
			}
			contents.clear();
			contents.add(pageFullyHostedMetadataPage3.getWidget());
		}
		else if ( currentPage == pageFullyHostedMetadataPage3 ) {
			if ( pageFullyHostedConfirmation == null ) {
				pageFullyHostedConfirmation = new RegisterNewPageFullyHostedConfirmation(this);
			}
			contents.clear();
			contents.add(pageFullyHostedConfirmation.getWidget());
		}
		
		///////////////////////////////////////////////////////////////////////////////////
		// re-hosted type pages

		else if ( currentPage == pageReHosted ) {
			if ( pageReHostedMetadataPage1 == null ) {
				pageReHostedMetadataPage1 = new MetadataPage(this, 
				new MetadataSection1(HostingType.RE_HOSTED) {
					protected void formChanged() {
						pageReHostedMetadataPage1.formChanged();
					}
				});
			}
			contents.clear();
			contents.add(pageReHostedMetadataPage1.getWidget());
		}
		else if ( currentPage == pageReHostedMetadataPage1 ) {
			if ( pageReHostedMetadataPage2 == null ) {
				pageReHostedMetadataPage2 = new MetadataPage(this, 
				new MetadataSection2() {
					protected void formChanged() {
						pageReHostedMetadataPage2.formChanged();
					}
				});
			}
			contents.clear();
			contents.add(pageReHostedMetadataPage2.getWidget());
		}
		else if ( currentPage == pageReHostedMetadataPage2 ) {
			if ( pageReHostedMetadataPage3 == null ) {
				pageReHostedMetadataPage3 = new MetadataPage(this, 
				new MetadataSection3()  {
					protected void formChanged() {
						pageReHostedMetadataPage3.formChanged();
					}
				});
			}
			contents.clear();
			contents.add(pageReHostedMetadataPage3.getWidget());
		}
		else if ( currentPage == pageReHostedMetadataPage3 ) {
			if ( pageReHostedConfirmation == null ) {
				pageReHostedConfirmation = new RegisterNewPageReHostedConfirmation(this);
			}
			contents.clear();
			contents.add(pageReHostedConfirmation.getWidget());
		}
		
	}
	
	@Override
	protected void pageBack(WizardPageBase cp) {
		BasePage currentPage = (BasePage) cp;
		if ( currentPage == page2 ) {
			contents.clear();
			contents.add(page1.getWidget());
		}
		else if ( currentPage == pageFullyHosted 
		||   currentPage == pageReHosted 
		||   currentPage == pageIndexed 
		) {
			contents.clear();
			contents.add(page2.getWidget());
		}
		
		else if ( currentPage == pageFullyHostedMetadataPage1
		     ||   currentPage == pageReHostedMetadataPage1
		) {
			BasePage nextPage = null;
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
		else if ( currentPage == pageFullyHostedMetadataPage2 ) {
			contents.clear();
			contents.add(pageFullyHostedMetadataPage1.getWidget());
		}
		else if ( currentPage == pageFullyHostedMetadataPage3 ) {
			contents.clear();
			contents.add(pageFullyHostedMetadataPage2.getWidget());
		}
		else if ( currentPage == pageFullyHostedConfirmation ) {
			contents.clear();
			contents.add(pageFullyHostedMetadataPage3.getWidget());
		}
		
		else if ( currentPage == pageReHostedMetadataPage2 ) {
			contents.clear();
			contents.add(pageReHostedMetadataPage1.getWidget());
		}
		else if ( currentPage == pageReHostedMetadataPage3 ) {
			contents.clear();
			contents.add(pageReHostedMetadataPage2.getWidget());
		}
		else if ( currentPage == pageReHostedConfirmation ) {
			contents.clear();
			contents.add(pageReHostedMetadataPage3.getWidget());
		}
		
	}


	void hostingTypeSelected(HostingType hostingType) {
		this.hostingType = hostingType;
		Main.log("hostingTypeSelected: " +hostingType);
	}


	@Override
	protected void finish(WizardPageBase cp) {
		BasePage currentPage = (BasePage) cp;
		
		if ( getTempOntologyInfo() == null ) {
			// this should not normally happen -- only while I'm testing other functionalities
			Window.alert("No ontology info has been specified--Please report this bug.");
			return;
		}
		if ( PortalControl.getInstance().getLoginResult() == null
		||   PortalControl.getInstance().getLoginResult().getError() != null
		) {
			// this should not normally happen -- only while I'm testing other functionalities
			Window.alert("No user logged in at this point--Please report this bug.");
			return;
		}
		

		assert currentPage == pageFullyHostedConfirmation
		    || currentPage == pageReHostedConfirmation
//		    || currentPage == pageIndexedConfirmation    TODO
		;
		
		/////////////////////////////////////////////////////////////////////
		// Finish: fully hosted registration
		if ( currentPage == pageFullyHostedConfirmation ) {
			
			// collect information and run the "review and register"
			String error;
			Map<String, String> newValues = new HashMap<String, String>();
			if ( (error = pageFullyHosted.authorityShortNamePanel.putValues(newValues, true)) != null
			||   (error = pageFullyHostedMetadataPage1.mdSection.putValuesInMap(newValues, true)) != null
			||   (error = pageFullyHostedMetadataPage2.mdSection.putValuesInMap(newValues, true)) != null
			||   (error = pageFullyHostedMetadataPage3.mdSection.putValuesInMap(newValues, true)) != null
			) {
				// Should not happen
				Window.alert(error);
				return;
			}
			
			CreateOntologyInfo createOntologyInfo = new CreateOntologyInfo();
			createOntologyInfo.setHostingType(HostingType.FULLY_HOSTED);
			
			createOntologyInfo.setMetadataValues(newValues);
			
			OtherDataCreationInfo dataCreationInfo = new OtherDataCreationInfo();
			dataCreationInfo.setTempOntologyInfo(getTempOntologyInfo());
			createOntologyInfo.setDataCreationInfo(dataCreationInfo);
			
			// set info of original ontology:
			createOntologyInfo.setBaseOntologyInfo(getTempOntologyInfo());
			
			// set the desired authority/shortName combination:
			createOntologyInfo.setAuthority(pageFullyHosted.getAuthority());
			createOntologyInfo.setShortName(pageFullyHosted.getShortName());
			
			RegisterNewExecute execute = new RegisterNewExecute(createOntologyInfo);
			
			execute.reviewAndRegisterNewOntology();
		}
		
		/////////////////////////////////////////////////////////////////////
		// Finish: re-hosted registration
		else if ( currentPage == pageReHostedConfirmation ) {
			
			// collect information and run the "review and register"
			String error;
			Map<String, String> newValues = new HashMap<String, String>();
			if ( (error = pageReHostedMetadataPage1.mdSection.putValuesInMap(newValues, true)) != null
			||   (error = pageReHostedMetadataPage2.mdSection.putValuesInMap(newValues, true)) != null
			||   (error = pageReHostedMetadataPage3.mdSection.putValuesInMap(newValues, true)) != null
			) {
				// Should not happen
				Window.alert(error);
				return;
			}
			
			CreateOntologyInfo createOntologyInfo = new CreateOntologyInfo();
			createOntologyInfo.setHostingType(HostingType.RE_HOSTED);
			
			createOntologyInfo.setMetadataValues(newValues);
			
			OtherDataCreationInfo dataCreationInfo = new OtherDataCreationInfo();
			dataCreationInfo.setTempOntologyInfo(getTempOntologyInfo());
			createOntologyInfo.setDataCreationInfo(dataCreationInfo);
			
			// set info of original ontology:
			createOntologyInfo.setBaseOntologyInfo(getTempOntologyInfo());
			
			
			RegisterNewExecute execute = new RegisterNewExecute(createOntologyInfo);
			
			execute.reviewAndRegisterNewOntology();
		}
		
		/////////////////////////////////////////////////////////////////////
		// TODO Finish: indexed registration
//		else if ( currentPage == pageIndexedConfirmation ) {
//			
//		}
		

	}


	String getOntologyUri() {
		// TODO review for the other types of hosting
		if ( pageFullyHosted != null ) {
			return pageFullyHosted.getOntologyUri();
		}
		else {
			String namespace = getTempOntologyInfo().getUri();
			return namespace;
		}
	}

}
