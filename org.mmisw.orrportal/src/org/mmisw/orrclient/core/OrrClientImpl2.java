package org.mmisw.orrclient.core;

/*

  TODO overall review/comparison with the other impl
  TODO replace occurrences of OntServiceUtil
  TODO send email to notify successful registration

*/



import com.google.gson.Gson;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.client.SignInResult;
import org.mmisw.ont.client.util.HttpUtil;
import org.mmisw.ont.mmiuri.MmiUri;
import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;
import org.mmisw.orrclient.IOrrClient;
import org.mmisw.orrclient.core.ontmodel.OntModelUtil;
import org.mmisw.orrclient.core.util.OntServiceUtil;
import org.mmisw.orrclient.core.util.Util2;
import org.mmisw.orrclient.core.util.ontinfo.OntInfoUtil;
import org.mmisw.orrclient.gwt.client.rpc.*;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Implementation against orr-ont.
 */
public class OrrClientImpl2 extends OrrClientImplBase {

  private static final Log log = LogFactory.getLog(OrrClientImpl2.class);

  private static IOrrClient _instance;

  public static IOrrClient init() throws Exception {
    _instance = new OrrClientImpl2();
    return _instance;
  }

  public static IOrrClient getInstance() {
    return _instance;
  }

  ////////////////////////////////////////////////////////////////////////////

  private OrrClientImpl2() throws Exception {
    super();
    cfg = readConfig();
    log.info("OrrClientImpl2 created");
  }

  protected List<RegisteredOntologyInfo> _doGetAllOntologies(boolean includeAllVersions) throws Exception {
    log.debug("_doGetAllOntologies: includeAllVersions=" +includeAllVersions);

    List<RegisteredOntologyInfo> onts = new ArrayList<>();

    // TODO
    String url = config.ontServiceUrl + "/api/v0/ont";
    log.info("GET all onts url=" +url);

    GetMethod method = new GetMethod(url);
    HttpClient client = createHttpClient();
    includeAutorization(method);

    log.info("Executing " + method.getName());

    try {
      int status = client.executeMethod(method);
      String msg = method.getResponseBodyAsString();

      if (status == HttpStatus.SC_OK) {
        List<Object> list = gson.fromJson(msg, List.class);
        for (Object obj: list) {
          System.out.println("---- " + obj);
          if (obj instanceof Map) {
            Map<String, String> map = (Map<String, String>) obj;
            RegisteredOntologyInfo registeredOntologyInfo = _createRegisteredOntologyInfo(map, includeAllVersions);
            onts.add(registeredOntologyInfo);
          }
        }
      }
      else {
        throw new Exception(HttpStatus.getStatusText(status));
      }
    }
    finally {
      method.releaseConnection();
    }

    return onts;
  }

  private RegisteredOntologyInfo _createRegisteredOntologyInfo(Map<String, ?> map, boolean includeAllVersions) {
    String uri  = getOrEmpty(map, "uri");
    String name = getOrEmpty(map, "name");
    String ontologyTypeName = getOrElse(map, "ontologyType", OntologyType.OTHER.name());
    OntologyType ontologyType = OntologyType.valueOf(ontologyTypeName.toUpperCase());
    String userId = "TODO_USER_ID";

    String contactName = getOrEmpty(map, "author");
    String versionNumber = getOrEmpty(map, "version");
    String dateCreated = "TODO_dateCreated";
    String userName = getOrEmpty(map, "submitter");
    String ontologyId = "TODO_ontologyId";
    String versionStatus = getOrEmpty(map, "status");
    String unversionedUri = uri;
    String authority = getOrEmpty(map, "orgName");
    String shortName = null;   //  TODO shortName

    RegisteredOntologyInfo registeredOntologyInfo = _createOntologyInfo(
        uri,
        name,
        ontologyType,
        userId,
        contactName,
        versionNumber,
        dateCreated,
        userName,
        ontologyId,
        versionStatus,
        unversionedUri,
        authority,
        shortName
    );

    if ( includeAllVersions ) {
      Object versionsObj = map.get("versions");
      log.info("_createRegisteredOntologyInfo, map.versions=" + versionsObj);
      if (versionsObj instanceof List<?>) {
        // TODO get details for each of the versions. At this point we are using the
        // same details as the latest version, only with the version piece updated ...
        List<?> versions = (List<?>) versionsObj;
        ArrayList<RegisteredOntologyInfo> versionedList = new ArrayList<>();
        for (Object versionObj: versions) {
          String version = String.valueOf(versionObj);
          RegisteredOntologyInfo roi2 = _createOntologyInfo(
              uri,
              name,
              ontologyType,
              userId,
              contactName,
              version,          // ... only this piece updated
              dateCreated,
              userName,
              ontologyId,
              versionStatus,
              unversionedUri,
              authority,
              shortName
          );
          versionedList.add(roi2);
        }
        registeredOntologyInfo.getPriorVersions().addAll(versionedList);
      }
    }
    return registeredOntologyInfo;
  }

  private static String getOrEmpty(Map<String, ?> map, String key) {
    return getOrElse(map, key, "");
  }

  private static String getOrElse(Map<String, ?> map, String key, String default_) {
    Object val = map.get(key);
    return val != null ? String.valueOf(val) : default_;
  }

  @Override
  public LoginResult authenticateUser(String userName, String userPassword) {
    log.info("authenticating username=" +userName+ " password=*");
    LoginResult loginResult = new LoginResult();
    String authRestUrl = config.ontServiceUrl + "/api/v0/user/auth";
    log.info("authentication REST URL =" +authRestUrl);

    PostMethod method = new PostMethod(authRestUrl);
    String json = String.format("{\"userName\": \"%s\",\"password\": \"%s\"}",
        userName, userPassword);

    try {
      StringRequestEntity requestEntity = new StringRequestEntity(
          json,
          "application/json",
          "UTF-8");

      method.setRequestEntity(requestEntity);

      HttpClient client = createHttpClient();

      log.info("Executing " + method.getName());

      int status = client.executeMethod(method);

      String msg = method.getResponseBodyAsString();

      if (status == HttpStatus.SC_OK) {
        log.info("Authentication complete, response=[" + msg + "]");
        Map<String, String> map = gson.fromJson(msg, Map.class);
        log.info("Authentication complete, map=" + map);

        loginResult.setUserName(userName);
        loginResult.setSessionId("SESSION_ID_TODO");
        loginResult.setUserId("USER_ID_TODO");

        if (map.containsKey("role")) {
          String role = map.get("role");
          if (role.equals("extra")) {
            loginResult.setUserRole("ROLE_ADMINISTRATOR");
          }
        }

      }
      else {
        String statusText = HttpStatus.getStatusText(status);
        log.info("Authentication failed, status text=" + statusText);
        log.info("Authentication failed, response=" + msg);
        if ( msg == null ) {
          msg = statusText;
        }
        loginResult.setError(msg);
      }
    }
    catch (Exception ex) {
      loginResult.setError(ex.getMessage());
    }
    finally {
      method.releaseConnection();
    }

    return loginResult;
  }

  protected Map<String,String> getUserInfoMap(String username) throws Exception {

    log.info("getUserInfoMap username=" +username);
    String url = config.ontServiceUrl + "/api/v0/user/" + username;
    log.info("GET user info url=" +url);

    GetMethod method = new GetMethod(url);
    HttpClient client = createHttpClient();
    includeAutorization(method);

    log.info("Executing " + method.getName());

    try {
      int status = client.executeMethod(method);

      String msg = method.getResponseBodyAsString();

      if (status == HttpStatus.SC_OK) {
        Map<String, String> map = (Map<String, String>) gson.fromJson(msg, Map.class);
        Map<String, String> props = new HashMap<>();
        for (Map.Entry<String, String> e : map.entrySet()) {
          props.put(e.getKey().toLowerCase(), e.getValue());
        }
        return props;
      }
      else {
        throw new Exception(HttpStatus.getStatusText(status));
      }
    }
    finally {
      method.releaseConnection();
    }
  }

