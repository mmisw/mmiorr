package org.mmisw.ontmd.gwt.client.portal.extont;


import org.mmisw.orrclient.gwt.client.rpc.ResolveUriResult;
import org.mmisw.orrclient.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.ontmd.gwt.client.Orr;
import org.mmisw.ontmd.gwt.client.portal.PortalConsts;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The starting page when the user indicates re-hosting type
 * 
 * @author Carlos Rueda
 */
class RegisterNewPageReHosted extends BasePage {
	
	private VerticalPanel contents = new VerticalPanel();
	
	private TempOntologyInfo tempOntologyInfo = null;
	protected HTML infoHtml = new HTML("(No URI indicated)");
	
	
	RegisterNewPageReHosted(RegisterNewWizard wizard) {
		this(wizard, true, true, false);
	}
	
	protected RegisterNewPageReHosted(RegisterNewWizard wizard,
			boolean includeBack, boolean includeNext, boolean includeFinish
	) {
		super(wizard, includeBack, includeNext, includeFinish);
		contents.setSize("650px", "200px");
		addContents(contents);
		
		recreate();
		nextButton.setEnabled(true);
	}

	
	private void recreate() {
		contents.clear();
		
		FlexTable panel = new FlexTable();
		panel.setWidth("100%");
		int row = 0;
		
		panel.setWidget(row, 0, infoHtml);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);

		contents.add(panel);
	}
	
	public void activate() {
		
		String error = null;
		
		final String uri = tempOntologyInfo != null ? tempOntologyInfo.getUri() : null;
		if ( uri == null ) {
			error = "<font color=\"red\">" +  
				"Error: your file cannot be registered. " +
				"</font>" +
				"<br/>" +
				"A URI is required to register your ontology in re-hosted mode, and this URI " +
				"is obtained from the contents of your file. However, no appropriate namespace was found " +
				"in your file." 
			;
		}
		else if ( tempOntologyInfo.isOntResolvable() ) {
			error = "<font color=\"red\">" +  
				"Error: your ontology cannot be registered in re-hosted mode. " +
				"</font>" +
				"<br/>" +
				"The associated URI corresponds to a fully hosted URI. You will need to register the " +
				"the ontology in fully hosted mode."
			;
		}
		
		if ( error != null ) {
			nextButton.setEnabled(false);
			statusHtml.setHTML(error);
			return;
		}
		
		// check that the uri is NOT registered already
		enable(false);
		
		statusHtml.setHTML("<img src=\"" +GWT.getModuleBaseURL()+ "images/loading.gif\"> " +
				"<i><font color=\"blue\">Please wait, checking URI ...</font></i>");
				
		
		AsyncCallback<ResolveUriResult> callback = new AsyncCallback<ResolveUriResult>() {
			public void onFailure(Throwable thr) {
				enable(true);
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				statusHtml.setHTML("<font color=\"red\">" +error+ "</font>");
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Window.alert(error);
			}

			public void onSuccess(ResolveUriResult resolveUriResult) {
				Orr.log("resolveUri <" +uri+ ">: call completed. resolveUriResult=" +resolveUriResult);
				enable(true);
				nextButton.setEnabled(false);
				
				String error = null;
				if ( resolveUriResult == null ) {
					// OK: not found.
					Orr.log("resolveUri: URI not found: " +uri);
				}
				else if ( resolveUriResult.getError() != null ) {
					error = "<font color=\"red\">" + 
							"error while checking URI: " +resolveUriResult.getError()+
							"</font>" +
							"<br/>" +
							"Please try again later.";
				}
				else if ( resolveUriResult.getRegisteredOntologyInfo() != null ) {
					// URI for ontology already exists.
					error = "<font color=\"red\">" +  
						"An ontology by this URI is already registered." +
						"</font>" +
						"<br/>" +
						"If you want to create a new version of the previously registered ontology " +
						"by this URI, you will need to browse to that ontology and select the " +
						"'Create new version' option."
					;
				}
				else if ( resolveUriResult.getEntityInfo() != null ) {
					// URI for entity exists.
					error = "<font color=\"red\">" +  
						"A term by this URI is already existing in the knowledge base." +
						"</font>" +
						"<br/>" +
						"Although no ontology is directly registered with this URI, " +
						"the MMI ORR cannot proceed with this registration because of the potential " +
						"URI conflict."
					;
				}
				else {
					// OK.
					Orr.log("resolveUri: URI not found: " +uri);
				}
				
				if ( error != null ) {
					statusHtml.setHTML(error);
				}
				else {
					statusHtml.setHTML(
							"Click Next to proceed with providing metadata to this ontology and " +
							"then complete the registration."
					);
					enable(true);
					nextButton.setEnabled(true);
				}
			}
		};
		Orr.service.resolveUri(uri, callback);
	}

	void updateTempOntologyInfo(TempOntologyInfo tempOntologyInfo) {
		this.tempOntologyInfo = tempOntologyInfo;
		
		String uri = tempOntologyInfo != null ? tempOntologyInfo.getUri() : null;
		infoHtml.setHTML(
			"<br/>" +
			"You have chosen your ontology " +
			(uri != null ? "<b>" +uri+ "</b>" : "") +
			"<br/>" +
			"to be <b>re-hosted</b> at the MMI ORR." +
			"<br/>" +
			"<br/>" +
			"<br/>" +
			"Please, see this <a target=\"_blank\" href=\"" +PortalConsts.REG_TYPE_HELP_PAGE+ "\"" +
					">manual page</a> for details." +
			"<br/>" 
		);

	}

}
