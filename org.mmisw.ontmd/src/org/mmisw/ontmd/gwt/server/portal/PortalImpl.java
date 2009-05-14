package org.mmisw.ontmd.gwt.server.portal;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.core.IServer;
import org.mmisw.iserver.core.Server;
import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.ontmd.gwt.client.rpc.PortalBaseInfo;
import org.mmisw.ontmd.gwt.server.Config;



/**
 * The main Voc2Rdf back-end operations. 
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class PortalImpl  {

	private final Log log = LogFactory.getLog(PortalImpl.class);
	
	private final AppInfo appInfo = new AppInfo("MMI Portal");
	
	private PortalBaseInfo baseInfo = null;
	
	
	private IServer iserver = Server.getInstance();


	
	
	public PortalImpl() {
		log.info("initializing " +appInfo.getAppName()+ "...");
		appInfo.setVersion(
				Config.Prop.VERSION.getValue()+ " (" +
				Config.Prop.BUILD.getValue()  + ")"
		);

		log.info(appInfo.toString());
	}

	public AppInfo getAppInfo() {
		return appInfo;
	}
	

	public PortalBaseInfo getBaseInfo() {
		if ( baseInfo == null ) {
			prepareBaseInfo();
		}
		return baseInfo;
	}
	
	private void prepareBaseInfo() {
		log.info("preparing base info ...");
		

		log.info("preparing base info ... Done.");
	}
	
	
	
	public List<OntologyInfo> getAllOntologies() {
		return iserver.getAllOntologies();
	}
}