  private HttpClient createHttpClient() {
    HttpClient client = new HttpClient();
    client.getHttpConnectionManager().getParams().setConnectionTimeout(10*1000);
    return client;
  }

  private void includeAutorization(HttpMethodBase method) {
    String username = cfg.getString("todo.username");  // FIXME
    String password = cfg.getString("todo.password");  // FIXME
    log.debug("includeAutorization: username=" + username + " password=" + password);
    try {
      String encoded = DatatypeConverter.printBase64Binary((username + ":" + password).getBytes("UTF-8"));
      method.addRequestHeader(new Header("Authorization", "Basic " + encoded));
    }
    catch (UnsupportedEncodingException ex) {
      log.warn("should not happen", ex);
    }
  }

  @Override
  public RegisteredOntologyInfo getOntologyInfo(String ontologyUri) {

    log.info("getOntologyInfo ontologyUri=" +ontologyUri);

    String[] toks = ontologyUri.split("\\?");
    ontologyUri = toks[0];

    String version = null;
    if ( toks.length > 1 && toks[1].startsWith("version=") ) {
      version = toks[1].substring("version=".length());
    }

    log.debug("getOntologyInfo: ontologyUri=" +ontologyUri+ "  version=" +version);

    String url = config.ontServiceUrl + "/api/v0/ont";

    GetMethod method = new GetMethod(url);
    HttpClient client = createHttpClient();
    includeAutorization(method);

    List<NameValuePair> params = new ArrayList<>();
    params.add(new NameValuePair("uri", ontologyUri));

    if (version != null) {
      params.add(new NameValuePair("version", version));
    }
    params.add(new NameValuePair("format", "!md"));

    NameValuePair[] data = params.toArray(new NameValuePair[params.size()]);
    method.setQueryString(data);
    if (log.isInfoEnabled()) {
      log.info(debugMethod(method, params));
    }

    RegisteredOntologyInfo roi = new RegisteredOntologyInfo();

    try {
      int status = client.executeMethod(method);

      if (status == HttpStatus.SC_OK) {
        String msg = method.getResponseBodyAsString();

        Map<String, Object> map = gson.fromJson(msg, Map.class);
        log.info("getOntologyInfo complete, map=" + map);
        roi = _createRegisteredOntologyInfo(map, true);
      }
      else {
        String error = HttpStatus.getStatusText(status);
        log.error("getOntologyInfo: Error in URI: " +ontologyUri + " error=" + error);
        roi = new RegisteredOntologyInfo();
        roi.setError(error);
      }
    }
    catch (Exception ex) {
      log.error("resolveUri: exception ontologyUri: " +ontologyUri, ex);
      roi = new RegisteredOntologyInfo();
      roi.setError("resolveUri: exception ontologyUri: " +ontologyUri + " error=" + ex.getMessage());
    }
    finally {
      method.releaseConnection();
    }

    return roi;
  }

  @Override
  public ResolveUriResult resolveUri(String uri) {
    log.debug("resolveUri uri=" + uri);
    ResolveUriResult resolveUriResult = new ResolveUriResult(uri);

    // try ontology:
    RegisteredOntologyInfo roi = getOntologyInfo(uri);
    if ( roi != null && roi.getError() == null ) {
      resolveUriResult.setRegisteredOntologyInfo(roi);
      return resolveUriResult;
    }

    // TODO try term:
    //_getEntityInfo(uri, resolveUriResult);

    return resolveUriResult;
  }

  @Override
  public RegisteredOntologyInfo getOntologyMetadata(RegisteredOntologyInfo registeredOntologyInfo, String version) {
    if ( log.isDebugEnabled() ) {
      log.debug("getOntologyMetadata(RegisteredOntologyInfo): \n" +
          "version=" + version+ "\n" +
          " uri=" + registeredOntologyInfo.getUri() + "\n" +
          "uuri=" + registeredOntologyInfo.getUnversionedUri() + "\n" +
          "roi.version=" + registeredOntologyInfo.getVersionNumber()
      );
    }

    if (version == null) {
      version = registeredOntologyInfo.getVersionNumber();
    }

    OntModel ontModel;
    try {
      ontModel = retrieveModel(registeredOntologyInfo.getUri(), version);
    }
    catch (Exception e) {
      String error = "Error loading model: " +e.getMessage();
      log.error(error, e);
      registeredOntologyInfo.setError(error);
      return registeredOntologyInfo;
    }

    // Metadata:
    if ( log.isDebugEnabled() ) {
      log.debug("getOntologyMetadata(RegisteredOntologyInfo): getting metadata");
    }
    MetadataExtractor.prepareOntologyMetadata(metadataBaseInfo, ontModel, registeredOntologyInfo);

    registeredOntologyInfo.setSize(ontModel.size());
    if ( log.isDebugEnabled() ) {
      log.debug("getOntologyMetadata(RegisteredOntologyInfo): ontology size=" + registeredOntologyInfo.getSize());
    }

    return registeredOntologyInfo;
  }

  @Override
  public RegisteredOntologyInfo getOntologyContents(RegisteredOntologyInfo registeredOntologyInfo, String version) {
    if ( log.isDebugEnabled() ) {
      log.debug("getOntologyContents, version=" + version);
    }

    if (version == null) {
      version = registeredOntologyInfo.getVersionNumber();
    }

    OntModel ontModel;
    try {
      ontModel = retrieveModel(registeredOntologyInfo.getUri(), version);
    }
    catch (Exception e) {
      String error = "Error loading model: " +e.getMessage();
      log.error(error, e);
      registeredOntologyInfo.setError(error);
      return registeredOntologyInfo;
    }

    // Metadata:
    // NOTE: the metadata are retrieved here regardless of whether have been already retrieved.
    if ( log.isDebugEnabled() ) {
      log.debug("getOntologyContents(RegisteredOntologyInfo): getting metadata");
    }
    MetadataExtractor.prepareOntologyMetadata(metadataBaseInfo, ontModel, registeredOntologyInfo);


    // Data
    if ( log.isDebugEnabled() ) {
      log.debug("getOntologyContents(RegisteredOntologyInfo): getting entities");
    }

    try {
      OntInfoUtil.getEntities(registeredOntologyInfo, ontModel);
    }
    catch (Exception e) {
      String error = "Error getting entities: " +e.getMessage();
      log.error(error, e);
      registeredOntologyInfo.setError(error);
      return registeredOntologyInfo;
    }

    return registeredOntologyInfo;
  }

  @Override
  public SparqlQueryResult runSparqlQuery(SparqlQueryInfo queryInfo) {

    log.debug("runSparqlQuery: query=" +queryInfo);


    SparqlQueryResult sparqlQueryResult = new SparqlQueryResult();

    final String query = queryInfo.getQuery();
    final String format = queryInfo.getFormat();
    final String[] acceptEntries = queryInfo.getAcceptEntries();

    try {
      String url = cfg.getString("agraph.sparql");
      GetMethod method = new GetMethod(url);

      List<NameValuePair> params = new ArrayList<>();
      params.add(new NameValuePair("query", query));
      params.add(new NameValuePair("infer", "false")); // Note: with infer=false.

      if (acceptEntries != null && acceptEntries.length > 0) {
        for ( String acceptEntry : acceptEntries ) {
          method.addRequestHeader("accept", acceptEntry);
        }
      }
      else if (format.equals("html-frag")) {
        method.addRequestHeader("accept", "application/json");
      }

      HttpClient client = createHttpClient();

      NameValuePair[] data = params.toArray(new NameValuePair[params.size()]);
      method.setQueryString(data);
      if (log.isInfoEnabled()) {
        log.info(debugMethod(method, params));
      }

      try {
        int status = client.executeMethod(method);
        String msg = method.getResponseBodyAsString();

        if (status == HttpStatus.SC_OK) {

          if (format.equals("html-frag")) {
            msg = jsonResultToHtml(msg);
          }
          //log.debug("query result:\n" + msg);

          sparqlQueryResult.setResult(msg);
        }
        else {
          throw new Exception(HttpStatus.getStatusText(status));
        }
      }
      finally {
        method.releaseConnection();
      }
    }
    catch (Exception e) {
      String error = "Error while dispatching query: " +e.getMessage();
      sparqlQueryResult.setError(error);
    }
    return sparqlQueryResult;
  }

