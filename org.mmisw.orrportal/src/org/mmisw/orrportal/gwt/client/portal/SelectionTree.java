package org.mmisw.orrportal.gwt.client.portal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.LoginResult;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;

/**
 * Selection tree.
 * 
 * @author Carlos Rueda
 */
public class SelectionTree extends Tree implements TreeListener {

	private static final String ALL_ONTOLOGIES = "All ontologies";
	private static final String REGISTERED_BY_ME = "Registered by me";
	private static final String REGISTERED_BY = "Registered by:";
	
	
	// {username -> userId} map
	private final Map<String,String> authors = new HashMap<String,String>();
	
	
	private final BrowsePanel browsePanel;
	private final Tree tree = this;
	
	private TreeItem allOntsTreeItem;
	
	private class AuthorItem extends TreeItem implements Comparable<AuthorItem> {
		String username;
		String userId;
		
		AuthorItem(String html, String username, String userId) {
			super(html);
			this.username = username;
			this.userId = userId;
		}
		
		public boolean equals(Object other) {
			return other instanceof AuthorItem
			    && ((AuthorItem) other).userId.equals(this.userId);
		}
		
		public int hashCode() {
			return userId.hashCode();
		}
		
		public int compareTo(AuthorItem o) {
			return userId.compareTo(o.userId);
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
	
	SelectionTree(BrowsePanel browsePanel) {
		super();
		this.browsePanel = browsePanel;
		
		initTree();
		tree.addTreeListener(this);
		
		tree.setAnimationEnabled(true);
	}
	
	private void initTree() {
		tree.removeItems();
		allOntsTreeItem = new TreeItem(ALL_ONTOLOGIES);
		tree.addItem(allOntsTreeItem);
	}
	
	void update(List<RegisteredOntologyInfo> ontologyInfos, LoginResult loginResult) {
		initTree();
		
		authors.clear();
		
		List<String> authorities = new ArrayList<String>();
		List<String> types = new ArrayList<String>();
		
		for ( RegisteredOntologyInfo oi : ontologyInfos ) {

			String username = oi.getUsername();
			if ( ! authors.containsKey(username) ) {
				authors.put(username, oi.getUserId());
			}

			String type = oi.getType().toString().toLowerCase();
			if ( ! types.contains(type) ) {
				types.add(type);
			}

			String auth = oi.getAuthority().toLowerCase();
			if ( ! authorities.contains(auth) ) {
				authorities.add(auth);
			}
			
		}

		if ( loginResult != null ) {
			allOntsTreeItem.addItem(new AuthorItem(REGISTERED_BY_ME, loginResult.getUserName(), loginResult.getUserId()));
		
			TreeItem registerByTreeItem = new TreeItem(REGISTERED_BY);

			allOntsTreeItem.addItem(registerByTreeItem);
		
			List<String> usernames = new ArrayList<String>();
			usernames.addAll(authors.keySet());
			usernames.remove(loginResult.getUserName());
			Collections.sort(usernames);
			for ( String author : usernames ) {
				registerByTreeItem.addItem(new AuthorItem(author, author, authors.get(author)));	
			}
		}
		allOntsTreeItem.setState(true);
		
		if ( types.size() > 0 ) {
			Collections.sort(types);
			TreeItem typeItem = new TreeItem("Type");
			tree.addItem(typeItem);
			for ( String type : types ) {
				typeItem.addItem(new TypeItem(type));
			}
			typeItem.setState(true);
		}
		
		if ( authorities.size() > 0 ) {
			Collections.sort(authorities);
			TreeItem authItem = new TreeItem("Authority");
			tree.addItem(authItem);
			for ( String auth : authorities ) {
				authItem.addItem(new AuthorityItem(auth));
			}
		}
		
	}

	public void onTreeItemSelected(TreeItem item) {
		
		if ( item instanceof AuthorItem ) {
//			String userId = ((AuthorItem) item).getUserId();
			String username = ((AuthorItem) item).username;
			History.newItem(PortalConsts.T_REGISTERED_BY_USER + "/" + username);
//			browsePanel.authorSelected(userId, false);
		}
		else if ( item instanceof AuthorityItem ) {
			String authority = item.getText();
			History.newItem(PortalConsts.T_REGISTERED_BY_AUTHORITY + "/" + authority);
//			browsePanel.authoritySelected(authority);
		}
		else if ( item instanceof TypeItem ) {
			String type = item.getText();
			browsePanel.typeSelected(type);
		}
		else {
			String text = item.getText();
			if ( text.equalsIgnoreCase(ALL_ONTOLOGIES) ) {
				History.newItem(PortalConsts.T_BROWSE);
				browsePanel.allSelected();
			}
		}
	}

	public void onTreeItemStateChanged(TreeItem item) {
		// TODO Auto-generated method stub
	}
	
}
