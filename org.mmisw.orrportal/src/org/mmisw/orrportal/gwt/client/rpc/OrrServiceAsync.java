package org.mmisw.orrportal.gwt.client.rpc;

import java.util.List;
import java.util.Map;

import org.mmisw.orrclient.gwt.client.rpc.AppInfo;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.CreateUpdateUserAccountResult;
import org.mmisw.orrclient.gwt.client.rpc.InternalOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.LoginResult;
import org.mmisw.orrclient.gwt.client.rpc.MetadataBaseInfo;
import org.mmisw.orrclient.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.ResetPasswordResult;
import org.mmisw.orrclient.gwt.client.rpc.ResolveUriResult;
import org.mmisw.orrclient.gwt.client.rpc.SparqlQueryInfo;
import org.mmisw.orrclient.gwt.client.rpc.SparqlQueryResult;
import org.mmisw.orrclient.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.UnregisterOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.UserInfoResult;
import org.mmisw.orrclient.gwt.client.rpc.vine.RelationInfo;
import org.mmisw.orrclient.gwt.client.vocabulary.AttrDef;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async interface.
 * 
 * See javadoc in {@link OrrService}.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface OrrServiceAsync {

	void getAppInfo(AsyncCallback<AppInfo> callback);
	
	void getPortalBaseInfo(AsyncCallback<PortalBaseInfo> callback);
	
	void getMetadataBaseInfo(boolean includeVersion, AsyncCallback<MetadataBaseInfo> callback);

	void getAllOntologies(boolean includePriorVersions, AsyncCallback <List<RegisteredOntologyInfo>> callback);

	void refreshOptions(AttrDef attrDef, AsyncCallback<AttrDef> callback);

	void resolveUri(String uri, AsyncCallback<ResolveUriResult> callback);
	
	
	void getOntologyContents(RegisteredOntologyInfo ontologyInfo, String version, AsyncCallback<RegisteredOntologyInfo> callback);
	
	
	void createOntology(CreateOntologyInfo createOntologyInfo, AsyncCallback<CreateOntologyResult> callback);
	
	
	void registerOntology(CreateOntologyResult createOntologyResult, LoginResult loginResult,
			AsyncCallback<RegisterOntologyResult> callback) ;
	
	
	void getTempOntologyInfo(String fileType, String uploadResults, boolean includeContents,
			boolean includeRdf, AsyncCallback<TempOntologyInfo> callback);
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// VINE:
	
	void getDefaultVineRelationInfos(AsyncCallback<List<RelationInfo>> callback);
	
	
	// Search:
	
	void runSparqlQuery(SparqlQueryInfo query, AsyncCallback<SparqlQueryResult> callback);

	
	// login
	
	void authenticateUser(String userName, String userPassword, AsyncCallback<LoginResult> callback);
	void resetUserPassword(String username, AsyncCallback<ResetPasswordResult> callback);
	void getUserInfo(String username, AsyncCallback<UserInfoResult> callback);
	void createUpdateUserAccount(Map<String,String> values, AsyncCallback<CreateUpdateUserAccountResult> callback);
	
	
	// Admin:
	void prepareUsersOntology(LoginResult loginResult, AsyncCallback<InternalOntologyResult> callback);
	void createGroupsOntology(LoginResult loginResult, AsyncCallback<InternalOntologyResult> callback);
	
	void unregisterOntology(LoginResult loginResult, RegisteredOntologyInfo oi, AsyncCallback<UnregisterOntologyResult> callback);
	

	
	//////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////

	//
	// TODO REMOVE THESE OLD OPERATIONS
	//
	
	void getOntologyInfoFromRegistry(String ontologyUri, AsyncCallback<OntologyInfoPre> callback);
	
	void getOntologyInfoFromPreLoaded(String uploadResults, AsyncCallback<OntologyInfoPre> callback);
	
	void getOntologyInfoFromFileOnServer(String fullPath, AsyncCallback<OntologyInfoPre> callback);
	
	void review(OntologyInfoPre ontologyInfoPre, LoginResult loginResult_Old, AsyncCallback<ReviewResult_Old> callback);
	
	void upload(ReviewResult_Old reviewResult_Old, LoginResult loginResult_Old, AsyncCallback<UploadResult> callback);
	
	
	// TODO remove
	void getBaseInfo(Map<String, String> params, AsyncCallback<MetadataBaseInfo> callback);
	
}