  /**
   * Some of this from Util.csv2html in Ont module
   */
  private String jsonResultToHtml(String msg) {
    StringBuilder html = new StringBuilder("<table class=\"inline2\">\n");

    Map<String,?> map = gson.fromJson(msg, Map.class);
    Object namesObj = map.get("names");
    Object valuesObj = map.get("values");

    List<String> names = new ArrayList<>();

    if (namesObj instanceof List<?>) {
      for (Object obj : (List<?>) namesObj) {
        names.add(String.valueOf(obj));
      }
    }
    log.debug("~~~names=" + names);

    html.append("<tr>");
    for(String name: names) {
      html.append("<th>").append(toHtml(name)).append("</th>");
    }
    html.append("</tr>");

    List<List<String>> values = new ArrayList<>();

    if (valuesObj instanceof List<?>) {
      for (Object obj : (List<?>) valuesObj) {
        if (obj instanceof List<?>) {
          List<String> vals = new ArrayList<>();
          for (Object obj2 : (List<?>) obj) {
            vals.add(String.valueOf(obj2));
          }
          values.add(vals);
        }
      }
    }
    log.debug("~~~values=" + values);

    for(List<String> vals: values) {
      html.append("<tr>");
      for(String col: vals) {
        while (col.startsWith("\"") && col.endsWith("\"")) {
          col = col.substring(1, col.length() -1);
        }
        if (col.startsWith("<") && col.endsWith(">")) {
          col = col.substring(1, col.length() -1);
        }

        String link = getLink(col);
        if ( link != null ) {
          col = String.format("<a target=\"_blank\" href=\"%s\">%s</a>", link, toHtml(col));
        }
        else {
          col = toHtml(col);
        }

        html.append("<td>").append(col).append("</td>");
      }
      html.append("</tr>");
    }

    html.append("</table>");
    return html.toString();
  }

  // TODO actually append .html in the following as in Ont module?
  /**
   * Returns a string that can be used as a link.
   * If it is an MmiUri, a ".html" is appended;
   * otherwise, if it is a valid URL, it is returned as it is;
   * otherwise, null is returned.
   *
   * @param value a potential URL
   * @return the string that can be used as a link as stated above; null if value is not a URL.
   */
  private static String getLink(String value) {
    // try mmiUri:
    try {
      MmiUri mmiUri = new MmiUri(value);
      return mmiUri.getTermUri() + ".html";
    }
    catch (URISyntaxException e1) {
      // ignore. Try URL below.
    }

    // try regular URL:
    try {
      URL url = new URL(value);
      return url.toString();
    }
    catch (MalformedURLException e) {
      // ignore.
    }

    return null;
  }

  private static String toHtml(String s) {
    return s.replaceAll("&", "&amp;").replaceAll("<", "&lt;");
  }

  @Override
  /*TODO*/public ExternalOntologyInfo getExternalOntologyInfo(String ontologyUri) {
    throw new UnsupportedOperationException();
    //return null;
  }

  @Override
  public CreateOntologyResult createOntology(CreateOntologyInfo createOntologyInfo) {
    log.info("createOntology: called. createOntologyInfo = " +createOntologyInfo);
    final HostingType hostingType = createOntologyInfo.getHostingType();

    if (hostingType == null) {
      // should not happen
      CreateOntologyResult createOntologyResult = new CreateOntologyResult();
      createOntologyResult.setError("defined hostingType expected!");
      return createOntologyResult;
    }

    return _createOntology_newMethod(createOntologyInfo);
  }

  private CreateOntologyResult _createOntology_newMethod(CreateOntologyInfo createOntologyInfo) {

    final HostingType hostingType = createOntologyInfo.getHostingType();

    CreateOntologyResult createOntologyResult = new CreateOntologyResult();

    if ( createOntologyInfo.getMetadataValues() == null ) {
      String error = "Unexpected: createOntologyInfo.getMetadataValues returned null. Please report this bug";
      createOntologyResult.setError(error);
      log.info(error);
      return createOntologyResult;
    }

    createOntologyResult.setCreateOntologyInfo(createOntologyInfo);

    switch ( hostingType ) {
      case FULLY_HOSTED:
        return _createOntologyFullyHosted(createOntologyInfo, createOntologyResult);
      case RE_HOSTED:
        return _createOntologyReHosted(createOntologyInfo, createOntologyResult);
      default: {
        String error = "Hosting type "+hostingType+ " NOT yet implemented.";
        createOntologyResult.setError(error);
        log.info(error);
        return createOntologyResult;
      }
    }
  }

