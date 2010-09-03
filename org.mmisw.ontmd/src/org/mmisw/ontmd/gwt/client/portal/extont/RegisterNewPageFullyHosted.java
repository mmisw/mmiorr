package org.mmisw.ontmd.gwt.client.portal.extont;


import org.mmisw.iserver.gwt.client.rpc.ResolveUriResult;
import org.mmisw.ontmd.gwt.client.Orr;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The end page when the user indicates fully hosting type
 * 
 * @author Carlos Rueda
 */
class RegisterNewPageFullyHosted extends BasePage {
	private static final String ONT_SERVICE_URL_FRAG = "XXXX";
	
	private static final String INFO_TEMPLATE = 
		"<br/>" +
		"You have chosen to have this ontology <b>fully hosted</b> at the MMI ORR." +
		"<br/>" +
		"<br/>" +
		"MMI ORR will assign an <b>" +ONT_SERVICE_URL_FRAG+ "</b>-based namespace to your ontology. " +
		"<br/>" +
		"<br/>" +
		"Please, provide the following information to compose the final URI for your ontology. " +
		"<br/>" +
		"Click Check to verify the resulting URI is not already registered."
		;


	private VerticalPanel contents = new VerticalPanel();
	AuthorityShortNamePanel authorityShortNamePanel = new AuthorityShortNamePanel(this);
	
	
	RegisterNewPageFullyHosted(RegisterNewWizard wizard) {
		super(wizard, true, true, false);
		contents.setSize("650px", "200px");
		addContents(contents);
		
		nextButton.setEnabled(false);
		recreate();
	}
	
	
	private void recreate() {
		contents.clear();
		
		FlexTable panel = new FlexTable();
		panel.setWidth("100%");
		int row = 0;
		
		String ontServiceUrl = Orr.getPortalBaseInfo().getOntServiceUrl();
		
		panel.setWidget(row, 0, new HTML(INFO_TEMPLATE.replaceFirst(ONT_SERVICE_URL_FRAG, ontServiceUrl)));
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		panel.setWidget(row, 0, authorityShortNamePanel);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		contents.add(panel);
	}

	public Widget getWidget() {
		authorityShortNamePanel.initFields();
		return super.getWidget();
	}

	void formChanged() {
		statusHtml.setHTML("");
		nextButton.setEnabled(false);
	}

	void checkAuthorityShortName(boolean fullCheck, String authority, String shortName) {
		if ( ! _checkComponent("authority abbreviation", authority) ) {
			return;
		}
		if ( ! _checkComponent("short name", shortName) ) {
			return;
		}
		
		if ( fullCheck ) {
			// Check resulting URI against the registry
			String ontServiceUrl = Orr.getPortalBaseInfo().getOntServiceUrl() + "/";
			String uri = ontServiceUrl + authority+"/" + shortName;
			resolveUri(uri);
		}
	}
	
	private boolean  _checkComponent(String compName, String value) {
		if ( value.length() == 0 ) {
			statusHtml.setHTML("<font color=\"red\">Please specify the " +compName+ " component</font>");
			return false;
		}
		if ( value.matches(".+(/|\\|:|\\s).*") ) {
			statusHtml.setHTML("<font color=\"red\">Invalid value for " +compName+ " component" +
					" (spaces or path separators are not valid)</font>");
			return false;
		}
		return true;
	}
	
	/**
	 * Requests an ontology or term to the back-end to verify that it is not registered
	 * (either as an ontology or a term).
	 * TODO perhaps better use a simplified RPC operation to simply check existence.
	 * @param uri
	 */
	private void resolveUri(final String uri) {
		AsyncCallback<ResolveUriResult> callback = new AsyncCallback<ResolveUriResult>() {
			public void onFailure(Throwable thr) {
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Window.alert(error);
			}

			public void onSuccess(ResolveUriResult resolveUriResult) {
				Orr.log("RegisterExternalOntologyPageFullyHosted <" +uri+ ">: call completed.");
				
				String error = null;
				if ( resolveUriResult == null ) {
					// OK, not found.
				}
				else if ( resolveUriResult.getError() != null ) {
					error = resolveUriResult.getError();
				}
				else if ( resolveUriResult.getRegisteredOntologyInfo() != null ) {
					// URI exists
					error = "There is an ontology already registered with this URI";
				}
				else if ( resolveUriResult.getEntityInfo() != null ) {
					// URI exists
					error = "There is a semantic entity in the repository identified with this URI";
				}
				//Else: OK, not found.
				
				if ( error != null ) {
					statusHtml.setHTML("<font color=\"red\">" +error+ "</font>");
				    return;
				}
				
				// OK
				statusHtml.setHTML("<font color=\"green\">Resulting URI is OK.</font> " +
						"<br/>" +
						"Click Next to continue."
				);
				nextButton.setEnabled(true);
			}
		};

		statusHtml.setHTML("<font color=\"blue\">Checking ...</font>");
		Orr.log("RegisterExternalOntologyPageFullyHosted: checking URI = " +uri);
		Orr.service.resolveUri(uri, callback);
	}


	String getAuthority() {
		return authorityShortNamePanel.getAuthority();
	}


	String getShortName() {
		return authorityShortNamePanel.getShortName();
	}


	String getOntologyUri() {
		return authorityShortNamePanel.getOntologyUri();
	}

}
