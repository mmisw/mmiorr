package org.mmisw.ontmd.gwt.client.portal;

import java.util.ArrayList;
import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.util.Util;
import org.mmisw.ontmd.gwt.client.util.table.IOntologyTable;
import org.mmisw.ontmd.gwt.client.util.table.OntologyTableCreator;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The main panel.
 * 
 * @author Carlos Rueda
 */
public class BrowsePanel extends VerticalPanel {

	private final SelectionTree selTree = new SelectionTree(this);
	final IOntologyTable ontologyTable = OntologyTableCreator.create(PortalControl.getInstance().getQuickInfo(), false);

	// all the ontologies from the registry
	private List<RegisteredOntologyInfo> allOntologyInfos;
	
	// working ontologies
	private List<RegisteredOntologyInfo> ontologyInfos;
	
	// the current displayed elements
	private final List<RegisteredOntologyInfo> selectedOntologyInfos = new ArrayList<RegisteredOntologyInfo>();

	
	private LoginResult loginResult;
	
	private HorizontalSplitPanel hSplit = new HorizontalSplitPanel();
	
	
	/**
	 * 
	 * @param ontologyInfos List of all ontologies to be considered. Some of them may be skipped
	 *             if no user is logged in or is user is not an admin.
	 * @param loginResult Infor about user logged in, if any.
	 */
	BrowsePanel(List<RegisteredOntologyInfo> ontologyInfos, LoginResult loginResult) {
		super();
		
		this.loginResult = loginResult;
		
		setAllOntologyInfos(ontologyInfos);
	    selTree.update(this.ontologyInfos, loginResult);
	    ontologyTable.setOntologyInfos(this.ontologyInfos, loginResult);
	    
	    
	    hSplit.setLeftWidget(selTree);
	    hSplit.setRightWidget(ontologyTable.getWidget());
		
	    hSplit.setSplitPosition("200px");
	    hSplit.setHeight("500px");
	    
	    _setSplitWidth100();
	    
	    DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(hSplit);
	    
	    HorizontalPanel hp = new HorizontalPanel();
//	    hp.setBorderWidth(1);
	    hp.setWidth("100%");
	    hp.add(decPanel);
	    
//	    this.add(decPanel);
	    this.add(hp);
	}
	
	
	/**
	 * Sets the list of all ontologies.
	 * @param ontologyInfos
	 */
	public void setAllOntologyInfos(List<RegisteredOntologyInfo> ontologyInfos) {
		this.allOntologyInfos = ontologyInfos;
		updatedAllOntologyInfosAndLogin();
	}
	
	/**
	 * Does updates according to current allOntologyInfos and loginResult
	 */
	private void updatedAllOntologyInfosAndLogin() {
		Main.log("updatedOntologyInfosAndLogin: loginResult=" +loginResult);
		
		
		if ( loginResult != null && loginResult.isAdministrator() ) {
			// admin? use all ontologies
			ontologyInfos = allOntologyInfos;
		}
		else {
			// not admin: remove "testing" and "internal" entries unless owned
			// by the logged-in user, if any:
			
			String userId = loginResult != null ? loginResult.getUserId() : null;
			ontologyInfos = new ArrayList<RegisteredOntologyInfo>();
			
			for ( RegisteredOntologyInfo oi : allOntologyInfos ) {
				if ( Util.isTestingOntology(oi) || Util.isInternalOntology(oi) ) {
					if ( userId != null && userId.equals(oi.getOntologyUserId()) ) {
						ontologyInfos.add(oi);	
					}
				}
				else {
					ontologyInfos.add(oi);
				}
			}
		}
		
	    selTree.update(ontologyInfos, loginResult);
	    ontologyTable.setOntologyInfos(ontologyInfos, loginResult);
	}
	
	/**
	 * Makes hSplit's width 100%.  
	 * Ideally hSplit.setWidth("100%") should work, but this is workaround to a GWT bug:
	 * http://code.google.com/p/google-web-toolkit/issues/detail?id=1599
	 */
	private void _setSplitWidth100() {
//		hSplit.setWidth("100%");
	    _setSplitWidth();
	    Window.addWindowResizeListener(new WindowResizeListener() {
			public void onWindowResized(int width, int height) {
				_setSplitWidth();
			}
	    });
	}
	private void _setSplitWidth() {
	    int clientWidth = (int) (0.99 * Window.getClientWidth());
	    if ( clientWidth < 400 ) {
	    	clientWidth = 400;
	    }
	    hSplit.setWidth(clientWidth+ "px");
	}
	
	
	void allSelected() {
		ontologyTable.showProgress();
		ontologyTable.setOntologyInfos(ontologyInfos, loginResult);
	}
		
	void authorSelected(final String userId) {
		ontologyTable.showProgress();
		
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				selectedOntologyInfos.clear();
				for ( RegisteredOntologyInfo oi : ontologyInfos ) {
					if ( userId.equalsIgnoreCase(oi.getUserId()) ) {
						selectedOntologyInfos.add(oi);
					}
				}
				ontologyTable.setOntologyInfos(selectedOntologyInfos, loginResult);
			}
		});
	}

	void authoritySelected(final String auth) {
		ontologyTable.showProgress();
		
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				selectedOntologyInfos.clear();
				for ( RegisteredOntologyInfo oi : ontologyInfos ) {
					if ( auth.equalsIgnoreCase(oi.getAuthority()) ) {
						selectedOntologyInfos.add(oi);
					}
				}
				ontologyTable.setOntologyInfos(selectedOntologyInfos, loginResult);
			}
		});
	}

	void typeSelected(final String type) {
		ontologyTable.showProgress();
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				selectedOntologyInfos.clear();
				for ( RegisteredOntologyInfo oi : ontologyInfos ) {
					if ( type.equalsIgnoreCase(oi.getType()) ) {
						selectedOntologyInfos.add(oi);
					}
				}
				ontologyTable.setOntologyInfos(selectedOntologyInfos, loginResult);
			}
		});
		
	}

	void setLoginResult(LoginResult loginResult) {
		this.loginResult = loginResult;
		updatedAllOntologyInfosAndLogin();
	}


	void searchOntologies(final String searchString, final Command doneCmd) {
		ontologyTable.showProgress();
		
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				try {
					selectedOntologyInfos.clear();
					
					// empty searchString? -> add all:
					if ( searchString.trim().length() == 0 ) {
						selectedOntologyInfos.addAll(ontologyInfos);
					}
					else {
						for ( RegisteredOntologyInfo oi : ontologyInfos ) {
							boolean include = false;
							
							String[] strings = { 
									oi.getDisplayLabel(),
									oi.getContactName(),
									oi.getVersionNumber(),
									oi.getUri(),
							};
							
							for ( String string : strings ) {
								if ( string != null && string.toLowerCase().indexOf(searchString.toLowerCase()) >= 0 ) {
									include = true;
									break;
								}
							}
							
							if ( include ) {
								selectedOntologyInfos.add(oi);
							}
						}
					}
					
					ontologyTable.setOntologyInfos(selectedOntologyInfos, loginResult);
				}
				finally {
					if ( doneCmd != null ) {
						doneCmd.execute();
					}
				}
			}
		});
	
	}


}