  private CreateOntologyResult _createOntologyFullyHosted(CreateOntologyInfo createOntologyInfo, CreateOntologyResult createOntologyResult) {

    final String createOntUri = createOntologyInfo.getUri();
    log.debug("#356 _createOntologyFullyHosted; createOntologyInfo.getUri()=" + createOntUri);

    Map<String, String> newValues = createOntologyInfo.getMetadataValues();
    assert ( newValues != null ) ;

    DataCreationInfo dataCreationInfo = createOntologyInfo.getDataCreationInfo();
    assert ( dataCreationInfo instanceof OtherDataCreationInfo ) ;
    final OtherDataCreationInfo otherDataCreationInfo = (OtherDataCreationInfo) dataCreationInfo;
    final TempOntologyInfo tempOntologyInfo = otherDataCreationInfo.getTempOntologyInfo();


    // to check if this is going to be a new submission (if ontologyId == null) or, otherwise, a new version.
    final String ontologyId = createOntologyInfo.getPriorOntologyInfo().getOntologyId();


    final String namespaceRoot = defaultNamespaceRoot;
    final String orgAbbreviation = createOntologyInfo.getAuthority();
    final String shortName = createOntologyInfo.getShortName();


    if ( orgAbbreviation == null ) {
      // should not happen.
      String error = "missing authority abbreviation";
      log.info(error);
      createOntologyResult.setError(error);
      return createOntologyResult;
    }
    if ( shortName == null ) {
      // should not happen.
      String error = "missing short name";
      log.info(error);
      createOntologyResult.setError(error);
      return createOntologyResult;
    }

    if ( ontologyId == null ) {
      // This is a new submission. We need to check for any conflict with a preexisting
      // ontology in the repository with the same shortName+orgAbbreviation combination
      //
      if ( ! checkNoPreexistingOntology(namespaceRoot, orgAbbreviation, shortName, createOntologyResult) ) {
        return createOntologyResult;
      }
    }
    else {
      // This is a submission of a *new version* of an existing ontology.
      // We need to check the shortName+orgAbbreviation combination as any changes here
      // would imply a *new* ontology, not a new version.
      //

      BaseOntologyInfo baseOntologyInfo = createOntologyInfo.getBaseOntologyInfo();
      assert baseOntologyInfo instanceof RegisteredOntologyInfo;
      RegisteredOntologyInfo roi = (RegisteredOntologyInfo) baseOntologyInfo;

      String originalOrgAbbreviation = roi.getAuthority();
      String originalShortName = roi.getShortName();

      if ( ! Util2.checkUriKeyCombinationForNewVersion(
          originalOrgAbbreviation, originalShortName,
          orgAbbreviation, shortName, createOntologyResult) ) {
        return createOntologyResult;
      }
    }


    ////////////////////////////////////////////////////////////////////////////
    // section to create the ontology the base:

    // external ontology case: the base ontology is already available, just use it
    // by setting the full path in the createOntologyResult:

    String full_path;

    if ( tempOntologyInfo != null ) {
      // new contents were provided. Use that:
      full_path = tempOntologyInfo.getFullPath();
    }
    else {
      // No new contents. Only possible way for this to happen is that this is
      // a new version of an existing ontology.

      if ( ontologyId != null ) {
        // Just indicate a null full_path; see below.
        full_path = null;
      }
      else {
        // This shouldn't happen!
        String error = "Unexpected: Submission of new ontology, but not contents were provided. " +
            "This should be detected before submission. Please report this bug";
        createOntologyResult.setError(error);
        log.info(error);
        return createOntologyResult;
      }

    }
    createOntologyResult.setFullPath(full_path);


    // current date:
    final Date date = new Date(System.currentTimeMillis());

    ///////////////////////////////////////////////////////////////////
    // creation date:
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    final String creationDate = sdf.format(date);


    ///////////////////////////////////////////////////////////////////
    // version:
    // Note, if the version is given from the client, then use it
    String version = newValues.get(Omv.version.getURI());
    if ( version != null && version.trim().length() > 0 ) {
      // check that the given version is OK:
      boolean ok = VERSION_PATTERN.matcher(version).find();
      if ( ! ok ) {
        String error = "Given version is invalid: " +version;
        log.info(error);
        createOntologyResult.setError(error);
        return createOntologyResult;
      }
    }
    else {
      // otherwise: assign it here:
      sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
      version = sdf.format(date);
    }


    ////////////////////////////////////////////
    // load  model

    OntModel model;
    String uriForEmpty;
    String newContentsFileName;

    if ( createOntologyResult.getFullPath() != null ) {
      //
      // new contents to check.
      // Get model from the new contents.
      //
      full_path = createOntologyResult.getFullPath();
      log.info("Loading model: " +full_path);

      File file = new File(full_path);

      try {
        model = Util2.loadModelWithCheckingUtf8(file, null);
      }
      catch ( Throwable ex ) {
        String error = "Unexpected error: " +ex.getClass().getName()+ " : " +ex.getMessage();
        log.info(error);
        createOntologyResult.setError(error);
        return createOntologyResult;
      }

      // get original namespace associated with the ontology, if any:
      uriForEmpty = Util2.getDefaultNamespace2(model, file, createOntologyResult);
      // 2009-12-21: previously returning error if uriForEmpty==null. Not anymore; see below.

      newContentsFileName = file.getName();
    }
    else {
      // NO new contents.
      // Use contents from prior version.
      CreateOntologyInfo.PriorOntologyInfo priorVersionInfo = createOntologyInfo.getPriorOntologyInfo();

      try {
        model = OntServiceUtil.retrieveModel(createOntologyInfo.getUri(), priorVersionInfo.getVersionNumber());
      }
      catch (Exception e) {
        String error = "error while retrieving registered ontology: " +e.getMessage();
        log.info(error, e);
        createOntologyResult.setError(error);
        return createOntologyResult;
      }

      uriForEmpty = model.getNsPrefixURI("");
      if ( uriForEmpty == null ) {
        // Shouldn't happen -- we're reading in an already registered version.
        String error = "error while getting URI for empty prefix for a registered version. " +
            "Please report this bug.";
        log.info(error);
        createOntologyResult.setError(error);
        return createOntologyResult;
      }

      // replace ':', '/', or '\' for '_'
      newContentsFileName = uriForEmpty.replaceAll(":|/|\\\\", "_");
    }


    final String original_ns_ = uriForEmpty;

    String ns_;
    String base_;


    final String finalUri = namespaceRoot + "/" +
        orgAbbreviation + "/" +
        version + "/" +
        shortName;

    ns_ = JenaUtil2.appendFragment(finalUri);
    base_ = JenaUtil2.removeTrailingFragment(finalUri);


    log.info("Setting prefix \"\" for URI " + ns_);
    model.setNsPrefix("", ns_);


    if ( original_ns_ != null ) {
      // Update statements  according to the new namespace:
      log.info("createOntologyFullyHosted: original namespace: '" +uriForEmpty+ "'. " +
          "Elements here will be transferred to new namespace " +ns_
      );
      Util2.replaceNameSpace(model, original_ns_, ns_);
    }
    else {
      log.info("createOntologyFullyHosted: no original namespace, so no transfer will be done.");
    }




    /////////////////////////////////////////////////////////////////
    // Is there an existing OWL.Ontology individual?
    Resource ontRes = null;
    if ( original_ns_ != null ) {
      String originalOntologyUri = JenaUtil2.removeTrailingFragment(original_ns_);
      ontRes = model.getOntology(originalOntologyUri);
      log.debug("#356: createOntologyFullyHosted: model.getOntology(" +originalOntologyUri+ ") = " + ontRes);
    }
    //else {
    //  // #356: don't do this; arbitrarily some OWL.Ontology individual would be considered
    //	ontRes = JenaUtil2.getFirstIndividual(model, OWL.Ontology);
    //}

    List<Statement> prexistStatements = null;
    if ( ontRes != null ) {
      prexistStatements = new ArrayList<Statement>();
      log.info("Getting pre-existing properties for OWL.Ontology individual: " +ontRes.getURI());
      StmtIterator iter = ontRes.listProperties();
      while ( iter.hasNext() ) {
        Statement st = iter.nextStatement();
        prexistStatements.add(st);
      }
    }


    // The new OntModel that will contain the pre-existing attributes (if any),
    // plus the new and updated attributes:
    final OntModel newOntModel = _createOntModel(model);
    final Ontology ont_ = newOntModel.createOntology(base_);
    log.info("New ontology created with namespace " + ns_ + " base " + base_);
    newOntModel.setNsPrefix("", ns_);

    // set preferred prefixes:
    Map<String, String> preferredPrefixMap = MdHelper.getPreferredPrefixMap();
    for ( String uri : preferredPrefixMap.keySet() ) {
      String prefix = preferredPrefixMap.get(uri);
      newOntModel.setNsPrefix(prefix, uri);
    }


    // Set internal attributes, which are updated in the newValues map itself
    // so we facilite the processing below:
    newValues.put(Omv.version.getURI(), version);

    newValues.put(Omv.creationDate.getURI(), creationDate);


    // set some properties from the explicit values
    newValues.put(OmvMmi.origMaintainerCode.getURI(), orgAbbreviation);


    //////////////////////////////////////////////////
    // transfer any preexisting attributes, and then remove all properties from
    // pre-existing ontRes so just the new OntModel gets added.
    if ( ontRes != null ) {
      for ( Statement st : prexistStatements ) {
        Property prd = st.getPredicate();

        //
        // Do not tranfer pre-existing/pre-assigned-above attributes
        //
        String newValue = newValues.get(prd.getURI());
        if ( newValue == null || newValue.trim().length() == 0 ) {
          log.info("  Transferring: " +st.getSubject()+ " :: " +prd+ " :: " +st.getObject());
          newOntModel.add(ont_, prd, st.getObject());
        }
        else {
          log.info(" Removing pre-existing values for predicate: " +prd+ " because of new value " +newValue);
          newOntModel.removeAll(ont_, prd, null);
        }
      }


      if ( ! createOntologyResult.isPreserveOriginalBaseNamespace() ) {

        //
        // Only, when we're creating a new model, ie., per the new namespace, do the following removals.
        // (If we did this on a model with the same original namespace, we would remove the owl:Ontology
        // entry altogether and get an "rdf:Description" instead.
        //

        log.info("Removing original OWL.Ontology individual");
        ontRes.removeProperties();
        // TODO the following may be unnecesary but doesn't hurt:
        model.remove(ontRes, RDF.type, OWL.Ontology);
      }
    }



    ///////////////////////////////////////////////////////
    // Update attributes in model:

    Map<String, Property> uriPropMap = MdHelper.getUriPropMap();
    for ( String uri : newValues.keySet() ) {
      String value = newValues.get(uri).trim();
      if ( value.length() > 0 ) {
        Property prop = uriPropMap.get(uri);
        if ( prop == null ) {
          log.info("No property found for uri='" +uri+ "'");
          continue;
        }

        log.info(" Assigning: " +uri+ " = " +value);
        ont_.addProperty(prop, value);
      }
    }



    // Set the missing DC attrs that have defined e	equivalent MMI attrs:
    Util2.setDcAttributes(ont_);

    ////////////////////////////////////////////////////////////////////////
    // Done with the model.
    ////////////////////////////////////////////////////////////////////////

    // Get resulting string:
    JenaUtil2.removeUnusedNsPrefixes(model);
    String rdf = JenaUtil2.getOntModelAsString(model, "RDF/XML-ABBREV") ;  // XXX newOntModel);

    if ( log.isDebugEnabled() ) {
      if ( createOntologyResult.isPreserveOriginalBaseNamespace() ) {
        debugRdf("", rdf);
      }
    }

    log.debug("createOntology: setting URI: " +base_);
    createOntologyResult.setUri(base_);


    // write new contents to a new file under previewDir:

    File reviewedFile = new File(previewDir, newContentsFileName);
    createOntologyResult.setFullPath(reviewedFile.getAbsolutePath());

    _writeRdfToFile(rdf, reviewedFile, createOntologyResult);

    // Done.

    return createOntologyResult;
  }

