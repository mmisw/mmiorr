package org.mmisw.ontmd.gwt.client.rpc;

import java.util.List;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.iserver.gwt.client.rpc.BasicOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.EntityInfo;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.MetadataBaseInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.UploadOntologyResult;
import org.mmisw.iserver.gwt.client.vocabulary.AttrDef;
import org.mmisw.ontmd.gwt.client.voc2rdf.rpc.ConversionResult;
import org.mmisw.ontmd.gwt.client.voc2rdf.rpc.Voc2RdfBaseInfo;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Interface to interact with the server.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface OntMdService extends RemoteService {

	/**
	 * Gets basic application info.
	 */
	AppInfo getAppInfo();
	
	/**
	 * Gets the base information (metadata attribute definitions).
	 */
	MetadataBaseInfo getBaseInfo(Map<String, String> params);
	
	/**
	 * Refreshes the options of the given attribute.
	 * @return the given argument.
	 */
	AttrDef refreshOptions(AttrDef attrDef);
	
	/**
	 * Authenticates a user.
	 */
	LoginResult login(String userName, String userPassword);
	
	
	/**
	 * Gets ontology info from an ontology that can be resolved by the Ont service
	 * (ie., from the MMI registry).
	 */
	OntologyInfoPre getOntologyInfoFromRegistry(String ontologyUri);

	/**
	 * Gets ontology info from a pre-loaded file.
	 */
	OntologyInfoPre getOntologyInfoFromPreLoaded(String uploadResults);

	/**
	 * Gets ontology info from a file located in the server.
	 * The main "client" for this service is Voc2Rdf
	 */
	OntologyInfoPre getOntologyInfoFromFileOnServer(String fullPath);

	/**
	 * Reviews the pre-loaded model with the associated new values.
	 */
	ReviewResult_Old review(OntologyInfoPre ontologyInfoPre, LoginResult loginResult_Old);

	/**
	 * Uploads a reviewed model to the MMI Registry.
	 */
	UploadResult upload(ReviewResult_Old reviewResult_Old, LoginResult loginResult_Old);
	
	
	///////////////////////////////////////////////////////////////////////
	// Voc2RDF
	
	/**
	 * Gets basic application info.
	 */
	AppInfo getVoc2RdfAppInfo();
	
	Voc2RdfBaseInfo getVoc2RdfBaseInfo();
	
	ConversionResult convert2Rdf(Map<String,String> values);
	
	
	///////////////////////////////////////////////////////////////////////
	// data
	
	/**
	 * Reviews the pre-loaded model with the associated new values.
	 */
	@Deprecated
	DataResult getData(OntologyInfoPre ontologyInfoPre);

	
	/**
	 * Gets the list of entities associated with the given ontology. 
	 * @param ontologyUri URI of the desired ontology.
	 * @return list of entities
	 */
	public List<EntityInfo> getEntities(String ontologyUri);
	
	
	///////////////////////////////////////////////////////////////////////
	// Portal
	
	public AppInfo getPortalAppInfo();
	
	public PortalBaseInfo getPortalBaseInfo();
	
	public MetadataBaseInfo getMetadataBaseInfo(boolean includeVersion);

	public List<OntologyInfo> getAllOntologies(boolean includePriorVersions);
	
	/**
	 * Gets ontology info from an ontology that can be resolved by the Ont service
	 * (ie., from the MMI registry).
	 */
	public OntologyInfo getOntologyInfo(String ontologyUri);

	
	public OntologyInfo getOntologyContents(OntologyInfo ontologyInfo);
	
	
	/**
	 * Creates an ontology.
	 */
	public CreateOntologyResult createOntology(
			BasicOntologyInfo basicOntologyInfo, CreateOntologyInfo createOntologyInfo) ;



	public UploadOntologyResult uploadOntology(CreateOntologyResult createOntologyResult, LoginResult loginResult);
	
	
}
