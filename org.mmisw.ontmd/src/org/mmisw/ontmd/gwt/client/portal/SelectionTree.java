package org.mmisw.ontmd.gwt.client.portal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.ontmd.gwt.client.rpc.LoginResult;

import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;

/**
 * ontology table.
 * 
 * @author Carlos Rueda
 */
public class SelectionTree extends Tree implements TreeListener {

	private final PortalMainPanel portalMainPanel;
	private final Tree tree = this;
	
	private TreeItem authorMenu;
	
	private class AuthorItem extends TreeItem {
		String userId;
		AuthorItem(String name, String userId) {
			super(name);
			this.userId = userId;
		}
		String getUserId() {
			return userId;
		}
		
	}
	
	private class AuthorityItem extends TreeItem {
		AuthorityItem(String authority) {
			super(authority);
		}
	}
	
	private class TypeItem extends TreeItem {
		TypeItem(String type) {
			super(type);
		}
	}
	
	SelectionTree(PortalMainPanel portalMainPanel) {
		super();
		this.portalMainPanel = portalMainPanel;
		
		initTree();
		tree.addTreeListener(this);
		
		tree.setAnimationEnabled(true);
	}
	
	private void initTree() {
		tree.removeItems();
		authorMenu = new TreeItem("Author");
		tree.addItem(authorMenu);
	}
	
	void update(List<OntologyInfo> ontologyInfos, LoginResult loginResult) {
		initTree();
		
		authorMenu.addItem(new TreeItem("All"));
		if ( loginResult != null ) {
			authorMenu.addItem(new AuthorItem(loginResult.getUserName(), loginResult.getUserId()));
		}
		authorMenu.setState(true);
		
		List<String> auths = new ArrayList<String>();
		List<String> types = new ArrayList<String>();
		
		for ( OntologyInfo oi : ontologyInfos ) {

			String type = oi.getType().toLowerCase();
			if ( ! types.contains(type) ) {
				types.add(type);
			}

			String auth = oi.getAuthority().toLowerCase();
			if ( ! auths.contains(auth) ) {
				auths.add(auth);
			}
			
		}
		
		if ( types.size() > 0 ) {
			Collections.sort(types);
			TreeItem typeItem = new TreeItem("Type");
			tree.addItem(typeItem);
			for ( String type : types ) {
				typeItem.addItem(new TypeItem(type));
			}
			typeItem.setState(true);
		}
		
		if ( auths.size() > 0 ) {
			Collections.sort(auths);
			TreeItem authItem = new TreeItem("Authority");
			tree.addItem(authItem);
			for ( String auth : auths ) {
				authItem.addItem(new AuthorityItem(auth));
			}
		}
		
	}

	public void onTreeItemSelected(TreeItem item) {
		
		if ( item instanceof AuthorItem ) {
			String userId = ((AuthorItem) item).getUserId();
			portalMainPanel.authorSelected(userId);
		}
		else if ( item instanceof AuthorityItem ) {
			String authority = item.getText();
			portalMainPanel.authoritySelected(authority);
		}
		else if ( item instanceof TypeItem ) {
			String type = item.getText();
			portalMainPanel.typeSelected(type);
		}
		else {
			String text = item.getText();
			if ( text.equalsIgnoreCase("all") ) {
				portalMainPanel.allSelected();
			}
		}
	}

	public void onTreeItemStateChanged(TreeItem item) {
		// TODO Auto-generated method stub
	}
	

}
