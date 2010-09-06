package org.mmisw.ontmd.gwt.client.portal.extont;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.HostingType;
import org.mmisw.orrclient.gwt.client.rpc.OtherDataCreationInfo;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.ontmd.gwt.client.portal.PortalControl;
import org.mmisw.ontmd.gwt.client.portal.PortalMainPanel;
import org.mmisw.ontmd.gwt.client.portal.extont.RegisterVersionPage2.MdInitMode;
import org.mmisw.ontmd.gwt.client.portal.md.MetadataSection1;
import org.mmisw.ontmd.gwt.client.portal.md.MetadataSection2;
import org.mmisw.ontmd.gwt.client.portal.md.MetadataSection3;
import org.mmisw.ontmd.gwt.client.portal.wizard.WizardPageBase;

import com.google.gwt.user.client.Window;

/**
 * Sequence of wizard pages to register an external ontology.
 * 
 * <p>
 * Note that the already registered ontology may be one created by the voc2rdf or
 * vine modules (so, not necesarily an "external" ontology).
 * 
 * @author Carlos Rueda
 */
public class RegisterVersionWizard extends BaseWizard {
	
	private final RegisterVersionPage0 page0 = new RegisterVersionPage0(this);
	private final RegisterVersionPage1 page1 = new RegisterVersionPage1(this);
	private final RegisterVersionPage2 page2 = new RegisterVersionPage2(this);
	
	

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
	
	
	// loaded and registerd values for a particular property URI 
	static class Detail {
		String key;
		String loadedValue;
		String registeredValue;
		Detail(String key, String loadedValue, String registeredValue) {
			super();
			this.key = key;
			this.loadedValue = loadedValue;
			this.registeredValue = registeredValue;
		}
		
		public String toString() {
			return key+ ": loaded=" +loadedValue+ "  registered=" +registeredValue;
		}
	}

	/**
	 * Details about the metadata for the new version.
	 */
	class MdDetails {
	
		// the metadata values from the registered ontology
		private final Map<String, String> registeredMdValues;
		
		// the metadata values from the loaded file, if any
		private Map<String, String> loadedMdValues;
		
		private final Map<String, String> mdValuesWithLoadedPreferred = new HashMap<String, String>();
		private final Map<String, String> mdValuesWithRegisteredPreferred = new HashMap<String, String>();
	
		// loaded and registered metadata values
		final List<Detail> details = new ArrayList<Detail>();
		
		// number of differences (keys with different, non-blank values)
		int noDiffs;
		
		MdDetails(Map<String, String> registeredMdValues) {
			this.registeredMdValues = registeredMdValues;
		}
		
		void prepare() {
			TempOntologyInfo tempOntologyInfo = getTempOntologyInfo();
			
			loadedMdValues = tempOntologyInfo.getOntologyMetadata().getOriginalValues();
			
			Set<String> allKeys = new HashSet<String>();
			allKeys.addAll(loadedMdValues.keySet());
			allKeys.addAll(registeredMdValues.keySet());
			
			mdValuesWithLoadedPreferred.clear();
			mdValuesWithRegisteredPreferred.clear();
			
			details.clear();
			noDiffs = 0;
			
			for ( String key : allKeys ) {
				String loadedValue = loadedMdValues.get(key);
				String registeredValue = registeredMdValues.get(key);
				
				loadedValue = loadedValue == null ? "" : loadedValue.trim();
				registeredValue = registeredValue == null ? "" : registeredValue.trim();
				
				if ( registeredValue.length() > 0  ||  loadedValue.length() > 0 ) {
					Detail detail = new Detail(key, loadedValue, registeredValue);
					details.add(detail);
				}
				
				if ( registeredValue.length() > 0  &&  loadedValue.length() > 0 ) {
					if ( ! loadedValue.equals(registeredValue) ) {
						noDiffs++;
					}
				}
				
				if ( loadedValue.length() > 0 ) {
					mdValuesWithLoadedPreferred.put(key, loadedValue);
				}
				else if ( registeredValue.length() > 0 ) {
					mdValuesWithLoadedPreferred.put(key, registeredValue);
				}
				
				if ( registeredValue.length() > 0 ) {
					mdValuesWithRegisteredPreferred.put(key, registeredValue);
				}
				else if ( loadedValue.length() > 0 ) {
					mdValuesWithRegisteredPreferred.put(key, loadedValue);
				}
				
			}
		}
		
		void setMetadataValuesAccordingToInitMode() {
			switch (initMdMode) {
			case ONLY_FROM_LOADED:
				metadataValues = loadedMdValues;
				break;
			case ONLY_FROM_REGISTERED:
				metadataValues = registeredMdValues;
				break;
			case MERGE_PREFER_LOADED:
				metadataValues = mdValuesWithLoadedPreferred;
				break;
			case MERGE_PREFER_REGISTERED:
				metadataValues = mdValuesWithRegisteredPreferred;
				break;
			default:
				throw new AssertionError();
			}
		}
	}
	
