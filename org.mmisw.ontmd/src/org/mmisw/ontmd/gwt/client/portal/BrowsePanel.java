package org.mmisw.ontmd.gwt.client.portal;

import java.util.ArrayList;
import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;

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
	final OntologyTable ontologyTable = new OntologyTable();


	// all the ontologies from the registry
	private List<OntologyInfo> ontologyInfos;
	
	private LoginResult loginResult;
	
	// the current displayed elements
	private final List<OntologyInfo> selectedOntologyInfos = new ArrayList<OntologyInfo>();

	
	private HorizontalSplitPanel hSplit = new HorizontalSplitPanel();
	
	
	BrowsePanel(List<OntologyInfo> ontologyInfos, LoginResult loginResult) {
		super();
		
		this.loginResult = loginResult;
		
		setOntologyInfos(ontologyInfos);
	    selTree.update(this.ontologyInfos, loginResult);
	    ontologyTable.setOntologyInfos(this.ontologyInfos, loginResult);
	    
	    
	    hSplit.setLeftWidget(selTree);
	    hSplit.setRightWidget(ontologyTable);
		
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
	
	
	public void setOntologyInfos(List<OntologyInfo> ontologyInfos) {
		this.ontologyInfos = ontologyInfos;
		
	    selTree.update(this.ontologyInfos, loginResult);
	    ontologyTable.setOntologyInfos(this.ontologyInfos, loginResult);
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

	void setLoginResult(LoginResult loginResult_Old) {
		this.loginResult = loginResult_Old;
		selTree.update(ontologyInfos, loginResult_Old);
		ontologyTable.setOntologyInfos(ontologyInfos, loginResult_Old);
	}


}
