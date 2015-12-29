package org.mmisw.orrportal.gwt.client.rpc2;

import com.google.gwt.http.client.*;
import com.google.gwt.json.client.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.mmisw.orrclient.gwt.client.rpc.*;
import org.mmisw.orrportal.gwt.client.Orr;

import java.util.ArrayList;
import java.util.List;


/**
 * #361 -- preliminaries
 */
public class OrrOntServiceAsync {

  private final String orrOntUrl = "http://localhost:8080/orr-ont";

  public void getAllOntologies(final boolean includeAllVersions, final AsyncCallback<GetAllOntologiesResult> callback) {
    String url = orrOntUrl + "/api/v0/ont";

    Orr.log("OrrOntServiceAsync: getAllOntologies");

    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

    try {
      builder.sendRequest(null, new RequestCallback() {
        public void onError(Request request, Throwable ex) {
          callback.onFailure(ex);
        }

        public void onResponseReceived(Request request, Response response) {
          List<RegisteredOntologyInfo> onts = new ArrayList<RegisteredOntologyInfo>();

          int status = response.getStatusCode();
          String msg = response.getText();
          Orr.log("OrrOntServiceAsync: getAllOntologies: status=" +status+ " text=" +msg);

          GetAllOntologiesResult result = new GetAllOntologiesResult();
          result.setOntologyList(onts);

          if (status == 200) {
            JSONArray list = (JSONArray) JSONParser.parse(msg);
            for (int i = 0; i < list.size(); i++) {
              JSONValue v = list.get(i);
              if (v instanceof JSONObject ){
                JSONObject map = (JSONObject) v;
                RegisteredOntologyInfo registeredOntologyInfo = _createRegisteredOntologyInfo(map, includeAllVersions);
                onts.add(registeredOntologyInfo);
              }
            }
            Orr.log("OrrOntServiceAsync: getAllOntologies: text=" +msg);

          }

          callback.onSuccess(result);
        }
      });
    }
    catch (RequestException ex) {
      callback.onFailure(ex);
    }
  }

  public void resolveUri(final String uri, final AsyncCallback<ResolveUriResult> callback) {
    String url = orrOntUrl + "/api/v0/ont";

    String[] toks = uri.split("\\?");
    final String ontologyUri = toks[0];

    url += "?uri=" +ontologyUri;

    String version = null;
    if ( toks.length > 1 && toks[1].startsWith("version=") ) {
      version = toks[1].substring("version=".length());
      url += "&version=" +version;
    }

    final ResolveUriResult resolveUriResult = new ResolveUriResult(uri);

    Orr.log("OrrOntServiceAsync: resolveUri: url=" +url);

    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

    try {
      builder.sendRequest(null, new RequestCallback() {
        public void onError(Request request, Throwable ex) {
          callback.onFailure(ex);
        }

        public void onResponseReceived(Request request, Response response) {

          int status = response.getStatusCode();
          String msg = response.getText();
          Orr.log("OrrOntServiceAsync: getAllOntologies: status=" +status+ " text=" +msg);

          resolveUriResult.setUri(ontologyUri);


          callback.onSuccess(resolveUriResult);
        }
      });
    }
    catch (RequestException ex) {
      callback.onFailure(ex);
    }
  }

  private RegisteredOntologyInfo _createRegisteredOntologyInfo(JSONObject map, boolean includeAllVersions) {
    String uri  = getOrEmpty(map, "uri");
    String name = getOrEmpty(map, "name");
    String ontologyTypeName = getOrElse(map, "ontologyType", OntologyType.OTHER.name());
    OntologyType ontologyType = getOntologyType(ontologyTypeName);
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
      if (Orr.isLogEnabled() && versionsObj != null) {
        Orr.log("_createRegisteredOntologyInfo, map.versions=" + versionsObj);
      }
      if (versionsObj instanceof List<?>) {
        // TODO get details for each of the versions. At this point we are using the
        // same details as the latest version, only with the version piece updated ...
        List<?> versions = (List<?>) versionsObj;
        ArrayList<RegisteredOntologyInfo> versionedList = new ArrayList<RegisteredOntologyInfo>();
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
    boolean ontResolvableUri = Orr.isOntResolvableUri(uri);

    HostingType hostingType;
    if ( ontResolvableUri ) {
      hostingType = HostingType.FULLY_HOSTED;
    }
    else {
      hostingType = HostingType.RE_HOSTED;
    }
    // TODO: Determine HostingType.INDEXED case.

    registeredOntologyInfo.setHostingType(hostingType);

    //if(log.isTraceEnabled()) {
    //  log.trace("_setHostingType: '" + uri + "' ontResolvableUri: " + ontResolvableUri +
    //      "-> hostingType=" + hostingType);
    //}
  }

  private static String getOrEmpty(JSONObject map, String key) {
    return getOrElse(map, key, "");
  }

  private static String getOrElse(JSONObject map, String key, String default_) {
    Object val = map.get(key);
    if (val instanceof JSONString) {
      return ((JSONString) val).stringValue();
    }
    return val != null ? String.valueOf(val) : default_;
  }

  private static OntologyType getOntologyType(String ontologyTypeName) {
    //Orr.log("OrrOntServiceAsync.getOntologyType: ontologyTypeName=" +ontologyTypeName);
    for (OntologyType ot: OntologyType.values()) {
      if (ot.name().toUpperCase().equals(ontologyTypeName.toUpperCase())) {
        return ot;
      }
    }
    return null;
  }
}
