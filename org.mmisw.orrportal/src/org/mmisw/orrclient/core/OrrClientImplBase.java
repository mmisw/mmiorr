package org.mmisw.orrclient.core;

import com.hp.hpl.jena.ontology.OntModel;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.vocabulary.OmvMmi;
import org.mmisw.orrclient.IOrrClient;
import org.mmisw.orrclient.core.util.MailSender;
import org.mmisw.orrclient.core.util.TempOntologyHelper;
import org.mmisw.orrclient.core.util.Util2;
import org.mmisw.orrclient.core.util.ontinfo.OntInfoUtil;
import org.mmisw.orrclient.core.vine.VineUtil;
import org.mmisw.orrclient.gwt.client.rpc.*;
import org.mmisw.orrclient.gwt.client.rpc.vine.RelationInfo;
import org.mmisw.orrclient.gwt.client.vocabulary.AttrDef;
import org.mmisw.orrclient.gwt.client.vocabulary.AttrGroup;
import org.mmisw.orrportal.gwt.server.OrrConfig;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;


public abstract class OrrClientImplBase implements IOrrClient {
  protected final OrrConfig config;

  private static final Log log = LogFactory.getLog(OrrClientImplBase.class);

  /** Ontology URI prefix including root: */
  protected final String defaultNamespaceRoot;

  protected final File previewDir;

  protected MetadataBaseInfo metadataBaseInfo = null;


  protected OrrClientImplBase() throws Exception {
    config = OrrConfig.instance();

    log.info("ontServiceUrl = " +config.ontServiceUrl);

    defaultNamespaceRoot = config.ontServiceUrl;

    previewDir = config.previewDir;
  }

  // TODO this mechanism copied from MmiUri (in ont project).
  protected static final Pattern VERSION_PATTERN =
      Pattern.compile("^\\d{4}(\\d{2}(\\d{2})?)?(T\\d{2})?(\\d{2}(\\d{2})?)?$");

  protected void _writeRdfToFile(String rdf, File reviewedFile, CreateOntologyResult createOntologyResult) {
    PrintWriter os;
    try {
      // #43: Handle non-UTF8 inputs
      // keep "UTF-8" character for consistency:
      os = new PrintWriter(reviewedFile, "UTF-8");
    }
    catch (FileNotFoundException e) {
      String error = "Unexpected: cannot open file for writing: " +reviewedFile;
      log.info(error);
      createOntologyResult.setError(error);
      return;
    }
    catch (UnsupportedEncodingException e) {
      String error = "Unexpected: cannot create file in UTF-8 encoding: " +reviewedFile;
      log.info(error);
      createOntologyResult.setError(error);
      return;
    }
    StringReader is = new StringReader(rdf);
    try {
      IOUtils.copy(is, os);
      os.flush();
    }
    catch (IOException e) {
      String error = "Unexpected: IO error while writing to: " +reviewedFile;
      log.info(error);
      createOntologyResult.setError(error);
    }
  }

  public RegisterOntologyResult registerOntologyDirectly(
      LoginResult loginResult,
      RegisteredOntologyInfo registeredOntologyInfo,
      CreateOntologyInfo createOntologyInfo,
      String graphId) {
    throw new UnsupportedOperationException();
  }

  public UserInfoResult getUserInfo(String username) {
    UserInfoResult result = new UserInfoResult();

    try {
      Map<String, String> props = getUserInfoMap(username);
      result.setProps(props);
      log.debug("getUserInfoMap returned=" + props);
    }
    catch (Exception e) {
      String error = "error getting user information: " +e.getMessage();
      result.setError(error);
      log.error(error, e);
    }

    return result;
  }

  protected abstract Map<String,String> getUserInfoMap(String username) throws Exception ;

  public List<RelationInfo> getDefaultVineRelationInfos() {
    if ( log.isDebugEnabled() ) {
      log.debug("getDefaultVineRelationInfos starting");
    }

    List<RelationInfo> relInfos = VineUtil.getDefaultVineRelationInfos();

    if ( log.isDebugEnabled() ) {
      log.debug("getDefaultVineRelationInfos returning: " +relInfos);
    }

    return relInfos;
  }

  public TempOntologyInfo getTempOntologyInfo(
      String fileType, String filename,
      boolean includeContents, boolean includeRdf
  ) {
    TempOntologyInfo tempOntologyInfo = new TempOntologyInfo();

    if ( metadataBaseInfo == null ) {
      tempOntologyInfo.setError(OrrClientImpl.class.getSimpleName()+ " not properly initialized. Please report this bug. (metadataBaseInfo not initialized)");
      return tempOntologyInfo;
    }

    TempOntologyHelper tempOntologyHelper = new TempOntologyHelper(metadataBaseInfo);
    tempOntologyHelper.getTempOntologyInfo(fileType, filename, tempOntologyInfo, includeRdf);

    if ( tempOntologyInfo.getError() != null ) {
      return tempOntologyInfo;
    }

    String ontologyUri = tempOntologyInfo.getUri();
    if (ontologyUri != null) {
      tempOntologyInfo.setIsOntResolvable(isOntResolvableUri(ontologyUri));
    }

    if ( includeContents ) {
      _getOntologyContents(tempOntologyInfo);
    }

    return tempOntologyInfo;
  }