  private boolean checkNoPreexistingOntology(String namespaceRoot, String orgAbbreviation, String shortName, BaseResult result) {
    // See issue 63: http://code.google.com/p/mmisw/issues/detail?id=63

    // the (unversioned) URI to check for preexisting ontology:
    String possibleOntologyUri = namespaceRoot + "/" +
        orgAbbreviation + "/" +
        shortName;

    return checkNoPreexistingOntology(possibleOntologyUri, result);
  }
  private boolean checkNoPreexistingOntology(String possibleOntologyUri, BaseResult result) {
    if ( log.isDebugEnabled() ) {
      log.debug("New submission; checking for preexisting ontology with unversioned URI: " +possibleOntologyUri);
    }

    boolean possibleOntologyExists = false;

    // we just need to know whether this URI resolves against the registry:
    try {
      possibleOntologyExists = isRegisteredOntologyUri(possibleOntologyUri);
    }
    catch (Exception e) {
      // report the error and return false (we shouldn't continue with the upload):
      String info = "Exception while checking for existence of URI: " +possibleOntologyUri+ " : " +e.getMessage();
      log.error(info, e);
      result.setError(info+ "\n\n Please try later.");
      return false;
    }

    if ( possibleOntologyExists ) {
      String info = "There is already a registered ontology with URI: " +possibleOntologyUri;

      if ( log.isDebugEnabled() ) {
        log.debug(info);
      }

      result.setError(info+ "\n\n" +
          "Note: if you want to submit a new version for the above ontology, " +
          "then you would need to browse to that entry in the main repository interface " +
          "and use one of the available options to create a new version."
      );
      return false;
    }

    // OK, no preexisting ontology:
    return true;
  }

  private boolean isRegisteredOntologyUri(String ontologyUri) throws Exception {
    String[] toks = ontologyUri.split("\\?");
    ontologyUri = toks[0];

    String version = null;
    if ( toks.length > 1 && toks[1].startsWith("version=") ) {
      version = toks[1].substring("version=".length());
    }

    log.debug("isRegisteredOntologyUri: ontologyUri=" +ontologyUri+ "  version=" +version);

    String url = config.ontServiceUrl + "/api/v0/ont";

    GetMethod method = new GetMethod(url);
    HttpClient client = createHttpClient();
    includeAutorization(method);

    List<NameValuePair> params = new ArrayList<>();
    params.add(new NameValuePair("uri", ontologyUri));

    if (version != null) {
      params.add(new NameValuePair("version", version));
    }
    params.add(new NameValuePair("format", "!md"));

    NameValuePair[] data = params.toArray(new NameValuePair[params.size()]);
    method.setQueryString(data);
    if (log.isInfoEnabled()) {
      log.info(debugMethod(method, params));
    }

    try {
      int status = client.executeMethod(method);
      return status == HttpStatus.SC_OK;
    }
    catch (Exception ex) {
      log.error("resolveUri: exception ontologyUri: " +ontologyUri, ex);
      return false;
    }
    finally {
      method.releaseConnection();
    }
  }

  private OntModel _createOntModel(OntModel ontModel) {
    ontModel = ModelFactory.createOntologyModel(ontModel.getSpecification(), ontModel);
    return ontModel;
  }



