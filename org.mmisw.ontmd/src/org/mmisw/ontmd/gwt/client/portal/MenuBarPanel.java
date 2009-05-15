package org.mmisw.ontmd.gwt.client.portal;

import org.mmisw.ontmd.gwt.client.rpc.LoginResult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * @author Carlos Rueda
 */
public class MenuBarPanel extends VerticalPanel {

	private LoginResult loginResult;
	private MenuBar mb = null;

	/** Initially the menu bar is not shown */
	MenuBarPanel() {

	}

	void setLoginResult(LoginResult loginResult) {
		this.loginResult = loginResult;
	}

	void showMenuBar(boolean show) {
		clear();
		if (show) {
			if (mb == null) {
				createMenuBar();
			}
			add(mb);
		}
	}

	
	private String createLinkVoc2Rdf() {
		String link = GWT.getModuleBaseURL()+ "voc2rdf/";
		if ( GWT.isClient() ) {
			link += "index.html";
		}
		if ( loginResult != null ) {
			link += "?userId=" +loginResult.getUserId();
			link += "&sessionid=" +loginResult.getSessionId();
		}
		return link;
	}
	
	private String createLinkVine() {
		String link = "http://mmisw.org/vine/";
		if ( loginResult != null ) {
			link += "?userId=" +loginResult.getUserId();
			link += "&sessionid=" +loginResult.getSessionId();
		}
		return link;
	}

	private String createLinkUpload() {
		String link = GWT.getModuleBaseURL()+ "?_edit=y";
		if ( loginResult != null ) {
			link += "&userId=" +loginResult.getUserId();
			link += "&sessionid=" +loginResult.getSessionId();
		}
		return link;
	}

	private void createMenuBar() {
		mb = new MenuBar();

		// New
		MenuBar new_mb = new MenuBar(true);
		new_mb.addItem(new MenuItem("Vocabulary (Voc2RDF)", new Command() {
			public void execute() {
				String url = createLinkVoc2Rdf();
				String features = null;
				Window.open(url, "_blank", features);
			}
		}));
		new_mb.addItem(new MenuItem("Mapping (Vine)", new Command() {
			public void execute() {
				String url = createLinkVine();
				String features = null;
				Window.open(url, "_blank", features);
			}
		}));
		mb.addItem(new MenuItem("New", new_mb));

		mb.addItem(new MenuItem("Upload", new Command() {
			public void execute() {
				String url = createLinkUpload();
				String features = null;
				Window.open(url, "_blank", features);
			}
		}));

	}
}
