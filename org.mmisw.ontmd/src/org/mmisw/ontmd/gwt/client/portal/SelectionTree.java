package org.mmisw.ontmd.gwt.client.portal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;

import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;

/**
 * ontology table.
 * 
 * @author Carlos Rueda
 */
public class SelectionTree extends Tree implements TreeListener {

	private static final String ALL = "-All-";
	private final BrowsePanel browsePanel;
	private final Tree tree = this;
	
	private TreeItem authorMenu;
	
	private class AuthorItem extends TreeItem implements Comparable<AuthorItem> {
		private String userId;
		AuthorItem(String name, String userId) {
			super(name);
			this.userId = userId;
		}
		String getUserId() {
			return userId;
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
		authorMenu = new TreeItem("Author");
		tree.addItem(authorMenu);
	}
	
	void update(List<OntologyInfo> ontologyInfos, LoginResult loginResult_Old) {
		initTree();
		
		// {username -> userId} map
		Map<String,String> authors = new HashMap<String,String>();
		
		List<String> authorities = new ArrayList<String>();
		List<String> types = new ArrayList<String>();
		
		for ( OntologyInfo oi : ontologyInfos ) {

			String username = oi.getUsername();
			if ( ! authors.containsKey(username) ) {
				authors.put(username, oi.getUserId());
			}

			String type = oi.getType().toLowerCase();
			if ( ! types.contains(type) ) {
				types.add(type);
			}

			String auth = oi.getAuthority().toLowerCase();
			if ( ! authorities.contains(auth) ) {
				authorities.add(auth);
			}
			
		}
		
		authorMenu.addItem(new TreeItem(ALL));
		if ( loginResult_Old != null ) {
			List<String> usernames = new ArrayList<String>();
			usernames.addAll(authors.keySet());
			Collections.sort(usernames);
			for ( String author : usernames ) {
				authorMenu.addItem(new AuthorItem(author, authors.get(author)));	
			}
		}
		authorMenu.setState(true);
		
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
			String userId = ((AuthorItem) item).getUserId();
			browsePanel.authorSelected(userId);
		}
		else if ( item instanceof AuthorityItem ) {
			String authority = item.getText();
			browsePanel.authoritySelected(authority);
		}
		else if ( item instanceof TypeItem ) {
			String type = item.getText();
			browsePanel.typeSelected(type);
		}
		else {
			String text = item.getText();
			if ( text.equalsIgnoreCase(ALL) ) {
				browsePanel.allSelected();
			}
		}
	}

	public void onTreeItemStateChanged(TreeItem item) {
		// TODO Auto-generated method stub
	}
	

}
