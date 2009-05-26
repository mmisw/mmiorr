package org.mmisw.iserver.core;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.iserver.gwt.client.rpc.EntityInfo;
import org.mmisw.iserver.gwt.client.rpc.MetadataBaseInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.iserver.gwt.client.vocabulary.AttrGroup;
import org.mmisw.ont.MmiUri;
import org.mmisw.ont.vocabulary.Omv;

import com.hp.hpl.jena.ontology.OntModel;



/**
 * Implementation of IServerService. 
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class Server implements IServer {
	private static final long serialVersionUID = 1L;
	
	private static final String ONT = "http://mmisw.org/ont";
	private static final String LISTALL = ONT + "?listall";

	
	private final AppInfo appInfo = new AppInfo("MMISW IServer");
	private final Log log = LogFactory.getLog(Server.class);
	
	
	private static IServer _instance ;
	
	public static IServer getInstance() {
		if ( _instance == null ) {
			_instance = new Server();
		}
		
		return _instance;
	}
	
	private Server() {
		log.info("initializing " +appInfo.getAppName()+ "...");
		try {
			Config.getInstance();
			
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
		log.info(appInfo+ ": destroy called.\n\n");
	}
	
	public AppInfo getAppInfo() {
		return appInfo;
	}
	

	private MetadataBaseInfo metadataBaseInfo = null;
	
	public MetadataBaseInfo getMetadataBaseInfo(
			boolean includeVersion, String resourceTypeClassUri,
			String authorityClassUri) {
		
		log.info("preparing base info ...");
		
		if ( metadataBaseInfo == null ) {
			metadataBaseInfo = new MetadataBaseInfo();
			
			metadataBaseInfo.setResourceTypeUri(Omv.acronym.getURI());
			
			MdHelper.prepareGroups(includeVersion, resourceTypeClassUri, authorityClassUri);
			AttrGroup[] attrGroups = MdHelper.getAttrGroups();
			metadataBaseInfo.setAttrGroups(attrGroups);
			log.info("preparing base info ... DONE");

		}
		
		return metadataBaseInfo;
	}

	public List<EntityInfo> getEntities(String ontologyUri) {
		if ( log.isDebugEnabled() ) {
			log.debug("getEntities(String) starting");
		}
		return  Util.getEntities(ontologyUri, null);
	}


	
	/**
	 * 
	 */
	public List<OntologyInfo> getAllOntologies(boolean includePriorVersions) throws Exception {
		
		//
		// strategy
		// - for each unversioned URI, collect all related, versioned ontologies 
		// - then populate list to be returned with the latest version of each group
		// - if includePriorVersions is true, add the prior versions to each main entry
		//
		
		// unversionedUri -> list of versioned URIs
		Map<String, List<OntologyInfo>> unversionedToVersioned = new LinkedHashMap<String, List<OntologyInfo>>();
		
		String uri = LISTALL;
		
		if ( log.isDebugEnabled() ) {
			log.debug("getAsString. uri= " +uri);
		}

		String response = getAsString(uri, Integer.MAX_VALUE);
		
		String[] lines = response.split("\n|\r\n|\r");
		for ( String line : lines ) {
			// remove leading and trailing quote:
			line = line.replaceAll("^'|'$", "");
			String[] toks = line.trim().split("'\\|'");
			OntologyInfo ontologyInfo = new OntologyInfo();
			String ontologyUri = toks[0];
			
			ontologyInfo.setUri(ontologyUri);
			ontologyInfo.setDisplayLabel(toks[1]);

			ontologyInfo.setType(toks[2]);
			
			ontologyInfo.setUserId(toks[3]);
			ontologyInfo.setContactName(toks[4]);
			ontologyInfo.setVersionNumber(toks[5]);
			ontologyInfo.setDateCreated(toks[6]);
			ontologyInfo.setUsername(toks[7]);
			
			String unversionedUri;
			
			try {
				MmiUri mmiUri = new MmiUri(ontologyUri);
				ontologyInfo.setAuthority(mmiUri.getAuthority());
				
				unversionedUri = mmiUri.copyWithVersion(null).getOntologyUri();
				
			}
			catch (URISyntaxException e) {
				// shouldn't happen.
				log.error("error creating MmiUri from: " +ontologyUri, e);
				continue;
			}

			
			List<OntologyInfo> versionedList = unversionedToVersioned.get(unversionedUri);
			if ( versionedList == null ) {
				versionedList = new ArrayList<OntologyInfo>();
				unversionedToVersioned.put(unversionedUri, versionedList);
			}
			versionedList.add(ontologyInfo);
			
			
		}
		
		List<OntologyInfo> onts = new ArrayList<OntologyInfo>();
		
		for ( String unversionedUri : unversionedToVersioned.keySet() ) {

			List<OntologyInfo> versionedList = unversionedToVersioned.get(unversionedUri);
			
			// sort in descending versionNumber
			Collections.sort(versionedList, new Comparator<OntologyInfo>() {
				public int compare(OntologyInfo arg0, OntologyInfo arg1) {
					return - arg0.getVersionNumber().compareTo(arg1.getVersionNumber());
				}
			});
			
			// extract first element, ie., most recent ontology version, which will be added
			// to the returned list:
			OntologyInfo ontologyInfo = versionedList.remove(0);
			
			// assign UNversioned URI just to this main entry:
			ontologyInfo.setUnversionedUri(unversionedUri);

			// if requested, include prior versions in the priorVersions property 
			if ( includePriorVersions ) {
				ontologyInfo.getPriorVersions().addAll(versionedList);
			}
			
			// add it to returned list:
			onts.add(ontologyInfo);
		}
		
		return onts;
	}
	
	public OntologyInfo getEntities(OntologyInfo ontologyInfo) {
		if ( log.isDebugEnabled() ) {
			log.debug("getEntities(OntologyInfo) starting");
		}
		Util.getEntities(ontologyInfo, null);
		return ontologyInfo;
	}


	private static String getAsString(String uri, int maxlen) throws Exception {
		HttpClient client = new HttpClient();
	    GetMethod meth = new GetMethod(uri);
	    try {
	        client.executeMethod(meth);

	        if (meth.getStatusCode() == HttpStatus.SC_OK) {
	            return meth.getResponseBodyAsString(maxlen);
	        }
	        else {
	          throw new Exception("Unexpected failure: " + meth.getStatusLine().toString());
	        }
	    }
	    finally {
	        meth.releaseConnection();
	    }
	}
	
	
	
	
	public OntologyInfo getOntologyContents(OntologyInfo ontologyInfo) {
		
		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyContents(OntologyInfo): loading model");
		}
		
		OntModel ontModel = Util.loadModel(ontologyInfo.getUri());

		// Metadata:
		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyContents(OntologyInfo): getting metadata");
		}
		MetadataExtractor.prepareOntologyMetadata(metadataBaseInfo, ontModel, ontologyInfo);

		
		// Data
		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyContents(OntologyInfo): getting entities");
		}
		Util.getEntities(ontologyInfo, ontModel);
		
		
		return ontologyInfo;
	
	}

	

}
