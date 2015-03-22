package org.mmisw.orrclient.gwt.client.rpc.vine;

import java.io.Serializable;
import java.util.Map;

/**
 * Info about a mapping.
 * 
 * @author Carlos Rueda
 */
public class Mapping implements Serializable {
	private static final long serialVersionUID = 1L;

	private String left;
	private String relation;
	private String right;
	
	private Map<String,String> metadata;
	
	/** no-arg ctor required for the serialization */
	Mapping() {
	}
	
	
	public Mapping(String left, String relation, String right) {
		super();
		this.left = left;
		this.relation = relation;
		this.right = right;
	}
	
	
	public String getLeft() {
		return left;
	}
	public void setLeft(String left) {
		this.left = left;
	}
	public String getRelation() {
		return relation;
	}
	
	public void setRelation(String relation) {
		this.relation = relation;
	}


	public String getRight() {
		return right;
	}
	public void setRight(String right) {
		this.right = right;
	}


	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}
	
	public String toString() {
		return "Mapping<" +left+ ", " +relation+ ", " +right+ ">: " +metadata;
	}
}