  private CreateOntologyResult _createOntologyReHosted(CreateOntologyInfo createOntologyInfo, CreateOntologyResult createOntologyResult) {

    Map<String, String> newValues = createOntologyInfo.getMetadataValues();
    assert ( newValues != null ) ;

    DataCreationInfo dataCreationInfo = createOntologyInfo.getDataCreationInfo();
    assert ( dataCreationInfo instanceof OtherDataCreationInfo ) ;
    final OtherDataCreationInfo otherDataCreationInfo = (OtherDataCreationInfo) dataCreationInfo;
    final TempOntologyInfo tempOntologyInfo = otherDataCreationInfo.getTempOntologyInfo();


    // to check if this is going to be a new submission (if ontologyId == null) or, otherwise, a new version.
    final String ontologyId = createOntologyInfo.getPriorOntologyInfo().getOntologyId();

    final String ontUri = createOntologyInfo.getUri();


    ////////////////////////////////////////////////////////////////////////////
    // section to create the ontology the base:

    // external ontology case: the base ontology is already available, just use it
    // by setting the full path in the createOntologyResult:

    String full_path;

    if ( tempOntologyInfo != null ) {
      // new contents were provided. Use that:
      full_path = tempOntologyInfo.getFullPath();
    }
    else {
      // No new contents. Only possible way for this to happen is that this is
      // a new version of an existing ontology.

      if ( ontologyId != null ) {
        // Just indicate a null full_path; see below.
        full_path = null;
      }
      else {
        // This shouldn't happen!
        String error = "Unexpected: Submission of new ontology, but not contents were provided. " +
            "This should be detected before submission. Please report this bug";
        createOntologyResult.setError(error);
        log.info(error);
        return createOntologyResult;
      }

    }
    createOntologyResult.setFullPath(full_path);


    // current date:
    final Date date = new Date(System.currentTimeMillis());

    ///////////////////////////////////////////////////////////////////
    // creation date:
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    final String creationDate = sdf.format(date);


    ///////////////////////////////////////////////////////////////////
    // version:
    // Note, if the version is given from the client, then use it
    String version = newValues.get(Omv.version.getURI());
    if ( version != null && version.trim().length() > 0 ) {
      // check that the given version is OK:
      boolean ok = VERSION_PATTERN.matcher(version).find();
      if ( ! ok ) {
        String error = "Given version is invalid: " +version;
        log.info(error);
        createOntologyResult.setError(error);
        return createOntologyResult;
      }
    }
    else {
      // otherwise: assign it here:
      sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
      version = sdf.format(date);
    }


    ////////////////////////////////////////////
    // load  model

    OntModel model;
    Ontology ont;
    String uriForEmpty;
    String newContentsFileName;

    if ( createOntologyResult.getFullPath() != null ) {
      //
      // new contents to check.
      // Get model from the new contents.
      //
      full_path = createOntologyResult.getFullPath();
      log.info("Loading model: " +full_path);

      File file = new File(full_path);

      try {
        model = Util2.loadModelWithCheckingUtf8(file, null);
      }
      catch ( Throwable ex ) {
        String error = "Unexpected error: " +ex.getClass().getName()+ " : " +ex.getMessage();
        log.info(error);
        createOntologyResult.setError(error);
        return createOntologyResult;
      }

      // fix #354: get ontology resource by ontUri from the model:
      ont = model.getOntology(ontUri);
      if (ont != null) {
        if (log.isDebugEnabled()) {
          log.debug("#354: _createOntologyReHosted: got ontology object by ontUri=" + ontUri);
        }
        uriForEmpty = ontUri;
      }
      else {
        uriForEmpty = null;
      }

      newContentsFileName = file.getName();
    }
    else {
      // NO new contents.
      // Use contents from prior version.
      CreateOntologyInfo.PriorOntologyInfo priorVersionInfo = createOntologyInfo.getPriorOntologyInfo();

      try {
        model = OntServiceUtil.retrieveModel(createOntologyInfo.getUri(), priorVersionInfo.getVersionNumber());
      }
      catch (Exception e) {
        String error = "error while retrieving registered ontology: " +e.getMessage();
        log.info(error, e);
        createOntologyResult.setError(error);
        return createOntologyResult;
      }

      ont = model.getOntology(ontUri);  // #356 get Ontology for the specific URI
      if ( ont == null ) {
        // Shouldn't happen -- we're reading in an already registered version.
        String error = "#356: _createOntologyReHosted: dit not get ontology object from previous " +
            "version by ontUri=" + ontUri + ". Please report this bug.";
        log.info(error);
        createOntologyResult.setError(error);
        return createOntologyResult;
      }
      uriForEmpty = ont.getURI();

      // replace ':', '/', or '\' for '_'
      newContentsFileName = uriForEmpty.replaceAll(":|/|\\\\", "_");
    }

    final String original_base_ = uriForEmpty == null ? null : JenaUtil2.removeTrailingFragment(uriForEmpty);

    final String base_ = JenaUtil2.removeTrailingFragment(ontUri);

    log.info("createOntologyReHosted: original namespace: " +original_base_);
    if ( original_base_ != null && ! original_base_.equals(base_) ) {
      // In this re-hosted case, we force the original URI and the new URI to be the same.
      // This may happen only in the case of a submission of a new version.
      String error = "The new base URI (" +base_+ ") is not equal to the registered " +
          "base URI (" +original_base_+ ") "
          ;
      log.debug(error);
      createOntologyResult.setError(error);
      return createOntologyResult;
    }

    /////////////////////////////////////////////////////////////////
    // If there is an pre-existing Ontology resource, get the associated statements:
    List<Statement> prexistStatements = null;
    if ( ont != null ) {
      prexistStatements = new ArrayList<Statement>();
      if ( log.isDebugEnabled() ) {
        log.debug("Getting pre-existing properties from Ontology: " +ont.getURI());
      }
      StmtIterator iter = ont.listProperties();
      while ( iter.hasNext() ) {
        Statement st = iter.nextStatement();
        prexistStatements.add(st);
      }
    }


    // The new OntModel that will contain the pre-existing attributes (if any),
    // plus the new and updated attributes:
    final OntModel newOntModel = OntModelUtil.createOntModel(base_, model);
    final Ontology ont_ = newOntModel.getOntology(ontUri);
    if ( log.isDebugEnabled() ) {
      log.debug("New ontology created with namespace " + newOntModel.getNsPrefixURI("") + " base " + base_);
    }

    // Set internal attributes, which are updated in the newValues map itself
    // so we facilite the processing below:
    newValues.put(Omv.version.getURI(), version);
    newValues.put(Omv.creationDate.getURI(), creationDate);


    //////////////////////////////////////////////////
    // transfer any preexisting attributes, and then remove all properties from
    // pre-existing ont so just the new OntModel gets added.
    if ( prexistStatements != null ) {
      for ( Statement st : prexistStatements ) {
        Property prd = st.getPredicate();

        //
        // Do not tranfer pre-existing/pre-assigned-above attributes
        //
        String newValue = newValues.get(prd.getURI());
        if ( newValue == null || newValue.trim().length() == 0 ) {
          if ( log.isDebugEnabled() ) {
            log.debug("  Transferring: " +st.getSubject()+ " :: " +prd+ " :: " +st.getObject());
          }
          newOntModel.add(ont_, prd, st.getObject());
        }
        else {
          if ( log.isDebugEnabled() ) {
            log.debug(" Removing pre-existing values for predicate: " +prd+ " because of new value " +newValue);
          }
          newOntModel.removeAll(ont_, prd, null);
        }
      }

    }



    ///////////////////////////////////////////////////////
    // Update attributes in model:

    Map<String, Property> uriPropMap = MdHelper.getUriPropMap();
    for ( String uri : newValues.keySet() ) {
      String value = newValues.get(uri);
      if ( value != null && value.trim().length() > 0 ) {
        Property prop = uriPropMap.get(uri);
        if ( prop == null ) {
          log.info("No property found for uri='" +uri+ "'");
          continue;
        }

        log.info(" Assigning: " +uri+ " = " +value);
        ont_.addProperty(prop, value);
      }
    }



    // Set the missing DC attrs that have defined e	equivalent MMI attrs:
    Util2.setDcAttributes(ont_);

    ////////////////////////////////////////////////////////////////////////
    // Done with the model.
    ////////////////////////////////////////////////////////////////////////

    // Get resulting string:
    JenaUtil2.removeUnusedNsPrefixes(model);
    String rdf = JenaUtil2.getOntModelAsString(model, "RDF/XML-ABBREV") ;  // XXX newOntModel);

    if ( log.isDebugEnabled() ) {
      debugRdf("", rdf);
    }

    log.debug("createOntology: setting URI: " +base_);
    createOntologyResult.setUri(base_);


    // write new contents to a new file under previewDir:

    File reviewedFile = new File(previewDir, newContentsFileName);
    createOntologyResult.setFullPath(reviewedFile.getAbsolutePath());

    _writeRdfToFile(rdf, reviewedFile, createOntologyResult);

    // Done.

    return createOntologyResult;
  }


  @Override
  public RegisterOntologyResult registerOntology(CreateOntologyResult createOntologyResult, LoginResult loginResult) {
    CreateOntologyInfo createOntologyInfo = createOntologyResult.getCreateOntologyInfo();

    final HostingType hostingType = createOntologyInfo.getHostingType();
    log.info("registerOntology: called. hostingType = " +hostingType);

    if (hostingType == null) {
      // should not happen
      RegisterOntologyResult registerOntologyResult = new RegisterOntologyResult();
      registerOntologyResult.setError("defined hostingType expected!");
      return registerOntologyResult;
    }
    RegisterOntologyResult registerOntologyResult = new RegisterOntologyResult();

    switch ( hostingType ) {
      case FULLY_HOSTED:
        return registerOntologyFullyHosted(createOntologyResult, registerOntologyResult, loginResult);
      case RE_HOSTED:
        return registerOntologyReHosted(createOntologyResult, registerOntologyResult, loginResult);
      default: {
        // should not happen
        String error = "Hosting type "+hostingType+ " not implemented.";
        registerOntologyResult.setError(error);
        log.info(error);
        return registerOntologyResult;
      }
    }
  }

