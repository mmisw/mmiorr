package org.mmisw.ontmd.gwt.client.portal.extont;

import java.util.HashMap;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.HostingType;
import org.mmisw.iserver.gwt.client.rpc.OtherDataCreationInfo;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.TempOntologyInfo;
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
 * <p>
 * TODO complete implementation
 * 
 * @author Carlos Rueda
 */
public class RegisterVersionWizard extends BaseWizard {
	
	private final RegisterVersionPage0 page0 = new RegisterVersionPage0(this);
	private final RegisterVersionPage1 page1 = new RegisterVersionPage1(this);
	

	///////////////////////////////////////////////////////////////////////////////////
	// fully-hosted type pages
	private MetadataPage pageFullyHostedMetadataPage1;
	private MetadataPage pageFullyHostedMetadataPage2;
	private MetadataPage pageFullyHostedMetadataPage3;
	private RegisterVersionFullyHostedConfirmation pageFullyHostedConfirmation;

	
	///////////////////////////////////////////////////////////////////////////////////
	// re-hosted type pages
	private MetadataPage pageReHostedMetadataPage1;
	private MetadataPage pageReHostedMetadataPage2;
	private MetadataPage pageReHostedMetadataPage3;
	private RegisterVersionReHostedConfirmation pageReHostedConfirmation;

	
	///////////////////////////////////////////////////////////////////////////////////
	// TODO indexed type pages
	//private RegisterVersionIndexedConfirmation pageIndexedConfirmation;
	
	
	private final RegisteredOntologyInfo registeredOntologyInfo;
	private final HostingType hostingType;


	private boolean uploadFileIndicated;

	/**
	 * @param portalMainPanel 
	 */
	public RegisterVersionWizard(PortalMainPanel portalMainPanel, 
			RegisteredOntologyInfo registeredOntologyInfo,
			HostingType hostingType
	) {
		super(portalMainPanel);
		this.registeredOntologyInfo = registeredOntologyInfo;
		this.hostingType = hostingType;
		
		contents.setSize("650px", "300px");
		
		contents.add(page0.getWidget());
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
		
		if ( currentPage == page0 && uploadFileIndicated ) {
			contents.clear();
			contents.add(page1.getWidget());
			page1.activate();
		}
		
		else if ( currentPage == page0 || currentPage == page1 ) {
			assert hostingType != null;
			
			BasePage nextPage = null;
			
			switch ( hostingType ) {
			case FULLY_HOSTED:
				if ( pageFullyHostedMetadataPage1 == null ) {
					pageFullyHostedMetadataPage1 = new MetadataPage(this, 
					new MetadataSection1(HostingType.FULLY_HOSTED) {
						protected void formChanged() {
							pageFullyHostedMetadataPage1.formChanged();
						}
					});
				}
				nextPage = pageFullyHostedMetadataPage1;
				break;
			case RE_HOSTED:
				if ( pageReHostedMetadataPage1 == null ) {
					pageReHostedMetadataPage1 = new MetadataPage(this, 
					new MetadataSection1(HostingType.RE_HOSTED) {
						protected void formChanged() {
							pageReHostedMetadataPage1.formChanged();
						}
					});
				}
				nextPage = pageReHostedMetadataPage1;
				break;
			case INDEXED:
				// TODO
				Window.alert("hostingType " +hostingType+ " => TODO");
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
				pageFullyHostedConfirmation = new RegisterVersionFullyHostedConfirmation(this);
			}
			contents.clear();
			contents.add(pageFullyHostedConfirmation.getWidget());
		}
		
		///////////////////////////////////////////////////////////////////////////////////
		// re-hosted type pages

		if ( currentPage == pageReHostedMetadataPage1 ) {
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
				pageReHostedConfirmation = new RegisterVersionReHostedConfirmation(this);
			}
			contents.clear();
			contents.add(pageReHostedConfirmation.getWidget());
		}
		
	}
	
	@Override
	protected void pageBack(WizardPageBase cp) {
		BasePage currentPage = (BasePage) cp;
		
		if ( currentPage == pageFullyHostedMetadataPage1
		||   currentPage == pageReHostedMetadataPage1
		) {
			BasePage nextPage = page1;
			
			if ( uploadFileIndicated ) {
				nextPage = page1;
			}
			else {
				nextPage = page0;
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


	//////////////
	//
	// TODO finish()
	// REVIEW ALL THE FOLLOWING, WHICH WAS JUST COPIED-PASTED FROM THE
	// RegisterNew.. Wizard !!!!!!
	//
	// we need to assocciate the previous version, registeredOntologyInfo,
	// combined with the new file, if any, and the new metadata values ....
	///////////////////////////////////////////////
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
			if ( (error = pageFullyHostedMetadataPage1.mdSection.putValues(newValues, true)) != null
			||   (error = pageFullyHostedMetadataPage2.mdSection.putValues(newValues, true)) != null
			||   (error = pageFullyHostedMetadataPage3.mdSection.putValues(newValues, true)) != null
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
			createOntologyInfo.setAuthority(registeredOntologyInfo.getAuthority());
			createOntologyInfo.setShortName(registeredOntologyInfo.getShortName());
			
			RegisterNewExecute execute = new RegisterNewExecute(createOntologyInfo);
			
			execute.reviewAndRegisterNewOntology();
		}
		
		/////////////////////////////////////////////////////////////////////
		// Finish: re-hosted registration
		else if ( currentPage == pageReHostedConfirmation ) {
			
			// collect information and run the "review and register"
			String error;
			Map<String, String> newValues = new HashMap<String, String>();
			if ( (error = pageReHostedMetadataPage1.mdSection.putValues(newValues, true)) != null
			||   (error = pageReHostedMetadataPage2.mdSection.putValues(newValues, true)) != null
			||   (error = pageReHostedMetadataPage3.mdSection.putValues(newValues, true)) != null
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
		// TODO review this
		return registeredOntologyInfo.getUnversionedUri();
	}


	void setUploadFileIndicated(boolean uploadFileIndicated) {
		this.uploadFileIndicated = uploadFileIndicated;
	}

}