	private final MdDetails mdDetails;
	
	// the values to be used to fill in the metadata pages
	private Map<String, String> metadataValues;


	private boolean uploadFileIndicated;
	private MdInitMode initMdMode;

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
		assert hostingType != null;
		
		mdDetails = new MdDetails(registeredOntologyInfo.getOntologyMetadata().getOriginalValues());
		
		contents.setSize("650px", "300px");
		
		contents.add(page0.getWidget());
		statusLoad.setText("");
	}
	
	
	void ontologyInfoObtained(TempOntologyInfo tempOntologyInfo) {
		assert tempOntologyInfo.getError() == null;
		
		this.setTempOntologyInfo(tempOntologyInfo);
		prepareMdInitOptions();
		page2.diffsUpdated();
	}
	
	
	private void prepareMdInitOptions() {
		mdDetails.prepare();
	}
	
	@Override
	protected void pageNext(WizardPageBase cp) {
		BasePage currentPage = (BasePage) cp;
		
		if ( currentPage == page0 && uploadFileIndicated ) {
			contents.clear();
			contents.add(page1.getWidget());
			page1.activate();
		}
		
		else if ( currentPage == page1 ) {
			contents.clear();
			contents.add(page2.getWidget());
			page2.activate();
		}
		
		else if ( currentPage == page0 || currentPage == page2 ) {
			MetadataPage nextPage = null;
			
			if ( currentPage == page0 ) {
				metadataValues = mdDetails.registeredMdValues;
			}
			else {
				assert initMdMode != null;
				mdDetails.setMetadataValuesAccordingToInitMode();
			}
			
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
				nextPage.mdSection.setValuesFromMap(metadataValues, false);
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
		
		if ( currentPage == page1 ) {
			contents.clear();
			contents.add(page0.getWidget());
		}
		else if ( currentPage == page2 ) {
			contents.clear();
			contents.add(page1.getWidget());
		}
		else if ( currentPage == pageFullyHostedMetadataPage1
		||        currentPage == pageReHostedMetadataPage1
		) {
			BasePage nextPage = uploadFileIndicated ? page2 : page0 ;
			
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
		
		if ( PortalControl.getInstance().getLoginResult() == null
		||   PortalControl.getInstance().getLoginResult().getError() != null
		) {
			// this should not normally happen -- only while I'm testing other functionalities
			Window.alert("No user logged in at this point--Please report this bug.");
			return;
		}
		
		TempOntologyInfo tempOntologyInfo = getTempOntologyInfo();
		
		if ( uploadFileIndicated ) {
			if ( tempOntologyInfo == null ) {
				// this should not normally happen -- only while I'm testing other functionalities
				Window.alert("Upload of file as indicated but no ontology has been uploaded--Please report this bug.");
				return;
			}
		}
		else {
			assert tempOntologyInfo == null;
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
			if ( (error = pageFullyHostedMetadataPage1.mdSection.putValuesInMap(newValues, true)) != null
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
			dataCreationInfo.setTempOntologyInfo(tempOntologyInfo);
			createOntologyInfo.setDataCreationInfo(dataCreationInfo);
			
			// set info of original ontology:
			createOntologyInfo.setBaseOntologyInfo(registeredOntologyInfo);
			
			createOntologyInfo.setPriorOntologyInfo(
					registeredOntologyInfo.getOntologyId(), 
					registeredOntologyInfo.getOntologyUserId(), 
					registeredOntologyInfo.getVersionNumber()
			);
			
			// set the desired authority/shortName combination:
			createOntologyInfo.setAuthority(registeredOntologyInfo.getAuthority());
			createOntologyInfo.setShortName(registeredOntologyInfo.getShortName());
			
			RegisterVersionExecute execute = new RegisterVersionExecute(createOntologyInfo);
			
			execute.reviewAndRegisterVersionOntology();
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
			createOntologyInfo.setBaseOntologyInfo(registeredOntologyInfo);
			
			createOntologyInfo.setPriorOntologyInfo(
					registeredOntologyInfo.getOntologyId(), 
					registeredOntologyInfo.getOntologyUserId(), 
					registeredOntologyInfo.getVersionNumber()
			);
			
			// set the desired authority/shortName combination:
			createOntologyInfo.setAuthority(registeredOntologyInfo.getAuthority());
			createOntologyInfo.setShortName(registeredOntologyInfo.getShortName());
			
			RegisterVersionExecute execute = new RegisterVersionExecute(createOntologyInfo);
			
			execute.reviewAndRegisterVersionOntology();
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
		
		// always, reset this
		setTempOntologyInfo(null);
		
		if ( !uploadFileIndicated ) {
			metadataValues = mdDetails.registeredMdValues;
		}
	}


	RegisteredOntologyInfo getRegisteredOntologyInfo() {
		return registeredOntologyInfo;
	}


	void InitMdModeSelected(MdInitMode initMdMode) {
		this.initMdMode = initMdMode;
	}


	public MdDetails getMdDetails() {
		return mdDetails;
	}

}