  private RegisterOntologyResult registerOntologyFullyHosted(CreateOntologyResult createOntologyResult, RegisterOntologyResult registerOntologyResult, LoginResult loginResult) {

    String full_path = createOntologyResult.getFullPath();

    log.info("registerOntology: Reading in temporary file: " +full_path);

    File file = new File(full_path);

    // Get resulting model:
    String rdf;
    try {
//			rdf = Util2.readRdf(file);
      rdf = Util2.readRdfWithCheckingUtf8(file);
    }
    catch (Throwable e) {
      String error = "Unexpected: error while reading from: " +full_path+ " : " +e.getMessage();
      log.info(error);
      registerOntologyResult.setError(error);
      return registerOntologyResult;
    }

    // ok, we have our ontology:


    //////////////////////////////////////////////////////////////////////////
    // finally, do actual registration to MMI registry

    // Get final URI of resulting model
    final String uri = createOntologyResult.getUri();
    assert uri != null;
    assert loginResult.getUserId() != null;
    assert loginResult.getSessionId() != null;

    log.info(": registering URI: " +uri+ " ...");

    CreateOntologyInfo createOntologyInfo = createOntologyResult.getCreateOntologyInfo();

    String ontologyId = createOntologyInfo.getPriorOntologyInfo().getOntologyId();
    String ontologyUserId = createOntologyInfo.getPriorOntologyInfo().getOntologyUserId();

    if ( ontologyId != null ) {
      log.info("Will create a new version for ontologyId = " +ontologyId+ ", userId=" +ontologyUserId);
    }


    Map<String, String> newValues = createOntologyInfo .getMetadataValues();


    try {
      // this is to get the filename for the registration
      String fileName = new URL(uri).getPath();

      if ( ontologyId == null ) {
        //
        // Submission of a new ontology (not version of a registered one)
        //

        // We are about to do the actual registration. But first, re-check that there is NO a preexisting
        // ontology that may conflict with this one.
        // NOTE: this check has been done already in the review operation; however, we repeat it here
        // in case there is a new registration done by other user in the meantime. Of course, we
        // are NOT completely solving the potential concurrency problem with this re-check; we are just
        // reducing the chances of that event.


        // TODO: the following can be simplified by obtaining the unversioned form of the URI and
        // calling Util2.checkNoPreexistingOntology(unversionedUri, registerOntologyResult)

        final String namespaceRoot = newValues.get("namespaceRoot") != null
            ? newValues.get("namespaceRoot")
            :  defaultNamespaceRoot;

        final String orgAbbreviation = createOntologyInfo.getAuthority();
        final String shortName = createOntologyInfo.getShortName();

        if ( ! checkNoPreexistingOntology(namespaceRoot, orgAbbreviation, shortName, registerOntologyResult) ) {
          return registerOntologyResult;
        }

      }
      else {
        // This is a submission of a *new version* of an existing ontology.
        // Nothing needs to be checked here.
        // NOTE: We don't need to repeat the _checkUriKeyCombinationForNewVersion step here
        // as any change in the contents of the metadata forces the user to explicitly
        // do the "review" operation, which already takes care of that check.
      }

      // OK, now do the actual registration:
      String res = uploadOntology(uri, fileName, rdf,
          loginResult,
          ontologyId, ontologyUserId,
          newValues
      );

      if ( res.startsWith("OK") ) {
        registerOntologyResult.setUri(uri);
        registerOntologyResult.setInfo(res);

        loadOntologyInGraph(uri, null);
      }
      else {
        registerOntologyResult.setError(res);
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
      registerOntologyResult.setError(ex.getClass().getName()+ ": " +ex.getMessage());
    }

    log.info("registerOntologyResult = " +registerOntologyResult);


    return registerOntologyResult;
  }

  private boolean loadOntologyInGraph(String ontologyUri, String graphId) throws Exception {
    String url = config.ontServiceUrl + "/api/v0/ts";

    PostMethod method = new PostMethod(url);
    HttpClient client = createHttpClient();
    includeAutorization(method);

    List<NameValuePair> params = new ArrayList<>();
    params.add(new NameValuePair("uri", ontologyUri));

    if (graphId != null) {
      params.add(new NameValuePair("graphId", graphId));
    }

    NameValuePair[] data = params.toArray(new NameValuePair[params.size()]);
    method.setQueryString(data);
    if (log.isInfoEnabled()) {
      log.info(debugMethod(method, params));
    }

    try {
      int status = client.executeMethod(method);
      log.debug("loadOntologyInGraph: result: status=" +status+ ": " + HttpStatus.getStatusText(status));
      return status == HttpStatus.SC_OK;
    }
    catch (Exception ex) {
      log.error("loadOntologyInGraph: exception ontologyUri: " +ontologyUri, ex);
      return false;
    }
    finally {
      method.releaseConnection();
    }
  }

  // TODO
  private RegisterOntologyResult registerOntologyReHosted(CreateOntologyResult createOntologyResult, RegisterOntologyResult registerOntologyResult, LoginResult loginResult) {

    String full_path = createOntologyResult.getFullPath();

    log.info("registerOntology: Reading in temporary file: " +full_path);

    File file = new File(full_path);

    // Get resulting model:
    String rdf;
    try {
      rdf = Util2.readRdfWithCheckingUtf8(file);
    }
    catch (Throwable e) {
      String error = "Unexpected: error while reading from: " +full_path+ " : " +e.getMessage();
      log.info(error);
      registerOntologyResult.setError(error);
      return registerOntologyResult;
    }

    // ok, we have our ontology:


    //////////////////////////////////////////////////////////////////////////
    // finally, do actual registration to MMI registry

    // Get final URI of resulting model
    final String uri = createOntologyResult.getUri();
    assert uri != null;
    assert loginResult.getUserId() != null;
    assert loginResult.getSessionId() != null;

    log.info(": registering ...");

    CreateOntologyInfo createOntologyInfo = createOntologyResult.getCreateOntologyInfo();

    String ontologyId = createOntologyInfo.getPriorOntologyInfo().getOntologyId();
    String ontologyUserId = createOntologyInfo.getPriorOntologyInfo().getOntologyUserId();

    if ( ontologyId != null ) {
      log.info("Will create a new version for ontologyId = " +ontologyId+ ", userId=" +ontologyUserId);
    }


    Map<String, String> newValues = createOntologyInfo .getMetadataValues();


    try {

      String fileName = new URL(uri).getPath();

      //
      // make sure the fileName ends with ".owl" as the aquaportal back-end seems
      // to add that fixed extension in some operations (at least in the parse operation)
      //
      if ( ! fileName.toLowerCase().endsWith(".owl") ) {
        log.info("register: setting file extension to .owl per aquaportal requirement.");
        fileName += ".owl";
      }


      // OK, now do the actual registration:
      String res = uploadOntology(uri, fileName, rdf,
          loginResult,
          ontologyId, ontologyUserId,
          newValues
      );

      if ( res.startsWith("OK") ) {
        registerOntologyResult.setUri(uri);
        registerOntologyResult.setInfo(res);

        // issue #168 fix:
        // request that the ontology be loaded in the "ont" graph:
        loadOntologyInGraph(uri, null);
      }
      else {
        registerOntologyResult.setError(res);
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
      registerOntologyResult.setError(ex.getClass().getName()+ ": " +ex.getMessage());
    }

    if ( log.isDebugEnabled() ) {
      log.debug("registerOntologyResult = " +registerOntologyResult);
    }

    return registerOntologyResult;
  }

  private String uploadOntology(String uri, String fileName, String RDF,
                                LoginResult loginResult,
                                String ontologyId, String ontologyUserId,
                                Map<String, String> values
  ) throws Exception {

    log.debug("uploadOntology: uri=" + uri + " loginResult=" + loginResult
        + " ontologyId=" + ontologyId+ " ontologyUserId=" + ontologyUserId
        + " values=" + values);

    SignInResult signInResult = new SignInResult();
    signInResult.setSessionId(loginResult.getSessionId());
    signInResult.setUserId(loginResult.getUserId());
    signInResult.setUserName(loginResult.getUserName());
    signInResult.setUserRole(loginResult.getUserRole());

    OntUploader ontUploader = new OntUploader(uri, fileName, RDF,
        signInResult,
        ontologyId, ontologyUserId,
        values);

    String url = config.ontServiceUrl + "/api/v0/ont";
    return ontUploader.create(url);

  }

  @Override
  /*TODO*/public ResetPasswordResult resetUserPassword(String username) {
    throw new UnsupportedOperationException();
    //return null;
  }

  @Override
  /*TODO*/public CreateUpdateUserAccountResult createUpdateUserAccount(Map<String, String> values) {
    throw new UnsupportedOperationException();
    //return null;
  }

  @Override
  /*TODO*/public InternalOntologyResult prepareUsersOntology(LoginResult loginResult) {
    throw new UnsupportedOperationException();
    //return null;
  }

  @Override
  /*TODO*/public InternalOntologyResult createGroupsOntology(LoginResult loginResult) {
    throw new UnsupportedOperationException();
    //return null;
  }

  @Override
  /*TODO*/public UnregisterOntologyResult unregisterOntology(LoginResult loginResult, RegisteredOntologyInfo oi) {
    throw new UnsupportedOperationException();
    //return null;
  }

  @Override
  /*TODO*/public String markTestingOntology(LoginResult loginResult, RegisteredOntologyInfo oi, boolean markTesting) {
    throw new UnsupportedOperationException();
    //return null;
  }

  private OntModel retrieveModel(String uriModel, String version) throws Exception {
    if ( log.isDebugEnabled() ) {
      log.debug("retrieveModel: uri: " +uriModel+ "  version: " +version);
    }

    String str = resolveOntologyUri(uriModel, version, "application/rdf+xml");

    OntModel model = createDefaultOntModel();
    uriModel = JenaUtil2.removeTrailingFragment(uriModel);

    StringReader sr = new StringReader(str);

    try {
      model.read(sr, uriModel);
    }
    catch (Exception e) {
      log.warn("Error reading model from retrieved contents: [" +str+ "]", e);
      throw e;
    }

    return model;
  }

  private String resolveOntologyUri(String uriModel, String version, String... acceptEntries) throws Exception {
    String ontServiceUrl = config.ontServiceUrl + "/api/v0/ont";
    uriModel = URLEncoder.encode(uriModel, "UTF-8");
    String ontServiceRequest = ontServiceUrl + "?uri=" +uriModel;
    if ( version != null ) {
      ontServiceRequest += "&version=" +version;
    }
    if ( log.isDebugEnabled() ) {
      log.debug("resolveOntologyUri: ontServiceRequest=" +ontServiceRequest);
    }
    String str = HttpUtil.getAsString(ontServiceRequest, acceptEntries);

    return str;
  }

  private static OntModel createDefaultOntModel() {
    OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
    OntDocumentManager docMang = new OntDocumentManager();
    spec.setDocumentManager(docMang);
    OntModel model = ModelFactory.createOntologyModel(spec, null);
    // removeNotNeccesaryNamespaces(model);

    return model;
  }

  private String debugMethod(HttpMethodBase method, List<NameValuePair> params) {
    StringBuilder sb = new StringBuilder(
        "Executing " + method.getName() + " " + method.getPath() + " : ");
    String comma = "";
    for (NameValuePair np: params) {
      sb.append(comma).append(np.getName()).append("=").append(np.getValue());
      comma = ", ";
    }
    Header[] requestHeaders = method.getRequestHeaders();
    if (requestHeaders != null && requestHeaders.length > 0) {
      sb.append(comma).append("requestHeaders: ");
      comma = "";
      for (Header h: requestHeaders) {
        sb.append(comma).append(h.getName()).append("=").append(h.getValue());
        comma = ", ";
      }
    }
    return sb.toString();
  }

  private final Gson gson = new Gson();
  private final Config cfg;

  // TODO get this location from build.properties (via web.xml)
  private final static String configFilename = "/etc/mmiorr.conf";

  private static Config readConfig() throws Exception {
    File configFile = new File(configFilename);
    if (!configFile.canRead()) {
      throw new ServiceConfigurationError("Could not read configuration file " + configFile);
    }
    Config c = ConfigFactory.parseFile(configFile);
    if (log.isDebugEnabled()) {
      log.debug("readConfig: Loaded configuration=" + c);
    }
    return c;
  }

  class OntUploader {

    /**
     * Constructor.
     * @param uri
     * @param fileName
     * @param RDF Contents of the ontology
     * @param signInResult
     * @param ontologyId Aquaportal ontology ID when creating a new version.
     *
     * @param values   Used to fill in some of the fields in the aquaportal request
     * @throws Exception
     */
    OntUploader(String uri, String fileName, String RDF,
                SignInResult signInResult,
                String ontologyId, String ontologyUserId,
                Map<String, String> values
    ) throws Exception {

      this.uri = uri;
      this.signInResult = signInResult;
      this.values = values;

      PartSource partSource = new ByteArrayPartSource(fileName, RDF.getBytes(CHARSET_UTF8));
      filePart = new FilePart("file", partSource);
      filePart.setCharSet(CHARSET_UTF8);
    }


    /**
     * Executes the POST operation to upload the ontology.
     *
     * @return The message in the response from the POST operation, prefixed with "OK:" if
     *         the result was successful; otherwise, the description of the error
     *         prefixed with "ERROR:"
     */
    String create(String ontologiesRestUrl)	throws Exception {

      String userName = signInResult.getUserName();

      if ( log.isDebugEnabled() ) {
        log.debug("ontologiesRestUrl = " +ontologiesRestUrl);
      }
      PostMethod post = new PostMethod(ontologiesRestUrl);
      try {
        List<Part> partList = new ArrayList<>();

        partList.add(filePart);

        partList.add(new StringPart("uri", uri));
        partList.add(new StringPart("userName", userName));

        String displayLabel = _getDisplayLabel();
        partList.add(new StringPart("name", displayLabel));

        String orgName = "mmitest";  // TODO orgName
        partList.add(new StringPart("orgName", orgName));

        String format = "rdf";  // TODO format
        partList.add(new StringPart("format", format));

        String versionNumber = _getVersionNumber();
        partList.add(new StringPart("version", versionNumber));


        // now, perform the POST:
        Part[] parts = partList.toArray(new Part[partList.size()]);
        post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
        HttpClient client = createHttpClient();
        includeAutorization(post);

        log.info("Executing POST ...");

        String msg;
        int status = client.executeMethod(post);
        if (status == HttpStatus.SC_CREATED) {
          msg = post.getResponseBodyAsString();
//				log.info("Upload complete, response=" + msg);
          msg = "OK:" +msg;
        }
        else {
          String body = post.getResponseBodyAsString();
          msg = HttpStatus.getStatusText(status);
          log.info("Upload failed, status=" + status+ ": " +msg+ "\n" +body);
          msg = "ERROR:" +msg+ "\n" +body ;
        }

        return msg;
      }
      finally {
        post.releaseConnection();
      }
    }

    private String _getVersionNumber() {
      String versionNumber = values.get(Omv.version.getURI());
      if ( versionNumber == null ) {
        // shouldn't happen;
        versionNumber = "0.0.0";
      }
      return versionNumber;
    }


    private String _getContactEmail() {
      // TODO: define something like: OmvMmi.contactEmail
      String contactEmail = "";
      return contactEmail;
    }


    private String _getDisplayLabel() {
      String displayLabel = values.get(Omv.name.getURI());
      if ( displayLabel == null ) {
        // shouldn't happen, but, well assign the same uri:
        displayLabel = uri;
      }
      return displayLabel;
    }


    private String _getContactName() {
      // try hasContentCreator:
      String value = values.get(OmvMmi.hasContentCreator.getURI());
      if ( value != null ) {
        if ( log.isDebugEnabled() ) {
          log.debug("_getContactName: using value of OmvMmi.hasContentCreator: " +value);
        }
        return value;
      }

      // try Omv.hasCreator or DC.creator as before:
      value = values.get(Omv.hasCreator.getURI());
      if ( value != null ) {
        if ( log.isDebugEnabled() ) {
          log.debug("_getContactName: using value of Omv.hasCreator: " +value);
        }
        return value;
      }

      // shouldn't happen; try with DC.creator
      value = values.get(DC.creator.getURI());
      if ( value != null ) {
        if ( log.isDebugEnabled() ) {
          log.debug("_getContactName: using value of DC.creator: " +value);
        }
        return value;
      }

      // shouldn't happen; just assign ""
      value = "";
      if ( log.isDebugEnabled() ) {
        log.debug("_getContactName: no value available. Using \"\"");
      }

      return value;
    }

    private static final String CHARSET_UTF8 = "UTF-8";

    private final Log log = LogFactory.getLog(OntUploader.class);

    private FilePart filePart;
    private String uri;
    private SignInResult signInResult;

    private Map<String, String> values;

  }

}