  protected boolean isOntResolvableUri(String uri) {
    return uri.toLowerCase().startsWith(config.ontServiceUrl.toLowerCase());
  }

  /** similar to getOntologyContents(RegisteredOntologyInfo) but reading the model
   * from the internal path.
   */
  private TempOntologyInfo _getOntologyContents(TempOntologyInfo tempOntologyInfo) {

    if ( log.isDebugEnabled() ) {
      log.debug("_getOntologyContents(TempOntologyInfo): loading model");
    }

    String full_path = tempOntologyInfo.getFullPath();
    File file = new File(full_path );
    String uriFile = file.toURI().toString();
    log.info("Loading model: " +uriFile);

    OntModel ontModel;
    try {
      ontModel = JenaUtil2.loadModel(uriFile, false);
    }
    catch (Throwable ex) {
      String error = "Unexpected error: " +ex.getClass().getName()+ " : " +ex.getMessage();
      log.info(error);
      tempOntologyInfo.setError(error);
      return tempOntologyInfo;
    }


    // Metadata:
    if ( log.isDebugEnabled() ) {
      log.debug("_getOntologyContents(TempOntologyInfo): getting metadata");
    }
    MetadataExtractor.prepareOntologyMetadata(metadataBaseInfo, ontModel, tempOntologyInfo);


    // Data
    if ( log.isDebugEnabled() ) {
      log.debug("_getOntologyContents(TempOntologyInfo): getting entities");
    }

    try {
      OntInfoUtil.getEntities(tempOntologyInfo, ontModel);
    }
    catch (Exception e) {
      String error = "Error loading model: " +e.getMessage();
      log.error(error, e);
      tempOntologyInfo.setError(error);
      return tempOntologyInfo;
    }


    return tempOntologyInfo;

  }

  public AttrDef refreshOptions(AttrDef attrDef) {
    return MdHelper.refreshOptions(attrDef);
  }

  public MetadataBaseInfo getMetadataBaseInfo(
      boolean includeVersion, String resourceTypeClassUri,
      String authorityClassUri) {


    if ( metadataBaseInfo == null ) {
      if ( log.isDebugEnabled() ) {
        log.debug("preparing base info ...");
      }

      metadataBaseInfo = new MetadataBaseInfo();

//			metadataBaseInfo.setResourceTypeUri(Omv.acronym.getURI());
      metadataBaseInfo.setResourceTypeUri(OmvMmi.hasResourceType.getURI());

      metadataBaseInfo.setAuthorityAbbreviationUri(OmvMmi.origMaintainerCode.getURI());

      MdHelper.prepareGroups(includeVersion, resourceTypeClassUri, authorityClassUri);
      AttrGroup[] attrGroups = MdHelper.getAttrGroups();
      metadataBaseInfo.setAttrGroups(attrGroups);

      metadataBaseInfo.setAuthorityAttrDef(MdHelper.getAuthorityAttrDef());

      metadataBaseInfo.setResourceTypeAttrDef(MdHelper.getResourceTypeAttrDef());

      metadataBaseInfo.setUriAttrDefMap(MdHelper.getUriAttrDefMap());

      if ( log.isDebugEnabled() ) {
        log.debug("preparing base info ... DONE");
      }
    }

    return metadataBaseInfo;
  }

  public GetAllOntologiesResult getAllOntologies(boolean includeAllVersions) {
    GetAllOntologiesResult result = new GetAllOntologiesResult();
    try {
      List<RegisteredOntologyInfo> list = _doGetAllOntologies(includeAllVersions);
      result.setOntologyList(list);
    }
    catch (Throwable ex) {
      String error = "Error getting list of all ontologies: " +ex.getMessage();
      log.warn(error, ex);
      result.setError(error);
    }
    return result;
  }

  protected abstract List<RegisteredOntologyInfo> _doGetAllOntologies(boolean includeAllVersions) throws Exception;

  protected RegisteredOntologyInfo _createOntologyInfo(
      String ontologyUri,   // = toks[0];
      String displayLabel,  // = toks[1];
      OntologyType type,    // = toks[2];
      String userId,        // = toks[3];
      String contactName,   // = toks[4];
      String versionNumber, // = toks[5];
      String dateCreated,   // = toks[6];
      String userName,      // = toks[7];
      String ontologyId,    // = toks[8];
      String versionStatus, // = toks[9];

      String unversionedUri,
      String authority,
      String shortName
  ) {
    RegisteredOntologyInfo registeredOntologyInfo = new RegisteredOntologyInfo();

    registeredOntologyInfo.setUri(ontologyUri);
    registeredOntologyInfo.setDisplayLabel(displayLabel);
    registeredOntologyInfo.setType(type);
    registeredOntologyInfo.setUserId(userId);
    registeredOntologyInfo.setContactName(contactName);
    registeredOntologyInfo.setVersionNumber(versionNumber);
    registeredOntologyInfo.setDateCreated(dateCreated);
    registeredOntologyInfo.setUsername(userName);
    registeredOntologyInfo.setOntologyId(ontologyId, userId);
    registeredOntologyInfo.setVersionStatus(versionStatus);

    registeredOntologyInfo.setUnversionedUri(unversionedUri);
    registeredOntologyInfo.setAuthority(authority);
    registeredOntologyInfo.setShortName(shortName);

    _setHostingType(registeredOntologyInfo);

    return registeredOntologyInfo;
  }

