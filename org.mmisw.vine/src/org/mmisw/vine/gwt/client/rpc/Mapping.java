package org.mmisw.vine.gwt.client.rpc;

import java.io.Serializable;

/**
 * Info about a mapping.
 * 
 * @author Carlos Rueda
 */
public class Mapping implements Serializable {
	private static final long serialVersionUID = 1L;

	// TODO determine proper attributes of this class
	private String left;
	private String center;
	private String right;
	

	public Mapping(String left, String center, String right) {
		super();
		this.left = left;
		this.center = center;
		this.right = right;
	}
	
	
	public String getLeft() {
		return left;
	}
	public void setLeft(String left) {
		this.left = left;
	}
	public String getCenter() {
		return center;
	}
	public void setCenter(String center) {
		this.center = center;
	}
	public String getRight() {
		return right;
	}
	public void setRight(String right) {
		this.right = right;
	}

}
