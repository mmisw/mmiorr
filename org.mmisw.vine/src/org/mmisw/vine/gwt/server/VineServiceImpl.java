package org.mmisw.vine.gwt.server;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.core.IServer;
import org.mmisw.iserver.core.Server;
import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.vine.gwt.client.rpc.RelationInfo;
import org.mmisw.vine.gwt.client.rpc.VineService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;



/**
 * Implementation of VineService. 
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class VineServiceImpl extends RemoteServiceServlet implements VineService {
	private static final long serialVersionUID = 1L;
	
//	private static final String ONT = "http://mmisw.org/ont";
//	private static final String VOCABS = ONT + "?vocabs";
//	private static final String MAPPINGS = ONT + "?mappings";

	private static final List<OntologyInfo> EMPTY_ONTOLOGY_INFO_LIST = new ArrayList<OntologyInfo>();

	
	private final AppInfo appInfo = new AppInfo("Web VINE");
	private final Log log = LogFactory.getLog(VineServiceImpl.class);
	
	
	private IServer iserver = Server.getInstance();
	
	
	
	public void init() throws ServletException {
		super.init();
		log.info("initializing " +appInfo.getAppName()+ "...");
		try {
			Config.getInstance().init(getServletConfig(), log);
			
			appInfo.setVersion(
					Config.Prop.VERSION.getValue()+ " (" +
						Config.Prop.BUILD.getValue()  + ")"
			);
					
			log.info(appInfo.toString());
			
		}
		catch (Throwable ex) {
			log.error("Cannot initialize: " +ex.getMessage(), ex);
			// TODO throw ServletException
			// NOTE: apparently this happens because getServletConfig fails in hosted mode (GWT 1.5.2). 
			// Normally we should throw a Servlet exception as the following: 
//			throw new ServletException("Cannot initialize", ex);
			// but, I'm ignoring it as this is currently only for version information.
		}
	}
	
	public void destroy() {
		super.destroy();
		log.info(appInfo+ ": destroy called.\n\n");
	}
	
	public AppInfo getAppInfo() {
		return appInfo;
	}
	

	
	/**
	 * 
	 */
	public List<OntologyInfo> getAllOntologies() {
		try {
			return iserver.getAllOntologies(false);
		}
		catch (Exception e) {
			log.error("error getting list of ontologies", e);
			return EMPTY_ONTOLOGY_INFO_LIST;
		}
	}
	
	public OntologyInfo getEntities(OntologyInfo ontologyInfo) {
		return iserver.getEntities(ontologyInfo);
	}


	public String performMapping(List<String> leftTerms, int relationCode,
			List<String> rightTerms) {
		// TODO performMapping not implemented yet
		return null;
	}

	
	
	public List<RelationInfo> getRelationInfos() {
		if ( log.isDebugEnabled() ) {
			log.debug("getRelationInfos starting");
		}
	
		// TODO: determine mechanism to obtain the list of default mapping relations, for example,
		// from an ontology.
		
		// For now, creating a hard-coded list
		
		List<RelationInfo> relInfos = new ArrayList<RelationInfo>();
		
//      URI="http://www.w3.org/2008/05/skos#exactMatch"
//      icon="icons/exactMatch28.png"
//      name="exactMatch"
//      tooltip="The property skos:exactMatch is used to link two concepts, indicating a high degree of confidence that the concepts can be used interchangeably across a wide range of information retrieval applications. [SKOS Section 10.1] (transitive, symmetric)"
		relInfos.add(new RelationInfo(
				"http://www.w3.org/2008/05/skos#exactMatch", 
				"exactMatch28.png", 
				"exactMatch",
				"The property skos:exactMatch is used to link two concepts, indicating a high degree of confidence that the concepts can be used interchangeably across a wide range of information retrieval applications. [SKOS Section 10.1] (transitive, symmetric)"
		));
		
//      URI="http://www.w3.org/2008/05/skos#closeMatch"
//      icon="icons/closeMatch28.png"
//      name="closeMatch"
//      tooltip="A skos:closeMatch link indicates that two concepts are sufficiently similar that they can be used interchangeably in some information retrieval applications. [SKOS Section 10.1] (symmetric)"
		relInfos.add(new RelationInfo(
				"http://www.w3.org/2008/05/skos#closeMatch", 
				"closeMatch28.png", 
				"closeMatch",
				"A skos:closeMatch link indicates that two concepts are sufficiently similar that they can be used interchangeably in some information retrieval applications. [SKOS Section 10.1] (symmetric)"
		));
		
//      URI="http://www.w3.org/2008/05/skos#broadMatch"
//      icon="icons/broadMatch28.png"
//      name="broadMatch"
//      tooltip="'has the broader concept': the second (object) concept is broader than the first (subject) concept [SKOS Section 8.1] (infers broaderTransitive, a transitive relation)"
		relInfos.add(new RelationInfo(
				"http://www.w3.org/2008/05/skos#broadMatch", 
				"broadMatch28.png", 
				"broadMatch",
				"'has the broader concept': the second (object) concept is broader than the first (subject) concept [SKOS Section 8.1] (infers broaderTransitive, a transitive relation)"
		));

//      URI="http://www.w3.org/2008/05/skos#narrowMatch"
//      icon="icons/narrowMatch28.png"
//      name="narrowMatch"
//      tooltip="'has the narrower concept': the second (object) concept is narrower than the first (subject) concept [SKOS Section 8.1] (infers narrowTransitive, a transitive relation)"
		relInfos.add(new RelationInfo(
				"http://www.w3.org/2008/05/skos#narrowMatch", 
				"narrowMatch28.png", 
				"narrowMatch",
				"'has the narrower concept': the second (object) concept is narrower than the first (subject) concept [SKOS Section 8.1] (infers narrowTransitive, a transitive relation)"
		));

//      URI="http://www.w3.org/2008/05/skos#relatedMatch"
//      icon="icons/relatedMatch28.png"
//      name="relatedMatch"
//      tooltip="The property skos:relatedMatch is used to state an associative mapping link between two concepts. [SKOS Section 8.1] (symmetric)"
		relInfos.add(new RelationInfo(
				"http://www.w3.org/2008/05/skos#relatedMatch", 
				"relatedMatch28.png", 
				"relatedMatch",
				"The property skos:relatedMatch is used to state an associative mapping link between two concepts. [SKOS Section 8.1] (symmetric)"
		));

		if ( log.isDebugEnabled() ) {
			log.debug("getRelationInfos returning: " +relInfos);
		}

		return relInfos;
	}
}
