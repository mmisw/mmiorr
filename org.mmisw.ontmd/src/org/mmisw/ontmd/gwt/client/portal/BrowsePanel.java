package org.mmisw.ontmd.gwt.client.portal;

import java.util.ArrayList;
import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.ontmd.gwt.client.rpc.LoginResult;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The main panel.
 * 
 * @author Carlos Rueda
 */
public class BrowsePanel extends VerticalPanel {

	private final MenuBarPanel menuBarPanel = new MenuBarPanel();
	private final SelectionTree selTree = new SelectionTree(this);
	final OntologyTable ontologyTable = new OntologyTable();


	// all the ontologies from the registry
	private List<OntologyInfo> ontologyInfos;
	
	private LoginResult loginResult;
	
	// the current displayed elements
	private final List<OntologyInfo> selectedOntologyInfos = new ArrayList<OntologyInfo>();

	
	private HorizontalSplitPanel hSplit = new HorizontalSplitPanel();
	
	
	BrowsePanel(List<OntologyInfo> ontologyInfos, LoginResult loginResult) {
		super();
		
		this.ontologyInfos = ontologyInfos;
		this.loginResult = loginResult;
		
	    menuBarPanel.setLoginResult(loginResult);
	    
	    selTree.update(this.ontologyInfos, loginResult);
	    ontologyTable.setOntologyInfos(this.ontologyInfos, loginResult);
	    
	    
	    this.add(menuBarPanel);
	    
	    menuBarPanel.showMenuBar(loginResult != null);
	    
	    hSplit.setLeftWidget(selTree);
	    hSplit.setRightWidget(ontologyTable);
		
	    hSplit.setSplitPosition("200px");
	    hSplit.setHeight("500px");
	    
	    
		DecoratorPanel decPanel = new DecoratorPanel();
		this.add(decPanel);
	    decPanel.setWidget(hSplit);

	    hSplit.setWidth("1200px");

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
				for ( OntologyInfo oi : ontologyInfos ) {
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
				for ( OntologyInfo oi : ontologyInfos ) {
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
				for ( OntologyInfo oi : ontologyInfos ) {
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
		menuBarPanel.showMenuBar(loginResult != null);
		selTree.update(ontologyInfos, loginResult);
		ontologyTable.setOntologyInfos(ontologyInfos, loginResult);
	}


}