  private void _setHostingType(RegisteredOntologyInfo registeredOntologyInfo) {
    String uri = registeredOntologyInfo.getUri();
    boolean ontResolvableUri = isOntResolvableUri(uri);

    HostingType hostingType;
    if ( ontResolvableUri ) {
      hostingType = HostingType.FULLY_HOSTED;
    }
    else {
      hostingType = HostingType.RE_HOSTED;
    }
    // TODO: Determine HostingType.INDEXED case.

    registeredOntologyInfo.setHostingType(hostingType);

    if(log.isTraceEnabled()) {
      log.trace("_setHostingType: '" + uri + "' ontResolvableUri: " + ontResolvableUri +
          "-> hostingType=" + hostingType);
    }
  }


  protected void _notifyUserCreated(final LoginResult loginResult) {
    Thread t = new Thread() {
      public void run() {
        final Set<String> recipients = _getRecipients();
        if (recipients.size() == 0) {
          return;
        }
        final Map<String, String> data = new LinkedHashMap<>();
        String username = loginResult.getUserName();
        data.put("username", username);
        try {
          Map<String,String> userInfo = getUserInfoMap(username);
          String[] skip = {"username", "id", "date_created"};
          for (String k: skip) {
            if (userInfo.containsKey(k)) {
              userInfo.remove(k);
            }
          }
          data.putAll(userInfo);
        }
        catch (Exception e) {
          log.warn("getUserInfo: username=" + username+ ": " +e.getMessage());
        }

        _sendEmail("User created/updated",
            "The following user account has been created or updated:",
            data, recipients);
      }
    };
    t.setDaemon(true);
    t.start();
  }

  private void _sendEmail(String subject, String header, Map<String, String> data, Set<String> recipients) {
    final String mail_user     = OrrConfig.instance().emailUsername;
    final String mail_password = OrrConfig.instance().emailPassword;
    if ( mail_user == null || mail_user.equals("-") ) {
      String error = "Email server account not configured. Please report this bug. (u)";
      log.error(error);
      return;
    }
    if ( mail_password == null || mail_password.equals("-") ) {
      String error = "Email server account not configured. Please report this bug. (p)";
      log.error(error);
      return;
    }

    boolean debug = false;
    final String from    = OrrConfig.instance().emailFrom;
    final String replyTo = OrrConfig.instance().emailReplyTo;

    String text = header + "\n";
    for (String key: data.keySet()) {
      text += "\n    " + key + ": " + data.get(key);
    }
    text += "\n\n";
    text += "(you have received this email because your address is included in "
        +OrrConfig.instance().notifyEmailsFilename + ")";

    String to = "", comma = "";
    for (String recipient : recipients) {
      to += comma + recipient;
      comma = ",";
    }
    log.debug("sending email to notify event: " +data+ "; recipients: " +to);
    try {
      MailSender.sendMessage(mail_user, mail_password, debug, from, to, replyTo, subject, text);
      log.debug("email sent to notify event: " + data + "; recipients: " + to);
    }
    catch (Exception e) {
      String error = "Error sending email to notify event: " + data
          + "; recipients: " +to+ ": " +e.getMessage();
      log.error(error, e);
    }
  }

  private Set<String> _getRecipients() {
    final Set<String> recipients = new LinkedHashSet<>();
    String notifyEmailsFilename = OrrConfig.instance().notifyEmailsFilename;
    if (notifyEmailsFilename != null) {
      try {
        File f = new File(notifyEmailsFilename);
        FileInputStream is = new FileInputStream(f);
        for (Object line : IOUtils.readLines(is)) {
          String email = String.valueOf(line).trim();
          if (email.length() > 0 && !email.startsWith("#")) {
            recipients.add(email);
          }
        }
        IOUtils.closeQuietly(is);
      }
      catch (Exception e) {
        log.warn("could not read in: " + notifyEmailsFilename, e);
      }
    }
    if (recipients.size() == 0) {
      log.debug("no recipients to send email: " + notifyEmailsFilename);
    }
    return recipients;
  }

  protected void debugRdf(String pre, String rdf) {
    String show = rdf.length() > 1000 ? rdf.substring(0, 900) + " [...] " + rdf.substring(rdf.length() - 100) : rdf;
    log.debug(pre + show);
  }
}
