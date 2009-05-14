package org.mmisw.ontmd.gwt.client.rpc;

import java.util.List;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.ontmd.gwt.client.voc2rdf.rpc.ConversionResult;
import org.mmisw.ontmd.gwt.client.voc2rdf.rpc.Voc2RdfBaseInfo;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrDef;

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
	
	void getBaseInfo(Map<String, String> params, AsyncCallback<BaseInfo> callback);
	
	void login(String userName, String userPassword, AsyncCallback<LoginResult> callback);
	
	void getOntologyInfoFromRegistry(String ontologyUri, AsyncCallback<OntologyInfo> callback);
	
	void getOntologyInfoFromPreLoaded(String uploadResults, AsyncCallback<OntologyInfo> callback);
	
	void getOntologyInfoFromFileOnServer(String fullPath, AsyncCallback<OntologyInfo> callback);
	
	void review(OntologyInfo ontologyInfo, LoginResult loginResult, AsyncCallback<ReviewResult> callback);
	
	void upload(ReviewResult reviewResult, LoginResult loginResult, AsyncCallback<UploadResult> callback);
	
	
	///////////////////////////////////////////////////////////////////////
	// Voc2RDF
	
	void getVoc2RdfAppInfo(AsyncCallback<AppInfo> callback);
	
	void getVoc2RdfBaseInfo(AsyncCallback<Voc2RdfBaseInfo> callback);
	
	void convert2Rdf(Map<String,String> values, AsyncCallback<ConversionResult> callback);

	
	///////////////////////////////////////////////////////////////////////
	// data

	void getData(OntologyInfo ontologyInfo, AsyncCallback<DataResult> callback);


	///////////////////////////////////////////////////////////////////////
	// Portal
	
	void getPortalAppInfo(AsyncCallback<AppInfo> callback);
	
	void getPortalBaseInfo(AsyncCallback<PortalBaseInfo> callback);
	
	void getAllOntologies(AsyncCallback <List<org.mmisw.iserver.gwt.client.rpc.OntologyInfo>> callback);
}
