package org.mmisw.iserver.gwt.client.rpc;

import java.util.Map;


/**
 * Info about the result of a getUserInfo operation.
 * 
 * @author Carlos Rueda
 */
public class UserInfoResult extends BaseResult {
	private static final long serialVersionUID = 1L;

	
	private Map<String,String> props;


	public Map<String, String> getProps() {
		return props;
	}


	public void setProps(Map<String, String> props) {
		this.props = props;
	}

	
}
