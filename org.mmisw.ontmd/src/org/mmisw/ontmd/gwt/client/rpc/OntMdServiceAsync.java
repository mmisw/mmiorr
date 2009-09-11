package org.mmisw.ontmd.gwt.client.rpc;

import java.util.List;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.EntityInfo;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.MetadataBaseInfo;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.ResetPasswordResult;
import org.mmisw.iserver.gwt.client.rpc.SparqlQueryInfo;
import org.mmisw.iserver.gwt.client.rpc.SparqlQueryResult;
import org.mmisw.iserver.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.vine.RelationInfo;
import org.mmisw.iserver.gwt.client.vocabulary.AttrDef;
import org.mmisw.ontmd.gwt.client.voc2rdf.rpc.ConversionResult;
import org.mmisw.ontmd.gwt.client.voc2rdf.rpc.Voc2RdfBaseInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async interface for Voc2RdfService
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface OntMdServiceAsync {

	void getAppInfo(AsyncCallback<AppInfo> callback);
	
	void refreshOptions(AttrDef attrDef, AsyncCallback<AttrDef> callback);
	
	void getBaseInfo(Map<String, String> params, AsyncCallback<MetadataBaseInfo> callback);
	
	void getOntologyInfoFromRegistry(String ontologyUri, AsyncCallback<OntologyInfoPre> callback);
	
	void getOntologyInfoFromPreLoaded(String uploadResults, AsyncCallback<OntologyInfoPre> callback);
	
	void getOntologyInfoFromFileOnServer(String fullPath, AsyncCallback<OntologyInfoPre> callback);
	
	void review(OntologyInfoPre ontologyInfoPre, LoginResult loginResult_Old, AsyncCallback<ReviewResult_Old> callback);
	
	void upload(ReviewResult_Old reviewResult_Old, LoginResult loginResult_Old, AsyncCallback<UploadResult> callback);
	
	
	///////////////////////////////////////////////////////////////////////
	// Voc2RDF
	
	void getVoc2RdfAppInfo(AsyncCallback<AppInfo> callback);
	
	void getVoc2RdfBaseInfo(AsyncCallback<Voc2RdfBaseInfo> callback);
	
	void convert2Rdf(Map<String,String> values, AsyncCallback<ConversionResult> callback);

	
	///////////////////////////////////////////////////////////////////////
	// data

	@Deprecated
	void getData(OntologyInfoPre ontologyInfoPre, AsyncCallback<DataResult> callback);

	void getEntities(String ontologyUri, AsyncCallback<List<EntityInfo>> callback);
	

	///////////////////////////////////////////////////////////////////////
	// Portal
	
	void getPortalAppInfo(AsyncCallback<AppInfo> callback);
	
	void getPortalBaseInfo(AsyncCallback<PortalBaseInfo> callback);
	
	void getMetadataBaseInfo(boolean includeVersion, AsyncCallback<MetadataBaseInfo> callback);

	void getAllOntologies(boolean includePriorVersions, AsyncCallback <List<RegisteredOntologyInfo>> callback);

	void getOntologyInfo(String ontologyUri, AsyncCallback<RegisteredOntologyInfo> callback);
	
	void getOntologyContents(RegisteredOntologyInfo ontologyInfo, String version, AsyncCallback<RegisteredOntologyInfo> callback);
	
	
	void createOntology(CreateOntologyInfo createOntologyInfo, AsyncCallback<CreateOntologyResult> callback);
	
	
	void registerOntology(CreateOntologyResult createOntologyResult, LoginResult loginResult,
			AsyncCallback<RegisterOntologyResult> callback) ;
	
	
	void getTempOntologyInfo(String uploadResults, boolean includeContents,
			boolean includeRdf, AsyncCallback<TempOntologyInfo> callback);
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// VINE:
	
	void getVineRelationInfos(AsyncCallback<List<RelationInfo>> callback);
	
	// :VINE
	////////////////////////////////////////////////////////////////////////////////////////////

	
	// Search:
	
	void runSparqlQuery(SparqlQueryInfo query, AsyncCallback<SparqlQueryResult> callback);

	
	// login
	
	void authenticateUser(String userName, String userPassword, AsyncCallback<LoginResult> callback);
	void resetUserPassword(String username, AsyncCallback<ResetPasswordResult> callback);
}
