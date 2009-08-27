package org.mmisw.iserver.gwt.client.rpc.vine;

import java.io.Serializable;

/**
 * Info about a mapping.
 * 
 * @author Carlos Rueda
 */
public class Mapping implements Serializable {
	private static final long serialVersionUID = 1L;

	private String left;
	private RelationInfo relInfo;
	private String right;
	
	/** no-arg ctor required for the serialization */
	public Mapping() {
	}
	
	
	public Mapping(String left, RelationInfo relInfo, String right) {
		super();
		this.left = left;
		this.relInfo = relInfo;
		this.right = right;
	}
	
	
	public String getLeft() {
		return left;
	}
	public void setLeft(String left) {
		this.left = left;
	}
	public RelationInfo getRelationInfo() {
		return relInfo;
	}
	public String getRight() {
		return right;
	}
	public void setRight(String right) {
		this.right = right;
	}

}
