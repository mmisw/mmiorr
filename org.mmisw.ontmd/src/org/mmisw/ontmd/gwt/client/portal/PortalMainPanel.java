package org.mmisw.ontmd.gwt.client.portal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.ontmd.gwt.client.LoginListener;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.UserPanel;
import org.mmisw.ontmd.gwt.client.metadata.MainPanel;
import org.mmisw.ontmd.gwt.client.rpc.BaseInfo;
import org.mmisw.ontmd.gwt.client.rpc.LoginResult;
import org.mmisw.ontmd.gwt.client.util.MyDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The main panel.
 * 
 * @author Carlos Rueda
 */
public class PortalMainPanel extends VerticalPanel implements LoginListener, HistoryListener {

	private final LoginControlPanel loginControlPanel = new LoginControlPanel(this);
	
	private final HeaderPanel headerPanel = new HeaderPanel(loginControlPanel); 

	private final VerticalPanel bodyPanel = new VerticalPanel();
	private final BrowsePanel browsePanel;


	private UserPanel userPanel;
	private LoginResult loginResult;
	
	private MyDialog signInPopup;

	
	static Map<String, Object> historyTokenMap = new HashMap<String, Object>();
	
	
	
	PortalMainPanel(final Map<String, String> params, List<OntologyInfo> ontologyInfos) {
		super();
		
		History.addHistoryListener(this);
		
		///////////////////////////////////////////////////////////////////////////
		// conveniences for testing in development environment
		if ( ! GWT.isScript() ) {
			
			if ( true ) {    // true for auto-login
				loginResult = new LoginResult();
				loginResult.setSessionId("22222222222222222");
				loginResult.setUserId("1002");
				loginResult.setUserName("carueda");
			}
		}
		
		
	    if ( loginResult == null && params.get("sessionId") != null && params.get("userId") != null ) {
	    	loginResult = new LoginResult();
	    	loginResult.setSessionId(params.get("sessionId"));
	    	loginResult.setUserId(params.get("userId"));
	    }

	    browsePanel = new BrowsePanel(ontologyInfos, loginResult);
	    loginControlPanel.update(loginResult);
	    
		
	    this.add(headerPanel);
	    
	    this.add(bodyPanel);

	    bodyPanel.add(browsePanel);

	}
	
	
	void userSignedOut() {
		browsePanel.ontologyTable.showProgress();
	    loginResult = null;
	    browsePanel.setLoginResult(loginResult);
	    loginControlPanel.update(null);
	    
	}
	
	void userToSignIn() {
		if ( userPanel == null ) {
			userPanel = new UserPanel(this);
			signInPopup = new MyDialog(userPanel) {
				public boolean onKeyUpPreview(char key, int modifiers) {
					// avoid ENTER close the popup
					if ( key == KeyboardListener.KEY_ESCAPE  ) {
						hide();
						return false;
					}
					return true;
				}
			};
		}
		signInPopup.setText("Sign in");
		signInPopup.show();
	}

	public void loginOk(final LoginResult loginResult) {
		this.loginResult = loginResult;
		browsePanel.ontologyTable.showProgress();
		
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				browsePanel.setLoginResult(loginResult);
				loginControlPanel.update(loginResult);
				if ( signInPopup != null ) {
					signInPopup.hide();
				}
			}
		});
	    
	}


	public void onHistoryChanged(String historyToken) {
		
		Main.log("onHistoryChanged: " +historyToken);
		
		Object obj = historyTokenMap.get(historyToken);
		if ( obj instanceof OntologyInfo ) {
			OntologyInfo oi = (OntologyInfo) obj;
			
			Main.log("onHistoryChanged: OntologyInfo: " +oi.getUri());
			
			Map<String, String> params = new HashMap<String, String>();
			
			params.put("ontologyUri", oi.getUri());
			
			getBaseInfo(params);
		}
		
	}

	
	private void getBaseInfo(final Map<String, String> params) {
		AsyncCallback<BaseInfo> callback = new AsyncCallback<BaseInfo>() {
			public void onFailure(Throwable thr) {
				String error = thr.toString();
				while ( ( thr = thr.getCause()) != null ) {
					error += "\n" + thr.toString();
				}
				RootPanel.get().add(new Label(error));
			}

			public void onSuccess(BaseInfo bInfo) {
				String error = bInfo.getError();
				if ( error != null ) {
					RootPanel.get().add(new Label(error));
				}
				else {
					Main.baseInfo = bInfo;
					Widget mainPanel = new MainPanel(params);
					
					bodyPanel.clear();
					bodyPanel.add(mainPanel);
				}
			}
		};

		Main.log("Getting base info ...");
		Main.ontmdService.getBaseInfo(params, callback);
	}


}
